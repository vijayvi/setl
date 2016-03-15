package com.kumarvv.ketl.core;

import com.kumarvv.ketl.mocks.JdbcRowSetMock;
import com.kumarvv.ketl.model.*;
import com.kumarvv.ketl.utils.Interpolator;
import com.kumarvv.ketl.utils.RowSetUtil;
import com.kumarvv.ketl.utils.SqlRunner;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.rowset.JdbcRowSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class LoaderTest {

    Loader loader;
    BlockingQueue<Row> queue;
    Status status;
    Def def;
    DS toDS;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void setup() {
        queue = mock(SynchronousQueue.class);
        status = spy(new Status((c) -> {}));
        def = mock(Def.class);
        loader = spy(new Loader("test", queue, status, def));

        loader.interpolator = mock(Interpolator.class);
        loader.sqlRunner = mock(SqlRunner.class);
        loader.rowSetUtil = mock(RowSetUtil.class);

        toDS = mock(DS.class);
        doReturn(toDS).when(def).getToDS();
    }

    @Test
    public void testNulls() {
        loader = spy(new Loader(null, null, null, null));
        loader.interpolator = null;
        loader.sqlRunner = null;
        loader.rowSetUtil = null;
        loader.rowSetUtil = null;

        Row row1 = spy(new Row(new HashMap<>(), new HashMap<>()));
        Row row2 = spy(new Row(new HashMap<>(), new HashMap<>()));
        Row row3 = Row.DONE;
        when(loader.getRowFromQueue()).thenReturn(row1, row2, row3);
        loader.run();
    }

    @Test
    public void testLoad() {
        Row row1 = spy(new Row(new HashMap<>(), new HashMap<>()));
        Row row2 = spy(new Row(new HashMap<>(), new HashMap<>()));
        Row row3 = Row.DONE;
        when(loader.getRowFromQueue()).thenReturn(row1, row2, row3);

        doReturn(true).when(loader).loadRow(any(Row.class));
        loader.load();
        assertEquals(status.getRowsProcessed(), 2, "status processed");
        assertEquals(loader.processed, 2, "processed");

        status.reset();
        loader = spy(new Loader("test", null, null, def));
        when(loader.getRowFromQueue()).thenReturn(row1, null, row2, row3);
        loader.transformer = null;
        loader.run();
        assertEquals(status.getRowsProcessed(), 0, "status is null");
        assertEquals(loader.processed, 1, "processed");
    }

    @Test
    public void testGetRowFromQueue() throws InterruptedException {
        Row row = new Row(new HashMap<>(), new HashMap<>());
        doReturn(row).when(queue).take();
        Row result = loader.getRowFromQueue();
        assertEquals(result, row, "same row");

        doThrow(InterruptedException.class).when(queue).take();
        result = loader.getRowFromQueue();
        assertEquals(result, null, "exception");

        loader = spy(new Loader("test", null, status, def));
        result = loader.getRowFromQueue();
        assertEquals(result, null, "null");
    }

    @Test
    public void testLoadRow() throws SQLException {
        Row row = spy(new Row(new HashMap<>(), new HashMap<>()));
        doReturn(Arrays.asList(new Load(), new Load())).when(def).getLoads();
        doReturn(true).when(loader).insertOrUpdateRow(any(Load.class), any(Row.class), any(JdbcRowSet.class));
        boolean result = loader.loadRow(row);
        assertEquals(result, true, "all is well");

        doThrow(SQLException.class).when(loader).insertOrUpdateRow(any(Load.class), any(Row.class), any(JdbcRowSet.class));
        result = loader.loadRow(row);
        assertEquals(result, false, "exception");
    }

    @Test
    public void testLoadRowNull() {
        boolean result = loader.loadRow(null);
        assertEquals(result, true, "null1");

        loader = spy(new Loader("test", queue, status, null));
        result = loader.loadRow(null);
        assertEquals(result, false, "null2");
    }

    @Test
    public void testInsertOrUpdateRow() throws SQLException {
        Load load = spy(new Load());
        Row row = new Row(new HashMap<>(), new HashMap<>());
        JdbcRowSet jrs = mock(JdbcRowSet.class);
        doNothing().when(loader).initLoadToColumns(load, jrs);
        doReturn(true).when(loader).processReturns(load, row, jrs);

        doReturn(true).when(loader).updateRow(load, row, jrs);
        doReturn(true).when(loader).insertRow(load, row, jrs);
        doReturn(true).when(loader).isRowExists(load, row, jrs);
        doReturn(true).when(load).isShouldUpdate();
        boolean result = loader.insertOrUpdateRow(load, row, jrs);
        assertEquals(result, true, "all ok");

        doReturn(false).when(loader).updateRow(load, row, jrs);
        result = loader.insertOrUpdateRow(load, row, jrs);
        assertEquals(result, false, "update failed");

        doReturn(false).when(loader).isRowExists(load, row, jrs);
        result = loader.insertOrUpdateRow(load, row, jrs);
        assertEquals(result, true, "all ok");

        doReturn(false).when(loader).insertRow(load, row, jrs);
        result = loader.insertOrUpdateRow(load, row, jrs);
        assertEquals(result, false, "update failed");
    }

    @Test
    public void testInsertOrUpdateRowNull() throws SQLException {
        boolean result = loader.insertOrUpdateRow(null, null, null);
        assertEquals(result, false, "null1");

        result = loader.insertOrUpdateRow(null, null, mock(JdbcRowSet.class));
        assertEquals(result, false, "null2");

        result = loader.insertOrUpdateRow(null, new Row(null, null), mock(JdbcRowSet.class));
        assertEquals(result, false, "null3");
    }


    @Test
    public void testProcessReturns() throws SQLException {
        Load load = new Load();
        Row row = new Row(new HashMap<>(), new HashMap<>());
        JdbcRowSet jrs = mock(JdbcRowSet.class);
        boolean result = loader.processReturns(load, row, jrs);
        assertEquals(result, true, "normal");

        Set<String> returns = new HashSet<>();
        returns.add("id");
        load.setReturns(returns);
        load.setTable("emp");
        doReturn("123").when(jrs).getObject("id");
        result = loader.processReturns(load, row, jrs);
        assertEquals(result, true, "returns");
        assertEquals(row.getData().containsKey("rc_emp_id"), true, "return key");
        assertEquals(row.getData().get("rc_emp_id").toString(), "123", "return val");

        doThrow(SQLException.class).when(jrs).getObject("id");
        result = loader.processReturns(load, row, jrs);
    }

    @Test
    public void testProcessReturnsNull() throws SQLException {
        boolean result = loader.processReturns(null, null, null);
        assertEquals(result, false, "null1");

        result = loader.processReturns(null, null, mock(JdbcRowSet.class));
        assertEquals(result, false, "null2");

        result = loader.processReturns(null, new Row(null, null), mock(JdbcRowSet.class));
        assertEquals(result, false, "null3");
    }

    @Test
    public void testIsRowExists() throws SQLException {
        Load load = new Load();
        Row row = new Row(new HashMap<>(), new HashMap<>());
        JdbcRowSet jrs = mock(JdbcRowSet.class);
        doReturn("select 1 from dual").when(loader.interpolator).interpolate(anyString(), any(Map.class));
        doReturn(true).when(jrs).next();
        boolean result = loader.isRowExists(load, row, jrs);
        assertEquals(result, true, "normal");
    }

    @Test
    public void testIsRowExistsNull() throws SQLException {
        boolean result = loader.isRowExists(null, null, null);
        assertEquals(result, false, "null1");

        result = loader.isRowExists(null, null, mock(JdbcRowSet.class));
        assertEquals(result, false, "null2");

        result = loader.isRowExists(null, new Row(null, null), mock(JdbcRowSet.class));
        assertEquals(result, false, "null3");
    }

    @Test(expectedExceptions = {SQLException.class})
    public void testIsRowExistsException() throws SQLException {
        Load load = new Load();
        Row row = new Row(new HashMap<>(), new HashMap<>());
        JdbcRowSet jrs = mock(JdbcRowSet.class);
        doReturn("select 1 from dual").when(loader.interpolator).interpolate(anyString(), any(Map.class));
        doThrow(SQLException.class).when(jrs).next();
        loader.isRowExists(load, row, jrs);
    }

    @Test
    public void testUpdateRow() throws SQLException {
        Column c1 = new Column();
        Column c2 = new Column();
        c2.setAuto(true);
        Column c3 = new Column();
        c3.setNk(true);
        Column c4 = new Column();
        c4.setUpdate(false);
        Column c5 = new Column();
        c5.setGenerator("something");
        Load load = new Load();
        load.setColumns(Arrays.asList(c1, c2, c3, c4, c5));
        Map<String, Integer> cols = new HashMap<>();
        cols.put("id", 1);
        cols.put("code", 2);
        cols.put("name", 3);
        cols.put("dept_id", 4);
        Map<String, Object> data = new HashMap<>();
        data.put("id", 1);
        data.put("code", "NYC");
        data.put("name", "New York");
        data.put("dept_code", "IT");
        Row row = new Row(cols, data);
        JdbcRowSet jrs = spy(new JdbcRowSetMock());
        doNothing().when(jrs).updateRow();

        doNothing().when(loader).setColumnValue(any(Load.class), any(Row.class), any(Column.class), any(JdbcRowSet.class));

        boolean result = loader.updateRow(load, row, jrs);
        assertEquals(result, true, "update1");

        doThrow(SQLException.class).when(jrs).updateRow();
        result = loader.updateRow(load, row, jrs);
        assertEquals(result, false, "update2");
    }

    @Test
    public void testUpdateRowNull() {
        boolean result = loader.updateRow(null, null, null);
        assertEquals(result, false, "null 1");

        JdbcRowSet jrs = spy(new JdbcRowSetMock());
        result = loader.updateRow(null, null, jrs);
        assertEquals(result, false, "null 2");

        result = loader.updateRow(null, new Row(null, null), jrs);
        assertEquals(result, false, "null 3");

        Load load = new Load();
        load.setColumns(null);
        result = loader.updateRow(load, new Row(null, null), jrs);
        assertEquals(result, false, "null 4");
    }

    @Test
    public void testInsertRow() throws SQLException {
        Column c1 = new Column();
        Column c2 = new Column();
        c2.setAuto(true);
        Load load = new Load();
        load.setColumns(Arrays.asList(c1, c2));
        Map<String, Integer> cols = new HashMap<>();
        cols.put("id", 1);
        cols.put("code", 2);
        cols.put("name", 3);
        cols.put("dept_id", 4);
        Map<String, Object> data = new HashMap<>();
        data.put("id", 1);
        data.put("code", "NYC");
        data.put("name", "New York");
        data.put("dept_code", "IT");
        Row row = new Row(cols, data);
        JdbcRowSet jrs = spy(new JdbcRowSetMock());
        doNothing().when(jrs).insertRow();
        doReturn(true).when(jrs).first();

        doNothing().when(loader).setColumnValue(any(Load.class), any(Row.class), any(Column.class), any(JdbcRowSet.class));

        boolean result = loader.insertRow(load, row, jrs);
        assertEquals(result, true, "insert");

        doThrow(SQLException.class).when(jrs).insertRow();
        result = loader.insertRow(load, row, jrs);
        assertEquals(result, false, "insert2");
    }

    @Test
    public void testInsertRowNull() {
        boolean result = loader.insertRow(null, null, null);
        assertEquals(result, false, "null 1");

        JdbcRowSet jrs = spy(new JdbcRowSetMock());
        result = loader.insertRow(null, null, jrs);
        assertEquals(result, false, "null 2");

        result = loader.insertRow(null, new Row(null, null), jrs);
        assertEquals(result, false, "null 3");

        Load load = new Load();
        load.setColumns(null);
        result = loader.insertRow(load, new Row(null, null), jrs);
        assertEquals(result, false, "null 4");
    }

    @Test
    public void testSetColumnValue() throws SQLException {
        Load load = new Load();
        Map<String, Integer> cols = new HashMap<>();
        cols.put("id", 1);
        cols.put("code", 2);
        cols.put("name", 3);
        cols.put("dept_id", 4);
        Map<String, Object> data = new HashMap<>();
        data.put("id", 1);
        data.put("code", "NYC");
        data.put("name", "New York");
        data.put("dept_code", "IT");
        Row row = new Row(cols, data);
        JdbcRowSet jrs = spy(new JdbcRowSetMock());

        Column col = new Column();
        col.setName("code");
        doReturn("NYC").when(loader).getColumnValue(load, row, col);
        loader.setColumnValue(load, row, col, jrs);
        assertEquals(jrs.getObject("code").toString(), "NYC", "code");

        col = new Column();
        col.setName("code");
        col.setGenerator("select emp_seq.nextval from dual");
        doReturn(123).when(loader.sqlRunner).getSingleValue(anyString(), any(DS.class));
        loader.setColumnValue(load, row, col, jrs);
        assertEquals(jrs.getObject("code").toString(), "123", "generator");
    }

    @Test
    public void testSetColumnValueNull() throws SQLException {
        loader.setColumnValue(null, null, null, null);

        JdbcRowSet jrs = spy(new JdbcRowSetMock());
        loader.setColumnValue(null, null, null, jrs);
        assertEquals(jrs.getObject("code"), null, "null 1");

        Column col = new Column();
        col.setName("code");
        doReturn("NYC").when(loader).getColumnValue(any(Load.class), any(Row.class), any(Column.class));
        loader.setColumnValue(null, null, col, jrs);
        assertEquals(jrs.getObject("code"), null, "null 2");

        col = new Column();
        col.setName("code");
        doReturn("NYC").when(loader).getColumnValue(any(Load.class), any(Row.class), any(Column.class));
        loader.setColumnValue(null, new Row(null, null), col, jrs);
        assertEquals(jrs.getObject("code"), null, "null 3");
    }

    @Test
    public void testGetColumnValue() {
        Load load = new Load();
        Map<String, Integer> cols = new HashMap<>();
        cols.put("id", 1);
        cols.put("code", 2);
        cols.put("name", 3);
        cols.put("dept_id", 4);
        Map<String, Object> data = new HashMap<>();
        data.put("id", 1);
        data.put("code", "NYC");
        data.put("name", "New York");
        data.put("dept_code", "IT");
        Row row = new Row(cols, data);

        Column col = new Column();
        col.setName("code");
        col.setRef("code");
        Object val = loader.getColumnValue(load, row, col);
        assertEquals(val.toString(), "NYC", "ref");

        col = new Column();
        col.setName("code");
        col.setValue("NYC");
        val = loader.getColumnValue(load, row, col);
        assertEquals(val.toString(), "NYC", "value");

        col = new Column();
        col.setName("dept_id");
        col.setSql("select id from dept where code = :dept");
        doReturn("select id from dept where code = 'IT'").when(loader.interpolator).interpolate(col.getSql(), row.getData());
        doReturn(123).when(loader.sqlRunner).getSingleValue("select id from dept where code = 'IT'", toDS);
        val = loader.getColumnValue(load, row, col);
        assertEquals(val.toString(), "123", "sql");

        col = new Column();
        col.setName("code");
        col.setValue("%NOW%");
        val = loader.getColumnValue(load, row, col);
        assertEquals(val.toString().substring(0, 10), new Timestamp(new Date().getTime()).toString().substring(0, 10), "systemVar");
    }

    @Test
    public void testGetColumnValueNull() {
        Object val = loader.getColumnValue(null, null, null);
        assertEquals(val, null, "all null");

        val = loader.getColumnValue(new Load(), null, null);
        assertEquals(val, null, "null 1");

        val = loader.getColumnValue(new Load(), new Row(null, null), null);
        assertEquals(val, null, "null 2");

        val = loader.getColumnValue(new Load(), new Row(null, null), new Column());
        assertEquals(val, null, "null 3");

        Load load = mock(Load.class);
        doReturn(999).when(load).getToColumnType("code");
        Column col = new Column();
        col.setName("code");
        col.setValue("NYC");
        val = loader.getColumnValue(load, new Row(null, null), col);
        assertEquals(val.toString(), "NYC", "value");
    }

    @Test
    public void testInitLoadToColumns() {
        Map<String, Integer> cols = new HashMap<>();
        cols.put("id", 1);
        cols.put("code", 2);
        doReturn(cols).when(loader.rowSetUtil).getMetaColumns(any(JdbcRowSet.class));

        Load load = new Load();
        JdbcRowSet jrs = mock(JdbcRowSet.class);
        loader.initLoadToColumns(load, jrs);
        assertEquals(load.getToColumns().size(), 2, "size");
        assertEquals(load.getToColumns().get("id"), Integer.valueOf(1), "id");
        assertEquals(load.getToColumns().get("code"), Integer.valueOf(2), "code");

        cols.put("name", 3);
        loader.initLoadToColumns(load, jrs);
        assertEquals(load.getToColumns().containsKey("name"), false, "name");

        loader.initLoadToColumns(null, jrs);
    }

    @Test
    public void testBuildSqlExistsAll() {
        Load l1 = new Load();
        Load l2 = new Load();
        Load l3 = new Load();
        doReturn(Arrays.asList(l1, l2, l3)).when(def).getLoads();
        doReturn("").when(loader).buildSqlExists(l1);
        doReturn("SELECT id, code, name, dept_id FROM emp WHERE 1 > 2").when(loader).buildSqlExists(l2);
        doReturn("SELECT id, code, name, dept_id FROM emp WHERE code = :code").when(loader).buildSqlExists(l3);
        loader.buildSqlExistsAll();
        assertEquals(l1.getSqlExists(), "", "blank");
        assertEquals(l2.getSqlExists(), "SELECT id, code, name, dept_id FROM emp WHERE 1 > 2", "no nk");
        assertEquals(l3.getSqlExists(), "SELECT id, code, name, dept_id FROM emp WHERE code = :code", "nk");
    }

    @Test
    public void testBuildSqlExists() {
        Load load = new Load();
        load.setTable("emp");
        load.setColumns(mockColumns());
        doReturn("id, code, name, dept_id").when(loader).buildSelectColumns(load);
        String result = loader.buildSqlExists(load);
        assertEquals(result, "SELECT id, code, name, dept_id FROM emp WHERE code = :code AND dept_id = :dept_id", "2 nks");

        load.setColumns(new ArrayList<>());
        result = loader.buildSqlExists(load);
        assertEquals(result, "SELECT id, code, name, dept_id FROM emp WHERE 1 > 2", "no nks");
    }

    @Test
    public void testBuildSqlExistsNull() {
        String result = loader.buildSqlExists(null);
        assertEquals(result, "", "null1");

        Load load = new Load();
        result = loader.buildSqlExists(load);
        assertEquals(result, "", "null2");

    }

    @Test
    public void testBuildSelectColumns() throws SQLException {
        Load load = new Load();
        load.setTable("emp");

        ResultSetMetaData meta = mockMeta();
        JdbcRowSet jrs = mock(JdbcRowSet.class);
        doReturn(jrs).when(loader.rowSetUtil).getRowSet(any(DS.class));
        doReturn(meta).when(jrs).getMetaData();

        String result = loader.buildSelectColumns(load);
        assertEquals(result, "id, code, name, dept_id", "columns");
    }

    @Test
    public void testBuildSelectColumnsNulls() throws SQLException {
        ResultSetMetaData meta = mockMeta();
        JdbcRowSet jrs = mock(JdbcRowSet.class);
        doReturn(jrs).when(loader.rowSetUtil).getRowSet(any(DS.class));
        doReturn(meta).when(jrs).getMetaData();

        String result = loader.buildSelectColumns(null);
        assertEquals(result, "", "null1");

        Load load = new Load();
        result = loader.buildSelectColumns(load);
        assertEquals(result, "", "null2");

        load.setTable("emp");
        doThrow(SQLException.class).when(loader.rowSetUtil).getRowSet(any(DS.class));
        result = loader.buildSelectColumns(load);
        assertEquals(result, "", "exception");
    }

    private List<Column> mockColumns() {
        Column c1 = new Column();
        c1.setName("id");
        c1.setAuto(true);
        c1.setGenerator("select seq_emp.nextval from dual");
        Column c2 = new Column();
        c2.setName("code");
        c2.setRef("code");
        c2.setNk(true);
        Column c3 = new Column();
        c3.setName("name");
        c3.setRef("name");
        Column c4 = new Column();
        c4.setName("dept_id");
        c4.setRef("dept_id");
        c4.setNk(true);

        return Arrays.asList(c1, c2, c3, c4);
    }

    private ResultSetMetaData mockMeta() throws SQLException {
        ResultSetMetaData meta = mock(ResultSetMetaData.class);
        doReturn(4).when(meta).getColumnCount();
        doReturn("id").when(meta).getColumnName(1);
        doReturn("code").when(meta).getColumnName(2);
        doReturn("name").when(meta).getColumnName(3);
        doReturn("dept_id").when(meta).getColumnName(4);
        return meta;
    }

}
