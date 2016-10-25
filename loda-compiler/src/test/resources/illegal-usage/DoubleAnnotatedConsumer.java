package loda.test;

import ph.codeia.loda.Loda;

class DoubleAnnotatedConsumer {
    @Loda.Lazy(1)
    String one() {
        return "foo";
    }

    @Loda.Async(2)
    String two() {
        return "foo";
    }

    @Loda.Got(1)
    @Loda.Got(2)
    void gotBoth(String data) {
    }
}