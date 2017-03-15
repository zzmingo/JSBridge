package com.zzmingo.androidjsbridge;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mingo on 2016/12/20.
 */

public class JSBridgeModule {

    protected final JSBridgeManager bridgeMgr;

    public JSBridgeModule(JSBridgeManager bridgeMgr) {
        this.bridgeMgr = bridgeMgr;
    }

    private void callback(String handleId, JSONObject resp) {
        this.bridgeMgr.callback(handleId, resp.toString());
    }

    private void callback(String handleId, JSONArray resp) {
        this.bridgeMgr.callback(handleId, resp.toString());
    }

    public void callbackOK(String handleId, Object value) {
        JSONObject resp = new JSONObject();
        try {
            Log.e("unBridgeModule", "try");
            resp.put("ec", 200);
            resp.put("data", value);
            callback(handleId, resp);
            Log.e("unBridgeModule", "tryfinish");
        } catch (JSONException e) {
            Log.e("unBridgeModule", "err:" + e.getMessage());
            e.printStackTrace();
            callbackFail(handleId, 400, e.getMessage());
        }
    }

    public void callbackOK(String handleId, JSONObject data) {
        JSONObject resp = new JSONObject();
        try {
            resp.put("ec", 200);
            resp.put("data", data);
            callback(handleId, resp);
        } catch (JSONException e) {
            callbackFail(handleId, 400, e.getMessage());
        }
    }

    public void callbackOK(String handleId, JSONArray data) {
        JSONObject resp = new JSONObject();
        try {
            resp.put("ec", 200);
            resp.put("data", data);
            callback(handleId, resp);
        } catch (JSONException e) {
            callbackFail(handleId, 400, e.getMessage());
        }
    }

    public void callbackFail(String handleId, int ec, String em) {
        JSONObject resp = new JSONObject();
        try {
            resp.put("ec", ec);
            resp.put("em", em);
            callback(handleId, resp);
        } catch (JSONException e) {
        }
    }
}
