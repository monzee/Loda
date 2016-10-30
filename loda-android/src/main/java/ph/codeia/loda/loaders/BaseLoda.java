/*
 * Copyright (c) 2016 by Mon Zafra.
 */

package ph.codeia.loda.loaders;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;

import ph.codeia.loda.Loda;

/**
 * This file is a part of the Loda project.
 */

public abstract class BaseLoda {

    protected static final String BAD_ID = "ID not found in loader registry.";

    public Loda.Hook prepare(FragmentActivity activity) {
        return prepare(activity.getSupportLoaderManager(), activity.getApplicationContext());
    }

    public Loda.Hook prepare(Fragment fragment) {
        return prepare(fragment.getLoaderManager(), fragment.getContext());
    }

    protected abstract Loda.Hook prepare(LoaderManager manager, Context context);

}
