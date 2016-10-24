package ph.codeia.loda;

import com.google.auto.common.AnnotationMirrors;
import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
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
                        .addField(hostType, "host", Modifier.PRIVATE, Modifier.FINAL)
                        .addMethod(MethodSpec.constructorBuilder()
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
        }
        return true;
    }

    private static String nameAfter(TypeElement host) {
        StringBuilder parts = new StringBuilder();
        while (host.getNestingKind() != NestingKind.TOP_LEVEL) {
            parts.insert(0, '$');
            parts.insert(1, host.getSimpleName().toString());
            host = MoreElements.asType(host.getEnclosingElement());
        }
        parts.insert(0, host.getSimpleName().toString());
        return parts.toString() + "_Loda";
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

    private void sketch() {
        // look for classes with methods annotated with Lazy, Async, Got

        // (anonymous classes?)

        // pair up Lazy-Got and Async-Got with the same id
        // pair ids should be unique
        // producer return and client param types should match
        // producer return should not be void

        // left-over Lazy and Async should have FireAndForget
        // pair them up with NullClient
        // void methods allowed here

        // left-over Got should have ShotInTheDark
        // pair them up with NullProducer

        // ANDROID-SPECIFIC:

        // generate base class BaseLoda with the common inner classes and methods
        // only do this once. should be in the root package.

        // generate class {HostName}Loda extends BaseLoda

        // constructor should take {HostName} and assign it to a 'host' field

        // get the Lazy-Got pairs
        // for each pair, generate something like
        /*
        manager.initLoader({pair.id}, null, new Callbacks<{pair.type}>() {
            @Override public Loader<{pair.type}> onCreateLoader(int id, Bundle args) {
                return new SyncLoader<>(context, host.{pair.produceCall});   [1] [2]
            }
            @Override public void onLoadFinished(Loader<{pair.type}> loader, {pair.type} data) {
                host.{pair.clientCall}(data);  [3]
            }
            @Override public void onResetLoader(Loader<{pair.type}> loader) {}
        });
        [1] produceCall should include the parens because the method might have params.
        [2] if the pair has a NullProducer, the method should just return null.
        [3] if the pair has a NullClient, this method should be empty.
        */

        // get the Async-Got pairs
        /*
        [0]
        manager.initLoader({pair.id}, null, new Callbacks<Result<{pair.type}>>() {
            @Override public Loader<Result<{pair.type}>> onCreateLoader(int id, Bundle args) {
                return new AsyncLoader<>(context, new Callable<{pair.type}>() {
                    @Override public List<{pair.type}> call() throws Exception {
                        return host.{pair.produceCall};
                    }
                });
            }
            @Override public void onLoadFinished(Loader<Result<{pair.type}>> loader, Result<{pair.type}> data) {
                host.{pair.clientCall}(data.value, new Loda.Caught(data.error));  [1] [2]
            }
            @Override public void onResetLoader(Loader<Result<{pair.type}>> loader) {}
        });
        [0] above notes apply here too
        [1] if the client method doesn't take a Loda.Caught param, this should just be (data.value)
        [2] the producer method doesn't have to be checked if the client has a Caught param. it
            might throw RuntimeExceptions too, those are still caught by the async loader
            implementation.
         */

        // NON-ANDROID: later
    }

}
