package com.kumarvv.ketl.enums;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.Types;
import java.util.Date;

import static org.testng.Assert.assertEquals;

public class SqlTypeTest {

    @DataProvider(name = "convertData")
    public Object[][] data() {
        Date d = new Date();

        return new Object[][]{
                {SqlType.BIT, 1, 1},
                {SqlType.TINYINT, 123, 123},
                {SqlType.SMALLINT, 123, 123},
                {SqlType.INTEGER, 123, 123},
                {SqlType.BIGINT, 1234567890, Long.valueOf(1234567890)},
                {SqlType.FLOAT, 1234567890.12345f, Float.valueOf(1234567890.12345f)},
                {SqlType.REAL, 1234567890.12345f, Float.valueOf(1234567890.12345f)},
                {SqlType.DOUBLE, 1234567890.12345, Double.valueOf(1234567890.12345)},
                {SqlType.NUMERIC, 1234567890.12345, Double.valueOf(1234567890.12345)},
                {SqlType.DECIMAL, 1234567890.12345, 1234567890.12345},
                {SqlType.CHAR, 'K', 'K'},
                {SqlType.VARCHAR, "Ketl", "Ketl"},
                {SqlType.LONGVARCHAR, "Ketl", "Ketl"},
                {SqlType.DATE, d.toString(), d.toString()},
                {SqlType.TIME, d.toString(), d.toString()},
                {SqlType.TIMESTAMP, d.toString(), d.toString()},
                {SqlType.BINARY, 1010101, Integer.valueOf(1010101)},
                {SqlType.VARCHAR, null, null}
        };
    }

    @Test(dataProvider = "convertData")
    public void testConvert(SqlType type, Object val, Object expected) {
        Object result = type.convert(val);
        assertEquals(result, expected, type.toString());
        if (result != null && expected != null) {
            assertEquals(result.getClass(), expected.getClass(), type.toString() + " class");
        }
    }

    @Test
    public void testForValue() {
        assertEquals(SqlType.forValue(Types.DATE), SqlType.DATE, "DATE");
        assertEquals(SqlType.forValue(Types.VARCHAR), SqlType.VARCHAR, "VARCHAR");
        assertEquals(SqlType.forValue(-999), null, "invalid");
    }
}
