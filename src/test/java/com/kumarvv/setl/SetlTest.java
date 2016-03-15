package com.kumarvv.setl;

import com.kumarvv.setl.core.ExtractorTest;
import com.kumarvv.setl.model.Def;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class SetlTest {

    Setl setl;

    @BeforeMethod
    public void setup() {
        setl = spy(Setl.class);
    }

    @Test
    public void testStart() {
        Def def = mock(Def.class);
        setl.start(def);
        setl.start(null);
    }

    @Test
    public void testLoadFile() {
        setl.loadFile(getThisPath() + "test.json");
        setl.loadFile(getThisPath() + "csv.json");
        setl.loadFile("invalid.json");
        setl.loadFile(null);
    }

    private String getThisPath() {
        return ExtractorTest.class.getResource("/").getPath();
    }

}
