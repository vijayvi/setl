package com.kumarvv.ketl.utils;

import java.sql.Timestamp;

public class SystemVar {

    public static final String NOW = "%NOW%";

    private static SystemVar _instance;

    public static SystemVar getInstance() {
        if (_instance == null) {
            _instance = new SystemVar();
        }
        return _instance;
    }

    protected SystemVar() {}

    public Object process(Object val) {
        if (val == null || !(val instanceof String)) {
            return val;
        }

        if (val.equals(NOW)) {
            return new Timestamp(new java.util.Date().getTime());
        }
        return val;
    }
}
