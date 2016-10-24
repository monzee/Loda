import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import java.util.concurrent.Callable;

public abstract class BaseLoda {

    protected static class SyncLoader<T> extends Loader<T> {
        final T value;

        SyncLoader(Context context, T value) {
            super(context);
            this.value = value;
        }

        @Override
        protected void onStartLoading() {
            deliverResult(value);
        }
    }

    protected static class Result<T> {
        boolean ready = false;
        T value;
        Exception error;
    }

    protected static class AsyncLoader<T> extends AsyncTaskLoader<Result<T>> {
        final Result<T> result = new Result<T>();
        Callable<T> maker;

        public AsyncLoader(Context context, Callable<T> maker) {
            super(context);
            this.maker = maker;
        }

        @Override
        public Result<T> loadInBackground() {
            try {
                result.value = maker.call();
                maker = null;
            } catch (Exception e) {
                result.error = e;
            }
            result.ready = true;
            return result;
        }

        @Override
        protected void onStartLoading() {
            if (result.ready) {
                deliverResult(result);
            } else {
                super.onStartLoading();
            }
        }
    }

    public void prepare(FragmentActivity activity) {
        prepare(activity.getSupportLoaderManager(), activity);
    }

    public void prepare(Fragment fragment) {
        prepare(fragment.getLoaderManager(), fragment.getContext());
    }

    abstract protected void prepare(LoaderManager manager, Context context);

}
