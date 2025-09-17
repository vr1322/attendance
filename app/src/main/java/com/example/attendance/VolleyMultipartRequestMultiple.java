package com.example.attendance;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.AuthFailureError;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class VolleyMultipartRequestMultiple extends Request<NetworkResponse> {

    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();

    private Response.Listener<NetworkResponse> mListener;
    private Response.ErrorListener mErrorListener;

    private Map<String, String> mHeaders;
    private Map<String, String> mParams;
    private Map<String, ArrayList<DataPart>> mByteData;

    public VolleyMultipartRequestMultiple(int method, String url,
                                          Response.Listener<NetworkResponse> listener,
                                          Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
    }

    // --- Headers ---
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return (mHeaders != null) ? mHeaders : super.getHeaders();
    }

    public void setHeaders(Map<String, String> headers) {
        this.mHeaders = headers;
    }

    // --- Params (text fields) ---
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return (mParams != null) ? mParams : super.getParams();
    }

    public void setParams(Map<String, String> params) {
        this.mParams = params;
    }

    // --- File data ---
    public void setByteData(Map<String, ArrayList<DataPart>> byteData) {
        this.mByteData = byteData;
    }

    // --- Content type ---
    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    // --- Build body ---
    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            // Add params
            if (mParams != null && !mParams.isEmpty()) {
                for (Map.Entry<String, String> entry : mParams.entrySet()) {
                    bos.write((twoHyphens + boundary + lineEnd).getBytes());
                    bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + lineEnd).getBytes());
                    bos.write((lineEnd).getBytes());
                    bos.write((entry.getValue() + lineEnd).getBytes());
                }
            }

            // Add byte data
            if (mByteData != null && !mByteData.isEmpty()) {
                for (Map.Entry<String, ArrayList<DataPart>> entry : mByteData.entrySet()) {
                    String key = entry.getKey();
                    ArrayList<DataPart> dataList = entry.getValue();

                    for (DataPart dataFile : dataList) {
                        bos.write((twoHyphens + boundary + lineEnd).getBytes());
                        bos.write(("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + dataFile.getFileName() + "\"" + lineEnd).getBytes());
                        bos.write(("Content-Type: " + dataFile.getType() + lineEnd).getBytes());
                        bos.write(lineEnd.getBytes());

                        bos.write(dataFile.getContent());
                        bos.write(lineEnd.getBytes());
                    }
                }
            }

            bos.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());

        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }

        return bos.toByteArray();
    }

    // --- Response ---
    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, null);
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(com.android.volley.VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }

    // --- DataPart class ---
    public static class DataPart {
        private String fileName;
        private byte[] content;
        private String type;

        public DataPart(String name, byte[] data) {
            this(name, data, "application/octet-stream");
        }

        public DataPart(String name, byte[] data, String type) {
            this.fileName = name;
            this.content = data;
            this.type = type;
        }

        public String getFileName() { return fileName; }
        public byte[] getContent() { return content; }
        public String getType() { return type; }
    }
}
