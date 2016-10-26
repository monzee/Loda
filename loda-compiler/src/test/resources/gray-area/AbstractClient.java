package loda.test;

import ph.codeia.loda.Loda;

public abstract class AbstractClient {
    @Loda.Lazy(1)
    int data() {
        return 123;
    }

    @Loda.Got(1)
    abstract void data(int n);
}