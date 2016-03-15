package com.kumarvv.ketl.core;

import com.kumarvv.ketl.model.Column;
import com.kumarvv.ketl.model.DS;
import com.kumarvv.ketl.model.Def;
import com.kumarvv.ketl.model.Transform;
import com.kumarvv.ketl.utils.Interpolator;
import com.kumarvv.ketl.utils.SqlRunner;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class TransformerTest {

    Transformer transformer;
    Def def;
    Transform transform;
    DS toDS;

    @BeforeMethod
    public void setup() {
        toDS = new DS();
        def = mock(Def.class);
        doReturn(toDS).when(def).getToDS();

        transform = mock(Transform.class);
        doReturn(transform).when(def).getTransform();

        transformer = spy(new Transformer(def));
        transformer.interpolator = mock(Interpolator.class);
        transformer.sqlRunner = mock(SqlRunner.class);

        transformer.cache.clear();
    }

    @Test
    public void testTransform() {
        Column c1 = new Column();
        c1.setName("port_id");
        c1.setSql("select id from port where code = :code");
        Column c2 = new Column();
        c2.setName("port_ref");
        c2.setSql("select id from ref where code = :ref");
        Set<Column> cols = new HashSet<>(Arrays.asList(c1, c2));
        doReturn(cols).when(transform).getColumns();

        Map<String, Object> row = new HashMap<>();

        doReturn("select id from port where code = 'NYC'").when(transformer.interpolator).interpolate(c1.getSql(), row);
        doReturn("select id from ref where code = 'XX'").when(transformer.interpolator).interpolate(c2.getSql(), row);
        doReturn(123).when(transformer.sqlRunner).getSingleValue("select id from port where code = 'NYC'", toDS);
        doReturn(456).when(transformer.sqlRunner).getSingleValue("select id from ref where code = 'XX'", toDS);

        transformer.transform(row);

        assertEquals(row.get("port_id").toString(), "123", "port_id");
        assertEquals(row.get("port_ref").toString(), "456", "port_ref");
    }

    @Test
    public void testTransformNulls() {
        doReturn(null).when(def).getTransform();
        Map<String, Object> row = new HashMap<>();
        transformer.transform(row);
        assertEquals(row.size(), 0, "null1");

        doReturn(transform).when(def).getTransform();
        doReturn(null).when(transform).getColumns();
        transformer.transform(row);
        assertEquals(row.size(), 0, "null1");
    }

    @Test
    public void testTransformSql() {
        Map<String, Object> row = new HashMap<>();
        Column col = new Column();
        col.setName("port_id");
        col.setSql("select id from table where col = :val");
        col.setCache(true);

        doReturn("select id from table where col = 999").when(transformer.interpolator).interpolate(col.getSql(), row);
        doReturn(123).when(transformer.sqlRunner).getSingleValue(anyString(), any(DS.class));

        transformer.transformSql(row, col);
        assertEquals(row.get("port_id").toString(), "123", "port_id");

        // cached
        row = new HashMap<>();
        doReturn("select id from table where col = 999").when(transformer.interpolator).interpolate(col.getSql(), row);
        doReturn(123).when(transformer.sqlRunner).getSingleValue(anyString(), any(DS.class));
        transformer.transformSql(row, col);
        assertEquals(row.get("port_id").toString(), "123", "port_id");
        assertEquals(transformer.cache.size(), 1, "cache size");
    }

    @Test
    public void testTransformSqlNull() {
        transformer.transformSql(null, null);
        assertEquals(transformer.cache.size(), 0, "none");

        Map<String, Object> row = new HashMap<>();
        transformer.transformSql(row, null);
        assertEquals(transformer.cache.size(), 0, "none");
        assertEquals(row.size(), 0, "row");

        transformer.transformSql(row, new Column());
        assertEquals(transformer.cache.size(), 0, "none");
        assertEquals(row.size(), 0, "row");
    }
}
