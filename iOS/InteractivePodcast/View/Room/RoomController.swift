//
//  RoomController.swift
//  InteractivePodcast
//
//  Created by XUCH on 2021/3/6.
//

import Foundation
import UIKit
import RxSwift
import RxCocoa
import IGListKit
import HyphenateChat
import DanmakuKit

class RoomController: BaseViewContoller, DialogDelegate, RoomDelegate {
    
    @IBOutlet weak var containerView: UIView! {
        didSet {
            containerView.roundCorners([.topLeft, .topRight], radius: 10)
        }
    }

    @IBOutlet weak var listenerView: UICollectionView! {
        didSet {
            listenerView.collectionViewLayout = UICollectionViewFlowLayout()
            listenerView.alwaysBounceHorizontal = true
        }
    }
    
    @IBOutlet weak var listView: UICollectionView! {
        didSet {
            listView.collectionViewLayout = UICollectionViewFlowLayout()
            listView.alwaysBounceVertical = true
        }
    }
    @IBOutlet weak var chatMessageList: UITableView! {
        didSet {
            chatMessageList.register(ChatView.classForCoder(), forCellReuseIdentifier: ChatView.reuseId)
            chatMessageList.separatorStyle = .none
            chatMessageList.delegate = self
            chatMessageList.dataSource = self
        }
    }
    
    @IBOutlet var danmakuView: DanmakuView! {
        didSet {
            danmakuView.isOverlap = true
            danmakuView.enableFloatingDanmaku = true
        }
    }
    
    @IBOutlet var bottomConstant: NSLayoutConstraint!
    
    private var danmakus: [AnyObject & DanmakuCellModel & DanmakuCellModelProtocal] = []
    @IBOutlet var tapReturn: UITapGestureRecognizer!
    @IBOutlet var moreAction: UITapGestureRecognizer!
    
    @IBOutlet weak var toolbarView: UIView!
    private var roomManagerToolbar: RoomManagerToolbar?
    private var roomSpeakerToolbar: RoomSpeakerToolbar?
    private var roomListenerToolbar: RoomListenerToolbar?
        
    var leaveAction: ((LeaveRoomAction, Room?) -> Void)? = nil
    private lazy var adapter: ListAdapter = {
        return ListAdapter(updater: ListAdapterUpdater(), viewController: self)
    }()
    
    private lazy var listenerAdapter: ListAdapter = {
        return ListAdapter(updater: ListAdapterUpdater(), viewController: self)
    }()

    private var dataSourceDisposable: Disposable? = nil
    private var actionDisposable: Disposable? = nil
    private let chatDisposeBag = DisposeBag()
    var viewModel: RoomViewModel = RoomViewModel()

    private func renderToolbar() {
        switch viewModel.role {
        case .manager:
            if (roomManagerToolbar == nil) {
                roomManagerToolbar = RoomManagerToolbar()
                toolbarView.addSubview(roomManagerToolbar!)
                roomManagerToolbar!.fill(view: toolbarView).active()
                roomManagerToolbar!.delegate = self
                
                if (roomSpeakerToolbar != nil || roomListenerToolbar != nil) {
                    roomSpeakerToolbar?.removeFromSuperview()
                    roomSpeakerToolbar = nil
                    roomListenerToolbar?.removeFromSuperview()
                    roomListenerToolbar = nil
                }
                
                roomManagerToolbar?.subcribeUIEvent()
                roomManagerToolbar?.subcribeRoomEvent()
            }
        case .speaker:
            if (roomSpeakerToolbar == nil) {
                roomSpeakerToolbar = RoomSpeakerToolbar()
                toolbarView.addSubview(roomSpeakerToolbar!)
                roomSpeakerToolbar!.fill(view: toolbarView).active()
                roomSpeakerToolbar!.delegate = self
                
                if (roomManagerToolbar != nil || roomListenerToolbar != nil) {
                    roomManagerToolbar?.removeFromSuperview()
                    roomManagerToolbar = nil
                    roomListenerToolbar?.removeFromSuperview()
                    roomListenerToolbar = nil
                }
                
                roomSpeakerToolbar?.subcribeUIEvent()
                roomSpeakerToolbar?.subcribeRoomEvent()
            }
        case .listener:
            if (roomListenerToolbar == nil) {
                roomListenerToolbar = RoomListenerToolbar()
                toolbarView.addSubview(roomListenerToolbar!)
                roomListenerToolbar!.fill(view: toolbarView).active()
                roomListenerToolbar!.delegate = self
                if (roomSpeakerToolbar != nil || roomManagerToolbar != nil) {
                    roomSpeakerToolbar?.removeFromSuperview()
                    roomSpeakerToolbar = nil
                    roomManagerToolbar?.removeFromSuperview()
                    roomManagerToolbar = nil
                    show(message: "You've been set as audience".localized, type: .error)
                }
                
                roomListenerToolbar?.subcribeUIEvent()
                roomListenerToolbar?.subcribeRoomEvent()
            }
        }
    }
    
