package com.kumarvv.ketl.model;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class Status {
    private AtomicLong rowsFound = new AtomicLong(0);
    private AtomicLong rowsProcessed = new AtomicLong(0);
    private AtomicLong rowsInserted = new AtomicLong(0);
    private AtomicLong rowsUpdated = new AtomicLong(0);
    private AtomicLong rowsFailed = new AtomicLong(0);

    private AtomicBoolean done = new AtomicBoolean(false);

    private final Consumer<Status> onUpdate;

    public Status(Consumer<Status> onUpdate) {
        this.onUpdate = onUpdate;
    }

    protected void onUpdateCallback() {
        if (onUpdate != null) {
            onUpdate.accept(this);
        }
    }

    public long getRowsFound() {
        return rowsFound.get();
    }

    public long getRowsProcessed() {
        return rowsProcessed.get();
    }

    public long getRowsInserted() {
        return rowsInserted.get();
    }

    public long getRowsUpdated() {
        return rowsUpdated.get();
    }

    public long getRowsFailed() {
        return rowsFailed.get();
    }

    public long incrementFound() {
        long c =  rowsFound.incrementAndGet();
        onUpdateCallback();
        return c;
    }

    public long incrementProcessed() {
        long c =  rowsProcessed.incrementAndGet();
        onUpdateCallback();
        return c;
    }

    public long incrementInserted() {
        long c = rowsInserted.incrementAndGet();
        onUpdateCallback();
        return c;
    }

    public long incrementUpdated() {
        long c = rowsUpdated.incrementAndGet();
        onUpdateCallback();
        return c;
    }

    public long incrementFailed() {
        long c = rowsFailed.incrementAndGet();
        onUpdateCallback();
        return c;
    }

    public boolean isDone() {
        return done.get();
    }

    public void markAsDone() {
        done.set(true);
        onUpdateCallback();
    }

    public void reset() {
        rowsProcessed = new AtomicLong(0);
        rowsInserted = new AtomicLong(0);
        rowsUpdated = new AtomicLong(0);
        rowsFailed = new AtomicLong(0);
        done = new AtomicBoolean(false);
    }
}
