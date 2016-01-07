package com.android.volley;


import com.android.volley.toolbox.Authenticator;
import com.android.volley.toolbox.OkVolley;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rxBus.RxBus;
import rxBus.RxData;


/**
 * 文件名：im.amomo.volley
 * 描述：
 * 时间：15/12/2
 * 作者: joker
 */
public abstract class OkRequest<T> extends Request<T> {

    /**
     * 'Authorization' header name
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * 'UTF-8' charset name
     */
    public static final String CHARSET_UTF8 = "UTF-8";

    /**
     * 'gzip' encoding header value
     */
    public static final String ENCODING_GZIP = "gzip";

    /**
     * 'Accept' header name
     */
    public static final String HEADER_ACCEPT = "Accept";

    /**
     * 'Accept-Charset' header name
     */
    public static final String HEADER_ACCEPT_CHARSET = "Accept-Charset";

    /**
     * 'Accept-Encoding' header name
     */
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";

    /**
     * 'User-Agent' header name
     */
    public static final String HEADER_USER_AGENT = "User-Agent";

    /**
     * 'X-Accept-Version' header name
     */
    public static final String HEADER_ACCEPT_VERSION = "X-Accept-Version";

    /**
     * 'Content-Type' header name
     */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * 'Content-Length' header name
     */
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";

    /**
     * 'Content-Encoding' header name
     */
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";

    /**
     * 'Referer' header name
     */
    public static final String HEADER_REFERER = "Referer";

    /**
     * Content type for request.
     */
    public static final String PROTOCOL_CONTENT_TYPE_JSON =
            String.format("application/json; charset=%s", CHARSET_UTF8);

    public static final String PROTOCOL_CONTENT_TYPE_FORM =
            String.format("application/x-www-form-urlencoded; charset=%s", CHARSET_UTF8);

    /**
     * 'charset' header value parameter
     */
    public static final String PARAM_CHARSET = "charset";

    private static final String BOUNDARY = "00douban0natalya0volley00";

    private static final String CONTENT_TYPE_MULTIPART = "multipart/form-data; boundary="
            + BOUNDARY;

    private static final String CRLF = "\r\n";

    protected ConcurrentHashMap<String, String> mRequestHeaders;
    //    protected ConcurrentHashMap<String, String> mRequestParams;
    private Response.Listener mListener;

    private String mContentType = PROTOCOL_CONTENT_TYPE_FORM;

    private boolean mMultipart;
    private boolean mForm;
    private RequestOutputStream mOutput;
    private int mBufferSize = 8192;
    private boolean mIgnoreCloseExceptions = true;
    private String mRequestUrl;


    /**
     * construct method
     *
     * @param method        request method
     * @param url           request url
     */
    public OkRequest(int method, String url, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mRequestHeaders = new ConcurrentHashMap<String, String>();
    }

    /**
     * construct method
     *
     * @param method        request method
     * @param url           request url
     */
    public OkRequest(int method, String url, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mRequestHeaders = new ConcurrentHashMap<String, String>();
    }

    /**
     * set response listener
     *
     * @return this request
     */
    public OkRequest<T> setReseponseListener(Response.Listener<T> listener) {
        this.mListener = listener;
        return this;
    }

    public Response.Listener getListener(){
        return this.mListener;
    }

    /**
     * Set 'Content-Type' header
     *
     * @param contentType {@link String} contentType
     * @return this request
     */
    public OkRequest<T> contentType(final String contentType) {
        this.mContentType = contentType;
        return this;
    }

    protected OkRequest<T> openOutput() {
        if (mOutput != null) {
            return this;
        }
        mOutput = new RequestOutputStream(CHARSET_UTF8);
        return this;
    }

    protected OkRequest<T> startPart() throws IOException {
        openOutput();
        if (!mMultipart) {
            mMultipart = true;
            contentType(CONTENT_TYPE_MULTIPART);
            mOutput.write("--" + BOUNDARY + CRLF);
        } else {
            mOutput.write(CRLF + "--" + BOUNDARY + CRLF);
        }
        return this;
    }

    /**
     * Write part header
     *
     * @param name
     * @param filename
     * @return this request
     * @throws java.io.IOException
     */
    protected OkRequest<T> writePartHeader(final String name, final String filename)
            throws IOException {
        return writePartHeader(name, filename, null);
    }

