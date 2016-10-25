package loda.test;

import ph.codeia.loda.Loda;

class OverloadedName {
    @Loda.Lazy(1)
    String data() {
        return "foo";
    }

    @Loda.Got(1)
    void data(String data) {
    }
}