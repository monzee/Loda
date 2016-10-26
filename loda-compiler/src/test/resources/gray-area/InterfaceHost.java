package loda.test;

import ph.codeia.loda.Loda;

public interface InterfaceHost {
    @Loda.Lazy(1)
    int data();

    @Loda.Got(1)
    void data(int n);

    @Loda.Async(2)
    @Loda.FireAndForget
    boolean flag();

    @Loda.Got(3)
    @Loda.ShotInTheDark
    void message(String s);
}