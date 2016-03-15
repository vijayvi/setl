package com.kumarvv.setl.utils;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

public class InterpolatorTest {

    Interpolator interpolator;

    @BeforeMethod
    public void setup() {
        interpolator = spy(Interpolator.getInstance());
    }

    @Test
    public void testInterpolate() {
        Date d = new Date();
        Map<String, Object> values = new HashMap<>();
        values.put("id", 123L);
        values.put("name", "Setl Processor");
        values.put("today", d);

        String result = interpolator.interpolate("select 1 from emp where id = :id and name = :name and refs in (:id, :name)  ", values);
        assertEquals(result, "select 1 from emp where id = 123 and name = 'Setl Processor' and refs in (123, 'Setl Processor')", "good1");

        result = interpolator.interpolate("select 1 from emp where created_at >= :today", values);
        assertEquals(result, "select 1 from emp where created_at >= '" + d.toString() + "'", "good2");

        result = interpolator.interpolate("select 1 from emp where created_at >= :unknown", values);
        assertEquals(result, "select 1 from emp where created_at >= :unknown", "good3");
    }

    @Test
    public void testInterpolateNull() {
        String result = interpolator.interpolate(null, null);
        assertEquals(result, null, "null1");

        result = interpolator.interpolate(null, new HashMap<>());
        assertEquals(result, null, "null1");

        result = interpolator.interpolate("select :id from dual", null);
        assertEquals(result, "select :id from dual", "null2");

        result = interpolator.interpolate("", null, null);
        assertEquals(result, "", "null3");

        result = interpolator.interpolate("select 1 from emp where id = :id", ":id", null);
        assertEquals(result, "select 1 from emp where id = null", "null4");
    }

    @Test
    public void testToInterpolateValue() {
        String result = interpolator.toInterpolateValue("hello");
        assertEquals(result, "'hello'", "string");

        Date d = new Date();
        result = interpolator.toInterpolateValue(d);
        assertEquals(result, "'" + d.toString() + "'", "java.util.Date");

        java.sql.Date sd = new java.sql.Date(d.getTime());
        result = interpolator.toInterpolateValue(sd);
        assertEquals(result, "'" + sd.toString() + "'", "java.sql.Date");

        Timestamp ts = new Timestamp(d.getTime());
        result = interpolator.toInterpolateValue(ts);
        assertEquals(result, "'" + ts.toString() + "'", "Timestamp");

        result = interpolator.toInterpolateValue(123);
        assertEquals(result, "123", "int");

        result = interpolator.toInterpolateValue(1234567890L);
        assertEquals(result, "1234567890", "long");

        result = interpolator.toInterpolateValue(1234.56);
        assertEquals(result, "1234.56", "decimal");

        result = interpolator.toInterpolateValue(true);
        assertEquals(result, "true", "boolean");

        result = interpolator.toInterpolateValue(null);
        assertEquals(result, "null", "null");
    }
}
