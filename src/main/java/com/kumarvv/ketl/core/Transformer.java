package com.kumarvv.ketl.core;

import com.kumarvv.ketl.model.Column;
import com.kumarvv.ketl.model.Def;
import com.kumarvv.ketl.utils.Interpolator;
import com.kumarvv.ketl.utils.KetlCache;
import com.kumarvv.ketl.utils.KetlLogger;
import com.kumarvv.ketl.utils.SqlRunner;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Transformer {
    private static final KetlLogger log = KetlLogger.getLogger(Transformer.class);

    private final Def def;

    protected KetlCache cache;
    protected Interpolator interpolator;
    protected SqlRunner sqlRunner;

    public Transformer(final Def def) {
        this.def = def;
        this.cache = KetlCache.getInstance();
        this.interpolator = Interpolator.getInstance();
        this.sqlRunner = SqlRunner.getInstance();
    }

    /**
     * - identifies transform columns and process them one by one
     * - interpolates sql variables with values
     * - check cache for sql results, if not available runs sql and caches
     *
     * @param row
     * @return
     */
    public Map<String, Object> transform(final Map<String, Object> row) {
        if (def == null || def.getTransform() == null) {
            return new HashMap<>();
        }

        final Set<Column> columns = def.getTransform().getColumns();
        if (CollectionUtils.isEmpty(columns)) {
            log.info("no columns in transformation");
            return new HashMap<>();
        }

        columns.parallelStream().forEach(col -> transformSql(row, col));
        return row;
    }

    /**
     * - interpolates sql variables with values
     * - check cache for sql results, if not available runs sql and caches
     *
     * @param row
     * @param col
     */
    void transformSql(final Map<String, Object> row, final Column col) {
        if (row == null || col == null || StringUtils.isEmpty(col.getSql())) {
            return;
        }
        String sql = interpolator.interpolate(col.getSql(), row);

        if (col.getCache() && cache.exists(sql)) {
            row.put(col.getName(), cache.get(sql));
        } else {
            Object v = sqlRunner.getValue(sql, def.getToDS());
            row.put(col.getName(), v);
            cache.set(sql, v);
        }
    }
}
