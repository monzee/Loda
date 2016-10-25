/*
 * Copyright (c) 2016 by Mon Zafra.
 */

package ph.codeia.loda;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This file is a part of the Loda project.
 */

class HeavenOrHell<T> implements Future<T> {

    private enum State { WAIT, OK, ERROR }
    private State status = State.WAIT;
    private T value;
    private Exception error;

    void pass(T value) {
        if (status != State.WAIT) {
            return;
        }
        synchronized (this) {
            this.value = value;
            status = State.OK;
            notifyAll();
        }
    }

    void fail(Exception error) {
        if (status != State.WAIT) {
            return;
        }
        synchronized (this) {
            this.error = error;
            status = State.ERROR;
            notifyAll();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!mayInterruptIfRunning || status != State.WAIT) {
            return false;
        }
        fail(new CancellationException());
        return true;
    }

    @Override
    public boolean isCancelled() {
        return status == State.ERROR && error instanceof CancellationException;
    }

    @Override
    public boolean isDone() {
        return status != State.WAIT;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException, CancellationException {
        while (true) switch (status) {
            case OK:
                return value;
            case ERROR:
                if (error instanceof CancellationException) {
                    throw (CancellationException) error;
                } else {
                    throw new ExecutionException(error);
                }
            case WAIT:
                synchronized (this) {
                    wait();
                }
        }
    }

    @Override
    public T get(
            long timeout, TimeUnit unit
    ) throws InterruptedException, ExecutionException, TimeoutException, CancellationException {
        if (timeout <= 0) {
            return get();
        }
        long remaining = unit.toNanos(timeout);
        while (true) switch (status) {
            case OK:
                return value;
            case ERROR:
                if (error instanceof CancellationException) {
                    throw (CancellationException) error;
                } else {
                    throw new ExecutionException(error);
                }
            case WAIT:
                if (remaining <= 0) {
                    throw new TimeoutException();
                }
                synchronized (this) {
                    long start = System.nanoTime();
                    TimeUnit.NANOSECONDS.timedWait(this, remaining);
                    long elapsed = System.nanoTime() - start;
                    remaining -= elapsed;
                }
        }
    }

}
