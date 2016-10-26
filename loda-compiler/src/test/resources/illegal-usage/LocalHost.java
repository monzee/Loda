package loda.test;

import ph.codeia.loda.Loda;

public class LocalHost {
    static void main() {
        class Host {
            @Loda.Lazy(1)
            int data() {
                return 123;
            }

            @Loda.Got(1)
            void data(int num) {
            }
        }
    }
}