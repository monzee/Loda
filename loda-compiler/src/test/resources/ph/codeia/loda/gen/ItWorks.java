package ph.codeia.loda.test;

import ph.codeia.loda.Loda;

class ItWorks {
    @Loda.Lazy(100)
    String syncProducer() {
        return "hey";
    }

    @Loda.Got(100)
    void syncClient(String s) {
        System.out.println(s);
    }

    @Loda.Async(200)
    int asyncProducer() {
        return 322;
    }

    @Loda.Got(200)
    void asyncClient(int n) {
        System.out.println(n);
    }
}