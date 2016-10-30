/*
 * Copyright (c) 2016 by Mon Zafra.
 */

package ph.codeia.loda.loaders;

import android.content.Context;
import android.support.v4.content.Loader;

/**
 * This file is a part of the Loda project.
 */

public class SyncLoader<T> extends Loader<T> {

    private final T value;

    public SyncLoader(Context context, T value) {
        super(context);
        this.value = value;
    }

    public T value() {
        return value;
    }

    @Override
    protected void onStartLoading() {
        deliverResult(value);
    }

}
