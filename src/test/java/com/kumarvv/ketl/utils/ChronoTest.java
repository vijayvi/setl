package com.kumarvv.ketl.utils;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

public class ChronoTest {

    Chrono chrono;

    @BeforeMethod
    public void setup() {
        chrono = spy(Chrono.start("test"));
    }

    @Test
    public void testAll() {
        long millis = chrono.millis;
        assertEquals(chrono.name, "test", "name");

        chrono.restart();
        assertEquals(millis < chrono.millis, true, "restart");
        millis = chrono.millis;

        chrono.stop();
    }

}
