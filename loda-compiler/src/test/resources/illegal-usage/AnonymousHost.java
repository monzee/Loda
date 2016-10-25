package loda.test;

import ph.codeia.loda.Loda;

class AnonymousHost {
    static void main() {
        new Object() {
            @Loda.Lazy(1)
            String data() {
                return "foo";
            }

            @Loda.Got(1)
            void got(String data) {
            }
        };
    }
}