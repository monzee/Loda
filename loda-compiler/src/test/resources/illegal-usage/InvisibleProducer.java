package loda.test;

import ph.codeia.loda.Loda;

class InvisibleProducer {
    @Loda.Lazy(1)
    protected int data() {
        return 123;
    }

    @Loda.Got(1)
    void data(int n) {
    }

    @Loda.Async(2)
    private boolean flag() {
        return false;
    }

    @Loda.Got(2)
    void flag(boolean flag) {
    }
}