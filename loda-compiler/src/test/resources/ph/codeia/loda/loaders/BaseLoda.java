package ph.codeia.loda.loaders;

import android.support.v4.app.LoaderManager;
import android.content.Context;
import ph.codeia.loda.Loda;

public abstract class BaseLoda {
    protected static final String BAD_ID = "bad id.";

    protected abstract Loda.Hook prepare(LoaderManager manager, Context context);
}