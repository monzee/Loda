package loda.test;

import ph.codeia.loda.Loda;

class NoClient {
    @Loda.Lazy(1)
    String data() {
        return s;
    }
}