package rxBus;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文件名：com.android.volley.rxBus
 * 描述：
 * 时间：16/1/6
 * 作者: joker
 */
public class RxData {

    private final static List<RxData> dataPool = new ArrayList<>();

    private String url;
    private byte [] data;
    private Map<String, String> header;
    private VolleyError error;

    public RxData(String url, byte[] data, Map<String, String> header) {
        this.url = url;
        this.data = data;
        this.header = header;
    }

    public VolleyError getError(){
        return this.error;
    }

    public static List<RxData> getDataPool() {
        return dataPool;
    }

    public String getUrl() {
        return url;
    }

    public byte[] getData() {
        return data;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }


    public void setError(VolleyError error) {
        this.error = error;
    }


    static RxData obtainRxData(String url, byte[] data, Map<String, String> header){
        return obtainRxData(url,data,header,null);
    }

    static RxData obtainRxData(String url, byte[] data, Map<String, String> header,VolleyError error){
        synchronized (dataPool){
            int size = dataPool.size();
            if(size>0){
                RxData rxData =  dataPool.get(0);
                rxData.data = data;
                rxData.header = header;
                rxData.url = url;
                rxData.error =error;
                return rxData;
            }else{
                return new RxData(url,data,header);
            }
        }
    }

    static void releaseRxData(RxData rxData){
        rxData.url =null;
        rxData.header =null;
        rxData.data = null;
        rxData.error =null;
        synchronized (dataPool){
            if(dataPool.size()<1000){
                dataPool.add(rxData);
            }
        }
    }


    public boolean isError(){
        return this.error !=null;
    }


}
