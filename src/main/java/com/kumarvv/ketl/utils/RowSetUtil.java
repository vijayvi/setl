package com.kumarvv.ketl.utils;

import javax.sql.rowset.JdbcRowSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class RowSetUtil {

    public static RowSetUtil getInstance() {
        return new RowSetUtil();
    }

    public Map<String, Integer> getMetaColumns(ResultSetMetaData meta) {
        final Map<String, Integer> metaColumns = new HashMap<>();
        try {
            int colCount = meta.getColumnCount();
            for (int c = 1; c <= colCount; c++) {
                metaColumns.put(meta.getColumnName(c), meta.getColumnType(c));
            }
        } catch (SQLException sqle) {
        }

        return metaColumns;
    }

    public Map<String, Integer> getMetaColumns(JdbcRowSet jrs) {
        try {
            return getMetaColumns(jrs.getMetaData());
        } catch (SQLException sqle) {
            return new HashMap<>();
        }
    }
}
