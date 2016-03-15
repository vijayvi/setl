package com.kumarvv.setl.utils;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

public class SetlCacheTest {

    SetlCache cache;

    @BeforeMethod
    public void setup() {
        cache = spy(SetlCache.getInstance());

        cache.set("select id from emp", 123L);
        cache.set("custom_sql_value", new Date());
        cache.set("name", "Setl Cache");
        cache.set("null_value", null);
        cache.set(null, null);
    }

    @Test void testCache() {
        assertEquals(cache.exists("name"), true, "exists");
        assertEquals(cache.exists("some unknown"), false, "not exists");
        assertEquals(cache.exists(null), false, "exists null");

        assertEquals(cache.get("name"), "Setl Cache", "name");
        assertEquals(cache.get("select id from emp"), Long.valueOf(123), "select...");
        assertEquals(cache.get("some unknown"), null, "unknown");
        assertEquals(cache.get(null), null, "get null");

        assertEquals(cache.size(), 3, "before clear");
        cache.clear();
        assertEquals(cache.size(), 0, "after clear");
    }
}
