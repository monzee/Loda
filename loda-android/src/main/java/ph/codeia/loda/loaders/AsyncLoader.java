/*
 * Copyright (c) 2016 by Mon Zafra.
 */

package ph.codeia.loda.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.concurrent.Callable;

/**
 * This file is a part of the Loda project.
 */

public class AsyncLoader<T> extends AsyncTaskLoader<AsyncLoader.Result<T>> {

    public static class Result<T> {
        public T value;
        public Exception error;
        private boolean ready = false;
    }

    private final Result<T> result = new Result<>();
    private Callable<T> block;

    public AsyncLoader(Context context, Callable<T> block) {
        super(context);
        this.block = block;
    }

    @Override
    public Result<T> loadInBackground() {
        if (result.ready) {
            return result;
        }
        try {
            result.value = block.call();
        } catch (Exception e) {
            result.error = e;
        }
        block = null;
        return result;
    }

    @Override
    public void deliverResult(Result<T> data) {
        result.ready = true;
        super.deliverResult(data);
    }

}
