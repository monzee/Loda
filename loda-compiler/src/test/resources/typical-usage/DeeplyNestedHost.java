package loda.test;

import ph.codeia.loda.Loda;

public class DeeplyNestedHost {
    public class Here {
        public class There {
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
    }
}