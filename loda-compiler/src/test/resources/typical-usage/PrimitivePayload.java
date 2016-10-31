package loda.test;

import ph.codeia.loda.Loda;

public class PrimitivePayload {
    @Loda.Lazy(1)
    int data() {
        return 123;
    }

    @Loda.Got(1)
    void got(int data) {
    }

    @Loda.Async(2)
    boolean asyncData() {
        return true;
    }

    @Loda.Got(2)
    void asyncGot(boolean data) {
    }
}