package com.riningan.frarg;

import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Size;
import android.util.SizeF;
import android.util.SparseArray;

import com.riningan.frarg.annotations.Argument;
import com.riningan.frarg.annotations.ArgumentedFragment;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;


public class FrargBinder {
    public static void bind(@NonNull android.app.Fragment fragment) {
        Bundle bundle = fragment.getArguments();
        if (bundle != null) {
            bind(fragment, bundle);
        } else {
            throw new RuntimeException("Bundle doesn't exist");
        }
    }

    public static void bind(@NonNull android.support.v4.app.Fragment fragment) {
        Bundle bundle = fragment.getArguments();
        if (bundle != null) {
            bind(fragment, bundle);
        } else {
            throw new RuntimeException("Bundle doesn't exist");
        }
    }

    @SuppressWarnings("ReflectionForUnavailableAnnotation")
    public static void bind(Object annotaitedInstance, @NonNull Bundle bundle) {
        if (annotaitedInstance.getClass().getAnnotation(ArgumentedFragment.class) == null) {
            throw new RuntimeException("Instance is not annotated as ArgumentedFragment");
        }
        ArrayList<Field> fields = getArgFields(annotaitedInstance.getClass());
        for (Field field : fields) {
            String argName = field.getName();
            if (!bundle.containsKey(argName)) {
                throw new RuntimeException("Bundle doesn't contain value with key " + argName);
            }
            field.setAccessible(true);
            try {
                field.set(annotaitedInstance, getValue(field, bundle));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Can't cast value for field " + argName);
            }
        }
    }


    @SuppressWarnings("ReflectionForUnavailableAnnotation")
    private static ArrayList<Field> getArgFields(Class cls) {
        ArrayList<Field> fields = new ArrayList<>();
        for (Field field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(Argument.class)) {
                fields.add(field);
            }
        }
        return fields;
    }

    private static Object getValue(Field field, Bundle bundle) {
        if (field.getType().isPrimitive()) {
            return getPrimitiveValue(field, bundle);
        } else if (field.getType().isArray()) {
            return getArrayValue(field, bundle);
        } else if (field.getType() == ArrayList.class) {
            return getArrayListValue(field, bundle);
        } else if (field.getType() == SparseArray.class) {
            return getSparseArrayValue(field, bundle);
        }
        Class type = field.getType();
        String argName = field.getName();
        if (type == CharSequence.class) {
            return bundle.getCharSequence(argName);
        } else if (type == String.class) {
            return bundle.getString(argName);
        } else if (type == Bundle.class) {
            return bundle.getBundle(argName);
        } else if (type == Parcelable.class) {
            return bundle.getParcelable(argName);
        } else if (type == Serializable.class) {
            return bundle.getSerializable(argName);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && type == Size.class) {
            return bundle.getSize(argName);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && type == SizeF.class) {
            return bundle.getSizeF(argName);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && type == IBinder.class) {
            return bundle.getBinder(argName);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && type == Binder.class) {
            return bundle.getBinder(argName);
        } else {
            for (Class _interface : type.getInterfaces()) {
                if (_interface == Parcelable.class) {
                    return bundle.getParcelable(argName);
                } else if (type == Serializable.class) {
                    return bundle.getSerializable(argName);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && type == IBinder.class) {
                    return bundle.getBinder(argName);
                }
            }
        }
        throw new RuntimeException("Unsuported argument type for field " + argName);
    }

    private static Object getPrimitiveValue(Field field, Bundle bundle) {
        Class type = field.getType();
        String argName = field.getName();
        if (type == boolean.class) {
            return bundle.getBoolean(argName);
        } else if (type == byte.class) {
            return bundle.getByte(argName);
        } else if (type == char.class) {
            return bundle.getChar(argName);
        } else if (type == double.class) {
            return bundle.getDouble(argName);
        } else if (type == float.class) {
            return bundle.getFloat(argName);
        } else if (type == int.class) {
            return bundle.getInt(argName);
        } else if (type == long.class) {
            return bundle.getLong(argName);
        } else if (type == short.class) {
            return bundle.getShort(argName);
        } else {
            throw new RuntimeException("Unsuported argument type for field " + argName);
        }
    }

    private static Object getArrayValue(Field field, Bundle bundle) {
        Class type = field.getType();
        String argName = field.getName();
        if (type == boolean[].class) {
            return bundle.getBooleanArray(argName);
        } else if (type == byte[].class) {
            return bundle.getByteArray(argName);
        } else if (type == char[].class) {
            return bundle.getCharArray(argName);
        } else if (type == double[].class) {
            return bundle.getDoubleArray(argName);
        } else if (type == float[].class) {
            return bundle.getFloatArray(argName);
        } else if (type == int[].class) {
            return bundle.getIntArray(argName);
        } else if (type == float[].class) {
            return bundle.getLongArray(argName);
        } else if (type == short[].class) {
            return bundle.getShortArray(argName);
        } else if (type == CharSequence[].class) {
            return bundle.getCharSequenceArray(argName);
        } else if (type == String[].class) {
            return bundle.getStringArray(argName);
        } else if (type == Parcelable[].class) {
            return bundle.getParcelableArray(argName);
        } else {
            for (Class _interface : type.getComponentType().getInterfaces()) {
                if (_interface == Parcelable.class) {
                    return bundle.getParcelableArray(argName);
                }
            }
        }
        throw new RuntimeException("Unsuported argument type for field " + argName);
    }

    private static Object getArrayListValue(Field field, Bundle bundle) {
        String argName = field.getName();
        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
        for (Type arg : parameterizedType.getActualTypeArguments()) {
            if (arg instanceof Class) {
                Class clsArg = (Class) arg;
                if (clsArg == CharSequence.class) {
                    return bundle.getCharSequenceArrayList(argName);
                } else if (clsArg == Integer.class) {
                    return bundle.getIntegerArrayList(argName);
                } else if (clsArg == String.class) {
                    return bundle.getStringArrayList(argName);
                } else if (clsArg == Parcelable.class) {
                    return bundle.getParcelableArrayList(argName);
                } else {
                    for (Class _interface : clsArg.getInterfaces()) {
                        if (_interface == Parcelable.class) {
                            return bundle.getSparseParcelableArray(argName);
                        }
                    }
                }
            } else if (arg instanceof WildcardType) {
                // ? extends
                for (Type extendType : ((WildcardType) arg).getUpperBounds()) {
                    Class clsArg = (Class) extendType;
                    if (clsArg == Parcelable.class) {
                        return bundle.getParcelableArrayList(argName);
                    }
                }
            }
        }
        throw new RuntimeException("Unsuported argument type for field " + argName);
    }

    private static Object getSparseArrayValue(Field field, Bundle bundle) {
        String argName = field.getName();
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        for (Type arg : type.getActualTypeArguments()) {
            if (arg instanceof Class) {
                Class clsArg = (Class) arg;
                if (clsArg == Parcelable.class) {
                    return bundle.getSparseParcelableArray(argName);
                } else {
                    for (Class _interface : clsArg.getInterfaces()) {
                        if (_interface == Parcelable.class) {
                            return bundle.getSparseParcelableArray(argName);
                        }
                    }
                }
            } else if (arg instanceof WildcardType) {
                // ? extends
                for (Type extendType : ((WildcardType) arg).getUpperBounds()) {
                    Class clsArg = (Class) extendType;
                    if (clsArg == Parcelable.class) {
                        return bundle.getParcelableArrayList(argName);
                    }
                }
            }
        }
        throw new RuntimeException("Unsuported argument type for field " + argName);
    }
}
