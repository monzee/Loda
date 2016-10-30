/*
 * Copyright (c) 2016 by Mon Zafra.
 */

package loda.test;

import ph.codeia.loda.Loda;

public class NoClient {
    @Loda.Lazy(1)
    String data() {
        return "foo";
    }

    @Loda.Async(2)
    String asyncData() {
        return "bar";
    }
}