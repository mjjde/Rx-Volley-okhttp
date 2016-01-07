package test;

import android.os.SystemClock;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.OkRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.OkVolley;


/**
 * 文件名：com.dianping.base.okutils
 * 描述：
 * 时间：15/12/2
 * 作者: joker
 */
public class BaseRequest extends OkRequest<String> {
    private long mRequestBeginTime = 0;
    private OkRequest instance ;

    public BaseRequest( int method, String url,
                       Response.Listener<String> listener,
                       Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        setReseponseListener(listener);
        acceptGzipEncoding();
        instance = this;
    }

    @Override
    public void doTask() {
        OkVolley.getInstance().add(instance);
    }


    @Override
    //解析返回的数据
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        byte[] data = response.data;
           Log.d("test", "url = " + getUrl() + ",response = " + new String(data));
        try {
            String model = new String(data);
            return Response.success(model, HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            //解析json出错
            e.printStackTrace();
            return Response.error(new VolleyError("服务器错误"));
        }


    }

    @Override
    public void addMarker(String tag) {
        super.addMarker(tag);
        if (mRequestBeginTime == 0) {
            mRequestBeginTime = SystemClock.elapsedRealtime();
        }
    }

}
