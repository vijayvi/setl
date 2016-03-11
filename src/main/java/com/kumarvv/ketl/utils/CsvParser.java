package com.kumarvv.ketl.utils;

import com.kumarvv.ketl.model.Csv;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvParser {

    protected Csv csv;

    public static CsvParser getInstance(Csv csv) {
        CsvParser parser = new CsvParser();
        parser.csv = csv;
        return parser;
    }

    protected CsvParser() {
        // not allowed
    }

    public List<Map<String, Object>> parse() {
        final List<Map<String, Object>> data = new ArrayList<>();
        String file = csv.getFilePath();
        if (StringUtils.isEmpty(file)) {
            return data;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            br.lines().forEach(line -> processLine(data, line));
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        return data;
    }

    protected void processLine(final List<Map<String, Object>> data, String line) {
        if (data == null || StringUtils.isEmpty(line)) {
            return;
        }

        String[] values = line.split(",");
        final Map<String, Object> row = new HashMap<>();
        for (int i = 0; i < csv.getColumns().size(); i++) {
            if (i >= values.length) {
                break;
            }
            row.put(csv.getColumns().get(i), values[i]);
        }
        data.add(row);
    }

}
