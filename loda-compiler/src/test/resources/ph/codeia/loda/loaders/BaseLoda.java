package ph.codeia.loda.loaders;

import android.support.v4.app.LoaderManager;
import android.content.Context;

public abstract class BaseLoda {
    protected abstract void prepare(LoaderManager manager, Context context);
}