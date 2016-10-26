package loda.test;

import ph.codeia.loda.Loda;

public class VoidProducer {
    @Loda.Lazy(1)
    @Loda.FireAndForget
    void data() {
    }
}