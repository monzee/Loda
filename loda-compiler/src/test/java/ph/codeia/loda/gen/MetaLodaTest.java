/*
 * Copyright (c) 2016 by Mon Zafra
 */

package ph.codeia.loda.gen;

import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaFileObject;

/**
 * This file is a part of the Loda project.
 */

public class MetaLodaTest {
    @Test
    public void creates_a_meta_factory() {
        List<JavaFileObject> files = new ArrayList<>();
        for (String filename : new String[] {
                "CheckedAsyncProducer",
                "OnePairOfEach",
                "SingleAsyncPair",
        }) {
            files.add(JavaFileObjects.forResource("typical-usage/" + filename + ".java"));
        }
        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
                .that(files)
                .processedWith(new AndroidLodaProcessor(), new MetaProcessor("/tmp"))
                .compilesWithoutError();
        Truth.assertThat(new File("/tmp/ph/codeia/loda/AndroidLoda.java").canRead())
                .isTrue();
    }
}
