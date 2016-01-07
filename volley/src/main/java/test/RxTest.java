package test;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.android.volley.Request;

import rx.functions.Action1;
import rxBus.RxData;

/**
 * 文件名：com.android.volley.test
 * 描述：
 * 时间：16/1/7
 * 作者: joker
 */
public class RxTest extends InstrumentationTestCase {
    String url = "http://v.juhe.cn/weather/index?format=2&cityname=%E4%B8%8A%E6%B5%B7&key=d436de499a6da2eac47d5f11fb303ec0";

    public void test() {
        new BaseRequest(Request.Method.GET, url, null, null)
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

                            }
                        }
        );
    }
}
