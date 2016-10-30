/*
 * Copyright (c) 2016 by Mon Zafra
 */

package ph.codeia.loda.gen;

import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourceSubjectFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * This file is a part of the Loda project.
 */

@RunWith(Parameterized.class)
public class HappyPathTest {

    @Parameterized.Parameter public String filename;

    @Test
    public void ok() {
        Truth.assertAbout(JavaSourceSubjectFactory.javaSource())
                .that(JavaFileObjects.forResource("typical-usage/" + filename + ".java"))
                .processedWith(new AndroidLodaProcessor("/tmp"))
                .compilesWithoutError();
    }

    @Parameterized.Parameters(name = "{0}")
    public static String[] typical_usage() {
        return new String[] {
                "SingleSyncPair",
                "SingleAsyncPair",
                "OnePairOfEach",
                "CheckedAsyncProducer",
                "CheckedSyncProducer",
                "CheckedAsyncClientHandlesError",
                "CheckedSyncClientHandlesError",
                "UncheckedAsyncClientHandlesError",
                "UncheckedSyncClientHandlesError",
                "NestedHost",
                "MultipleNestedHosts",
                "DeeplyNestedHost",
                "NestedInInterface",
                "NestedInEnum",
                "DoubleAnnotatedProducer",
                "OverloadedName",
                "PrimitivePayload",
                "NoProducer",
                "NoClient",
        };
    }

}
