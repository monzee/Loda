package loda.test;

import ph.codeia.loda.Loda;

public class VoidProducer {
    @Loda.Lazy(1)
    void data() {
    }

    @Loda.Got(1)
    void got(int data) {
    }
}