package com.riningan.frarg.processor;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;

import com.riningan.frarg.annotations.Argument;
import com.riningan.frarg.annotations.ArgumentedFragment;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;


@AutoService(Processor.class)
public class FrargProcessor extends AbstractProcessor {
    private static final String PACKAGE_NAME = FrargProcessor.class.getPackage().getName();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(ArgumentedFragment.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Starting FrargProcessor");
        if (annotations.isEmpty()) {
            return false;
        }

        ArrayList<MethodSpec> methods = new ArrayList<>();
        Set<? extends Element> fragmentElements = roundEnvironment.getElementsAnnotatedWith(ArgumentedFragment.class);
        for (Element fragmentElement : fragmentElements) {
            try {
                // collect arguments
                ArrayList<Element> fragmentArgs = new ArrayList<>();
                for (Element subFragmentElement : fragmentElement.getEnclosedElements()) {
                    if (subFragmentElement.getAnnotation(Argument.class) != null) {
                        fragmentArgs.add(subFragmentElement);
                    }
                }
                FragmentFields fragmentFields = parseFragmentElement(fragmentElement);
                ClassName fragmentClassName = fragmentFields.className;
                String fragmentAlias = fragmentFields.alias;
                // create newInstance method
                methods.add(createNewInstanceMethodWithAllArgs(fragmentClassName, fragmentAlias, fragmentArgs));
                if (fragmentArgs.size() > 0) {
                    // create arg class
                    createArgClass(fragmentArgs, fragmentAlias);
                    // create newInstance method with arg class
                    methods.add(createNewInstanceMethodWithArgClass(fragmentClassName, fragmentAlias, fragmentArgs));
                }
            } catch (BadAliasException | IOException | BadArgException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                return false;
            }
        }
        // create builder class
        try {
            TypeSpec fragmentBuilderClass = TypeSpec.classBuilder("FragmentBuilder")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethods(methods)
                    .build();
            createClassFile(fragmentBuilderClass);
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            return false;
        }
        return true;
    }

