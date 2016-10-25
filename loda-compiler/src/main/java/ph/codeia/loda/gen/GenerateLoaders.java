/*
 * Copyright (c) 2016 by Mon Zafra.
 */

package ph.codeia.loda.gen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.lang.model.element.Modifier;

/**
 * This file is a part of the Loda project.
 */

public class GenerateLoaders implements CodeGenerator {

    private static final ClassName LODA_INTERFACE =
            ClassName.get("ph.codeia.loda", "Loda");
    private static final ClassName BASE_CLASS =
            ClassName.get("ph.codeia.loda.loaders", "BaseLoda");
    private static final ClassName SYNC_LOADER =
            ClassName.get("ph.codeia.loda.loaders", "SyncLoader");
    private static final ClassName ASYNC_LOADER =
            ClassName.get("ph.codeia.loda.loaders", "AsyncLoader");
    private static final ClassName ASYNC_RESULT =
            ClassName.get("ph.codeia.loda.loaders", "AsyncLoader", "Result");
    private static final ClassName CONTEXT =
            ClassName.get("android.content", "Context");
    private static final ClassName LOADER_MANAGER =
            ClassName.get("android.support.v4.app", "LoaderManager");
    private static final ClassName LOADER_CALLBACKS =
            ClassName.get("android.support.v4.app", "LoaderManager", "LoaderCallbacks");
    private static final ClassName LOADER =
            ClassName.get("android.support.v4.content", "Loader");
    private static final ClassName BUNDLE =
            ClassName.get("android.os", "Bundle");
    private static final ClassName CALLABLE =
            ClassName.get("java.util.concurrent", "Callable");

    @Override
    public TypeSpec.Builder begin(TypeSpec.Builder partialClass) {
        return partialClass.superclass(BASE_CLASS).addSuperinterface(LODA_INTERFACE);
    }

    @Override
    public MethodSpec.Builder[] methods() {
        return new MethodSpec.Builder[] {
                MethodSpec.methodBuilder("prepare")
                        .returns(TypeName.VOID)
                        .addModifiers(Modifier.PROTECTED)
                        .addParameter(LOADER_MANAGER, "manager")
                        .addParameter(CONTEXT, "context")
                        .addAnnotation(Override.class),
        };
    }

    @Override
    public CodeBlock[] bodies(
            List<? extends Pair> sync,
            List<? extends Pair> async
    ) {
        CodeBlock.Builder body = CodeBlock.builder();
        for (Pair p : sync) {
            MethodSpec.Builder onCreate = blankOnCreate();
            MethodSpec.Builder onFinish = blankOnFinish();
            MethodSpec.Builder onReset = blankOnReset();
            TypeName payload = box(TypeName.get(p.type()));
            TypeName loader = ParameterizedTypeName.get(SYNC_LOADER, payload);
            String consumerParams = p.isUnaryConsumer()
                    ? "data"
                    : "data, Caught.NOTHING";
            TypeSpec callbacks = TypeSpec.anonymousClassBuilder("")
                    .superclass(ParameterizedTypeName.get(LOADER_CALLBACKS, payload))
                    .addMethod(onCreate
                            .returns(ParameterizedTypeName.get(LOADER, payload))
                            .addStatement("return new $T(context, host.$L())",
                                    loader, p.producer().toString())
                            .build())
                    .addMethod(onFinish
                            .addParameter(ParameterizedTypeName.get(LOADER, payload), "loader")
                            .addParameter(payload, "data")
                            .addStatement("host.$L($L)", p.consumer().toString(), consumerParams)
                            .build())
                    .addMethod(onReset
                            .addParameter(ParameterizedTypeName.get(LOADER, payload), "loader")
                            .build())
                    .build();
            body.addStatement("manager.initLoader($L, null, $L)", p.id(), callbacks);
        }
        for (Pair p : async) {
            MethodSpec.Builder onCreate = blankOnCreate();
            MethodSpec.Builder onFinish = blankOnFinish();
            MethodSpec.Builder onReset = blankOnReset();
            TypeName payload = box(TypeName.get(p.type()));
            TypeName wrapper = ParameterizedTypeName.get(ASYNC_RESULT, payload);
            TypeSpec callable = TypeSpec.anonymousClassBuilder("")
                    .superclass(ParameterizedTypeName.get(CALLABLE, payload))
                    .addMethod(MethodSpec.methodBuilder("call")
                            .returns(payload)
                            .addException(Exception.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(Override.class)
                            .addStatement("return host.$L()", p.producer().toString())
                            .build())
                    .build();
            TypeName loader = ParameterizedTypeName.get(ASYNC_LOADER, payload);
            String consumerParams = p.isUnaryConsumer() ? "data.value" : "data.value, data.error";
            TypeSpec callbacks = TypeSpec.anonymousClassBuilder("")
                    .superclass(ParameterizedTypeName.get(LOADER_CALLBACKS, wrapper))
                    .addMethod(onCreate
                            .returns(ParameterizedTypeName.get(LOADER, wrapper))
                            .addStatement("return new $T(context, $L)", loader, callable)
                            .build())
                    .addMethod(onFinish
                            .addParameter(ParameterizedTypeName.get(LOADER, wrapper), "loader")
                            .addParameter(wrapper, "data")
                            .addStatement("host.$L($L)", p.consumer().toString(), consumerParams)
                            .build())
                    .addMethod(onReset
                            .addParameter(ParameterizedTypeName.get(LOADER, wrapper), "loader")
                            .build())
                    .build();
            body.addStatement("manager.initLoader($L, null, $L)", p.id(), callbacks);
        }
        return new CodeBlock[] { body.build() };
    }

    private static MethodSpec.Builder blankOnReset() {
        return MethodSpec.methodBuilder("onLoaderReset")
                    .returns(TypeName.VOID)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC);
    }

    private static MethodSpec.Builder blankOnFinish() {
        return MethodSpec.methodBuilder("onLoadFinished")
                    .returns(TypeName.VOID)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC);
    }

    private static MethodSpec.Builder blankOnCreate() {
        return MethodSpec.methodBuilder("onCreateLoader")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(TypeName.INT, "id")
                    .addParameter(BUNDLE, "args");
    }

    private static TypeName box(TypeName mightBePrimitive) {
        if (mightBePrimitive.isPrimitive()) {
            return mightBePrimitive.box();
        } else {
            return mightBePrimitive;
        }
    }
}
