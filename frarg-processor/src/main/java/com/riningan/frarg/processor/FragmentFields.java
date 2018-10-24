package com.riningan.frarg.processor;

import com.squareup.javapoet.ClassName;

public class FragmentFields {
    public ClassName className;
    public String alias;

    public FragmentFields(ClassName className, String alias) {
        this.className = className;
        this.alias = alias.length() > 0 ? alias : className.simpleName();
    }
}
