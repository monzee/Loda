/*
 * Copyright (c) 2016 by Mon Zafra.
 */

package ph.codeia.loda.loaders;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;

/**
 * This file is a part of the Loda project.
 */

public abstract class BaseLoda {

    public void prepare(FragmentActivity activity) {
        prepare(activity.getSupportLoaderManager(), activity.getApplicationContext());
    }

    public void prepare(Fragment fragment) {
        prepare(fragment.getLoaderManager(), fragment.getContext());
    }

    protected abstract void prepare(LoaderManager manager, Context context);

}
