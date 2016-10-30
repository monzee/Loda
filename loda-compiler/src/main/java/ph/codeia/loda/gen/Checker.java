/*
 * Copyright (c) 2016 by Mon Zafra.
 */

package ph.codeia.loda.gen;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * This file is a part of the Loda project.
 */

@SuppressWarnings({"UseSparseArrays", "WeakerAccess"})
public class Checker implements Gen.Controller {

    private static class Nodes {
        final Map<Integer, Producer> lazyById = new LinkedHashMap<>();
        final Map<Integer, Producer> asyncById = new LinkedHashMap<>();
        final Map<Integer, Gen.Consumer> gotById = new LinkedHashMap<>();

        static Nodes of(TypeElement host, Map<TypeElement, Nodes> nodesByHost) {
            if (nodesByHost.containsKey(host)) {
                return nodesByHost.get(host);
            }
            Nodes nodes = new Nodes();
            nodesByHost.put(host, nodes);
            return nodes;
        }
    }

    private class Consumer implements Gen.Consumer {
        final int id;
        final ExecutableElement node;

        Consumer(int id, Element node) {
            this.id = id;
            this.node = MoreElements.asExecutable(node);
        }

        @Override
        public int id() {
            return id;
        }

        @Override
        public String name() {
            return node.getSimpleName().toString();
        }

        @Override
        public List<Gen.Param> params() {
            List<Gen.Param> params = new ArrayList<>();
            for (final VariableElement var : node.getParameters()) {
                params.add(new Gen.Param() {
                    @Override
                    public TypeMirror type() {
                        return var.asType();
                    }

                    @Override
                    public String name() {
                        return var.getSimpleName().toString();
                    }
                });
            }
            return params;
        }

        @Override
        public boolean isChecked() {
            return node.getParameters().size() > 1;
        }

        @Override
        public TypeMirror payload() {
            return node.getParameters().get(0).asType();
        }
    }

    private class Producer implements Gen.Producer {
        final int id;
        final ExecutableElement node;

        Producer(int id, Element node) {
            this.id = id;
            this.node = MoreElements.asExecutable(node);
        }

        @Override
        public TypeMirror type() {
            TypeMirror type = node.getReturnType();
            return type.getKind() == TypeKind.DECLARED
                    ? MoreTypes.asDeclared(type)
                    : MoreTypes.asPrimitiveType(type);
        }

        @Override
        public int id() {
            return id;
        }

        @Override
        public String name() {
            return node.getSimpleName().toString();
        }

        @Override
        public List<Gen.Param> params() {
            return Collections.emptyList();
        }

        @Override
        public boolean isChecked() {
            return !node.getThrownTypes().isEmpty();
        }

        void typecheck(TypeMirror param) throws Gen.TypeMismatch {
            if (!types.isAssignable(type(), param)) {
                throw new Gen.TypeMismatch(id, node);
            }
        }
    }

    final Types types;
    final Map<TypeElement, Nodes> nodes = new HashMap<>();

    public Checker(Types types) {
        this.types = types;
    }

    @Override
    public void addLazyNode(int id, Element node) throws Gen.DuplicateId, Gen.TypeMismatch {
        TypeElement host = enclosingClass(node);
        Nodes hostNode = Nodes.of(host, nodes);
        if (hostNode.lazyById.containsKey(id)) {
            throw new Gen.DuplicateId(id, "Producer", node);
        }
        Producer p = new Producer(id, node);
        Gen.Consumer c = hostNode.gotById.get(id);
        if (c != null) {
            p.typecheck(c.payload());
        }
        hostNode.lazyById.put(id, p);
    }

    @Override
    public void addAsyncNode(int id, Element node) throws Gen.DuplicateId, Gen.TypeMismatch {
        TypeElement host = enclosingClass(node);
        Nodes hostNode = Nodes.of(host, nodes);
        if (hostNode.asyncById.containsKey(id)) {
            throw new Gen.DuplicateId(id, "Producer", node);
        }
        Producer p = new Producer(id, node);
        Gen.Consumer c = hostNode.gotById.get(id);
        if (c != null) {
            p.typecheck(c.payload());
        }
        hostNode.asyncById.put(id, new Producer(id, node));
    }

    @Override
    public void addGotNode(int id, Element node) throws Gen.DuplicateId, Gen.TypeMismatch {
        TypeElement host = enclosingClass(node);
        Nodes hostNode = Nodes.of(host, nodes);
        if (hostNode.gotById.containsKey(id)) {
            throw new Gen.DuplicateId(id, "Consumer", node);
        }
        Consumer c = new Consumer(id, node);
        Producer p = hostNode.lazyById.get(id);
        p = p != null ? p : hostNode.asyncById.get(id);
        if (p != null) {
            p.typecheck(c.payload());
        }
        hostNode.gotById.put(id, c);
    }

    @Override
    public Map<TypeElement, Gen.Model> dump() {
        return Maps.asMap(nodes.keySet(), new Function<TypeElement, Gen.Model>() {
            @Override
            public Gen.Model apply(TypeElement host) {
                final Nodes hostNode = Nodes.of(host, nodes);
                return new Gen.Model() {
                    @Override
                    public List<Gen.Consumer> orphanConsumers() {
                        return Lists.newArrayList(Collections2.filter(
                                hostNode.gotById.values(),
                                new Predicate<Gen.Consumer>() {
                                    @Override
                                    public boolean apply(Gen.Consumer consumer) {
                                        int id = consumer.id();
                                        return !hostNode.lazyById.containsKey(id) &&
                                                !hostNode.asyncById.containsKey(id);
                                    }
                                }
                        ));
                    }

                    @Override
                    public Gen.Consumer pairedConsumer(int id) {
                        return hostNode.gotById.get(id);
                    }

                    @Override
                    public List<Gen.Producer> syncProducers() {
                        return Lists.<Gen.Producer>newArrayList(hostNode.lazyById.values());
                    }

                    @Override
                    public List<Gen.Producer> asyncProducers() {
                        return Lists.<Gen.Producer>newArrayList(hostNode.asyncById.values());
                    }
                };
            }
        });
    }

    private static TypeElement enclosingClass(Element elem) {
        while (!MoreElements.isType(elem)) {
            elem = elem.getEnclosingElement();
        }
        return MoreElements.asType(elem);
    }

}
