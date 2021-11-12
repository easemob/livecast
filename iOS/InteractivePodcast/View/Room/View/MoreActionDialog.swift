//
//  InvitedDialog.swift
//  InteractivePodcast
//
//  Created by XC on 2021/3/13.
//

import Foundation
import UIKit
import RxSwift

class MoreActionDialog: Dialog {
    weak var delegate: RoomController!
    
    var returnView: UIButton = {
       let view = UIButton()
        view.setImage(UIImage(named: "leave"), for: .normal)
        return view
    }()
    var label: UILabel = {
        let label = UILabel()
        label.text = "退出"
        label.textAlignment = .center
        label.font = UIFont.systemFont(ofSize: 13)
        label.textColor = UIColor(hex: Colors.White)
        return label
    }()
    
    var bgView: UIView = {
        let view = UIView()
        return view
     }()
    
    override func setup() {
        addSubview(bgView)
        bgView.addSubview(returnView)
        bgView.addSubview(label)
        
        self.width(constant: UIScreen.main.bounds.width)
            .height(constant: 224)
            .active()
        
        returnView.marginTop(anchor: bgView.topAnchor, constant: 28)
            .marginLeading(anchor: bgView.leadingAnchor, constant: 22)
            .height(constant: 48)
            .width(constant: 48)
            .active()
        label.marginTop(anchor: returnView.bottomAnchor, constant: 6)
            .marginLeading(anchor: returnView.leadingAnchor)
            .centerX(anchor: returnView.centerXAnchor)
            .active()
        bgView.marginTop(anchor: topAnchor)
            .marginLeading(anchor: leadingAnchor)
            .width(constant: UIScreen.main.bounds.width)
            .height(constant: 224)
            .active()

        returnView.rx.tap
            .throttle(RxTimeInterval.seconds(2), scheduler: MainScheduler.instance)
            .flatMap { [unowned self] _ in
                self.delegate.viewModel.leaveRoom(action: .leave)
            }
            .filter { [unowned self] result in
                if (!result.success) {
//                    self.delegate.show(message: result.message ?? "unknown error".localized, type: .error)
                }
                return result.success
            }
            .observe(on: MainScheduler.instance)
            .flatMap { [unowned self] _ in
                self.delegate.pop()
            }
            .subscribe(onNext: { [unowned self] result in
                self.delegate.leaveAction?(.leave, self.delegate.viewModel.isManager ? self.delegate.viewModel.room : nil)
                self.dismiss(controller: self.delegate)
            })
            .disposed(by: disposeBag)

        bgView.backgroundColor = UIColor(hex: "#202D3B")
        self.isUserInteractionEnabled = true
    }

    override func render() {
        shadow()
    }
    
    func show(delegate: RoomController) {
        self.delegate = delegate
        self.show(controller: delegate, style: .top, padding: 0)
    }
}
