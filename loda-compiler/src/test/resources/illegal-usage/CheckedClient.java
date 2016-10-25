package loda.test;

import ph.codeia.loda.Loda;

class CheckedClient {
    @Loda.Lazy(1)
    int data() {
        return 123;
    }

    @Loda.Got(1)
    void got(int data) throws Exception {
    }
}