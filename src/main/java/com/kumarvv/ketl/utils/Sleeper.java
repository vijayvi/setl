package com.kumarvv.ketl.utils;

public class Sleeper {
    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {}
    }
}
