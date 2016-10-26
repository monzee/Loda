package loda.test;

import ph.codeia.loda.Loda;

public class InvisibleClient {
    @Loda.Lazy(1)
    int data() {
        return 123;
    }

    @Loda.Got(1)
    protected void data(int n) {
    }

    @Loda.Async(2)
    boolean flag() {
        return false;
    }

    @Loda.Got(2)
    private void flag(boolean flag) {
    }
}