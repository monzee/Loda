/*
 * Copyright (c) 2016 by Mon Zafra.
 */

package ph.codeia.loda;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface Loda {

    /**
     * Generates a synchronous loader that generates a value only once.
     */
    @Target(ElementType.METHOD)
    @interface Lazy {
        /**
         * The id of this loader.
         */
        int value();
    }

    /**
     * Generates an AsyncTaskLoader with the given id.
     */
    @Target(ElementType.METHOD)
    @interface Async {
        /**
         * The id of this loader.
         */
        int value();

        /**
         * If true, a loader is not generated for this method, but this is
         * marked as a method called by an async loader.
         *
         * Used for verifying that potentially blocking methods are never
         * called in the main thread.
         */
        boolean auxiliary() default false;
    }

    /**
     * Generates the same loader as {@link Async} but passes a parameter that
     * the method can call to signal that the content has changed and should
     * be reloaded.
     */
    @Target(ElementType.METHOD)
    @interface Observe {
        /**
         * The id of this loader.
         */
        int value();
    }

    /**
     * Called by the generated callback attached to the loader with the given
     * id when a value is delivered.
     *
     * The type of the parameter of the annotated method must match the return
     * type of the loader.
     *
     * If the corresponding producer method is {@link Async} and checked, this
     * method should take an extra {@link Caught} parameter.
     */
    @Target(ElementType.METHOD)
    @interface Got {
        /**
         * The id of the loader being watched.
         */
        int value();
    }

    /**
     * Called by the generated callback attached to the loader with the given id
     * when the loader is reset.
     */
    @Target(ElementType.METHOD)
    @interface Reset {
        /**
         * The id of the loader being reset.
         */
        int value();
    }

    /**
     * Indicates that the implementor only wants to put values and is not
     * interested in the result.
     *
     * This allows {@link Async}- or {@link Lazy}- annotated methods to be void
     * or not have a corresponding {@link Got} method.
     */
    @Target(ElementType.METHOD)
    @interface FireAndForget {}

    /**
     * Indicates that the implementor assumes that the loader already exists
     * and only wants to capture the results.
     *
     * This allows {@link Got}-annotated methods to not have a corresponding
     * {@link Async} or {@link Lazy} producer.
     */
    @Target(ElementType.METHOD)
    @interface ShotInTheDark {}

    /**
     * Producer parameters must be annotated with this so that the compiler
     * will know which value to pull out of the Bundle.
     *
     * I can't pass the Bundle directly because one of the aims is to generate
     * a parallel impl that doesn't use any android types.
     */
    @Target(ElementType.PARAMETER)
    @interface Arg {
        /**
         * The key to get from the bundle. Runtime exception when types
         * don't match.
         */
        String value();
    }

    /**
     * Points back to the host class.
     *
     * Added to generated classes for the second round of annotation
     * processing to produce a factory that maps hosts to generated classes.
     */
    @Target(ElementType.TYPE)
    @interface Backlink {
        /**
         * The host class.
         */
        Class value();
    }

    /**
     * Auxiliary class for clients of checked async producers.
     */
    class Caught {

        public static final Caught NOTHING = new Caught(null);

        private final Exception error;

        public Caught(Exception error) {
            this.error = error;
        }

        public void rethrow() throws Exception {
            if (error != null) {
                throw error;
            }
        }

    }

    interface Box<T> {
        void send(T value);
    }

    interface Syncer<T> {
        void toSync(Box<T> ok, Box<Exception> error);
    }

    /**
     * Future factory.
     */
    final class Either {
        /**
         * Helper to convert asynchronous code to back to sync.
         *
         * Inside the async callback, Call {@code ok.send(T)} to set the return
         * value or {@code error.send(Exception)} when there's an error.
         *
         * @param block Contains asynchronous code. You need to make sure that
         *              the processing code is called in a different thread as
         *              the one where you make the {@link Future#get()} call,
         *              otherwise you'll get a deadlock.
         * @param <T> The type of the success value.
         * @return {@link Future#get()} will block until one of the {@link Box}
         * objects is invoked. {@link Future#cancel(boolean) Cancelling} has no
         * effect. Use {@link Future#get(long, TimeUnit)} to make sure that you
         * won't block the thread forever.
         */
        public static <T> Future<T> of(Syncer<T> block) {
            final HeavenOrHell<T> either = new HeavenOrHell<>();
            block.toSync(new Box<T>() {
                @Override
                public void send(T value) {
                    either.pass(value);
                }
            }, new Box<Exception>() {
                @Override
                public void send(Exception value) {
                    either.fail(value);
                }
            });
            return either;
        }
    }

    /**
     * Returned by the operative method of the generated class to trigger async
     * loads.
     */
    interface Hook {
        /**
         * Trigger a loader like loda got triggered in 2013.
         *
         * @param id The id of the loader.
         */
        void trigger(int id);
    }

}
