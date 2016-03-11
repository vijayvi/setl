package com.kumarvv.ketl.model;

import org.apache.commons.collections4.MapUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Row implements Serializable {
    private final Map<String, Integer> columns;
    private final Map<String, Object> data;

    public static Row DONE = new Row(new HashMap<>(), new HashMap<>()) {
        @Override
        public boolean isDone() {
            return true;
        }
    };

    public Row(Map<String, Integer> columns, Map<String, Object> data) {
        this.columns = MapUtils.emptyIfNull(columns);
        this.data = MapUtils.emptyIfNull(data);
    }

    public Map<String, Integer> getColumns() {
        return columns;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Integer typeOf(String col) {
        return columns.get(col);
    }

    public Object get(String col) {
        return data.get(col);
    }

    public boolean isDone() {
        return false;
    }
}
