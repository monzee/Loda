/*
 * Copyright (c) 2016 by Mon Zafra.
 */

package ph.codeia.loda.gen;

import com.google.auto.common.AnnotationMirrors;
import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import ph.codeia.loda.Loda;

/**
 * This file is a part of the Loda project.
 */

@AutoService(Processor.class)
public class LodaProcessor extends AbstractProcessor {

    private Types types;
    private Elements elems;
    private Filer filer;
    private Messager msg;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        types = processingEnv.getTypeUtils();
        elems = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        msg = processingEnv.getMessager();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> names = new HashSet<>();
        for (Class<?> c : new Class<?>[] {
                Loda.Lazy.class,
                Loda.Async.class,
                Loda.Got.class,
                Loda.Backlink.class,
        }) {
            names.add(c.getCanonicalName());
        }
        return names;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            Validator targets = new Validator(elems, types);
            for (Element elem : roundEnv.getElementsAnnotatedWith(Loda.Lazy.class)) try {
                //noinspection OptionalGetWithoutIsPresent
                targets.addLazy(
                        valueOf(MoreElements.getAnnotationMirror(elem, Loda.Lazy.class).get()),
                        elem
                );
            } catch (Validator.DuplicateId e) {
                err(e.getMessage(), e.target);
                return true;
            }
            for (Element elem : roundEnv.getElementsAnnotatedWith(Loda.Async.class)) try {
                //noinspection OptionalGetWithoutIsPresent
                targets.addAsync(
                        valueOf(MoreElements.getAnnotationMirror(elem, Loda.Async.class).get()),
                        elem
                );
            } catch (Validator.DuplicateId e) {
                err(e.getMessage(), e.target);
                return true;
            }
            for (Element elem : roundEnv.getElementsAnnotatedWith(Loda.Got.class)) try {
                //noinspection OptionalGetWithoutIsPresent
                targets.addGot(
                        valueOf(MoreElements.getAnnotationMirror(elem, Loda.Got.class).get()),
                        elem
                );
            } catch (Validator.DuplicateId e) {
                err(e.getMessage(), e.target);
                return true;
            }
            for (TypeElement host : targets) try {
                TypeName hostType = TypeName.get(host.asType());
                TypeSpec.Builder loda = TypeSpec
                        .classBuilder(nameAfter(host))
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(AnnotationSpec.builder(Loda.Backlink.class)
                                .addMember("value", CodeBlock.of("$T.class", hostType))
                                .build())
                        .addJavadoc("Generated by Loda\nhttps://github.com/monzee/Loda.git\n")
                        .addField(hostType, "host", Modifier.FINAL)
                        .addMethod(MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(hostType, "host")
                                .addStatement("this.host = host")
                                .build());
                compile(loda, targets.syncPairs(host), targets.asyncPairs(host));
                JavaFile.builder(elems.getPackageOf(host).toString(), loda.build())
                        .build()
                        .writeTo(new File("/tmp"));
            } catch (Validator.TypeMismatch | Validator.NoMatchingPair e) {
                err(e.getMessage(), e.target);
            } catch (IOException e) {
                err(e.getMessage(), host);
            }
        } catch (RuntimeException e) {
            msg.printMessage(
                    Diagnostic.Kind.ERROR,
                    "got a runtime exn during loda processing. good luck!"
            );
            throw e;
        }
        return true;
    }

    private static String nameAfter(TypeElement host) {
        StringBuilder parts = new StringBuilder("_Loda");
        while (host.getNestingKind() != NestingKind.TOP_LEVEL) {
            parts.insert(0, '$');
            parts.insert(1, host.getSimpleName().toString());
            host = MoreElements.asType(host.getEnclosingElement());
        }
        parts.insert(0, host.getSimpleName().toString());
        return parts.toString();
    }

    private static void compile(
            TypeSpec.Builder loda,
            List<? extends CodeGenerator.Pair> sync,
            List<? extends CodeGenerator.Pair> async
    ) {
        CodeGenerator gen = new GenerateLoaders();
        gen.begin(loda);
        MethodSpec.Builder[] methods = gen.methods();
        CodeBlock[] bodies = gen.bodies(sync, async);
        for (int i = 0; i < methods.length; i++) {
            methods[i].addCode(bodies[i]);
            loda.addMethod(methods[i].build());
        }
    }

    private static int valueOf(AnnotationMirror annotation) {
        return (int) AnnotationMirrors.getAnnotationElementAndValue(annotation, "value")
                .getValue()
                .getValue();
    }

    private void err(String message, Element elem) {
        msg.printMessage(Diagnostic.Kind.ERROR, message, elem);
    }

    /*
    TEST PLAN:
    - 2 producers with the same id
    - 2 clients with the same id
    - orphan producer
    - orphan client
    - producer return is not assignable to client param
    - producer is void
    - client is nullary
    - producer is not nullary
    - client arity > 2
    - client is checked
    - sync producer is checked
    - host is anonymous or local class
    - producer or client is protected or private
    - producer or client is abstract
    - producer or client is static
     */
}
