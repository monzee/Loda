package loda.test;

import ph.codeia.loda.Loda;

public class DualRoleMethod {
    @Loda.Lazy(1)
    @Loda.Got(1)
    String identity(String s) {
        return s;
    }
}