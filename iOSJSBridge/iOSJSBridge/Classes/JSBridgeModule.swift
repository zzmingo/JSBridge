//
//  UNBridgeModule.swift
//  UNWebView
//
//  Created by 张政明 on 2016/12/19.
//  Copyright © 2016年 uneed.com. All rights reserved.
//

import Foundation
import SwiftyJSON

public class Arguments : NSObject {
    public var json: JSON?
}

open class JSBridgeModule : NSObject {
    
    var bridgeMgr: JSBridgeManager?
    
    public override init() {
        super.init()
    }
    
    public func setBridgeManager(bridgeMgr: JSBridgeManager) {
        self.bridgeMgr = bridgeMgr
    }
    
    public func exec(method: String, handleId: String, data: JSON) {
        let selector = Selector(method + "::")
        if self.responds(to: selector) {
            let args = Arguments()
            args.json = data
            self.perform(selector, with: handleId, with: args)
        }
    }
    
    private func callback(_ handleId: String, data: JSON?) {
        bridgeMgr?.callback(handleId: handleId, data: data)
    }
    
    public func callbackOK(_ handleId: String, data: JSON?) {
        let resp = JSON([
            "ec": 0,
            "data": data!
        ])
        callback(handleId, data: resp)
    }
    
    public func callbackFail(_ handleId: String, ec: Int, em: String) {
        callback(handleId, data: JSON([
            "ec": ec,
            "em": em
        ]))
    }
    
}
