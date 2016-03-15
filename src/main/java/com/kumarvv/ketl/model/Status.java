/**
 * Copyright (c) 2016 Vijay Vijayaram
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
