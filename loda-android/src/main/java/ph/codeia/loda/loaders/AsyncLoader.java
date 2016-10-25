/*
 * Copyright (c) 2016 by Mon Zafra.
 */

package ph.codeia.loda.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

/**
 * This file is a part of the Loda project.
 */

public class AsyncLoader<T> extends AsyncTaskLoader<AsyncLoader.Result<T>> {

    public static class Result<T> {
        public T value;
        public Exception error;
        private boolean ready = false;
    }

    private static final String PREEMPTED =
            "The producer function got GCed before I had a chance to call it.";

    private final Result<T> result = new Result<>();
    private WeakReference<Callable<T>> block;

    public AsyncLoader(Context context, Callable<T> block) {
        super(context);
        this.block = new WeakReference<>(block);
    }

    @Override
    public Result<T> loadInBackground() {
        if (result.ready) {
            return result;
        }
        Callable<T> producer = block.get();
        if (producer == null) {
            result.error = new CancellationException(PREEMPTED);
            return result;
        }
        try {
            result.value = producer.call();
        } catch (Exception e) {
            result.error = e;
        }
        return result;
    }

    @Override
    public void deliverResult(Result<T> data) {
        result.ready = true;
        super.deliverResult(data);
    }

}
