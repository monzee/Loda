package loda.test;

import ph.codeia.loda.Loda;

public enum NestedInEnum {
    A, B, C;
    public class Here {
        @Loda.Lazy(1)
        String data() {
            return "foo";
        }

        @Loda.Got(1)
        void got(String data) {
        }
    }
}