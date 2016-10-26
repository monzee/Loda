package loda.test;

import ph.codeia.loda.Loda;

public class DoubleAnnotatedProducer {
    @Loda.Lazy(1)
    @Loda.Async(2)
    String both() {
        return "foo";
    }

    @Loda.Got(1)
    void got(String data) {
    }

    @Loda.Got(2)
    void gotAsync(String data) {
    }
}