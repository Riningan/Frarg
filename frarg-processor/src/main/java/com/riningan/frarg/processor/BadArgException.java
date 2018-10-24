package com.riningan.frarg.processor;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;


public class BadArgException extends Exception {
    private Element mArgElement;


    BadArgException(Element argElement) {
        mArgElement = argElement;
    }


    @Override
    public String getMessage() {
        return "Bad argument " + mArgElement.getSimpleName().toString() + " with type " + ClassName.get(mArgElement.asType());
    }
}
