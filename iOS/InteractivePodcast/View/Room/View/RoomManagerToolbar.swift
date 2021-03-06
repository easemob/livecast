//
//  RoomManagerToolBar.swift
//  InteractivePodcast
//
//  Created by XUCH on 2021/3/11.
//

import Foundation
import UIKit
import RxSwift

class RoomManagerToolbar: UIView {
    weak var delegate: RoomController! {
        didSet {
            messageView.sendMessageCallBack = delegate.sendMessage
        }
    }
    let disposeBag = DisposeBag()
 
    lazy var messageView: MessageView = {
        let view = MessageView()
        return view
    }()
    
    
    var returnView: IconButton = {
       let view = IconButton()
        view.icon = "iconExit"
        view.label = "Leave quietly".localized
        return view
    }()
    
    var handsupNoticeView: IconButton = {
       let view = IconButton()
        view.color = "#C2A679"
        view.icon = "iconUpNotice"
        return view
    }()
    
    var onMicView: IconButton = {
       let view = IconButton()
        view.color = "#C2A679"
        view.icon = "iconMicOn"
        return view
    }()
    
    var isMuted: Bool = false {
        didSet {
            onMicView.icon = isMuted ? "redMic" : "iconMicOn"
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .clear
        addSubview(messageView)
//        addSubview(returnView)
        addSubview(handsupNoticeView)
        addSubview(onMicView)
        
        onMicView.height(constant: 36)
            .marginTrailing(anchor: trailingAnchor, constant: 16)
            .marginBottom(anchor: bottomAnchor)
            .active()
        
        handsupNoticeView.height(constant: 36)
            .marginTrailing(anchor: onMicView.leadingAnchor, constant: 16)
            .marginBottom(anchor: bottomAnchor)
            .active()
        
        messageView.marginTop(anchor: topAnchor)
            .marginLeading(anchor: leadingAnchor, constant: 16)
            .marginTrailing(anchor: handsupNoticeView.leadingAnchor,constant: 16)
            .marginBottom(anchor: bottomAnchor)
            .active()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
    }
    
    func subcribeUIEvent() {
        returnView.rx.tap
            .throttle(RxTimeInterval.seconds(2), scheduler: MainScheduler.instance)
            .concatMap { [unowned self] _ in
                return self.delegate.showAlert(title: "Leave room".localized, message: "Leaving the room ends the session and removes everyone".localized)
            }
            .filter { close in
                return close
            }
            .flatMap { [unowned self] _ in
                return self.delegate.viewModel.leaveRoom(action: .leave)
            }
            .filter { [unowned self] result in
                if (!result.success) {
                    self.delegate.show(message: result.message ?? "unknown error".localized, type: .error)
                }
                return result.success
            }
            .observe(on: MainScheduler.instance)
            .flatMap { [unowned self] _ in
                return self.delegate.pop()
            }
            .subscribe(onNext: { [unowned self] _ in
                self.delegate.leaveAction?(.leave, self.delegate.viewModel.isManager ? self.delegate.viewModel.room : nil)
            })
            .disposed(by: disposeBag)
        
        onMicView.rx.tap
            .debounce(RxTimeInterval.microseconds(300), scheduler: MainScheduler.instance)
            .throttle(RxTimeInterval.seconds(1), scheduler: MainScheduler.instance)
            .flatMapLatest { [unowned self] _ in
                return self.delegate.viewModel.selfMute(mute: !self.delegate.viewModel.muted())
            }
            .subscribe(onNext: { [unowned self] result in
                if (!result.success) {
                    self.delegate.show(message: result.message ?? "unknown error".localized, type: .error)
                }
            })
            .disposed(by: disposeBag)
        
        handsupNoticeView.rx.tap
            .throttle(RxTimeInterval.seconds(2), scheduler: MainScheduler.instance)
            .subscribe(onNext: { [unowned self] _ in
                HandsupListDialog().show(delegate: self.delegate)
            })
            .disposed(by: disposeBag)
        
        self.delegate.viewModel.isMuted
            .startWith(self.delegate.viewModel.muted())
            .distinctUntilChanged()
            .observe(on: MainScheduler.instance)
            .subscribe(onNext: { [unowned self] muted in
                self.isMuted = muted
            })
            .disposed(by: disposeBag)
        
        self.delegate.viewModel.syncLocalUIStatus()
    }
    
    func onReceivedAction(_ result: Result<Action>) {
        if (!result.success) {
            Logger.log(message: result.message ?? "unknown error".localized, level: .error)
        } else {
            if let action = result.data {
                switch action.action {
                case .invite:
                    if (action.status == .refuse) {
                        self.delegate.show(message: "\(action.member.user.name) \("declines your request".localized)", type: .error)
                    }
                default:
                    Logger.log(message: "\(action.member.user.name)", level: .info)
                }
            }
        }
    }
    
    func subcribeRoomEvent() {
        self.delegate.viewModel.onHandsupListChange
            .observe(on: MainScheduler.instance)
            .subscribe(onNext: { [unowned self] list in
                self.handsupNoticeView.count = list.count
            })
            .disposed(by: disposeBag)
    }
}
