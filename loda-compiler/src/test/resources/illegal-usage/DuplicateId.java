package loda.test;

import ph.codeia.loda.Loda;

public class DuplicateId {
    @Loda.Lazy(1)
    String data() {
        return "foo";
    }

    @Loda.Got(1)
    void got(String data) {
    }

    @Loda.Lazy(1)
    String same() {
        return "foo";
    }

    @Loda.Got(1)
    void gotSame(String data) {
    }
}