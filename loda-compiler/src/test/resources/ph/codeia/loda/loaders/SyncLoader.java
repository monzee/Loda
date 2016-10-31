package ph.codeia.loda.loaders;

import android.content.Context;
import android.support.v4.content.Loader;

public class SyncLoader<T> extends Loader<T> {
    public SyncLoader(Context context, T value) {}
    public T value() {
        return null;
    }
}