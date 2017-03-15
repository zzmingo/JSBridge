package com.zzmingo.androidjsbridge;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by mingo on 2016/12/20.
 */

public class JSBridgeManager {

    public static final String LOG_TAG = JSBridgeManager.class.getName();

    public static final String LOW_LEVEL_BRIDGE_PROTOCOL = "mingoJSBridge://";

    private WebView webView;
    private BridgeObject bridgeObject;

    private HashMap<String, Class<? extends JSBridgeModule>> modules;
    private HashMap<String, IJSBridgeFactory> factories;

    public JSBridgeManager(WebView webView) {
        this.webView = webView;
        this.webView.getSettings().setUserAgentString(this.webView.getSettings().getUserAgentString() + " " + this.getUserAgent());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            this.webView.addJavascriptInterface(new BridgeObject(), "mingoAndroidJSBridge");
        } else {
            bridgeObject = new BridgeObject();
        }

        modules = new HashMap<>();
        factories = new HashMap<>();
    }

    public boolean isLowLevelBridge() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public String getUserAgent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return "mingoJSBridge/Normal";
        } else {
            return "mingoJSBridge/LowLevel";
        }
    }

    public WebView getWebView() {
        return this.webView;
    }

    public void addBridgeModule(String id, Class<? extends JSBridgeModule> clazz) {
        modules.put(id, clazz);
    }

    public void addBridgeFactory(String id, IJSBridgeFactory factory) {
        factories.put(id, factory);
    }

    public boolean isBridgeMessage(String message) {
        return message != null && message.startsWith(LOW_LEVEL_BRIDGE_PROTOCOL);
    }

    public void lowLevelBridgeCall(final String message) {
        final String bridgeMessage = message.replaceFirst(LOW_LEVEL_BRIDGE_PROTOCOL, "");
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    bridgeObject.exec(bridgeMessage);
                } catch(Exception e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
        });

    }

    public void callback(String handleId, String dataStr) {
        String callbackScript = "window.mingoJSBridge.onNativeMessage('#handleId', #data);";
        callbackScript = callbackScript.replace("#handleId", handleId);
        callbackScript = callbackScript.replace("#data", dataStr == null ? "null" : dataStr);

        final String script = callbackScript;

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
             @Override
             public void run() {
                 executeJavascript(script);
             }
         });
    }



    public void injectBridgeScript() {
        executeJavascript(join(JS_SOURCE));
    }

    public void executeJavascript(String script) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(script, null);
        } else {
            webView.loadUrl("javascript: " + script);
        }
    }

    class BridgeObject {

        public JSBridgeModule createModule(String id) throws Exception {
            Class<? extends JSBridgeModule> clazz = modules.get(id);
            if(clazz != null) {
                Log.d(LOG_TAG, clazz.getName());
                return clazz.getConstructor(JSBridgeManager.class).newInstance(JSBridgeManager.this);
            } else {
                IJSBridgeFactory factory = factories.get(id);
                return factory.createBridge(JSBridgeManager.this);
            }
        }

        @JavascriptInterface
        public void exec(String message) {
            try {
                Log.d(LOG_TAG, message);
                JSONObject msg = new JSONObject(message);
                final String handleId = msg.getString("handleId");
                JSONObject data = msg.getJSONObject("data");
                String moduleName = data.getString("module");
                String methodName = data.getString("method");
                final JSONObject args = data.getJSONObject("args");
                final JSBridgeModule module = createModule(moduleName);
                final Method method = module.getClass().getMethod(methodName, String.class, JSONObject.class);
                if (method.isAnnotationPresent(JavascriptInterface.class)) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                method.invoke(module, handleId, args);
                            } catch (Exception e) {
                                Log.e(LOG_TAG, e.getMessage(), e);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, Log.getStackTraceString(e));
            }
        }
    }

    private static final String join(String[] strArray) {
        StringBuilder builder = new StringBuilder();
        for(String str : strArray) {
            builder.append(str + "\n");
        }
        return builder.toString();
    }

    private static final String[] JS_SOURCE = new String[] {
        ";(function() {",
        "var idSeed = 1;",
        "var handles = {};",

        "function genHandleId() {",
        "    return 'BridgeHandle_' + (idSeed++);",
        "};",

        "var isAndroid;",
        "var isLowLevelBridge;",
        "var ua = window.navigator.userAgent;",
        "if(window.mingoAndroidJSBridge) {",
        "    isAndroid = true;",
        "    isLowLevelBridge = /mingoJSBridge\\/LowLevel/.test(ua);",
        "}",

        "let JSBridge = window.mingoJSBridge = {",

        "    onNativeMessage: function(handleId, data) {",
        "        var callback = handles[handleId];",
        "        callback && callback(data);",
        "        delete handles[handleId];",
        "    },",

        "    callNative: function(data, callback) {",
        "        var handleId = genHandleId();",
        "        if(callback) {",
        "            handles[handleId] = callback;",
        "        }",
        "        if(isAndroid) {",
        "            var messageStr = JSON.stringify({",
        "                handleId: handleId,",
        "                data: data",
        "            });",
        "            if(isLowLevelBridge) {",
        "                window.prompt('" + LOW_LEVEL_BRIDGE_PROTOCOL + "' + messageStr);",
        "            } else {",
        "                window.mingoAndroidJSBridge.exec(messageStr);",
        "            }",
        "        } else {",
        "            window.webkit.messageHandlers.mingoJSBridge.postMessage({",
        "                handleId: handleId,",
        "                data: data",
        "            });",
        "        }",
        "    }",
        "};",

        "if(!window.JSBridge) {",
        "    window.JSBridge = JSBridge;",
        "}",

        "})();"
    };
}