    /**
     * Write part header
     *
     * @param name
     * @param filename
     * @param contentType
     * @return this request
     * @throws java.io.IOException
     */
    protected OkRequest<T> writePartHeader(final String name,
                                           final String filename, final String contentType) throws IOException {
        final StringBuilder partBuffer = new StringBuilder();
        partBuffer.append("form-data; name=\"").append(name);
        if (filename != null) {
            partBuffer.append("\"; filename=\"").append(filename);
        }
        partBuffer.append('"');
        partHeader("Content-Disposition", partBuffer.toString());
        if (contentType != null) {
            partHeader(HEADER_CONTENT_TYPE, contentType);
        }
        return send(CRLF);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param part
     * @return this request
     */
    public OkRequest<T> part(final String name, final String part) throws IOException {
        return part(name, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param part
     * @return this request
     * @throws OkRequest
     */
    public OkRequest<T> part(final String name, final String filename,
                             final String part) throws IOException {
        return part(name, filename, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param contentType value of the Content-Type part header
     * @param part
     * @return this request
     */
    public OkRequest<T> part(final String name, final String filename,
                             final String contentType, final String part) throws IOException {

        startPart();
        writePartHeader(name, filename, contentType);
        mOutput.write(part);

        return this;
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param part
     * @return this request
     */
    public OkRequest<T> part(final String name, final Number part) throws IOException {
        return part(name, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param part
     * @return this request
     */
    public OkRequest<T> part(final String name, final String filename,
                             final Number part) throws IOException {
        return part(name, filename, part != null ? part.toString() : null);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param part
     * @return this request
     */
    public OkRequest<T> part(final String name, final File part) throws IOException {
        return part(name, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param part
     * @return this request
     */
    public OkRequest<T> part(final String name, final String filename,
                             final File part) throws IOException {
        return part(name, filename, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param contentType value of the Content-Type part header
     * @param part
     * @return this request
     */
    public OkRequest<T> part(final String name, final String filename,
                             final String contentType, final File part) throws IOException {
        final InputStream stream;
        stream = new BufferedInputStream(new FileInputStream(part));
//            incrementTotalSize(part.length());

        return part(name, filename, contentType, stream);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param part
     * @return this request
     */
    public OkRequest<T> part(final String name, final InputStream part) throws IOException {
        return part(name, null, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param contentType value of the Content-Type part header
     * @param part
     * @return this request
     */
    public OkRequest<T> part(final String name, final String filename,
                             final String contentType, final InputStream part) throws IOException {

        try {
            startPart();
            writePartHeader(name, filename, contentType);
            copy(part, mOutput);
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            VolleyLog.e(ex, "error on part");
        }


        return this;
    }

    /**
     * Write a multipart header to the response body
     *
     * @param name
     * @param value
     * @return this request
     */
    public OkRequest<T> partHeader(final String name, final String value) throws IOException {
        return send(name).send(": ").send(value).send(CRLF);
    }

    /**
     * Write contents of file to request body
     *
     * @param input
     * @return this request
     */
    public OkRequest<T> send(final File input) throws IOException {
        final InputStream stream;

        stream = new BufferedInputStream(new FileInputStream(input));
        return send(stream);
    }

    /**
     * Write byte array to request body
     *
     * @param input
     * @return this request
     */
    public OkRequest<T> send(final byte[] input) throws IOException {
        return send(new ByteArrayInputStream(input));
    }

    /**
     * Write stream to request body
     * <p/>
     * The given stream will be closed once sending completes
     *
     * @param input
     * @return this request
     */
    public OkRequest<T> send(final InputStream input) throws IOException {

        openOutput();

        copy(input, mOutput);

        return this;
    }

    /**
     * Write char sequence to request body
     * <p/>
     * The charset configured via {@link #contentType(String)} will be used and
     * UTF-8 will be used if it is unset.
     *
     * @param value
     * @return this request
     */
    public OkRequest<T> send(final CharSequence value) {

        openOutput();
        try {
            mOutput.write(value.toString());
        } catch (IOException e) {
            deliverError(new ParseError(e));
        }

        return this;
    }

    /**
     * Write the values in the map as form data to the request body
     * <p/>
     * The pairs specified will be URL-encoded in UTF-8 and sent with the
     * 'application/x-www-form-urlencoded' content-type
     *
     * @param values
     * @return this request
     */
    public OkRequest<T> form(final Map<String, String> values) {
        return form(values, CHARSET_UTF8);
    }

    /**
     * Write the key and value in the entry as form data to the request body
     * <p/>
     * The pair specified will be URL-encoded in UTF-8 and sent with the
     * 'application/x-www-form-urlencoded' content-type
     *
     * @param entry
     * @return this request
     */
    public OkRequest<T> form(final Map.Entry<String, String> entry) {
        return form(entry, CHARSET_UTF8);
    }

    /**
     * Write the key and value in the entry as form data to the request body
     * <p/>
     * The pair specified will be URL-encoded and sent with the
     * 'application/x-www-form-urlencoded' content-type
     *
     * @param entry
     * @param charset
     * @return this request
     */
    public OkRequest<T> form(final Map.Entry<String, String> entry, final String charset) {
        return form(entry.getKey(), entry.getValue(), charset);
    }

    /**
     * Write the name/value pair as form data to the request body
     * <p/>
     * The pair specified will be URL-encoded in UTF-8 and sent with the
     * 'application/x-www-form-urlencoded' content-type
     *
     * @param name
     * @param value
     * @return this request
     */
    public OkRequest<T> form(final String name, final String value) {
        return form(name, value, CHARSET_UTF8);
    }

    /**
     * Write the name/value pair as form data to the request body
     * <p/>
     * The values specified will be URL-encoded and sent with the
     * 'application/x-www-form-urlencoded' content-type
     *
     * @param name
     * @param value
     * @param charset
     * @return this request
     */
    public OkRequest<T> form(final String name, final String value, String charset) {
        final boolean first = !mForm;
        if (first) {
            contentType(PROTOCOL_CONTENT_TYPE_FORM);
            mForm = true;

        }
        charset = getValidCharset(charset);

        openOutput();
        if (!first) {
            mOutput.write('&');
        }
        try {
            if (VolleyLog.DEBUG) {
                VolleyLog.d("name=%1$s, value=%2$s", name, value);
            }
            mOutput.write(URLEncoder.encode(name, charset));
            mOutput.write("=");
            if (value != null) {
                mOutput.write(URLEncoder.encode(value, charset));
            }
        } catch (IOException e) {
            //Do Nothing
        }


        return this;
    }

    /**
     * Write the values in the map as encoded form data to the request body
     *
     * @param values
     * @param charset
     * @return this request
     */
    public OkRequest<T> form(final Map<String, String> values, final String charset) {
        if (!values.isEmpty()) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                form(entry, charset);
            }
        }
        return this;
    }

    /**
     * Write the value to url params
     *
     * @param key   key
     * @param value value
     * @return this request
     */
    public OkRequest<T> param(final String key, final String value) {
        StringBuilder urlBuilder = new StringBuilder(getUrl());
        try {
        if (getUrl().contains("?")) {
            urlBuilder.append("&");
        } else {
            urlBuilder.append("?");
        }
        urlBuilder.append(URLEncoder.encode(key, "UTF-8"));
        urlBuilder.append("=");
        urlBuilder.append(URLEncoder.encode(value, "UTF-8"));
            mRequestUrl =urlBuilder.toString();
        }catch (IOException e){

        }

        return this;
    }

    /**
     * Write the map entry to url params
     *
     * @param entry map entry
     * @return this request
     */
    public OkRequest<T> param(final Map.Entry<String, String> entry) {
        return param(entry.getKey(), entry.getValue());
    }

    /**
     * Write the map to url params
     *
     * @param values map
     * @return this request
     */
    public OkRequest<T> params(final Map<String, String> values) {
        if (!values.isEmpty()) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                param(entry);
            }
        }
        return this;
    }

    /**
     * Set header name to given value
     *
     * @param name
     * @param value
     * @return this request
     */
    public OkRequest<T> header(final String name, final String value) {
        mRequestHeaders.put(name, value);
        return this;
    }

    /**
     * Set header name to given value
     *
     * @param name
     * @param value
     * @return this request
     */
    public OkRequest<T> header(final String name, final Number value) {
        return header(name, value != null ? value.toString() : null);
    }

    /**
     * Set all headers found in given map where the keys are the header names and
     * the values are the header values
     *
     * @param headers
     * @return this request
     */
    public OkRequest<T> headers(final Map<String, String> headers) {
        if (!headers.isEmpty())
            for (Map.Entry<String, String> header : headers.entrySet())
                header(header);
        return this;
    }

    /**
     * Set header to have given entry's key as the name and value as the value
     *
     * @param header
     * @return this request
     */
    public OkRequest<T> header(final Map.Entry<String, String> header) {
        return header(header.getKey(), header.getValue());
    }

    public OkRequest<T> setAuthenticator(Authenticator authenticator) throws AuthFailureError {
        if (authenticator == null) return this;
//        if (authenticator == null) {
//            throw new NullPointerException("authenticator can not be null!");
//        }
        String accessToken = authenticator.getAuthToken();
        if (accessToken != null) {
            header(HEADER_AUTHORIZATION, String.format("Bearer %1$s", accessToken));
        }
        return this;
    }

    public OkRequest<T> setAuthenticator(String authenticator) throws AuthFailureError {
        if (authenticator == null) return this;

        String accessToken = authenticator;
        if (accessToken != null) {
            header(HEADER_AUTHORIZATION, String.format("Bearer %1$s", accessToken));
        }
        return this;
    }


    /**
     * Set the 'Accept' header to given value
     *
     * @param accept
     * @return this request
     */
    public OkRequest<T> accept(final String accept) {
        return header(HEADER_ACCEPT, accept);
    }

    /**
     * Set the 'Accept' header to 'application/json'
     *
     * @return this request
     */
    public OkRequest<T> acceptJson() {
        return accept(PROTOCOL_CONTENT_TYPE_JSON);
    }

    /**
     * Set the 'Accept-Encoding' header to given value
     *
     * @param acceptEncoding
     * @return this request
     */
    public OkRequest<T> acceptEncoding(final String acceptEncoding) {
        return header(HEADER_ACCEPT_ENCODING, acceptEncoding);
    }

    /**
     * Set the 'Accept-Encoding' header to 'gzip'
     *
     * @return this request
     */
    public OkRequest<T> acceptGzipEncoding() {
        return acceptEncoding(ENCODING_GZIP);
    }

    /**
     * Set the 'Accept-Charset' header to given value
     *
     * @param acceptCharset
     * @return this request
     */
    public OkRequest<T> acceptCharset(final String acceptCharset) {
        return header(HEADER_ACCEPT_CHARSET, acceptCharset);
    }

    public Observable<RxData> getResult(){
        doTask();
        return RxBus.getInstance().take(getUrl());
    }


    public abstract void doTask();

    /**
     * Set the 'User-Agent' header to given value
     *
     * @param userAgent
     * @return this request
     */
    public OkRequest<T> userAgent(final String userAgent) {
        return header(HEADER_USER_AGENT, userAgent);
    }

    /**
     * Set the 'Referer' header to given value
     *
     * @param referer
     * @return this request
     */
    public OkRequest<T> referer(final String referer) {
        return header(HEADER_REFERER, referer);
    }

    /**
     * Copy from input stream to output stream
     *
     * @param input
     * @param output
     * @return this request
     * @throws java.io.IOException
     */
    protected OkRequest<T> copy(final InputStream input, final OutputStream output) throws IOException {
        return new CloseOperation<OkRequest>(input, mIgnoreCloseExceptions) {

            @Override
            public OkRequest<T> call() throws IOException {
                boolean thrown = false;
                try {
                    final byte[] buffer = new byte[mBufferSize];
                    int read;
                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    return OkRequest.this;
                } catch (IOException e) {
                    thrown = true;
                    throw e;
                } finally {
                    try {
                        done();
                    } catch (IOException e) {
                        if (!thrown) {
                            throw e;
                        }
                    }

                }

            }
        }.call();
    }

    /**
     * get request header
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();
        if (!mRequestHeaders.isEmpty()) {
            if (headers.isEmpty()) {
                return mRequestHeaders;
            } else {
                headers.putAll(mRequestHeaders);
            }
        }
        return headers;
    }

    @Override
    protected abstract Response<T> parseNetworkResponse(NetworkResponse response);

    @Override
    protected void deliverResponse(T t) {
        OkVolley.getInstance().getList().remove(this);
        if (this.mListener != null) {
            this.mListener.onResponse(t);
        }
    }

    /**
     * get original request url
     *
     * @return original request url
     */
    public String getOriginUrl() {
        return super.getUrl();
    }

    /**
     * get request url
     */
    @Override
    public String getUrl() {
        if (mRequestUrl == null) {
            mRequestUrl = super.getUrl();
        }

        return mRequestUrl;
    }

    @Override
    @Deprecated
    protected Map<String, String> getParams() throws AuthFailureError {
        return super.getParams();
    }

    /**
     * get request body content
     */
    @Override
    public String getBodyContentType() {
        return this.mContentType;
    }

    /**
     * get request body
     */
    @Override
    public byte[] getBody() throws AuthFailureError {
        if (mOutput == null) {
            openOutput();
        }
        try {
            if (mMultipart) {

                mOutput.write(CRLF + "--" + BOUNDARY + "--" + CRLF);
            }
            return mOutput.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return mOutput.toByteArray();
        } finally {
            try {
                mOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mOutput = null;
        }

    }


    @Override
    public OkRequest<T> setRequestQueue(RequestQueue requestQueue) {
        super.setRequestQueue(requestQueue);
        return this;
    }

    private static String getValidCharset(final String charset) {
        if (charset != null && charset.length() > 0)
            return charset;
        else
            return CHARSET_UTF8;
    }

    public static class RequestOutputStream extends ByteArrayOutputStream {
        private final String charset;

        public RequestOutputStream(final String charset) {
            super();
            this.charset = charset;
        }

        public RequestOutputStream write(final String value) throws IOException {
//            final ByteBuffer bytes = encoder.encode(CharBuffer.wrap(value));
            super.write(value.getBytes(Charset.forName(charset)));
            return this;
        }
    }

    /**
     * Class that ensures a {@link java.io.Closeable} gets closed with proper exception
     * handling.
     *
     * @param <V>
     */
    protected static abstract class CloseOperation<V> implements Callable<V> {

        private final Closeable closeable;

        private final boolean ignoreCloseExceptions;

        /**
         * Create closer for operation
         *
         * @param closeable
         * @param ignoreCloseExceptions
         */
        protected CloseOperation(final Closeable closeable,
                                 final boolean ignoreCloseExceptions) {
            this.closeable = closeable;
            this.ignoreCloseExceptions = ignoreCloseExceptions;
        }

        protected void done() throws IOException {
            if (closeable instanceof Flushable)
                ((Flushable) closeable).flush();
            if (ignoreCloseExceptions)
                try {
                    closeable.close();
                } catch (IOException e) {
                    // Ignored
                }
            else
                closeable.close();
        }
    }

    @Override
    public int getMethod() {
        return super.getMethod();
    }

    @Override
    public Request<?> setTag(Object tag) {
        return super.setTag(tag);
    }

    @Override
    public Object getTag() {
        return super.getTag();
    }

    @Override
    public int getTrafficStatsTag() {
        return super.getTrafficStatsTag();
    }

    @Override
    public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
        return super.setRetryPolicy(retryPolicy);
    }

    @Override
    public void addMarker(String tag) {
        super.addMarker(tag);
    }

    @Override
    public String getCacheKey() {
        return this.getMethod() + ":" +this.getUrl();
    }

    @Override
    public Request<?> setCacheEntry(Cache.Entry entry) {
        return super.setCacheEntry(entry);
    }

    @Override
    public Cache.Entry getCacheEntry() {
        return super.getCacheEntry();
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    @Override
    protected String getParamsEncoding() {
        return super.getParamsEncoding();
    }

    @Override
    public Priority getPriority() {
        return super.getPriority();
    }

    @Override
    public RetryPolicy getRetryPolicy() {
        return super.getRetryPolicy();
    }

    @Override
    public void markDelivered() {
        super.markDelivered();
    }

    @Override
    public boolean hasHadResponseDelivered() {
        return super.hasHadResponseDelivered();
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        return super.parseNetworkError(volleyError);
    }

    @Override
    public void deliverError(VolleyError error) {
        super.deliverError(error);
    }

    @Override
    public int compareTo(Request<T> other) {
        return super.compareTo(other);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean isCanceled() {
        return super.isCanceled();
    }
}
