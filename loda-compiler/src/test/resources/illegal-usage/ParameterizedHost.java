package loda.test;

import ph.codeia.loda.Loda;

public class ParameterizedHost<T> {
    @Loda.Lazy(1)
    T data() {
        return null;
    }

    @Loda.Got(1)
    void data(T t) {}
}