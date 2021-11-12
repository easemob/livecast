//
//  Config.swift
//  InteractivePodcast
//
//  Created by XUCH on 2021/3/3.
//

import Foundation
import AgoraRtcKit

struct BuildConfig {
    static let AppId = ""
    static let Token: String? = nil
    
    static let LeanCloudAppId = ""
    static let LeanCloudAppKey = ""
    static let LeanCloudServerUrl = ""
    
    static var PrivacyPolicy: String {
        if (Utils.getCurrentLanguage() == "cn") {
            return "https://www.easemob.com/protocol"
        } else {
            return "https://www.easemob.com/protocol"
        }
    }
    static var SignupUrl: String {
        if (Utils.getCurrentLanguage() == "cn") {
            return "https://console.easemob.com/user/register"
        } else {
            return "https://console.easemob.com/user/register"
        }
    }
    static let PublishTime = "2021.9.30"
    static let SdkVersion = "3.8.5"
    static var AppVersion: String? {
        return Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String
    }
}
