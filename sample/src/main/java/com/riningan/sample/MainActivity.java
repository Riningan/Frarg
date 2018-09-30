package com.riningan.sample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.riningan.frarg.processor.FragmentBuilder;
import com.riningan.frarg.processor.FragmentCArgs;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
        findViewById(R.id.btn3).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Fragment fragment;
        switch (v.getId()) {
            case R.id.btn1:
                fragment = FragmentBuilder.newFragmentAInstance();
                break;
            case R.id.btn2:
                fragment = FragmentBuilder.newFragmentBInstance("param", 101);
                break;
            case R.id.btn3:
                ArrayList<String> strs = new ArrayList<>();
                strs.add("first");
                strs.add("second");
                FragmentCArgs fragmentCArgs = new FragmentCArgs(strs, 1020);
                fragment = FragmentBuilder.newFragmentCInstance(fragmentCArgs);
                break;
            default:
                return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
