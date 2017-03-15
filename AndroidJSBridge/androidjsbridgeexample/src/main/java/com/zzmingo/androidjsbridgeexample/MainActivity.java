package com.zzmingo.androidjsbridgeexample;

import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zzmingo.androidjsbridge.JSBridgeManager;
import com.zzmingo.androidjsbridge.JSBridgeModule;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    WebView webview;
    JSBridgeManager bridgeMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webview = new WebView(this);
        webview.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        setContentView(webview);

        bridgeMgr = new JSBridgeManager(webview);
        bridgeMgr.addBridgeModule("Example", ExampleModule.class);

        if(bridgeMgr.isLowLevelBridge()) {

            webview.setWebChromeClient(new WebChromeClient() {

                @Override
                public boolean onJsPrompt(WebView view, String url, final String message, String defaultValue, JsPromptResult result) {
                    if (bridgeMgr.isBridgeMessage(message)) {
                        result.cancel();
                        bridgeMgr.lowLevelBridgeCall(message);
                        return true;
                    }
                    return super.onJsPrompt(view, url, message, defaultValue, result);
                }

            });

        }

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                bridgeMgr.injectBridgeScript();
            }

        });

        String html = "<script type='text/javascript'>" +
                "setTimeout(function() {JSBridge.callNative({" +
                "   module: 'Example'," +
                "   method: 'log', " +
                "   args: {" +
                "       message: 'call Example.log'" +
                "   }" +
                "});}, 1000);" +
                "</script>";

        webview.loadData(html, "text/html", "utf-8");

    }



    public static class ExampleModule extends JSBridgeModule {

        int value = 100;

        public ExampleModule(JSBridgeManager bridgeMgr) {
            super(bridgeMgr);
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void log(String handleId, JSONObject args) throws Exception {
            Log.d(MainActivity.class.getName(), args.getString("message"));
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void getSomeValue(String handleId, JSONObject args) throws Exception {
            callbackOK(handleId, value);
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void getSomeValueFail(String handleId, JSONObject args) throws Exception {
            callbackFail(handleId, 500, "value not found");
        }

    }
}
