package loda.test;

import ph.codeia.loda.Loda;

public class NullaryClient {
    @Loda.Lazy(1)
    int data() {
        return 123;
    }

    @Loda.Got(1)
    void got() {
    }
}