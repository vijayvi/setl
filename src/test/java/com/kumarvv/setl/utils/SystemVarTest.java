package com.kumarvv.setl.utils;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.Date;

import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

public class SystemVarTest {

    SystemVar systemVar;

    @BeforeMethod
    public void setup() {
        systemVar = spy(SystemVar.getInstance());
    }

    @Test
    public void testProcess() {
        Object result = systemVar.process("%NOW%");
        assertEquals(result.toString().substring(0, 10), new Timestamp(new Date().getTime()).toString().substring(0, 10), "now");

        result = systemVar.process("Something");
        assertEquals(result, "Something", "Something");

        result = systemVar.process(null);
        assertEquals(result, null, "null");
    }
}
