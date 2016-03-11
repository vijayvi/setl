package com.kumarvv.ketl.core;

import com.kumarvv.ketl.model.Def;
import com.kumarvv.ketl.model.Row;
import com.kumarvv.ketl.model.Status;
import com.kumarvv.ketl.utils.CsvParser;
import com.kumarvv.ketl.utils.KetlLogger;
import com.kumarvv.ketl.utils.KetlRowSetFactory;
import com.kumarvv.ketl.utils.RowSetUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.sql.rowset.JdbcRowSet;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class Extractor implements Runnable {
    private static final KetlLogger log = KetlLogger.getLogger(Extractor.class);

    final BlockingQueue<Row> queue;
    final Status status;
    final Def def;
    final Map<String, Integer> fromColumns;
    final Consumer<Boolean> doneCallback;

    RowSetUtil rowSetUtil;
    KetlRowSetFactory rowSetFactory;

    public Extractor(BlockingQueue<Row> queue,
                     Def def,
                     Status status,
                     Consumer<Boolean> doneCallback) {
        this.queue = queue;
        this.def = def;
        this.status = status;
        this.fromColumns = new HashMap<>();
        this.doneCallback = doneCallback;
        this.rowSetUtil = RowSetUtil.getInstance();
        this.rowSetFactory = KetlRowSetFactory.getInstance();
    }

    /**
     * thread runner
     */
    @Override
    public void run() {
        boolean result = true;
        try {
            result = extract();
        } finally {
            System.out.printf("Extractor is done.\n");
            doneCallback.accept(result);
        }
    }

    /**
     * extracts data from three type of sources (in priority order)
     * - sql
     * - data elements
     * - csv file
     *
     * @return
     */
    boolean extract() {
        status.reset();

        if (def == null || def.getExtract() == null) {
            return false;
        }

        boolean result = false;
        if (StringUtils.isNotEmpty(def.getExtract().getSql())) {
            result = extractDataFromSql();
        } else if (CollectionUtils.isNotEmpty(def.getExtract().getData())) {
            result = extractDataFromData();
        } else if (def.getExtract().getCsv() != null) {
            result = extractDataFromCsv();
        } else {
            log.warn(def, "invalid source sql/csv configuration. skipping ETL.");
        }

        return result;
    }

    /**
     * extract data from data element in json
     *
     * @return
     */
    boolean extractDataFromData() {
        if (def.getExtract() == null || def.getExtract().getData() == null) {
            return true;
        }

        def.getExtract().getData().stream().forEach(row -> {
            addToQueue(new Row(fromColumns, row.getValues()));
        });

        return true;
    }

    /**
     * extract data from csv file
     *
     * @return
     */
    boolean extractDataFromCsv() {
        if (def.getExtract() == null || def.getExtract().getCsv() == null) {
            return true;
        }

        final CsvParser parser = CsvParser.getInstance(def.getExtract().getCsv());
        final List<Map<String, Object>> data = parser.parse();

        data.stream().forEach(row -> {
            addToQueue(new Row(fromColumns, row));
        });

        return true;
    }

    /**
     * extract data using sql definition
     *
     * @return
     */
    boolean extractDataFromSql() {
        if (def.getExtract() == null || StringUtils.isEmpty(def.getExtract().getSql())) {
            return true;
        }
        String sql = def.getExtract().getSql();

        try (JdbcRowSet jrs = rowSetFactory.getRowSet(def.getFromDS())) {
            jrs.setCommand(sql);
            jrs.execute();
            jrs.setFetchDirection(ResultSet.FETCH_FORWARD);
            jrs.setFetchSize(100);

            ResultSetMetaData meta = jrs.getMetaData();
            initFromColumns(meta);
            parseData(jrs, meta);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * initializes from columns
     *
     * @param meta
     * @throws SQLException
     */
    void initFromColumns(ResultSetMetaData meta) throws SQLException {
        fromColumns.putAll(rowSetUtil.getMetaColumns(meta));
    }

    /**
     * parse data from jdbc row set
     *
     * @param jrs
     * @param meta
     * @return
     * @throws SQLException
     */
    long parseData(JdbcRowSet jrs, ResultSetMetaData meta) throws SQLException {
        if (jrs == null || meta == null) {
            return 0;
        }

        long rowCount = 0;
        while (jrs.next()) {
            rowCount++;

            parseDataRow(jrs, meta);
            if (!isWithinLimit(rowCount)) {
                break;
            }
        }
        return rowCount;
    }

    /**
     * parses data row to {@link Row} object
     *
     * @param jrs
     * @param meta
     * @return {@link Row}
     * @throws SQLException
     */
    Row parseDataRow(JdbcRowSet jrs, ResultSetMetaData meta) throws SQLException {
        if (jrs == null || meta == null) {
            return null;
        }

        int colCount = meta.getColumnCount();

        Map<String, Object> row = new HashMap<>();
        for (int c = 1; c <= colCount; c++) {
            row.put(meta.getColumnName(c).toLowerCase(), jrs.getObject(c));
        }

        Row ro = new Row(fromColumns, row);
        addToQueue(ro);
        return ro;
    }

    /**
     * adds the row to blocking queue and holds the queue any loader threads
     *
     * @param row
     */
    void addToQueue(Row row) {
        try {
            queue.put(row);
            status.incrementFound();
        } catch (InterruptedException ie) {}
    }

    /**
     * checks if rowcount is within the specified limit in json config
     *
     * @param rowCount
     * @return true | false
     */
    boolean isWithinLimit(long rowCount) {
        return def.getExtract().getLimitRows() == 0 || rowCount <= def.getExtract().getLimitRows();
    }

}
