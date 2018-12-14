package com.riningan.frarg.processor;

import com.riningan.frarg.annotations.Argument;
import com.riningan.frarg.annotations.ArgumentedFragment;

import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;


class FragmentOptions {
    ClassName className;
    String classAlias;
    ArrayList<Element> argElements = new ArrayList<>();
    ArrayList<ArrayList<Element>> argCombinations = new ArrayList<>();


    @SuppressWarnings("ResultOfMethodCallIgnored")
    FragmentOptions(Element fragmentElement) throws BadAliasException {
        // get fragment class name and alias
        String argumentedCls = fragmentElement.getAnnotation(ArgumentedFragment.class).toString();
        Pattern p = Pattern.compile("@" + ArgumentedFragment.class.getCanonicalName() + "\\(alias=(.*), fragmentClass=" + "(.*)\\.(.*)\\)");
        Matcher matcher = p.matcher(argumentedCls);
        matcher.find();
        String argumentedClassAlias = matcher.group(1);
        String argumentedClassPackage = matcher.group(2);
        String argumentedClassSimple = matcher.group(3);
        if (argumentedClassAlias.length() > 0 && !argumentedClassAlias.matches("[a-zA-Z][a-zA-Z0-9]*")) {
            throw new BadAliasException(argumentedClassAlias, fragmentElement);
        }
        if (argumentedClassPackage.equals("java.lang") && argumentedClassSimple.equals("Object")) {
            className = ClassName.get((TypeElement) fragmentElement);
        } else {
            className = ClassName.get(argumentedClassPackage, argumentedClassSimple);
        }
        classAlias = argumentedClassAlias.length() > 0 ? argumentedClassAlias : className.simpleName();
        // collect arguments
        for (Element subFragmentElement : fragmentElement.getEnclosedElements()) {
            if (subFragmentElement.getAnnotation(Argument.class) != null) {
                argElements.add(subFragmentElement);
            }
        }
        // create get all posible arg combinations
        if (argElements.size()> 0) {
            argCombinations.add(new ArrayList<Element>());
            for (Element arg : argElements) {
                if (arg.getAnnotation(Argument.class).optional()) {
                    ArrayList<ArrayList<Element>> argCombinationsCopy = new ArrayList<>();
                    for (ArrayList<Element> argCombination : argCombinations) {
                        argCombinationsCopy.add(new ArrayList<>(argCombination));
                    }
                    for (ArrayList<Element> argCombination : argCombinations) {
                        argCombination.add(arg);
                    }
                    int index = 1;
                    for (ArrayList<Element> argCombinationCopy : argCombinationsCopy) {
                        argCombinations.add(index, argCombinationCopy);
                        index += 2;
                    }
                } else {
                    for (ArrayList<Element> argCombination : argCombinations) {
                        argCombination.add(arg);
                    }
                }
            }
        }
    }


    String getNewInstanceMethodName() {
        return "new" + classAlias + "Instance";
    }
}
