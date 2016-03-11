package com.kumarvv.ketl.mocks;

import com.sun.rowset.JdbcRowSetImpl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class JdbcRowSetMock extends JdbcRowSetImpl {
    Map<String, Object> data = new HashMap<>();
    @Override
    public void updateObject(String s, Object v) throws SQLException {
        data.put(s, v);
    }

    @Override
    public Object getObject(String s) throws SQLException {
        return data.get(s);
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        data = new HashMap<>();
    }
}
