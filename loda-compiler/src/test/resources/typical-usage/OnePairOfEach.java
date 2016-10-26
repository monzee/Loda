package loda.test;

import ph.codeia.loda.Loda;

public class OnePairOfEach {
    @Loda.Lazy(1)
    String data() {
        return "foo";
    }

    @Loda.Got(1)
    void got(String data) {
    }

    @Loda.Async(2)
    String otherData() {
        return "foo";
    }

    @Loda.Got(2)
    void gotOther(String data) {
    }
}