    private func subcribeUIEvent() {
//        meButton.rx.tap
//            .throttle(RxTimeInterval.seconds(2), scheduler: MainScheduler.instance)
//            .subscribe(onNext: { [unowned self] _ in
//                Logger.log(message: "pushViewController \(self.navigationController != nil)", level: .info)
//                self.navigationController?.pushViewController(
//                    MeController.instance(),
//                    animated: true
//                )
//            })
//            .disposed(by: disposeBag)
        
        tapReturn.rx.event
            .throttle(RxTimeInterval.seconds(2), scheduler: MainScheduler.instance)
            .flatMap { [unowned self] _ in
                return self.viewModel.leaveRoom(action: .mini)
            }
            .filter { [unowned self] result in
                if (!result.success) {
                    self.show(message: result.message ?? "unknown error".localized, type: .error)
                }
                return result.success
            }
            .flatMap { [unowned self] result in
                return self.pop()
            }
            .subscribe(onNext: { [unowned self] _ in
                self.leaveAction?(.mini, self.viewModel.room)
            })
            .disposed(by: disposeBag)
        
        moreAction.rx.event.throttle(RxTimeInterval.seconds(2), scheduler: MainScheduler.instance).subscribe(onNext: { [unowned self] _ in
            MoreActionDialog().show(delegate: self)
        })
        .disposed(by: disposeBag)
        
//        moreAction.rx.event
//            .throttle(RxTimeInterval.seconds(2), scheduler: MainScheduler.instance)
//            .concatMap { [unowned self] _ -> Observable<Bool> in
//                if (self.viewModel.isManager) {
//                    return self.showAlert(title: "Close room".localized, message: "Leaving the room ends the session and removes everyone".localized)
//                } else {
//                    return Observable.just(true)
//                }
//            }
//            .filter { close in
//                return close
//            }
//            .concatMap { [unowned self] _ in
//                return self.viewModel.leaveRoom(action: .closeRoom)
//            }
//            .filter { [unowned self] result in
//                if (!result.success) {
//                    self.show(message: result.message ?? "unknown error".localized, type: .error)
//                }
//                return result.success
//            }
//            .concatMap { [unowned self] _ in
//                return self.pop()
//            }
//            .subscribe(onNext: { [unowned self] _ in
//                self.leaveAction?(.closeRoom, self.viewModel.room)
//            })
//            .disposed(by: disposeBag)
        NotificationCenter.default.addObserver(self, selector: #selector(keybordWillShow), name: UIResponder.keyboardDidShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keybordWillShow), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keybordWillHide), name: UIResponder.keyboardWillHideNotification, object: nil)
        
    }
    @objc func keybordWillShow(_ notification: NSNotification) {
        if let keyboardSize = (notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue {
            let keyboardHeight : CGFloat = CGFloat(keyboardSize.height)
            print(keyboardHeight)
            self.bottomConstant.constant = keyboardHeight - self.view.safeAreaInsets.bottom + 10
        }
    }
    @objc func keybordWillHide(_ notification: NSNotification) {
        if let keyboardSize = (notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue {
            let keyboardHeight : CGFloat = CGFloat(keyboardSize.height)
            print(keyboardHeight)
            self.bottomConstant.constant = 10
        }
    }
    private func subcribeRoomEvent() {
        dataSourceDisposable?.dispose()
        dataSourceDisposable = viewModel.roomMembersDataSource()
            .observe(on: MainScheduler.instance)
            .flatMap { [unowned self] result -> Observable<Result<Bool>> in
                let roomClosed = result.data
                if (roomClosed == true) {
                    return self.viewModel.leaveRoom(action: .leave).map { _ in result }
                } else {
                    return Observable.just(result)
                }
            }
            .observe(on: MainScheduler.instance)
            .flatMap { [unowned self] result -> Observable<Result<Bool>> in
                if (result.data == true) {
                    Logger.log(message: "subcribeRoomEvent roomClosed", level: .info)
                    return self.popAsObservable().map { _ in result }
                } else {
                    self.adapter.performUpdates(animated: false)
                    self.listenerAdapter.performUpdates(animated: false)
                    return Observable.just(result)
                }
            }
            .subscribe(onNext: { [unowned self] result in
                let roomClosed = result.data
                if (!result.success) {
//                    self.show(message: result.message ?? "unknown error".localized, type: .error)
                } else if (roomClosed == true) {
                    self.leaveAction?(.leave, self.viewModel.room)
                    self.disconnect()
                } else {
                    self.renderToolbar()
                }
            })

        actionDisposable = viewModel.actionsSource()
            .observe(on: MainScheduler.instance)
            .subscribe(onNext: { [unowned self] result in
                switch self.viewModel.role {
                case .manager:
                    roomManagerToolbar?.onReceivedAction(result)
                case .speaker:
                    roomSpeakerToolbar?.onReceivedAction(result)
                case .listener:
                    roomListenerToolbar?.onReceivedAction(result)
                }
            })
    }
    
    func disconnect() {
        dataSourceDisposable?.dispose()
        dataSourceDisposable = nil
        actionDisposable?.dispose()
        actionDisposable = nil
    }
    
    private func popAsObservable() -> Observable<Bool> {
        return super.pop().asObservable()
    }
    
    override func pop() -> Single<Bool> {
        disconnect()
        return super.pop()
    }
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesBegan(touches, with: event)
        self.view.endEditing(true)
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        danmakuView.delegate = self
        EMClient.shared().chatManager?.add(self, delegateQueue: nil)
        adapter.collectionView = listView
        adapter.dataSource = self
        listenerAdapter.collectionView = listenerView
        listenerAdapter.dataSource = self
//        roomNameView.text = viewModel.room.channelName
//        closeButton.isHidden = !viewModel.isManager
        
        renderToolbar()
        subcribeUIEvent()
        subcribeRoomEvent()
        viewModel.messages.subscribe(onNext: {[weak self] values in
            guard let self = self else { return }
            self.chatMessageList.reloadData()
            if values.count > 0 {
                self.chatMessageList.scrollToRow(at: IndexPath(row: values.count - 1, section: 0), at: .bottom, animated: true)
            }
        }).disposed(by: chatDisposeBag)
        viewModel.loadMessage()
        danmakuView.play()
    }
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        danmakuView.recaculateTracks()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        self.navigationController?.interactivePopGestureRecognizer?.delegate = self
    }
    
    static func instance(leaveAction: @escaping ((LeaveRoomAction, Room?) -> Void)) -> RoomController {
        let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
        let controller = storyBoard.instantiateViewController(withIdentifier: "RoomController") as! RoomController
        controller.leaveAction = leaveAction
        controller.modalPresentationStyle = .fullScreen
        return controller
    }
    
    func sendMessage(text: String, isDanmu: Bool) -> Observable<Result<Bool>> {
        return ChatServer.shared().sendMessage(groupId: viewModel.room.chatGroupId, text: text, isDanMu: isDanmu, name: viewModel.account.name).do { result in
            let message = Message(message: text, from: self.viewModel.account.id, name: self.viewModel.account.name)
            var arr = self.viewModel.messages.value
            arr.append(message)
            self.viewModel.messages.accept(arr)
            if isDanmu {
                self.sendCommonDanmaku(text: "\(self.viewModel.account.name): \(text)")
            }
        }
    }
    func sendCommonDanmaku(text: String) {
        let cellModel = DanmakuTextCellModel(json: nil)
        cellModel.displayTime = 8
        cellModel.text = text
        cellModel.identifier = String(arc4random())
        cellModel.calculateSize()
        cellModel.type = .floating
        danmakuView.shoot(danmaku: cellModel)
        danmakus.append(cellModel)
    }
    func randomIntNumber(lower: Int = 0,upper: Int = Int(UInt32.max)) -> Int {
        return lower + Int(arc4random_uniform(UInt32(upper - lower)))
    }
}

extension RoomController: DanmakuViewDelegate {
    
