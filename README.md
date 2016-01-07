# RxVolley



1. Rxjava+volley+okhttp 响应式http库。

2. Rxjava 作为EventBus用来替换Volley的Delivery分发器，但并未去掉。

3. 使用了Okhtto来处理网络请求，可以处理文件下载，volley用来管理。

4. 使用Rxjava onError分发回调异常。

5. Request的构建更趋向链式表达



####  基本用法

       public void test(String url) {
        new BaseRequest (Request.Method.GET, url, null, null)
                .getResult()
                .subscribe(
                        new Action1<RxData>() {
                            @Override
                            public void call(RxData rxData) {
                                String data = new String(rxData.getData());
                                
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                if(throwable instanceof VolleyError) {
                                    VolleyError err = (VolleyError) throwable;
                                }
                               
                            }
                        }
                );
    }
    
    
