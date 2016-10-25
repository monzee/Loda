/*
 * Copyright (c) 2016 by Mon Zafra
 */

package ph.codeia.loda.gen;

import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourceSubjectFactory;

import org.junit.Test;

/**
 * This file is a part of the Loda project.
 */

public class HappyPathTest {

    private void ok(String filename) {
        Truth.assertAbout(JavaSourceSubjectFactory.javaSource())
                .that(JavaFileObjects.forResource(filename))
                .processedWith(new LodaProcessor())
                .compilesWithoutError();
    }

    @Test
    public void typical_usage() {
        ok("typical-usage/SingleSyncPair.java");
        ok("typical-usage/SingleAsyncPair.java");
        ok("typical-usage/OnePairOfEach.java");
        ok("typical-usage/CheckedAsyncProducer.java");
        ok("typical-usage/CheckedAsyncClientHandlesError.java");
        ok("typical-usage/UncheckedAsyncClientHandlesError.java");
        ok("typical-usage/UncheckedSyncClientHandlesError.java");
        ok("typical-usage/NestedHost.java");
        ok("typical-usage/MultipleNestedHosts.java");
        ok("typical-usage/DeeplyNestedHost.java");
        ok("typical-usage/NestedInInterface.java");
        ok("typical-usage/NestedInEnum.java");
        ok("typical-usage/DoubleAnnotatedProducer.java");
        ok("typical-usage/OverloadedName.java");
    }

}
