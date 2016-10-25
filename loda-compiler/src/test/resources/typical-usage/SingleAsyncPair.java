package loda.test;

import ph.codeia.loda.Loda;

class SingleAsyncPair {
    @Loda.Async(1)
    String data() {
        return "foo";
    }

    @Loda.Got(1)
    void got(String data) {
    }
}