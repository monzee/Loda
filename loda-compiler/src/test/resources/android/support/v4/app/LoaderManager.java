package android.support.v4.app;

import android.support.v4.content.Loader;
import android.os.Bundle;

public class LoaderManager {

    public <T> Loader<T> initLoader(int id, Bundle args, LoaderCallbacks<T> cb) {
        return null;
    }

    public interface LoaderCallbacks<T> {
        Loader<T> onCreateLoader(int id, Bundle bundle);
        void onLoadFinished(Loader<T> loader, T data);
        void onLoaderReset(Loader<T> loader);
    }
}