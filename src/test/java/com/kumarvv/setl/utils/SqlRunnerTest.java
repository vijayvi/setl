package com.kumarvv.setl.utils;

import com.kumarvv.setl.model.DS;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.rowset.JdbcRowSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class SqlRunnerTest {

    SqlRunner sqlRunner;
    DS toDS;

    @BeforeMethod
    public void setup() {
        sqlRunner = spy(SqlRunner.getInstance());
        sqlRunner.rowSetUtil = mock(RowSetUtil.class);
        toDS = mock(DS.class);
    }

    @Test
    public void testGetSingleValue() throws SQLException {
        JdbcRowSet jrs = mock(JdbcRowSet.class);
        doReturn(123L).when(jrs).getObject(1);
        doReturn(jrs).when(sqlRunner.rowSetUtil).getRowSet(toDS);

        doReturn(true).when(jrs).next();
        Object result = sqlRunner.getSingleValue("select id from emp", toDS);
        assertEquals(result, Long.valueOf(123), "id");

        doReturn("Setl").when(jrs).getObject(1);
        result = sqlRunner.getSingleValue("select name from emp", toDS);
        assertEquals(result, "Setl", "name");

        doReturn(false).when(jrs).next();
        result = sqlRunner.getSingleValue("select id from emp", toDS);
        assertEquals(result, null, "no rows");

        doThrow(SQLException.class).when(jrs).execute();
        result = sqlRunner.getSingleValue("select id from emp", toDS);
        assertEquals(result, null, "exception");

        result = sqlRunner.getSingleValue(null, null);
        assertEquals(result, null, "null");
    }

    @Test
    public void testGetSingleRowMap() throws SQLException {
        ResultSetMetaData meta = mock(ResultSetMetaData.class);
        doReturn(2).when(meta).getColumnCount();
        doReturn("id").when(meta).getColumnName(1);
        doReturn("code").when(meta).getColumnName(2);
        doReturn(1).when(meta).getColumnType(1);
        doReturn(2).when(meta).getColumnType(2);

        JdbcRowSet jrs = mock(JdbcRowSet.class);
        doReturn(123L).when(jrs).getObject(1);
        doReturn("Setl").when(jrs).getObject(2);
        doReturn(jrs).when(sqlRunner.rowSetUtil).getRowSet(toDS);
        doReturn(meta).when(jrs).getMetaData();

        doReturn(true).when(jrs).next();
        Map<String, Object> result = sqlRunner.getSingleRowMap("select id, code from emp", toDS);
        assertEquals(result.size(), 2, "size");
        assertEquals(result.get("id"), Long.valueOf(123), "id");
        assertEquals(result.get("code"), "Setl", "code");

        doReturn(false).when(jrs).next();
        result = sqlRunner.getSingleRowMap("select id, name from emp", toDS);
        assertEquals(result.size(), 0, "size");

        result = sqlRunner.getSingleRowMap(null, null);
        assertEquals(result.size(), 0, "size");
    }
}
