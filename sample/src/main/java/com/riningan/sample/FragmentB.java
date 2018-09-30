package com.riningan.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.riningan.frarg.FrargBinder;
import com.riningan.frarg.annotations.Argument;
import com.riningan.frarg.annotations.ArgumentedFragment;

@ArgumentedFragment()
public class FragmentB extends Fragment {
    @Argument
    String mArgumentString = null;

    @Argument
    int mArgumentInt = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FrargBinder.bind(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_b, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) view.findViewById(R.id.tv)).setText(mArgumentString + "\n" + mArgumentInt);
    }
}
