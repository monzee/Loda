package loda.test;

import ph.codeia.loda.Loda;

class VoidProducer {
    @Loda.Lazy(1)
    @Loda.FireAndForget
    void data() {
    }
}