package ph.codeia.loda.loaders;

import android.content.Context;
import java.util.concurrent.Callable;
import android.support.v4.content.Loader;

public class AsyncLoader<T> extends Loader<AsyncLoader.Result<T>> {

    public static class Result<T> {
        public T value;
        public Exception error;
    }

    public AsyncLoader(Context context, Callable<T> callable) {}
}