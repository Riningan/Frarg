package com.riningan.frarg.annotations;

public @interface ArgumentedFragment {
    Class fragmentCls() default Object.class;
}
