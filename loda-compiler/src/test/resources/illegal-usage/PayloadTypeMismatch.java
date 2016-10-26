package loda.test;

import ph.codeia.loda.Loda;

public class PayloadTypeMismatch {
    @Loda.Lazy(1)
    String data() {
        return "foo";
    }

    @Loda.Got(1)
    void got(int data) {
    }
}