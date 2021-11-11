//
//  NimServer.swift
//  InteractivePodcast_LeanCloud
//
//  Created by zliu on 2021/10/20.
//

import Foundation
import HyphenateChat
import RxSwift
import CloudKit


class ChatServer: NSObject {
    fileprivate static let instance = ChatServer()
    static func shared() -> ChatServer {
        return instance
    }
    func register(_ id: String) -> Observable<Result<Bool>> {
        return Observable.create({ observable -> Disposable in
            EMClient.shared().register(withUsername: id, password: "password")  { name, error in
                if error == nil || error?.code == EMErrorUserAlreadyExist {
                    observable.onNext(Result(success: true, data: true))
                } else {
                    observable.onNext(Result(success: false, message: error?.errorDescription))
                }
                observable.onCompleted()
            }
            return Disposables.create()
        })
    }
    
    func registerAndLogin(_ id: String) -> Observable<Result<Bool>> {
        return register(id).flatMap { result in
            return result.onSuccess {
                self.login(id)
            }
        }
    }
    func login(_ id: String) -> Observable<Result<Bool>> {
        EMClient.shared().logout(true)
        return Observable.create({ observable -> Disposable in
            EMClient.shared().login(withUsername: id, password: "password") { name, error in
                if error == nil || error?.code == EMErrorUserAlreadyLoginSame {
                    observable.onNext(Result(success: true, data: true))
                } else {
                    observable.onNext(Result(success: false, message: error?.errorDescription))
                }
                observable.onCompleted()
            }
            return Disposables.create()
        })
    }
    
    func createGroupChat(chatName: String) -> Observable<Result<String>> {
        let setting = EMGroupOptions()
        setting.style = EMGroupStylePublicOpenJoin
        
        return Observable.create({ observable -> Disposable in
            EMClient.shared().groupManager.createGroup(withSubject: chatName, description: "", invitees: [], message: "", setting: setting) { gp, error in
                if let group = gp {
                    observable.onNext(Result(success: true, data: group.groupId, message: ""))
                } else {
                    observable.onNext(Result(success: false, message: error?.errorDescription))
                }
                observable.onCompleted()
            }
            return Disposables.create()
        })
    }
    
    func joinGroup(groupId: String) -> Observable<Result<Bool>> {
        return Observable.create { observable -> Disposable in
            EMClient.shared().groupManager.joinPublicGroup(groupId) { aGroup, aError in
                if aError == nil || aError?.code == EMErrorGroupAlreadyJoined {
                    observable.onNext(Result(success: true, data: true))
                } else {
                    observable.onNext(Result(success: false, message: aError?.errorDescription))
                }
            }
            return Disposables.create()
        }
    }
    func quitGroup(groupId: String) -> Observable<Result<Bool>> {
        return Observable.create { observable -> Disposable in
            EMClient.shared().groupManager.leaveGroup(groupId) { aError in
                if aError == nil {
                    observable.onNext(Result(success: true, data: true))
                } else {
                    observable.onNext(Result(success: false, message: aError?.errorDescription))
                }
            }
            return Disposables.create()
        }
    }
    func sendMessage(groupId: String, text: String, isDanMu: Bool, name: String) -> Observable<Result<Bool>> {
        let messageBody = EMTextMessageBody(text: text)
        let messageExt = ["type": isDanMu ? "danmu" : "message","name": name];
        let conversation = EMClient.shared().chatManager.getConversation(groupId, type: EMConversationTypeGroupChat, createIfNotExist: true)
        
        let message = EMMessage(conversationID: conversation?.conversationId ?? groupId, from: EMClient.shared().currentUsername, to: conversation?.conversationId ?? groupId, body: messageBody, ext: messageExt)
        message?.chatType = EMChatTypeGroupChat
        return Observable.create { observable -> Disposable in
            EMClient.shared().chatManager.send(message, progress: nil) { aMessage, aError in
                if aError == nil {
                    observable.onNext(Result(success: true, data: true))
                } else {
                    observable.onNext(Result(success: false, message: aError?.errorDescription))
                }
                observable.onCompleted()
            }
            return Disposables.create()
        }
    }
}
