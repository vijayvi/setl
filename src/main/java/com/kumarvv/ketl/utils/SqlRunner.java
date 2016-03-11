package com.kumarvv.ketl.utils;

import com.kumarvv.ketl.model.DS;
import org.apache.commons.lang3.StringUtils;

import javax.sql.rowset.JdbcRowSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;

public class SqlRunner {

    KetlRowSetFactory rowSetFactory;

    private SqlRunner() {
        rowSetFactory = KetlRowSetFactory.getInstance();
    }

    public static SqlRunner getInstance() {
        return new SqlRunner();
    }

    public Object getValue(String sql, DS ds) {
        if (StringUtils.isEmpty(sql)) {
            return null;
        }
        try (JdbcRowSet jrs = rowSetFactory.getRowSet(ds)) {
            jrs.setCommand(sql);
            jrs.execute();
            if (jrs.next()) {
                return jrs.getObject(1);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public Map<String, Object> getRowMap(String sql, DS ds) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isEmpty(sql)) {
            return map;
        }

        try (JdbcRowSet jrs = rowSetFactory.getRowSet(ds)) {
            jrs.setCommand(sql);
            jrs.execute();
            if (!jrs.next()) {
                return map;
            }
            ResultSetMetaData meta = jrs.getMetaData();
            for (int i = 1; i < meta.getColumnCount(); i++) {
                map.put(meta.getColumnName(i), jrs.getObject(i));
            }
        } catch (Exception e) {
        }
        return map;
    }
}
