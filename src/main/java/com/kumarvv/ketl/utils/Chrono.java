package com.kumarvv.ketl.utils;

public class Chrono {
    private long millis = 0;
    private String name = "";

    public static Chrono start(String name) {
        Chrono ch = new Chrono();
        ch.millis = System.currentTimeMillis();
        ch.name = name;
        return ch;
    }

    public void restart() {
        millis = System.currentTimeMillis();
    }

    public void stop() {
        long diff = System.currentTimeMillis() - millis;
        System.out.printf("[%s] elapsed time: %dms\n", name, diff);
    }
}
