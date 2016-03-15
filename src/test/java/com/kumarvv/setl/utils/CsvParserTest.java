package com.kumarvv.setl.utils;

import com.kumarvv.setl.core.ExtractorTest;
import com.kumarvv.setl.model.Csv;
import org.apache.commons.csv.CSVParser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

public class CsvParserTest {

    CsvParser csvParser;

    @BeforeMethod
    public void setup() {
        csvParser = spy(CsvParser.getInstance(new Csv()));
    }

    @Test
    public void testParse() throws IOException {
        String line = "1,\"Hello World\",95.15";

        csvParser.csv.setFile("test.csv");
        csvParser.csv.setFilePath(getThisPath() + "test.csv");
        csvParser.csv.setColumns(Arrays.asList("code", "name", "age", "unknown"));
        List<Map<String, Object>> data = csvParser.parse();
        assertEquals(data.size(), 4, "size0");
        assertEquals(data.get(3).get("code").toString(), "CBM", "code");
        assertEquals(data.get(3).get("name").toString(), "Kambam, Bodi", "name");
        assertEquals(data.get(3).get("age").toString(), "33", "age");
        assertEquals(data.get(3).get("unknown"), null, "unknown");

        doThrow(IOException.class).when(csvParser).parseFile(any(File.class));
        data = csvParser.parse();
        assertEquals(data.size(), 0, "exception size");

        csvParser.csv.setFile("none.csv");
        csvParser.csv.setFilePath(getThisPath() + "none.csv");
        data = csvParser.parse();
        assertEquals(data.size(), 0, "size1");
    }

    @Test
    public void testParseNull() throws IOException {
        csvParser.csv = null;
        List<Map<String, Object>> data = csvParser.parse();
        assertEquals(data.size(), 0, "null1");

        CSVParser parser = csvParser.parseFile(null);
        assertEquals(parser, null, "null2");

        csvParser.processRecord(null, null);
    }


    private String getThisPath() {
        return ExtractorTest.class.getResource("/").getPath();
    }
}
