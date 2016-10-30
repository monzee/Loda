/*
 * Copyright (c) 2016 by Mon Zafra.
 */

package ph.codeia.loda.gen;

import com.squareup.javapoet.TypeSpec;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * This file is a part of the Loda project.
 */

public interface Gen {

    interface View {
        TypeSpec generate(TypeSpec.Builder partialClass);
    }

    interface Controller {
        void addLazyNode(int id, Element node) throws DuplicateId, TypeMismatch;
        void addAsyncNode(int id, Element node) throws DuplicateId, TypeMismatch;
        void addGotNode(int id, Element node) throws DuplicateId, TypeMismatch;
        Map<TypeElement, Model> dump();
    }

    interface Model {
        List<Consumer> orphanConsumers();
        Consumer pairedConsumer(int id);
        List<Producer> syncProducers();
        List<Producer> asyncProducers();
    }

    interface Consumer extends Role {
        TypeMirror payload();
    }

    interface Producer extends Role {
        TypeMirror type();
    }

    interface Role {
        int id();
        String name();
        List<Param> params();
        boolean isChecked();
    }

    interface Param {
        TypeMirror type();
        String name();
    }

    interface FetchedParam extends Param {
        String key();
    }

    interface JoinedParam extends Param {
        int id();
    }

    class ProcessingError extends Exception {
        final Element element;

        ProcessingError(String message, Element e) {
            super(message);
            element = e;
        }

        protected static String fmt(String tpl, Object... fmtArgs) {
            return String.format(tpl, fmtArgs);
        }

    }

    class DuplicateId extends ProcessingError {
        DuplicateId(int id, String kind, Element e) {
            super(fmt("[dupe] %s#%d already defined.", kind, id), e);
        }
    }

    class TypeMismatch extends ProcessingError {
        TypeMismatch(int id, Element e) {
            super(fmt(
                    "[mismatch] Return value of producer#%1d is not" +
                    "assignable to the first parameter of consumer#%1d",
                    id
            ), e);
        }
    }
}
