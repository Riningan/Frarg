package com.riningan.sample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.riningan.frarg.processor.FragmentBuilder;
import com.riningan.frarg.processor.FragmentCArgs;
import com.riningan.frarg.processor.FragmentEArgs;
import com.riningan.frarg.processor.FragmentNotDArgs;

import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
        findViewById(R.id.btn3).setOnClickListener(this);
        findViewById(R.id.btn4).setOnClickListener(this);
        findViewById(R.id.btn5).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Fragment fragment;
        switch (v.getId()) {
            case R.id.btn1:
                fragment = FragmentBuilder.newFragmentAInstance();
                break;
            case R.id.btn2:
                fragment = FragmentBuilder.newFragmentBInstance("param", 101, ArgEnum.THIRD);
                break;
            case R.id.btn3:
                ArrayList<String> strs = new ArrayList<>();
                strs.add("first");
                strs.add("second");
                FragmentCArgs fragmentCArgs = new FragmentCArgs(strs, 1020);
                fragment = FragmentBuilder.newFragmentCInstance(fragmentCArgs);
                break;
            case R.id.btn4:
                fragment = FragmentBuilder.newFragmentNotDInstance(new FragmentNotDArgs("param", 101, FragmentD.Arg.FIRST));
                break;
            case R.id.btn5:
                fragment = FragmentBuilder.newFragmentEInstance(new FragmentEArgs(new Date(), 23));
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
