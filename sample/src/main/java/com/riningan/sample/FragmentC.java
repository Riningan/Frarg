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


public class FragmentC extends Fragment {
    PresenterC presenter = new PresenterC();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        presenter.bind(getArguments());
        /*
        or you can use
        FrargBinder.bind(presenter, getArguments())
         */
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_c, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) view.findViewById(R.id.tv)).setText(presenter.mPresenterArgumentArrayListString.size() + "\n" + presenter.mPresenterArgumentInt);
    }
}
