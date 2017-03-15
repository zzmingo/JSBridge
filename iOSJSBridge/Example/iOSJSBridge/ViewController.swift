//
//  ViewController.swift
//  iOSJSBridge
//
//  Created by mingo on 03/15/2017.
//  Copyright (c) 2017 mingo. All rights reserved.
//

import UIKit
import WebKit
import SwiftyJSON
import iOSJSBridge

class ViewController : UIViewController {

    var webview:WKWebView!
    var bridgeMgr:JSBridgeManager!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        webview = WKWebView(frame: self.view.bounds)
        self.view = webview
        
        bridgeMgr = JSBridgeManager(webview)
        bridgeMgr.addBridgeModule(id: "Example", moduleCreateBlock: { return ExampleModule() })
        
        webview.loadHTMLString([
            "<script>",
            "JSBridge.callNative({",
                "'module': 'Example',",
                "'method': 'log',",
                "'args': {",
                "   'message': 'call example.log'",
                "}",
            "})",
            "</script>"
        ].joined(), baseURL: nil)
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

}

class ExampleModule : JSBridgeModule {
    
    let value = 100
    
    public func log(_ handleId:String, _ args:Arguments) {
        print(args.json?["message"].string ?? "")
    }
    
    public func getSomeValue(_ handleId:String, _ args:Arguments) {
        callbackOK(handleId, data: JSON(value))
    }
    
    public func getSomeValueFail(_ handleId:String, _ args:Arguments) {
        callbackFail(handleId, ec: 404, em: "Can't found a value")
    }
    
}

