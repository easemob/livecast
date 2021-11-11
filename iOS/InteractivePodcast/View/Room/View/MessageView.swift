//
//  MessageView.swift
//  InteractivePodcast
//
//  Created by zliu on 2021/10/21.
//

import Foundation
import UIKit
import RxSwift
import NextGrowingTextView

class MessageView: UIView {
    let backgroundView: UIView = {
        let view = UIView()
        view.translatesAutoresizingMaskIntoConstraints = false
        view.rounded(color: "#C2A679", borderWidth: 1, radius: 18)
        view.backgroundColor = UIColor(hex: "#202D3B")
        return view
    }()
    let messageIcon: UIImageView = {
        let view = UIImageView()
        view.translatesAutoresizingMaskIntoConstraints = false
        view.image = UIImage(named: "message")
        return view
    }()
    let input: NextGrowingTextView = {
        let view = NextGrowingTextView()
        view.textView.returnKeyType = .send
        view.textView.font = UIFont.systemFont(ofSize: 14)
        view.textView.textColor = UIColor(hex: Colors.White)
        view.placeholderAttributedText = NSAttributedString(string: "说点什么...",attributes: [NSAttributedString.Key.font : UIFont.systemFont(ofSize: 14), NSAttributedString.Key.foregroundColor: UIColor(hex: Colors.White)])
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    
    let switchButton: LabelSwitch = {
        let ls = LabelSwitchConfig(text: "弹幕",
                              textColor: .white,
                                   font: UIFont.boldSystemFont(ofSize: 14),
                                   backgroundColor: UIColor(hex: Colors.Black))
                
        let rs = LabelSwitchConfig(text: "普通",
                              textColor: .white,
                                   font: UIFont.boldSystemFont(ofSize: 14),
                        backgroundColor: UIColor(hex: Colors.Blue))

        let view = LabelSwitch(center: .zero, leftConfig: ls, rightConfig: rs)
        view.isHidden = true
        // Set the appearance of the circle button
        view.circleShadow = false
        view.circleColor = UIColor.white
        view.fullSizeTapEnabled = true
        view.layer.borderWidth = 1
        view.layer.borderColor = UIColor.white.cgColor
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    let disposeBag = DisposeBag()
    var leftConstraint: NSLayoutConstraint!
    var heightConstraint: NSLayoutConstraint!
    var sendMessageCallBack: ((_ text: String,_ isDanmu:Bool) -> Observable<Result<Bool>>)!
    
    init() {
        super.init(frame: CGRect.zero)
        addSubview(switchButton)
        addSubview(backgroundView)
        backgroundView.addSubview(messageIcon)
        backgroundView.addSubview(input)
        leftConstraint = backgroundView.leftAnchor.constraint(equalTo: self.leftAnchor, constant: 16)
        leftConstraint.isActive = true
        switchButton.leftAnchor.constraint(equalTo: self.leftAnchor, constant: 0).isActive = true
        backgroundView.topAnchor.constraint(equalTo: self.topAnchor).isActive = true
        backgroundView.rightAnchor.constraint(equalTo: self.rightAnchor).isActive = true
        backgroundView.bottomAnchor.constraint(equalTo: self.bottomAnchor).isActive = true
        heightConstraint = backgroundView.heightAnchor.constraint(equalToConstant: 36)
        heightConstraint.priority = .defaultHigh
        heightConstraint.isActive = true
        switchButton.widthAnchor.constraint(equalToConstant: 70).isActive = true
        switchButton.heightAnchor.constraint(equalToConstant: 30).isActive = true
        switchButton.bottomAnchor.constraint(equalTo: self.bottomAnchor, constant: -3).isActive = true
        messageIcon.widthAnchor.constraint(equalToConstant: 36).isActive = true
        messageIcon.heightAnchor.constraint(equalToConstant: 36).isActive = true
        messageIcon.leftAnchor.constraint(equalTo: backgroundView.leftAnchor, constant: 3).isActive = true
        messageIcon.bottomAnchor.constraint(equalTo: backgroundView.bottomAnchor).isActive = true
        input.leftAnchor.constraint(equalTo: messageIcon.rightAnchor).isActive = true
        input.rightAnchor.constraint(equalTo: backgroundView.rightAnchor).isActive = true
        input.topAnchor.constraint(equalTo: backgroundView.topAnchor, constant: 6).isActive = true
        input.bottomAnchor.constraint(equalTo: backgroundView.bottomAnchor, constant: -6).isActive = true
        
        input.delegates.didChangeHeight = { [weak self] height in
            guard let self = self else { return }
            self.heightConstraint.constant = height + 12
        }
        input.textView.delegate = self
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}
extension MessageView: UITextViewDelegate {
    func textViewDidBeginEditing(_ textView: UITextView) {
        switchButton.isHidden = false
        self.leftConstraint.constant = 78
    }
    func textViewDidEndEditing(_ textView: UITextView) {
        switchButton.isHidden = true
        self.leftConstraint.constant = 16
    }
    func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
        if text == "\n" {
            sendMessageCallBack(textView.text, switchButton.curState == .R).subscribe(onNext: {result in
                textView.endEditing(false)
                textView.text = ""
            }).disposed(by: disposeBag)
            return false
        }
        return true
    }
}
