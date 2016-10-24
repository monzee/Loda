/*
 * Copyright (c) 2016 by Mon Zafra.
 */

package ph.codeia.loda;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

/**
 * This file is a part of the Loda project.
 */

public interface CodeGenerator {

    interface Pair {
        int id();
        TypeElement type();
        Name producer();
        Name consumer();
        boolean isUnaryConsumer();
    }

    TypeSpec.Builder begin(TypeSpec.Builder partialClass);
    MethodSpec.Builder[] methods();
    CodeBlock[] bodies(List<? extends Pair> sync, List<? extends Pair> async);

}
