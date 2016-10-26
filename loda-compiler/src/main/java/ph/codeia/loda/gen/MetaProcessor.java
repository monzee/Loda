/*
 * Copyright (c) 2016 by Mon Zafra
 */

package ph.codeia.loda.gen;

import com.google.auto.common.AnnotationMirrors;
import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;

import ph.codeia.loda.Loda;

/**
 * This file is a part of the Loda project.
 *
 * @author mon
 */

@AutoService(Processor.class)
public class MetaProcessor extends AbstractProcessor {

    private final File destination;

    public MetaProcessor() {
        destination = null;
    }

    public MetaProcessor(String filename) {
        destination = new File(filename);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> names = new HashSet<>();
        names.add(Loda.Backlink.class.getCanonicalName());
        return names;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        TypeSpec.Builder metaLoda = TypeSpec
                .classBuilder("AndroidLoda")
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(LodaProcessor.SIGNATURE);
        Set<? extends Element> lodas = roundEnv.getElementsAnnotatedWith(Loda.Backlink.class);
        if (lodas.isEmpty()) {
            return false;
        }
        for (Element generated : lodas) {
            TypeName returnType = TypeName.get(generated.asType());
            //noinspection OptionalGetWithoutIsPresent
            DeclaredType argument = (DeclaredType) AnnotationMirrors.getAnnotationValue(
                    MoreElements.getAnnotationMirror(generated, Loda.Backlink.class).get(),
                    "value"
            ).getValue();
            metaLoda.addMethod(MethodSpec.methodBuilder("of")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(returnType)
                    .addParameter(TypeName.get(argument), "host")
                    .addStatement("return new $T(host)", returnType)
                    .build());
        }
        try {
            JavaFile file = JavaFile.builder("ph.codeia.loda", metaLoda.build()).build();
            if (destination == null) {
                file.writeTo(processingEnv.getFiler());
            } else {
                file.writeTo(destination);
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
        return true;
    }

}
