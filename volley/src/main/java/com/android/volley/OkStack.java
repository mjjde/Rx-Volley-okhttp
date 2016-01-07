package com.android.volley;

import com.squareup.okhttp.Response;
import java.io.IOException;
import java.util.Map;

/**
 * Created by GoogolMo on 6/4/14.
 */
public interface OkStack {
    Response performRequest(Request<?> request, Map<String, String> map) throws IOException, AuthFailureError;
}
