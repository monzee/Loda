/*
 * Copyright (c) 2016 by Mon Zafra
 */

package ph.codeia.loda.loaders;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ph.codeia.loda.Loda;

/**
 * This file is a part of the Loda project.
 *
 * @author mon
 */

public final class Akke {

    public interface Get<T> {
        T value() throws Exception;
    }

    public interface Then<T> {
        void got(T value, Loda.Caught error);
    }

    public static abstract class Sync<T> {
        public abstract T from(LoaderManager manager, Context context);

        public T from(FragmentActivity activity) {
            return from(activity.getSupportLoaderManager(), activity);
        }

        public T from(Fragment fragment) {
            return from(fragment.getLoaderManager(), fragment.getContext());
        }
    }

    public static abstract class Async {
        public abstract void from(LoaderManager manager, Context context);

        public void from(FragmentActivity activity) {
            from(activity.getSupportLoaderManager(), activity);
        }

        public void from(Fragment fragment) {
            from(fragment.getLoaderManager(), fragment.getContext());
        }
    }

    public static <T> void put(int id, Get<T> producer) {
        new AsyncImpl<>(id, producer, null);
    }

    public static <T> Sync<T> sync(int id, T fallback) {
        return new SyncImpl<T>(id, fallback);
    }

    public static <T> Sync<T> sync(int id, Get<T> producer) {
        return new SyncImpl<>(id, producer);
    }

    public static <T> Async async(int id, Get<T> producer, Then<T> consumer) {
        return new AsyncImpl<>(id, producer, consumer);
    }

    public static <T> Async async(int id, Then<T> consumer) {
        return new AsyncImpl<>(id, null, consumer);
    }

    private Akke() {}

}


class SyncImpl<T> extends Akke.Sync<T> {
    private static final String CALLED_BEFORE_ON_START =
            "Don't call this before activity/fragment #onStart()";

    private final int id;
    private final Akke.Get<T> producer;
    private final T fallback;

    SyncImpl(int id, Akke.Get<T> producer) {
        this.id = id;
        this.producer = producer;
        fallback = null;
    }

    SyncImpl(int id, T fallback) {
        this.id = id;
        this.fallback = fallback;
        producer = null;
    }

    @Override
    public T from(final LoaderManager manager, final Context context) {
        try {
            return Loda.Either.of(new Loda.Syncer<T>() {
                @Override
                public void toSync(final Loda.Box<T> ok, final Loda.Box<Exception> error) {
                    manager.initLoader(id, null, new LoaderManager.LoaderCallbacks<T>() {
                        @Override
                        public Loader<T> onCreateLoader(int id, Bundle args) {
                            T value = fallback;
                            if (producer != null) try {
                                value = producer.value();
                            } catch (Exception e) {
                                error.send(e);
                            }
                            return new SyncLoader<>(context, value);
                        }

                        @Override
                        public void onLoadFinished(Loader<T> loader, T data) {
                            ok.send(data);
                        }

                        @Override
                        public void onLoaderReset(Loader<T> loader) {}
                    });
                }
            }).get(1, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            return null;
        } catch (TimeoutException e) {
            throw new IllegalStateException(CALLED_BEFORE_ON_START);
        }
    }
}


class AsyncImpl<T> extends Akke.Async {
    private final int id;
    private final Akke.Get<T> producer;
    private final Akke.Then<T> consumer;

    AsyncImpl(int id, Akke.Get<T> producer, Akke.Then<T> consumer) {
        this.id = id;
        this.producer = producer;
        this.consumer = consumer;
    }

    @Override
    public void from(LoaderManager manager, final Context context) {
        manager.initLoader(id, null, new LoaderManager.LoaderCallbacks<AsyncLoader.Result<T>>() {
            @Override
            public Loader<AsyncLoader.Result<T>> onCreateLoader(int id, Bundle args) {
                if (producer == null) {
                    return null;
                }
                return new AsyncLoader<>(context, new Callable<T>() {
                    @Override
                    public T call() throws Exception {
                        return producer.value();
                    }
                });
            }

            @Override
            public void onLoadFinished(
                    Loader<AsyncLoader.Result<T>> loader,
                    AsyncLoader.Result<T> data
            ) {
                if (consumer != null) {
                    consumer.got(data.value, new Loda.Caught(data.error));
                }
            }

            @Override
            public void onLoaderReset(Loader<AsyncLoader.Result<T>> loader) {}
        });
    }
}
