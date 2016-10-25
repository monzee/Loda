package loda.test;

import ph.codeia.loda.Loda;

class MultipleNestedHosts {
    class Here {
        @Loda.Lazy(1)
        String data() {
            return "foo";
        }

        @Loda.Got(1)
        void got(String data) {
        }
    }
    class There {
        @Loda.Lazy(1)
        String data() {
            return "foo";
        }

        @Loda.Got(1)
        void got(String data) {
        }
    }
    class Everywhere {
        @Loda.Lazy(1)
        String data() {
            return "foo";
        }

        @Loda.Got(1)
        void got(String data) {
        }
    }
}