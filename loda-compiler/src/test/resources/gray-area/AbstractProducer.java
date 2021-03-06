package loda.test;

import ph.codeia.loda.Loda;

public abstract class AbstractProducer {
    @Loda.Lazy(1)
    abstract int data();

    @Loda.Got(1)
    void data(int n) {
    }
}