//
//  JSBridgeManager.swift
//  iOS-JSBridge
//
//  Created by mingo on 2017/3/15.
//  Copyright © 2017年 zzmingo. All rights reserved.
//

import Foundation
import WebKit
import SwiftyJSON

public typealias ModuleCreateBlock = () -> JSBridgeModule
private let EXPORT_ID: String = "mingoJSBridge"

public class JSBridgeManager : NSObject, WKScriptMessageHandler {
    
    public weak var webView: WKWebView?
    
    private var moduleDist: Dictionary<String, ModuleCreateBlock>
    
    public init(_ webView: WKWebView) {
        self.moduleDist = Dictionary()
        self.webView = webView
        
        super.init()
        
        self.webView?.configuration.userContentController = WKUserContentController()
        self.webView?.configuration.userContentController.add(self, name: EXPORT_ID)
        
        
        let bridgeScript = WKUserScript(source: jsSource.joined(separator: "\n"), injectionTime: .atDocumentStart, forMainFrameOnly: true)
        self.webView?.configuration.userContentController.addUserScript(bridgeScript)
        
    }
    
    public func addBridgeModule(id: String, moduleCreateBlock: @escaping ModuleCreateBlock) {
        moduleDist[id] = moduleCreateBlock;
    }
    
    public func cleanBridgeModule() {
        moduleDist.removeAll()
    }
    
    public func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        let name = message.name
        if(name != EXPORT_ID) {
            return
        }
        
        let payload = JSON(message.body)
        
        if(!payload["handleId"].exists()) {
            return
        }
        if(!payload["data"].exists()) {
            return
        }
        
        let handleId = payload["handleId"].string
        let data = payload["data"]
        
        if(!data["module"].exists()) {
            return
        }
        if(!data["method"].exists()) {
            return
        }
        
        let moduleName = data["module"].string
        let methodName = data["method"].string
        let args: JSON = data["args"];
        
        let moduleBlock: ModuleCreateBlock? = moduleDist[moduleName!]
        
        if(moduleBlock == nil) {
            return
        }
        
        let module = moduleBlock!()
        module.setBridgeManager(bridgeMgr: self)
        module.exec(method: methodName!, handleId: handleId!, data: args)
    }
    
    public func callback(handleId: String, data: JSON?) {
        var script: String;
        if(data != nil) {
            let dataStr: String = data!.rawString(String.Encoding.utf8, options: [])!
            script = "window.\(EXPORT_ID).onNativeMessage('\(handleId)', \(dataStr))"
        } else {
            script = "window.\(EXPORT_ID).onNativeMessage('\(handleId)')"
        }
        webView?.evaluateJavaScript(script, completionHandler: nil);
    }
    
    
    
}


let jsSource = [
    
"(function() {",
"var idSeed = 1",
"var handles = {}",

"function genHandleId() {",
"    return 'BridgeHandle_' + (idSeed++)",
"}",

"var isAndroid",
"var isLowLevelBridge",
"var ua = window.navigator.userAgent",
"if(window.mingoAndroidJSBridge) {",
"    isAndroid = true",
"    isLowLevelBridge = /mingoJSBridge\\/LowLevel/.test(ua)",
"}",

"let JSBridge = window.mingoJSBridge = {",
    
"    onNativeMessage: function(handleId, data) {",
"        var callback = handles[handleId]",
"        callback && callback(data)",
"        delete handles[handleId]",
"    },",
    
"    callNative: function(data, callback) {",
"        var handleId = genHandleId()",
"        if(callback) {",
"            handles[handleId] = callback",
"        }",
"        if(isAndroid) {",
"            var messageStr = JSON.stringify({",
"                handleId: handleId,",
"                data: data",
"            })",
"            if(isLowLevelBridge) {",
"                window.prompt('mingoJSBridge://' + messageStr)",
"            } else {",
"                window.mingoAndroidJSBridge.exec(messageStr)",
"            }",
"        } else {",
"            window.webkit.messageHandlers.mingoJSBridge.postMessage({",
"                handleId: handleId,",
"                data: data",
"            })",
"        }",
"    }",
"}",

"if(!window.JSBridge) {",
"    window.JSBridge = JSBridge",
"}",

"})();",
]
