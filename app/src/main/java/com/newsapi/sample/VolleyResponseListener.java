package com.newsapi.sample;

import com.android.volley.VolleyError;

import org.json.JSONObject;

public interface VolleyResponseListener {

    void onError(String message);

    void onResponse(String response);
}
