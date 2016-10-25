package loda.test;

import ph.codeia.loda.Loda;

class DeeplyNestedHost {
    class Here {
        class There {
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
    }
}