    func danmakuView(_ danmakuView: DanmakuView, didEndDisplaying danmaku: DanmakuCell) {
        guard let model = danmaku.model else { return }
        danmakus.removeAll { (cm) -> Bool in
            return cm.isEqual(to: model)
        }
    }
    
    func danmakuView(_ danmakuView: DanmakuView, didTapped danmaku: DanmakuCell) {
        guard var cellModel = danmaku.model as? (DanmakuCellModel & DanmakuCellModelProtocal) else { return }
        if let cm = danmaku.model as? DanmakuTextCellModel {
            print("tap %@ at tarck %d", cm.text, cm.track ?? 0)
        }
        if cellModel.isPause {
            danmakuView.play(cellModel)
            cellModel.isPause = false
        } else {
            danmakuView.pause(cellModel)
            cellModel.isPause = true
        }
    }
    
}


protocol RoomControlDelegate: class {
    func onTap(member: Member)
    func onTapInInvite()
}

extension RoomController: RoomControlDelegate {
    func onTap(member: Member) {
        if (viewModel.isManager) {
            if (!member.isSpeaker) {
                InviteSpeakerDialog().show(with: member, delegate: self)
            } else if (member.id != viewModel.member.id) {
                ManageSpeakerDialog().show(with: member, delegate: self)
            } else {
                // block self?
                ManageSpeakerDialog().show(with: member, delegate: self)
            }
        }
    }
    func onTapInInvite() {
        if (viewModel.isManager) {
//            if (!member.isSpeaker) {
//                InviteSpeakerDialog().show(with: member, delegate: self)
//            } else if (member.id != viewModel.member.id) {
//                ManageSpeakerDialog().show(with: member, delegate: self)
//            } else {
//                // block self?
//                ManageSpeakerDialog().show(with: member, delegate: self)
//            }
        }
    }
}

extension RoomController: ListAdapterDataSource {
    func objects(for listAdapter: ListAdapter) -> [ListDiffable] {
        if listAdapter == adapter {
            return viewModel.memberList as! [ListDiffable]
        }
        return viewModel.listenerList as! [ListDiffable]
    }
    
