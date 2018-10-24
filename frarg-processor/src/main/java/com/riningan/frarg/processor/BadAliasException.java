package com.riningan.frarg.processor;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;


public class BadAliasException extends Exception {
    private String mFragmentAlias;
    private Element mFragmentElement;


    BadAliasException(String fragmentAlias, Element fragmentElement) {
        mFragmentAlias = fragmentAlias;
        mFragmentElement = fragmentElement;
    }


    @Override
    public String getMessage() {
        return "Bad alias " + mFragmentAlias + " of fragment " + mFragmentElement.getSimpleName();
    }
}
