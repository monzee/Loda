package ph.codeia.loda;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

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
     * Auxiliary class for clients of checked async producers.
     */
    class Caught {
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

    interface Signal {
        void reload();
    }

}
