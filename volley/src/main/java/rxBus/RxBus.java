package rxBus;

import com.android.volley.VolleyError;

import java.util.Map;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * 文件名：com.android.volley.rxBus
 * 描述：
 * 时间：16/1/6
 * 作者: joker
 */
public class RxBus {
    private RxData data;

    private RxBus() {

    }

    private static volatile RxBus instance;

    public static RxBus getInstance() {
        if (instance == null) {
            synchronized (RxBus.class) {
                if (instance == null) {
                    instance = new RxBus();
                }
            }
        }
        return instance;
    }

    //http://blog.csdn.net/sun927/article/details/44818845
    private final Subject<Object, Object> subject = new SerializedSubject<>(PublishSubject.create());


    public void post(RxData evnet) {
        subject.onNext(evnet);
    }

    public Observable<RxData> take(final String url) {
        return subject.cast(RxData.class)
                .filter(new Func1<RxData, Boolean>() {
                    @Override
                    public Boolean call(RxData rxData) {
                        return rxData.getUrl().equals(url);
                    }
                });
    }

//    private ConcurrentHashMap<String, RxData> pool = new ConcurrentHashMap<>();

//    public rx.Observable<RxData> take(final String url) {
//
//        return rx.Observable.create(new rx.Observable.OnSubscribe<RxData>() {
//            @Override
//            public void call(Subscriber<? super RxData> subscriber) {
//                while (true) {
//                    data = pool.get(url);
//                    if (data != null) {
//                        if (data.isError()) {
//                            subscriber.onError(data.getError());
//                        } else {
//                            subscriber.onNext(data);
//                            subscriber.onCompleted();
//                        }
//
//                        break;
//                    }
//                }
//            }
//        }).doOnCompleted(new Action0() {
//            @Override
//            public void call() {
//                pool.remove(data);
//                RxData.releaseRxData(data);
//            }
//        }).subscribeOn(Schedulers.io());
//    }

    public void post(String url, byte[] data, Map<String, String> header) {
        post(RxData.obtainRxData(url, data, header));
    }

    public void post(String url, byte[] data, Map<String, String> header, VolleyError error) {
        post(RxData.obtainRxData(url, data, header, error));
    }

}
