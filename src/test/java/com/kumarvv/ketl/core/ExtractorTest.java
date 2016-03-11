package com.kumarvv.ketl.core;

import com.kumarvv.ketl.model.*;
import com.kumarvv.ketl.utils.KetlRowSetFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.rowset.JdbcRowSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class ExtractorTest {

    Extractor extractor;
    Def def;
    Extract extract;
    BlockingQueue<Row> queue;
    Status status;
    KetlRowSetFactory rowSetFactory;

    boolean allDone = false;

    @BeforeMethod
    public void setup() {
        def = mock(Def.class);
        extract = mock(Extract.class);
        doReturn(extract).when(def).getExtract();
        queue = mock(SynchronousQueue.class);
        status = spy(new Status((c) -> {}));

        extractor = spy(new Extractor(queue, def, status, (b) -> { status.markAsDone(); }));
        extractor.rowSetFactory = mock(KetlRowSetFactory.class);
    }

    @Test
    public void testRun() {
        doReturn(true).when(extractor).extract();
        extractor.run();
        assertEquals(status.isDone(), true, "done");
    }

    @Test
    public void testExtract() {
        doReturn("select sql").when(extract).getSql();
        doReturn(true).when(extractor).extractDataFromSql();
        doReturn(false).when(extractor).extractDataFromData();
        doReturn(false).when(extractor).extractDataFromCsv();
        boolean result = extractor.extract();
        assertEquals(result, true, "sql");

        doReturn(null).when(extract).getSql();
        Data d1 = new Data();
        d1.setValue("id", 123);
        d1.setValue("name", "NYC");
        doReturn(Arrays.asList(d1)).when(extract).getData();
        doReturn(false).when(extractor).extractDataFromSql();
        doReturn(true).when(extractor).extractDataFromData();
        doReturn(false).when(extractor).extractDataFromCsv();
        result = extractor.extract();
        assertEquals(result, true, "data");

        doReturn(null).when(extract).getSql();
        doReturn(null).when(extract).getData();
        doReturn(new Csv()).when(extract).getCsv();
        doReturn(false).when(extractor).extractDataFromSql();
        doReturn(false).when(extractor).extractDataFromData();
        doReturn(true).when(extractor).extractDataFromCsv();
        result = extractor.extract();
        assertEquals(result, true, "csv");

        doReturn(null).when(extract).getSql();
        doReturn(null).when(extract).getData();
        doReturn(null).when(extract).getCsv();
        doReturn(true).when(extractor).extractDataFromSql();
        doReturn(true).when(extractor).extractDataFromData();
        doReturn(true).when(extractor).extractDataFromCsv();
        result = extractor.extract();
        assertEquals(result, false, "all null");
    }

    @Test
    public void testExtractDataFromData() {
        Data d1 = new Data();
        d1.setValue("id", 123);
        d1.setValue("name", "NYC");
        Data d2 = new Data();
        d1.setValue("id", 234);
        d1.setValue("name", "LAX");
        List<Data> data = Arrays.asList(d1, d2);
        doReturn(data).when(extract).getData();

        boolean result = extractor.extractDataFromData();
        assertEquals(result, true, "data");
        assertEquals(status.getRowsFound(), 2, "data");

        doReturn(new ArrayList<Data>()).when(extract).getData();
        result = extractor.extractDataFromData();
        assertEquals(result, true, "data");
        assertEquals(status.getRowsFound(), 2, "data");
    }

    @Test
    public void testExtractDataFromDataNulls() {
        doReturn(null).when(extract).getData();
        boolean result = extractor.extractDataFromData();
        assertEquals(result, true, "null data");
        assertEquals(status.getRowsFound(), 0, "null data");

        doReturn(null).when(def).getExtract();
        result = extractor.extractDataFromData();
        assertEquals(result, true, "null data");
        assertEquals(status.getRowsFound(), 0, "null data");

    }

    @Test
    public void testExtractFromCsv() {
        Csv csv = spy(Csv.class);
        csv.setFilePath("none.csv");
        csv.setFile("none.csv");
        doReturn(csv).when(extract).getCsv();
        boolean result = extractor.extractDataFromCsv();
        assertEquals(result, true, "none.csv");
        assertEquals(status.getRowsFound(), 0, "none.csv");

        csv.setFilePath(getThisPath() + "test.csv");
        csv.setFile("test.csv");
        csv.setColumns(Arrays.asList("code","name","age"));
        doReturn(csv).when(extract).getCsv();
        result = extractor.extractDataFromCsv();
        assertEquals(result, true, "test.csv");
        assertEquals(status.getRowsFound(), 4, "test.csv");
    }

    @Test
    public void testExtractDataFromCsvNull() {
        doReturn(null).when(extract).getCsv();
        boolean result = extractor.extractDataFromCsv();
        assertEquals(result, true, "null1");
        assertEquals(status.getRowsFound(), 0, "null1");

        doReturn(null).when(def).getExtract();
        result = extractor.extractDataFromCsv();
        assertEquals(result, true, "null2");
        assertEquals(status.getRowsFound(), 0, "null2");
    }

    @Test
    public void testExtractDataFromSql() throws SQLException {
        JdbcRowSet jrs = mock(JdbcRowSet.class);
        doReturn(123).when(jrs).getObject(1);
        doReturn("NY").when(jrs).getObject(2);
        doReturn("New York").when(jrs).getObject(3);
        doReturn(jrs).when(extractor.rowSetFactory).getRowSet(any(DS.class));

        ResultSetMetaData meta = mock(ResultSetMetaData.class);
        doReturn(3).when(meta).getColumnCount();
        doReturn("id").when(meta).getColumnName(1);
        doReturn("code").when(meta).getColumnName(2);
        doReturn("name").when(meta).getColumnName(3);
        doReturn(1).when(meta).getColumnType(1);
        doReturn(2).when(meta).getColumnType(2);
        doReturn(3).when(meta).getColumnType(3);
        doReturn(meta).when(jrs).getMetaData();

        doReturn("select 1 from dual").when(extract).getSql();
        doNothing().when(extractor).initFromColumns(any(ResultSetMetaData.class));
        doReturn(1L).when(extractor).parseData(jrs, meta);

        boolean result = extractor.extractDataFromSql();
        assertEquals(result, true, "normal");
    }

    @Test
    public void testExtractDataFromSqlException() throws SQLException {
        doReturn("select 1 from dual").when(extract).getSql();
        doThrow(SQLException.class).when(extractor.rowSetFactory).getRowSet(any(DS.class));
        boolean result = extractor.extractDataFromSql();
        assertEquals(result, false, "exception");
    }

    @Test
    public void testExtractDataFromSqlNull() throws SQLException {
        doReturn(null).when(extract).getSql();
        boolean result = extractor.extractDataFromSql();
        assertEquals(result, true, "null1");

        doReturn(null).when(def).getExtract();
        doReturn("select 1 from dual").when(extract).getSql();
        result = extractor.extractDataFromSql();
        assertEquals(result, true, "null2");
    }

    @Test
    public void testInitFromColumns() throws SQLException {
        ResultSetMetaData meta = mock(ResultSetMetaData.class);
        doReturn(3).when(meta).getColumnCount();
        doReturn("id").when(meta).getColumnName(1);
        doReturn("code").when(meta).getColumnName(2);
        doReturn("name").when(meta).getColumnName(3);
        doReturn(1).when(meta).getColumnType(1);
        doReturn(2).when(meta).getColumnType(2);
        doReturn(3).when(meta).getColumnType(3);

        extractor.initFromColumns(meta);
        assertEquals(extractor.fromColumns.size(), 3, "size");
        assertEquals(extractor.fromColumns.get("id"), Integer.valueOf(1), "id");
        assertEquals(extractor.fromColumns.get("code"), Integer.valueOf(2), "code");
        assertEquals(extractor.fromColumns.get("name"), Integer.valueOf(3), "name");
    }

    @Test
    public void testParseData() throws SQLException {
        ResultSetMetaData meta = mock(ResultSetMetaData.class);
        doReturn(3).when(meta).getColumnCount();
        doReturn("id").when(meta).getColumnName(1);
        doReturn("code").when(meta).getColumnName(2);
        doReturn("name").when(meta).getColumnName(3);

        JdbcRowSet jrs = mock(JdbcRowSet.class);
        doReturn(123).when(jrs).getObject(1);
        doReturn("NY").when(jrs).getObject(2);
        doReturn("New York").when(jrs).getObject(3);

        doReturn(true).when(jrs).next();
        doReturn(false).when(extractor).isWithinLimit(1);

        long result = extractor.parseData(jrs, meta);
        assertEquals(result, 1, "rows");
    }

    @Test
    public void testParseDataNull() throws SQLException {
        JdbcRowSet jrs = mock(JdbcRowSet.class);
        ResultSetMetaData meta = mock(ResultSetMetaData.class);

        long result = extractor.parseData(jrs, null);
        assertEquals(result, 0, "null1");

        result = extractor.parseData(null, meta);
        assertEquals(result, 0, "null2");

        result = extractor.parseData(null, null);
        assertEquals(result, 0, "null3");
    }


    @Test
    public void testParseDataRow() throws SQLException {
        JdbcRowSet jrs = mock(JdbcRowSet.class);
        ResultSetMetaData meta = mock(ResultSetMetaData.class);

        doReturn(3).when(meta).getColumnCount();
        doReturn("id").when(meta).getColumnName(1);
        doReturn("code").when(meta).getColumnName(2);
        doReturn("name").when(meta).getColumnName(3);

        doReturn(123).when(jrs).getObject(1);
        doReturn("NY").when(jrs).getObject(2);
        doReturn("New York").when(jrs).getObject(3);

        Row result = extractor.parseDataRow(jrs, meta);

        assertEquals(result.getData().size(), 3, "size");
        assertEquals(result.getData().get("id"), 123, "id");
        assertEquals(result.getData().get("code"), "NY", "code");
        assertEquals(result.getData().get("name"), "New York", "name");
    }

    @Test
    public void testParseDataRowNull() throws SQLException {
        JdbcRowSet jrs = mock(JdbcRowSet.class);
        ResultSetMetaData meta = mock(ResultSetMetaData.class);

        Row result = extractor.parseDataRow(jrs, null);
        assertEquals(result, null, "null1");

        result = extractor.parseDataRow(null, meta);
        assertEquals(result, null, "null2");

        result = extractor.parseDataRow(null, null);
        assertEquals(result, null, "null3");
    }

    @Test
    public void testAddToQueue() throws InterruptedException {
        Row row = mock(Row.class);

        extractor.addToQueue(row);
        assertEquals(status.getRowsFound(), 1, "1 row");

        extractor.addToQueue(row);
        extractor.addToQueue(row);
        extractor.addToQueue(row);
        assertEquals(status.getRowsFound(), 4, "3 rows");

        queue.take();
        queue.take();
        queue.take();
    }

    @Test
    public void testIsWithinLimit() {
        doReturn(0).when(extract).getLimitRows();
        boolean result = extractor.isWithinLimit(0);
        assertEquals(result, true, "0");

        doReturn(7).when(extract).getLimitRows();
        result = extractor.isWithinLimit(6);
        assertEquals(result, true, "6");

        result = extractor.isWithinLimit(8);
        assertEquals(result, false, "8");
    }

    private String getThisPath() {
        return ExtractorTest.class.getResource("/").getPath();
    }
}
