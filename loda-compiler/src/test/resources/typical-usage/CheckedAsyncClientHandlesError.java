package loda.test;

import ph.codeia.loda.Loda;

public class CheckedAsyncClientHandlesError {
    @Loda.Async(1)
    String data() throws Throwable {
        return "foo";
    }

    @Loda.Got(1)
    void got(String data, Loda.Caught e) {
    }
}