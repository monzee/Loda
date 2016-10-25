/*
 * Copyright (c) 2016 by Mon Zafra.
 */

package ph.codeia.loda.gen;

import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubject;

import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static com.google.common.truth.Truth.*;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * This file is a part of the Loda project.
 */

public class SanityTest {
    @Test
    public void can_i_find_resources() throws URISyntaxException {
        URL resource = getClass()
                .getClassLoader()
                .getResource("ItWorks.java");
        assertThat(resource).isNotNull();
        assertThat(new File(resource.toURI()).canRead()).isTrue();
    }

    @Test
    public void it_works() {
        JavaSourcesSubject.SingleSourceAdapter it = assertAbout(javaSource())
                .that(JavaFileObjects.forResource("ItWorks.java"));
        it.compilesWithoutWarnings();
        it.compilesWithoutError();
    }
}
