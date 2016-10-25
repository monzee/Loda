package loda.test;

import ph.codeia.loda.Loda;

interface NestedInInterface {
    class Here {
        @Loda.Lazy(1)
        String data() {
            return "foo";
        }

        @Loda.Got(1)
        void got(String data) {
        }
    }
}