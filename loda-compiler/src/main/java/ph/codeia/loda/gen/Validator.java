/*
 * Copyright (c) 2016 by Mon Zafra.
 */

package ph.codeia.loda.gen;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * This file is a part of the Loda project.
 */

@Deprecated
public class Validator implements Iterable<TypeElement> {

    class ProcessingError extends Exception {
        final Element target;
        ProcessingError(String message, Element target) {
            super(message);
            this.target = target;
        }
    }

    class TypeMismatch extends ProcessingError {
        TypeMismatch(Element producer, Element consumer) {
            super(String.format(
                    "[mismatch] Return type of %s and param type of %s are incompatible",
                    producer,
                    consumer
            ), producer);
        }
    }

    class DuplicateId extends ProcessingError {
        DuplicateId(int id, String type, Element e) {
            super(String.format(
                    "[dupe] There's already a %s with id #%d", type, id
            ), e);
        }
    }

    class NoMatchingPair extends ProcessingError {
        NoMatchingPair(int id, String missingHalf, Element e) {
            super(String.format(
                    "[orphan] No matching %s for loader #%d", missingHalf, id
            ), e);
        }
    }

    class CallPair implements CodeGenerator.Pair {
        final int id;
        final TypeMirror target;
        final ExecutableElement producer;
        final ExecutableElement consumer;

        CallPair(
                int id,
                ExecutableElement producer,
                ExecutableElement consumer
        ) throws TypeMismatch {
            this.id = id;
            this.producer = producer;
            this.consumer = consumer;
            target = checkReturnIsAssignableToFirstParam(producer, consumer);
        }

        @Override
        public int id() {
            return id;
        }

        @Override
        public TypeMirror type() {
            return target;
        }

        @Override
        public Name producer() {
            return producer.getSimpleName();
        }

        @Override
        public Name consumer() {
            return consumer.getSimpleName();
        }

        @Override
        public boolean isUnaryConsumer() {
            return consumer.getParameters().size() == 1;
        }
    }

    private final Elements elems;
    private final Types types;

    private Set<TypeElement> hosts = new HashSet<>();
    private Map<TypeElement, Map<Integer, ExecutableElement>> lazyByIdByHost = new HashMap<>();
    private Map<TypeElement, Map<Integer, ExecutableElement>> asyncByIdByHost = new HashMap<>();
    private Map<TypeElement, Map<Integer, ExecutableElement>> gotByIdByHost = new HashMap<>();

    Validator(Elements elems, Types types) {
        this.elems = elems;
        this.types = types;
    }

    public void addLazy(int id, Element elem) throws DuplicateId {
        TypeElement host = enclosingClass(elem);
        hosts.add(host);
        Map<Integer, ExecutableElement> lazies = getOrInit(lazyByIdByHost, host);
        Map<Integer, ExecutableElement> asyncs = getOrInit(asyncByIdByHost, host);
        if (lazies.containsKey(id) || asyncs.containsKey(id)) {
            throw new DuplicateId(id, "producer", elem);
        }
        lazies.put(id, MoreElements.asExecutable(elem));
    }

    public void addAsync(int id, Element elem) throws DuplicateId {
        TypeElement host = enclosingClass(elem);
        hosts.add(host);
        Map<Integer, ExecutableElement> lazies = getOrInit(lazyByIdByHost, host);
        Map<Integer, ExecutableElement> asyncs = getOrInit(asyncByIdByHost, host);
        if (lazies.containsKey(id) || asyncs.containsKey(id)) {
            throw new DuplicateId(id, "producer", elem);
        }
        asyncs.put(id, MoreElements.asExecutable(elem));
    }

    public void addGot(int id, Element elem) throws DuplicateId {
        TypeElement host = enclosingClass(elem);
        hosts.add(host);
        Map<Integer, ExecutableElement> gots = getOrInit(gotByIdByHost, host);
        if (gots.containsKey(id)) {
            throw new DuplicateId(id, "consumer", elem);
        }
        gots.put(id, MoreElements.asExecutable(elem));
    }

    public List<CallPair> syncPairs(TypeElement host) throws TypeMismatch, NoMatchingPair {
        List<CallPair> pairs = new ArrayList<>();
        Map<Integer, ExecutableElement> lazyById = lazyByIdByHost.get(host);
        Map<Integer, ExecutableElement> gotById = gotByIdByHost.get(host);
        for (int id : lazyById.keySet()) {
            if (gotById.containsKey(id)) {
                pairs.add(new CallPair(id, lazyById.get(id), gotById.get(id)));
            } else {
                // TODO: check FireAndForget and ShotInTheDark
                throw new NoMatchingPair(id, "consumer", lazyById.get(id));
            }
        }
        return pairs;
    }

    public List<CallPair> asyncPairs(TypeElement host) throws TypeMismatch, NoMatchingPair {
        List<CallPair> pairs = new ArrayList<>();
        Map<Integer, ExecutableElement> asyncById = asyncByIdByHost.get(host);
        Map<Integer, ExecutableElement> gotById = gotByIdByHost.get(host);
        for (int id : asyncById.keySet()) {
            if (gotById.containsKey(id)) {
                pairs.add(new CallPair(id, asyncById.get(id), gotById.get(id)));
            } else {
                // TODO: check FireAndForget and ShotInTheDark
                throw new NoMatchingPair(id, "consumer", asyncById.get(id));
            }
        }
        return pairs;
    }

    @Override
    public Iterator<TypeElement> iterator() {
        return hosts.iterator();
    }

    private static TypeElement enclosingClass(Element elem) {
        while (!MoreElements.isType(elem)) {
            elem = elem.getEnclosingElement();
        }
        return MoreElements.asType(elem);
    }

    private static Map<Integer, ExecutableElement> getOrInit(
            Map<TypeElement, Map<Integer, ExecutableElement>> byHost,
            TypeElement host
    ) {
        Map<Integer, ExecutableElement> item;
        if (byHost.containsKey(host)) {
            item = byHost.get(host);
        } else {
            item = new HashMap<>();
            byHost.put(host, item);
        }
        return item;
    }

    private TypeMirror checkReturnIsAssignableToFirstParam(
            ExecutableElement src,
            ExecutableElement dest
    ) throws TypeMismatch {
        // dest should have one param
        // return type of src should be assignable to dest's param
        List<? extends VariableElement> params = dest.getParameters();
        if (params.size() < 1) {
            throw new TypeMismatch(src, dest);
        }
        TypeMirror param = params.get(0).asType();
        TypeMirror retval = src.getReturnType();
        if (!types.isAssignable(retval, param)) {
            throw new TypeMismatch(src, dest);
        }
        switch (param.getKind()) {
            case DECLARED:
                return MoreTypes.asDeclared(param);
            case TYPEVAR:
                return MoreTypes.asTypeVariable(param);
            default:
                return MoreTypes.asPrimitiveType(param);
        }
    }

}
