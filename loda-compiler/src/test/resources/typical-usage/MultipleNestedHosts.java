package loda.test;

import ph.codeia.loda.Loda;

public class MultipleNestedHosts {
    public class Here {
        @Loda.Lazy(1)
        String data() {
            return "foo";
        }

        @Loda.Got(1)
        void got(String data) {
        }
    }
    public class There {
        @Loda.Lazy(1)
        String data() {
            return "foo";
        }

        @Loda.Got(1)
        void got(String data) {
        }
    }
    public class Everywhere {
        @Loda.Lazy(1)
        String data() {
            return "foo";
        }

        @Loda.Got(1)
        void got(String data) {
        }
    }
}