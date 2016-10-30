/*
 * Copyright (c) 2016 by Mon Zafra.
 */

package ph.codeia.loda.gen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import javax.lang.model.element.Modifier;

/**
 * This file is a part of the Loda project.
 */

public class AndroidGenerator implements Gen.View {

    private static final ClassName BASE_LODA =
            ClassName.get("ph.codeia.loda.loaders", "BaseLoda");
    private static final ClassName CONTEXT =
            ClassName.get("android.content", "Context");
    private static final ClassName MANAGER =
            ClassName.get("android.support.v4.content", "LoaderManager");
    private static final ClassName CALLBACKS =
            ClassName.get("android.support.v4.content", "LoaderManager", "LoaderCallbacks");
    private static final ClassName LOADER =
            ClassName.get("android.support.v4.app", "Loader");
    private static final ClassName BUNDLE =
            ClassName.get("android.os", "Bundle");
    private static final ClassName LODA_HOOK =
            ClassName.get("ph.codeia.loda", "Loda", "Hook");
    private static final ClassName LODA_CAUGHT =
            ClassName.get("ph.codeia.loda", "Loda", "Caught");
    private static final ClassName SYNC_LOADER =
            ClassName.get("ph.codeia.loda.loaders", "SyncLoader");
    private static final ClassName ASYNC_LOADER =
            ClassName.get("ph.codeia.loda.loaders", "AsyncLoader");
    private static final ClassName RESULT =
            ClassName.get("ph.codeia.loda.loaders", "AsyncLoader", "Result");
    private static final ClassName CALLABLE =
            ClassName.get("java.util.concurrent", "Callable");

    private final Gen.Model model;

    public AndroidGenerator(Gen.Model model) {
        this.model = model;
    }

