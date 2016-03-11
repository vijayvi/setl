package com.kumarvv.ketl.core;

import com.kumarvv.ketl.model.Def;
import com.kumarvv.ketl.model.Row;
import com.kumarvv.ketl.model.Status;
import com.kumarvv.ketl.utils.Chrono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.IntStream;

public class KetlProcessor implements Runnable {
    public final int DEFAULT_NUM_THREADS = 6;

    final BlockingQueue<Row> queue;
    final Status status;
    final Def def;

    public KetlProcessor(final Status status, final Def def) {
        this.status = status;
        this.def = def;
//        this.queue = new SynchronousQueue<>();
        this.queue = new LinkedBlockingDeque<>(getNumThreads());
    }

    @Override
    public void run() {
        process();
    }

    void process() {
        status.reset();
        Chrono ch = Chrono.start("Processor");

        Thread et = startExtractor();
        List<Thread> lts = startLoaders();

        try {
            et.join();
            for (Thread lt : lts) {
                lt.join();
            }
        } catch (InterruptedException ie) {}
        ch.stop();
    }

    Thread startExtractor() {
        Extractor extractor = new Extractor(queue, def, status, (result) -> {
            IntStream.range(0, getNumThreads()).forEach((i) -> addDoneRow());
        });
        Thread et = new Thread(extractor, "extractor");
        et.start();

        return et;
    }

    void addDoneRow() {
        try {
            queue.put(Row.DONE);
        } catch (InterruptedException ie) {}
    }

    List<Thread> startLoaders() {
        List<Thread> lts = new ArrayList<>();
        IntStream.range(0, getNumThreads()).forEach((i) -> {
            Loader loader = new Loader("l"+i, queue, status, def);
            Thread lt = new Thread(loader);
            lt.start();
            lts.add(lt);
        });

        return lts;
    }

    int getNumThreads() {
        if (def != null && def.getThreads() > 0) {
            return def.getThreads();
        }
        return DEFAULT_NUM_THREADS;
    }
}