    private FragmentFields parseFragmentElement(Element fragmentElement) throws BadAliasException {
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
            return new FragmentFields(ClassName.get((TypeElement) fragmentElement), argumentedClassAlias);
        } else {
            return new FragmentFields(ClassName.get(argumentedClassPackage, argumentedClassSimple), argumentedClassAlias);
        }
    }


    private MethodSpec createNewInstanceMethodWithArgClass(ClassName fragmentClassName, String fragmentAlias, ArrayList<Element> fragmentArgs) {
        String newInstanceMethodName = "new" + fragmentAlias + "Instance";
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(newInstanceMethodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.get(PACKAGE_NAME, fragmentAlias + "Args"), "args")
                .returns(fragmentClassName);
        methodBuilder.addCode("return $N(", newInstanceMethodName);
        for (int i = 0; i < fragmentArgs.size(); i++) {
            String argName = fragmentArgs.get(i).getSimpleName().toString();
            if (i == 0) {
                methodBuilder.addCode("args." + argName);
            } else {
                methodBuilder.addCode("\n, args." + argName);
            }
        }
        methodBuilder.addCode(");\n");
        return methodBuilder.build();
    }

    private MethodSpec createNewInstanceMethodWithAllArgs(ClassName fragmentClassName, String fragmentAlias, ArrayList<Element> fragmentArgs) throws BadArgException {
        String newInstanceMethodName = "new" + fragmentAlias + "Instance";
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(newInstanceMethodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(fragmentClassName);
        for (Element fragmentArg : fragmentArgs) {
            methodBuilder.addParameter(ClassName.get(fragmentArg.asType()), fragmentArg.getSimpleName().toString());
        }
        methodBuilder.addStatement("$T fragment = new $T()", fragmentClassName, fragmentClassName)
                .addStatement("android.os.Bundle bundle = new android.os.Bundle()");
        for (Element fragmentArg : fragmentArgs) {
            String[] arg = addArg(fragmentArg);
            String argName = fragmentArg.getSimpleName().toString();
            if (arg.length == 1) {
                methodBuilder.addStatement(arg[0], argName, argName);
            } else {
                methodBuilder.addStatement(arg[0], argName, argName + arg[1]);
            }
        }
        methodBuilder.addStatement("fragment.setArguments(bundle)")
                .addStatement("return fragment");
        return methodBuilder.build();
    }


    private void createArgClass(ArrayList<Element> fragmentArgs, String fragmentClassSimpleName) throws IOException {
        ArrayList<FieldSpec> fields = new ArrayList<>();
        for (Element fragmentArg : fragmentArgs) {
            fields.add(FieldSpec.builder(ClassName.get(fragmentArg.asType()), fragmentArg.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC)
                    .build());
        }
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        for (Element fragmentArg : fragmentArgs) {
            String argName = fragmentArg.getSimpleName().toString();
            constructorBuilder.addParameter(ClassName.get(fragmentArg.asType()), argName)
                    .addStatement("this.$N = $N", argName, argName);
        }
        TypeSpec fragmentArgClass = TypeSpec.classBuilder(fragmentClassSimpleName + "Args")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addFields(fields)
                .addMethod(constructorBuilder.build())
                .build();
        createClassFile(fragmentArgClass);
    }


    private void createClassFile(TypeSpec classSpec) throws IOException {
        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, classSpec)
                .addFileComment("Generated by Frarg (https://github.com/Riningan/Frarg)")
                .build();
        JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(javaFile.packageName + "." + classSpec.name);
        Writer writer = new BufferedWriter(sourceFile.openWriter());
        javaFile.writeTo(writer);
        writer.close();
    }


    private String[] addArg(Element argElement) throws BadArgException {
        if (argElement.asType().getKind().isPrimitive()) {
            return new String[]{addPrimitiveArg(argElement)};
        } else if (argElement.asType().getKind() == TypeKind.ARRAY) {
            return new String[]{addArrayArg(argElement)};
        } else if (ClassName.get(argElement.asType()).toString().startsWith(ArrayList.class.getCanonicalName())) {
            return new String[]{addArrayListArg(argElement)};
        } else if (ClassName.get(argElement.asType()).toString().startsWith("android.util.SparseArray")) {
            return new String[]{addSparseArrayArg(argElement)};
        }
        String argClassName = ClassName.get(argElement.asType()).toString();
        if (argClassName.equals(CharSequence.class.getCanonicalName())) {
            return new String[]{"bundle.putCharSequence($S, $L)"};
        } else if (argClassName.equals(String.class.getCanonicalName())) {
            return new String[]{"bundle.putString($S, $L)"};
        } else if (argClassName.equals(Serializable.class.getCanonicalName())) {
            return new String[]{"bundle.putSerializable($S, $L)"};
        } else if (argClassName.equals("android.os.Bundle")) {
            return new String[]{"bundle.putBundle($S, $L)"};
        } else if (argClassName.equals("android.util.Size")) {
            return new String[]{"bundle.putSize($S, $L)"};
        } else if (argClassName.equals("android.util.SizeF")) {
            return new String[]{"bundle.putSizeF($S, $L)"};
        } else if (argClassName.equals("android.os.IBinder")) {
            return new String[]{"bundle.putBinder($S, $L)"};
        } else if (argClassName.equals("android.os.Parcelable")) {
            return new String[]{"bundle.putParcelable($S, $L)"};
        } else if (argElement.asType().getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) argElement.asType();
            TypeElement argClass = (TypeElement) declaredType.asElement();
            if (argClass.getKind() == ElementKind.ENUM) {
                return new String[]{"bundle.putString($S, $L)", ".name()"};
            } else {
                for (TypeMirror interfaceTypeMirror : argClass.getInterfaces()) {
                    if (interfaceTypeMirror.toString().equals("android.os.Parcelable")) {
                        return new String[]{"bundle.putParcelable($S, $L)"};
                    } else if (interfaceTypeMirror.toString().equals(Serializable.class.getCanonicalName())) {
                        return new String[]{"bundle.putSerializable($S, $L)"};
                    } else if (interfaceTypeMirror.toString().equals("android.os.IBinder")) {
                        return new String[]{"bundle.putBinder($S, $L)"};
                    }
                }
            }
        }
        throw new BadArgException(argElement);
    }

    private String addPrimitiveArg(Element argElement) throws BadArgException {
        String argClassName = ClassName.get(argElement.asType()).toString();
        if (argClassName.equals(boolean.class.getCanonicalName())) {
            return "bundle.putBoolean($S, $L)";
        } else if (argClassName.equals(byte.class.getCanonicalName())) {
            return "bundle.putByte($S, $L)";
        } else if (argClassName.equals(char.class.getCanonicalName())) {
            return "bundle.putChar($S, $L)";
        } else if (argClassName.equals(double.class.getCanonicalName())) {
            return "bundle.putDouble($S, $L)";
        } else if (argClassName.equals(float.class.getCanonicalName())) {
            return "bundle.putFloat($S, $L)";
        } else if (argClassName.equals(int.class.getCanonicalName())) {
            return "bundle.putInt($S, $L)";
        } else if (argClassName.equals(long.class.getCanonicalName())) {
            return "bundle.putLong($S, $L)";
        } else if (argClassName.equals(short.class.getCanonicalName())) {
            return "bundle.putShort($S, $L)";
        } else {
            throw new BadArgException(argElement);
        }
    }

    private String addArrayArg(Element argElement) throws BadArgException {
        String argClassName = ClassName.get(argElement.asType()).toString();
        if (argClassName.equals(boolean[].class.getCanonicalName())) {
            return "bundle.putBooleanArray($S, $L)";
        } else if (argClassName.equals(byte[].class.getCanonicalName())) {
            return "bundle.putByteArray($S, $L)";
        } else if (argClassName.equals(char[].class.getCanonicalName())) {
            return "bundle.putCharArray($S, $L)";
        } else if (argClassName.equals(double[].class.getCanonicalName())) {
            return "bundle.putDoubleArray($S, $L)";
        } else if (argClassName.equals(float[].class.getCanonicalName())) {
            return "bundle.putFloatArray($S, $L)";
        } else if (argClassName.equals(int[].class.getCanonicalName())) {
            return "bundle.putIntArray($S, $L)";
        } else if (argClassName.equals(long[].class.getCanonicalName())) {
            return "bundle.putLongArray($S, $L)";
        } else if (argClassName.equals(short[].class.getCanonicalName())) {
            return "bundle.putShortArray($S, $L)";
        } else if (argClassName.equals(CharSequence[].class.getCanonicalName())) {
            return "bundle.putCharSequenceArray($S, $L)";
        } else if (argClassName.equals(String[].class.getCanonicalName())) {
            return "bundle.putStringArray($S, $L)";
        } else {
            ArrayType arrayType = (ArrayType) argElement.asType();
            DeclaredType declaredType = (DeclaredType) arrayType.getComponentType();
            TypeElement argClass = (TypeElement) declaredType.asElement();
            for (TypeMirror interfaceTypeMirror : argClass.getInterfaces()) {
                if (interfaceTypeMirror.toString().equals("android.os.Parcelable")) {
                    return "bundle.putParcelableArray($S, $L)";
                }
            }
        }
        throw new BadArgException(argElement);
    }

    private String addArrayListArg(Element argElement) throws BadArgException {
        String argClassName = ClassName.get(argElement.asType()).toString();
        if (argClassName.equals(ArrayList.class.getCanonicalName() + "<" + CharSequence.class.getCanonicalName() + ">")) {
            return "bundle.putCharSequenceArrayList($S, $L)";
        } else if (argClassName.equals(ArrayList.class.getCanonicalName() + "<" + Integer.class.getCanonicalName() + ">")) {
            return "bundle.putIntegerArrayList($S, $L)";
        } else if (argClassName.equals(ArrayList.class.getCanonicalName() + "<" + String.class.getCanonicalName() + ">")) {
            return "bundle.putStringArrayList($S, $L)";
        } else if (argClassName.equals(ArrayList.class.getCanonicalName() + "<android.os.Parcelable>")) {
            return "bundle.putParcelableArrayList($S, $L)";
        } else if (argClassName.equals(ArrayList.class.getCanonicalName() + "<? extends android.os.Parcelable>")) {
            return "bundle.putParcelableArrayList($S, $L)";
        } else {
            DeclaredType declaredType = (DeclaredType) argElement.asType();
            for (TypeMirror genericArgClassTypeMirror : declaredType.getTypeArguments()) {
                TypeElement genericArgClass = (TypeElement) ((DeclaredType) genericArgClassTypeMirror).asElement();
                for (TypeMirror interfaceTypeMirror : genericArgClass.getInterfaces()) {
                    if (interfaceTypeMirror.toString().equals("android.os.Parcelable")) {
                        return "bundle.putParcelableArrayList($S, $L)";
                    }
                }
            }
        }
        throw new BadArgException(argElement);
    }

    private String addSparseArrayArg(Element argElement) throws BadArgException {
        String argClassName = ClassName.get(argElement.asType()).toString();
        switch (argClassName) {
            case "android.util.SparseArray<android.os.Parcelable>":
                return "bundle.putSparseParcelableArray($S, $L)";
            case "android.util.SparseArray<? extends android.os.Parcelable>":
                return "bundle.putSparseParcelableArray($S, $L)";
            default:
                DeclaredType declaredType = (DeclaredType) argElement.asType();
                for (TypeMirror genericArgClassTypeMirror : declaredType.getTypeArguments()) {
                    TypeElement genericArgClass = (TypeElement) ((DeclaredType) genericArgClassTypeMirror).asElement();
                    for (TypeMirror interfaceTypeMirror : genericArgClass.getInterfaces()) {
                        if (interfaceTypeMirror.toString().equals("android.os.Parcelable")) {
                            return "bundle.putSparseParcelableArray($S, $L)";
                        }
                    }
                }
                break;
        }
        throw new BadArgException(argElement);
    }
}
