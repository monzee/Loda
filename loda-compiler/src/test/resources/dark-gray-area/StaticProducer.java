package loda.test;

import ph.codeia.loda.Loda;

public class StaticProducer {
    @Loda.Lazy(1)
    static int data() {
        return 1;
    }

    @Loda.Got(1)
    void data(int n) {
    }
}