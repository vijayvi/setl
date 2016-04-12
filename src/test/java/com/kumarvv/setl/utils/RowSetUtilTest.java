package com.kumarvv.setl.utils;

import com.kumarvv.setl.model.DS;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.rowset.JdbcRowSet;
import javax.sql.rowset.RowSetFactory;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;

public class RowSetUtilTest {

    RowSetUtil util;
    DS dsFrom;
    DS dsTo;

    @BeforeMethod
    public void setup() {
        util = spy(RowSetUtil.getInstance());

        dsFrom = new DS();
        dsFrom.setUrl("mysql-url");
        dsFrom.setUsername("root");
        dsFrom.setPassword("password");

        dsTo = new DS();
        dsTo.setUrl("oracle-url");
        dsTo.setUsername("uid");
        dsTo.setPassword("pwd");

        doReturn(true).when(util).loadDriver(any(DS.class));
    }

    @Test
    public void testGetRowSet() throws SQLException {
        JdbcRowSet jrs = util.getRowSet(dsFrom);
        assertEquals(jrs.getUrl(), "mysql-url");
        assertEquals(jrs.getUsername(), "root");
        assertEquals(jrs.getPassword(), "password");
    }

    @Test(expectedExceptions = {SQLException.class})
    public void testGetRowSetExceptions() throws SQLException {
        util.rowSetFactory = mock(RowSetFactory.class);
        doThrow(SQLException.class).when(util.rowSetFactory).createJdbcRowSet();
        JdbcRowSet jrs = util.getRowSet(dsTo);
        assertEquals(jrs, null, "exception");
    }

    @Test(expectedExceptions = {SQLException.class})
    public void testGetRowSetNull() throws SQLException {
        JdbcRowSet jrs = util.getRowSet(null);
        assertEquals(jrs, null, "null");
    }

    @Test
    public void testGetMetaColumns() throws SQLException {
        ResultSetMetaData meta = mock(ResultSetMetaData.class);
        doReturn(3).when(meta).getColumnCount();
        doReturn("id").when(meta).getColumnName(1);
        doReturn("code").when(meta).getColumnName(2);
        doReturn("name").when(meta).getColumnName(3);
        doReturn(1).when(meta).getColumnType(1);
        doReturn(2).when(meta).getColumnType(2);
        doReturn(3).when(meta).getColumnType(3);

        Map<String, Integer> result = util.getMetaColumns(meta);
        assertEquals(result.size(), 3, "size");
        assertEquals(result.get("id"), Integer.valueOf(1), "id");
        assertEquals(result.get("code"), Integer.valueOf(2), "code");
        assertEquals(result.get("name"), Integer.valueOf(3), "name");

        JdbcRowSet jrs = mock(JdbcRowSet.class);
        doReturn(meta).when(jrs).getMetaData();
        result = util.getMetaColumns(jrs);
        assertEquals(result.size(), 3, "size");
        assertEquals(result.get("id"), Integer.valueOf(1), "id");
        assertEquals(result.get("code"), Integer.valueOf(2), "code");
        assertEquals(result.get("name"), Integer.valueOf(3), "name");

        doThrow(SQLException.class).when(meta).getColumnCount();
        result = util.getMetaColumns(meta);
        assertEquals(result.size(), 0, "exception1");

        doThrow(SQLException.class).when(jrs).getMetaData();
        result = util.getMetaColumns(jrs);
        assertEquals(result.size(), 0, "exception2");

        meta = null;
        result = util.getMetaColumns(meta);
        assertEquals(result.size(), 0, "null1");

        jrs = null;
        result = util.getMetaColumns(jrs);
        assertEquals(result.size(), 0, "null2");
    }

    @Test
    public void testLoadDriver() throws ClassNotFoundException {
        doCallRealMethod().when(util).loadDriver(any(DS.class));

        DS ds1 = new DS();
        ds1.setUrl("jdbc:oracle:thin:@ora12c:1521:orcl");
        boolean result = util.loadDriver(ds1);
        assertEquals(result, true, "oracle");

        DS ds2 = new DS();
        ds2.setUrl("jdbc:mysql://localhost:3306/tacsp");
        result = util.loadDriver(ds2);
        assertEquals(result, true, "mysql");

        DS ds3 = new DS();
        ds3.setUrl("unknown-url");
        result = util.loadDriver(ds3);
        assertEquals(result, false, "unknown");

        result = util.loadDriver(null);
        assertEquals(result, false, "null");
        result = util.loadDriver(new DS());
        assertEquals(result, false, "null driver");

        doThrow(ClassNotFoundException.class).when(util).loadDriverClass("com.mysql.jdbc.Driver");
        result = util.loadDriver(ds2);
        assertEquals(result, false, "exception");
    }
}
