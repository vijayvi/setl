package com.kumarvv.ketl.core;

import com.kumarvv.ketl.model.Def;
import com.kumarvv.ketl.model.Row;
import com.kumarvv.ketl.model.Status;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class KetlProcessorTest {

    KetlProcessor processor;
    BlockingQueue<Row> queue;
    Def def;
    Status status;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void setup() {
        queue = mock(SynchronousQueue.class);
        def = spy(new Def());
        status = spy(new Status((s) -> {}));
        processor = spy(new KetlProcessor(status, def));
    }

    @Test
    public void testProcess() {
        processor.process();
        processor.run();
    }

    @Test
    public void testStartExtractors() throws InterruptedException {
        Thread result = processor.startExtractor();
        assertEquals(result.getName(), "extractor", "thread");

        doThrow(InterruptedException.class).when(queue).put(any(Row.class));
        result = processor.startExtractor();
        assertEquals(result.getName(), "extractor", "thread");
    }

    @Test
    public void testStartLoaders() {
        List<Thread> result = processor.startLoaders();
        assertEquals(result.size(), 6, "default");

        doReturn(3).when(def).getThreads();
        result = processor.startLoaders();
        assertEquals(result.size(), 3, "def");
    }

    @Test
    public void testGetNumThreads() {
        int result = processor.getNumThreads();
        assertEquals(result, 6, "default");

        doReturn(3).when(def).getThreads();
        result = processor.getNumThreads();
        assertEquals(result, 3, "defined");

        processor = spy(new KetlProcessor(status, null));
        result = processor.getNumThreads();
        assertEquals(result, 6, "null");
    }
}
