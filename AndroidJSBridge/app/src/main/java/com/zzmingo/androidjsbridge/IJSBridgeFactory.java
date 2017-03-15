package com.zzmingo.androidjsbridge;

/***
 * Created by mingo on 2017/3/15.
 */
public interface IJSBridgeFactory {
    JSBridgeModule createBridge(JSBridgeManager bridgeManager);
}
