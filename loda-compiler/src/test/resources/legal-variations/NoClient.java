package loda.test;

import ph.codeia.loda.Loda;

public class NoClient {
    @Loda.Lazy(1)
    @Loda.FireAndForget
    int data() {
        return 123;
    }
}