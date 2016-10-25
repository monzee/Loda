package loda.test;

import ph.codeia.loda.Loda;

class NoClient {
    @Loda.Lazy(1)
    @Loda.FireAndForget
    int data() {
        return 123;
    }
}