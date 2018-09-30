package com.riningan.sample;

import android.os.Bundle;

import com.riningan.frarg.FrargBinder;
import com.riningan.frarg.annotations.Argument;
import com.riningan.frarg.annotations.ArgumentedFragment;

import java.util.ArrayList;

@ArgumentedFragment(fragmentCls = FragmentC.class)
public class PresenterC {
    @Argument
    ArrayList<String> mPresenterArgumentString = null;

    @Argument
    int mPresenterArgumentInt = 0;

    public void bind(Bundle bundle) {
        FrargBinder.bind(this, bundle);
    }
}
