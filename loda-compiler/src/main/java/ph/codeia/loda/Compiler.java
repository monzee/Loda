package ph.codeia.loda;

/**
 * This file is a part of the vanilla project.
 */

public class Compiler {

    public static void sketch() {
        // look for classes with methods annotated with Lazy, Async, Got

        // pair up Lazy-Got and Async-Got with the same id
        // pair ids should be unique
        // producer return and client param types should match
        // producer return should not be void

        // left-over Lazy and Async should have FireAndForget
        // pair them up with NullClient
        // void methods allowed here

        // left-over Got should have ShotInTheDark
        // pair them up with NullProducer

        // ANDROID-SPECIFIC:

        // generate base class BaseLoda with the common inner classes and methods
        // only do this once. should be in the root package.

        // generate class {HostName}Loda extends BaseLoda

        // constructor should take {HostName} and assign it to a 'host' field

        // get the Lazy-Got pairs
        // for each pair, generate something like
        /*
        manager.initLoader({pair.id}, null, new Callbacks<{pair.type}>() {
            @Override public Loader<{pair.type}> onCreateLoader(int id, Bundle args) {
                return new SyncLoader<>(context, host.{pair.produceCall});   [1] [2]
            }
            @Override public void onLoadFinished(Loader<{pair.type}> loader, {pair.type} data) {
                host.{pair.clientCall}(data);  [3]
            }
            @Override public void onResetLoader(Loader<{pair.type}> loader) {}
        });
        [1] produceCall should include the parens because the method might have params.
        [2] if the pair has a NullProducer, the method should just return null.
        [3] if the pair has a NullClient, this method should be empty.
        */

        // get the Async-Got pairs
        /*
        [0]
        manager.initLoader({pair.id}, null, new Callbacks<Result<{pair.type}>>() {
            @Override public Loader<Result<{pair.type}>> onCreateLoader(int id, Bundle args) {
                return new AsyncLoader<>(context, new Callable<{pair.type}>() {
                    @Override public List<{pair.type}> call() throws Exception {
                        return host.{pair.produceCall};
                    }
                });
            }
            @Override public void onLoadFinished(Loader<Result<{pair.type}>> loader, Result<{pair.type}> data) {
                host.{pair.clientCall}(data.value, new Loda.Caught(data.error));  [1] [2]
            }
            @Override public void onResetLoader(Loader<Result<{pair.type}>> loader) {}
        });
        [0] above notes apply here too
        [1] if the client method doesn't take a Loda.Caught param, this should just be (data.value)
        [2] the producer method doesn't have to be checked if the client has a Caught param. it
            might throw RuntimeExceptions too, those are still caught by the async loader
            implementation.
         */

        // NON-ANDROID: later
    }

}
