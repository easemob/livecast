//
//  RoomSpeakerToolbar.swift
//  InteractivePodcast
//
//  Created by XUCH on 2021/3/11.
//

import Foundation
import UIKit
import RxSwift

class RoomSpeakerToolbar: UIView {
    weak var delegate: RoomController! {
        didSet {
            messageView.sendMessageCallBack = delegate.sendMessage
        }
    }
    let disposeBag = DisposeBag()
    
//    var returnView: IconButton = {
//       let view = IconButton()
//        view.icon = "iconExit"
//        view.label = "Leave quietly".localized
//        return view
//    }()
    
    lazy var messageView: MessageView = {
        let view = MessageView()
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
        addSubview(onMicView)
        
        onMicView.height(constant: 36)
            .marginTrailing(anchor: trailingAnchor, constant: 16)
            .marginBottom(anchor: bottomAnchor)
            .active()
        
        messageView.marginTop(anchor: topAnchor)
            .marginLeading(anchor: leadingAnchor, constant: 16)
            .marginTrailing(anchor: onMicView.leadingAnchor,constant: 16)
            .marginBottom(anchor: bottomAnchor)
            .active()
//        returnView.height(constant: 36)
//            .marginLeading(anchor: leadingAnchor, constant: 16)
//            .centerY(anchor: centerYAnchor)
//            //.marginTrailing(anchor: handsupNoticeView.leadingAnchor, constant: 16, relation: .greaterOrEqual)
//            .active()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
    }
    
    func subcribeUIEvent() {
//        returnView.rx.tap
//            .throttle(RxTimeInterval.seconds(2), scheduler: MainScheduler.instance)
//            .flatMap { [unowned self] _ in
//                self.delegate.viewModel.leaveRoom(action: .leave)
//            }
//            .filter { [unowned self] result in
//                if (!result.success) {
//                    self.delegate.show(message: result.message ?? "unknown error".localized, type: .error)
//                }
//                return result.success
//            }
//            .observe(on: MainScheduler.instance)
//            .flatMap { [unowned self] _ in
//                self.delegate.pop()
//            }
//            .subscribe(onNext: { [unowned self] result in
//                self.delegate.leaveAction?(.leave, nil)
//            })
//            .disposed(by: disposeBag)
        
        onMicView.rx.tap
            .debounce(RxTimeInterval.microseconds(300), scheduler: MainScheduler.instance)
            .throttle(RxTimeInterval.seconds(1), scheduler: MainScheduler.instance)
            .filter {
                return !self.delegate.viewModel.member.isMuted
            }
            .flatMap { [unowned self] _ in
                return self.delegate.viewModel.selfMute(mute: !self.delegate.viewModel.muted())
            }
            .subscribe(onNext: { [unowned self] result in
                if (!result.success) {
                    self.delegate.show(message: result.message ?? "unknown error".localized, type: .error)
                }
            })
            .disposed(by: disposeBag)
        
        self.delegate.viewModel.isMuted
            .startWith(self.delegate.viewModel.muted())
            .distinctUntilChanged()
            .observe(on: MainScheduler.instance)
            .subscribe(onNext: { [unowned self] muted in
                self.isMuted = muted
                if (self.delegate.viewModel.member.isMuted) {
                    self.delegate.show(message: "You've been muted".localized, type: .error)
                }
            })
            .disposed(by: disposeBag)
        
        self.delegate.viewModel.syncLocalUIStatus()
    }
    
    func onReceivedAction(_ result: Result<Action>) {}
    func subcribeRoomEvent() {}
}
