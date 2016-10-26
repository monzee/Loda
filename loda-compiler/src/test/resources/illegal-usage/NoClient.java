package loda.test;

import ph.codeia.loda.Loda;

public class NoClient {
    @Loda.Lazy(1)
    String data() {
        return s;
    }
}