    func listAdapter(_ listAdapter: ListAdapter, sectionControllerFor object: Any) -> ListSectionController {
        if listAdapter == adapter {
            return SpeakersController(delegate: self)
        }
        return ListenersController(delegate: self)
    }
    
    func emptyView(for listAdapter: ListAdapter) -> UIView? {
        return nil
    }
}

extension RoomController: UIGestureRecognizerDelegate {
    func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
        false
    }
}


extension RoomController: UITableViewDelegate,UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return viewModel.messages.value.count
    }
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: ChatView.reuseId, for: indexPath)
        if let chatCell = cell as? ChatView {
            let message = viewModel.messages.value[indexPath.row]
            chatCell.setup(name: message.name, message: message.message, isOwner: message.from == viewModel.account.id)
        }
        return cell
    }
    func scrollViewWillBeginDragging(_ scrollView: UIScrollView) {
        self.view.endEditing(true)
    }
}


extension RoomController: EMChatManagerDelegate {
    func messagesDidReceive(_ aMessages: [Any]!) {
        for message in aMessages {
            if let eMessage = message as? EMChatMessage {
                let msgBody = eMessage.body
                if eMessage.to == viewModel.room.chatGroupId {
                    if let textMessage = msgBody as? EMTextMessageBody {
                        var arr = viewModel.messages.value
                        arr.append(Message(message: textMessage.text, from: eMessage.from, name: eMessage.ext?["name"] as? String ?? ""))
                        viewModel.messages.accept(arr)
                    }
                    if eMessage.ext?["type"] as! String == "danmu" {
                        if let textMessage = msgBody as? EMTextMessageBody {
                            sendCommonDanmaku(text: "\( eMessage.ext?["name"] as? String ?? ""): \(textMessage.text)")
                        }
                    }
                }
            }
        }
    }
    func cmdMessagesDidReceive(_ aCmdMessages: [Any]!) {
        for message in aCmdMessages {
            if let eMessage = message as? EMChatMessage {
                let msgBody = eMessage.body
                if eMessage.to == viewModel.room.chatGroupId {
                    if let textMessage = msgBody as? EMTextMessageBody {
                        var arr = viewModel.messages.value
                        arr.append(Message(message: textMessage.text, from: eMessage.from, name: eMessage.ext?["name"] as? String ?? ""))
                        viewModel.messages.accept(arr)
                    }
                    if eMessage.ext?["type"] as! String == "danmu" {
                        if let textMessage = msgBody as? EMTextMessageBody {
                            sendCommonDanmaku(text: "\( eMessage.ext?["name"] as? String ?? ""): \(textMessage.text)")
                        }
                    }
                }
            }
        }
    }
    
}
