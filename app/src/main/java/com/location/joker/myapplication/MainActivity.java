package com.location.joker.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.OkVolley;

import rx.functions.Action1;
import rxBus.RxData;
import test.BaseRequest;

public class MainActivity extends Activity {
    String url = "http://v.juhe.cn/weather/index?format=2&cityname=%E8%8B%8F%E5%B7%9E&key=d436de499a6da2eac47d5f11fb303ec0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OkVolley.getInstance().init(this);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });
        Log.e("Joker", "Hello");
    }

    public void test() {
        new BaseRequest (Request.Method.GET, url, null, null)
                .getResult()
                .subscribe(
                        new Action1<RxData>() {
                            @Override
                            public void call(RxData rxData) {
                                String data = new String(rxData.getData());
                                Log.e("Joker", data);
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                if(throwable instanceof VolleyError) {
                                    VolleyError err = (VolleyError) throwable;
                                }
                                Log.e("Joker", throwable.getMessage());
                            }
                        }
                );
    }

}
