package com.kumarvv.ketl;

import com.kumarvv.ketl.core.ExtractorTest;
import com.kumarvv.ketl.model.Def;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class KetlTest {

    Ketl ketl;

    @BeforeMethod
    public void setup() {
        ketl = spy(Ketl.class);
    }

    @Test
    public void testStart() {
        Def def = mock(Def.class);
        ketl.start(def);
        ketl.start(null);
    }

    @Test
    public void testLoadFile() {
        ketl.loadFile(getThisPath() + "test.json");
        ketl.loadFile(getThisPath() + "csv.json");
        ketl.loadFile("invalid.json");
        ketl.loadFile(null);
    }

    private String getThisPath() {
        return ExtractorTest.class.getResource("/").getPath();
    }

}