    @Override
    public TypeSpec generate(TypeSpec.Builder partialClass) {
        partialClass.superclass(BASE_LODA);
        MethodSpec.Builder prepare = MethodSpec.methodBuilder("prepare")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .returns(LODA_HOOK)
                .addParameter(MANAGER, "manager", Modifier.FINAL)
                .addParameter(CONTEXT, "context", Modifier.FINAL);
        for (Gen.Producer p : model.syncProducers()) {
            initSync(prepare, p);
        }
        CodeBlock.Builder dispatch = CodeBlock.builder().beginControlFlow("switch (id)");
        for (Gen.Producer p : model.asyncProducers()) {
            dispatch.add("case $L:\n", p.id());
            dispatch.indent();
            initAsync(dispatch, p);
            dispatch.addStatement("break");
            dispatch.unindent();
        }
        for (Gen.Consumer c : model.orphanConsumers()) {
            dispatch.add("case $L:\n", c.id());
            dispatch.indent();
            initAsync(dispatch, c);
            dispatch.addStatement("break");
            dispatch.unindent();
        }
        dispatch.addStatement("default: throw new IllegalArgumentException(BAD_ID)");
        dispatch.endControlFlow();
        TypeVariableName t = TypeVariableName.get("T");
        TypeName tLoader = ParameterizedTypeName.get(LOADER, t);
        TypeName tSyncLoader = ParameterizedTypeName.get(SYNC_LOADER, t);
        TypeSpec hook = TypeSpec.anonymousClassBuilder("")
                .superclass(LODA_HOOK)
                .addMethod(MethodSpec.methodBuilder("get")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addTypeVariable(t)
                        .returns(t)
                        .addParameter(TypeName.INT, "id")
                        .addParameter(t, "fallback")
                        .addStatement("$T loader = manager.getLoader(id)", tLoader)
                        .addCode(CodeBlock.builder()
                                .beginControlFlow("if (loader == null || !(loader instanceof $T))", tSyncLoader)
                                .addStatement("return fallback")
                                .endControlFlow()
                                .addStatement("return (($T) loader).value()", tSyncLoader)
                                .build())
                        .build())
                .addMethod(MethodSpec.methodBuilder("trigger")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.VOID)
                        .addParameter(TypeName.INT, "id", Modifier.FINAL)
                        .addCode(dispatch.build())
                        .build())
                .build();
        prepare.addStatement("return $L", hook);
        return partialClass.addMethod(prepare.build()).build();
    }

    private void initSync(MethodSpec.Builder method, Gen.Producer producer) {
        int id = producer.id();
        TypeName type = TypeName.get(producer.type()).box();
        MethodSpec create = callSyncProducer(onCreate(type), producer, type);
        MethodSpec finish = callSyncConsumer(onFinish(type), model.pairedConsumer(id));
        TypeSpec loader = TypeSpec.anonymousClassBuilder("")
                .superclass(ParameterizedTypeName.get(CALLBACKS, type))
                .addField(FieldSpec.builder(LODA_CAUGHT, "error", Modifier.PRIVATE)
                        .initializer("$T.NOTHING", LODA_CAUGHT)
                        .build())
                .addMethod(create)
                .addMethod(finish)
                .addMethod(onReset(type))
                .build();
        method.addStatement("manager.initLoader($L, null, $L)", id, loader);
    }

    private void initAsync(CodeBlock.Builder branch, Gen.Producer producer) {
        int id = producer.id();
        Gen.Consumer consumer = model.pairedConsumer(id);
        TypeName type = TypeName.get(producer.type()).box();
        TypeName wrapped = ParameterizedTypeName.get(RESULT, type);
        MethodSpec create = callAsyncProducer(onCreate(wrapped), producer, wrapped, type);
        MethodSpec finish = callAsyncConsumer(onFinish(wrapped), consumer);
        initAsync(branch, id, wrapped, create, finish);
    }

    private void initAsync(CodeBlock.Builder branch, Gen.Consumer consumer) {
        int id = consumer.id();
        TypeName type = TypeName.get(consumer.params().get(0).type()).box();
        TypeName wrapped = ParameterizedTypeName.get(RESULT, type);
        MethodSpec create = onCreate(wrapped).addStatement("return null").build();
        MethodSpec finish = callAsyncConsumer(onFinish(wrapped), consumer);
        initAsync(branch, id, wrapped, create, finish);
    }

    private static void initAsync(
            CodeBlock.Builder branch,
            int id,
            TypeName wrapped,
            MethodSpec create,
            MethodSpec finish
    ) {
        TypeSpec loader = TypeSpec.anonymousClassBuilder("")
                .superclass(ParameterizedTypeName.get(CALLBACKS, wrapped))
                .addMethod(create)
                .addMethod(finish)
                .addMethod(onReset(wrapped))
                .build();
        branch.addStatement("manager.initLoader($L, null, $L)", id, loader);
    }

    private static MethodSpec callSyncProducer(
            MethodSpec.Builder method,
            Gen.Producer producer,
            TypeName payload
    ) {
        TypeName loader = ParameterizedTypeName.get(SYNC_LOADER, payload);
        if (!producer.isChecked()) {
            method.addStatement("return new $T(context, host.$L())", loader, producer.name());
        } else {
            method.addCode(CodeBlock.builder()
                    .beginControlFlow("try")
                    .addStatement("return new $T(context, host.$L())", loader, producer.name())
                    .nextControlFlow("catch (Exception e)")
                    .addStatement("error = new $T(e)", LODA_CAUGHT)
                    .addStatement("return null")
                    .endControlFlow()
                    .build());
        }
        return method.build();
    }

    private static MethodSpec callAsyncProducer(
            MethodSpec.Builder method,
            Gen.Producer producer,
            TypeName wrapped,
            TypeName payload
    ) {
        TypeName loader = ParameterizedTypeName.get(ASYNC_LOADER, wrapped);
        TypeSpec callable = TypeSpec.anonymousClassBuilder("")
                .superclass(ParameterizedTypeName.get(CALLABLE, payload))
                .addMethod(MethodSpec.methodBuilder("call")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addException(Exception.class)
                        .returns(payload)
                        .addStatement("return host.$L()", producer.name())
                        .build())
                .build();
        method.addStatement("return new $T(context, $L)", loader, callable);
        return method.build();
    }

    private static MethodSpec callSyncConsumer(MethodSpec.Builder method, Gen.Consumer consumer) {
        if (consumer != null) {
            if (!consumer.isChecked()) {
                method.addStatement("host.$L(data)", consumer.name());
            } else {
                method.addStatement("host.$L(data, error)", consumer.name());
            }
        }
        return method.build();
    }

    private static MethodSpec callAsyncConsumer(MethodSpec.Builder method, Gen.Consumer consumer) {
        if (consumer != null) {
            if (!consumer.isChecked()) {
                method.addStatement("host.$L(data.value)", consumer.name());
            } else {
                method.addStatement(
                        "host.$L(data.value, new $T(data.error))",
                        consumer.name(),
                        LODA_CAUGHT
                );
            }
        }
        return method.build();
    }

    private static MethodSpec.Builder onCreate(TypeName payload) {
        return MethodSpec.methodBuilder("onCreateLoader")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(LOADER, payload))
                .addParameter(TypeName.INT, "id")
                .addParameter(BUNDLE, "args");
    }

    private static MethodSpec.Builder onFinish(TypeName payload) {
        return MethodSpec.methodBuilder("onFinishLoading")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(ParameterizedTypeName.get(LOADER, payload), "loader")
                .addParameter(payload, "data");
    }

    private static MethodSpec onReset(TypeName payload) {
        return MethodSpec.methodBuilder("onResetLoader")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(ParameterizedTypeName.get(LOADER, payload), "loader")
                .build();
    }
}
