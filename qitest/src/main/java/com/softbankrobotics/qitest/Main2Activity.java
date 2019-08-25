package com.softbankrobotics.qitest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        QLPepper.getInstance().register(this);
    }

    @Override
    protected void onDestroy() {
        QLPepper.getInstance().unregister(this);
        super.onDestroy();
    }
}
