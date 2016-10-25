package loda.test;

import ph.codeia.loda.Loda;

class StaticClient {
    @Loda.Lazy(1)
    int data() {
        return 1;
    }

    @Loda.Got(1)
    static void data(int n) {
    }
}