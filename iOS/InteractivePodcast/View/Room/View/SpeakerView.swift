//
//  SpeakerView.swift
//  InteractivePodcast
//
//  Created by XUCH on 2021/3/10.
//

import Foundation
import UIKit

class SpeakerView: BaseUICollectionViewCell<Member> {
    fileprivate static let font = UIFont.systemFont(ofSize: 12)
    fileprivate static let padding: CGFloat = 10
    fileprivate static let avatarWidth: CGFloat = 56
    
    weak var delegate: RoomControlDelegate?
    
    var isInvite = true {
        didSet {
            if isInvite {
                micStatus.isHidden = true
                roleIcon.isHidden = true
                avatar.image = UIImage(named: "speaker_placeholder.png")
                roleIcon.isHidden = true
                micStatus.isHidden = true
                roleName.textColor = UIColor(hex: Colors.Gray)
            }
        }
    }
    
    var number: Int = 1 {
        didSet {
            roleName.text = "\(number)号麦"
        }
    }
    
    override var model: Member! {
        didSet {
            isInvite = false
            micStatus.isHidden = !model.isSpeaker
            roleIcon.isHidden = !model.isManager
            roleName.text = model.user.name
            avatar.image = UIImage(named: model.user.getLocalAvatar())
            roleIcon.image = UIImage(named: _roleIcon)
            micStatus.image = UIImage(named: _micIcon)
            roleName.textColor = UIColor(hex: Colors.White)
        }
    }
    
    private var _roleIcon: String {
        return model.isManager ? "master" : "grayBroadcaster"
    }
    
    private var _micIcon: String {
        return (model.isMuted || model.isSelfMuted) ? "redMic" : "blueMic"
    }
    
    var avatar: UIImageView = {
        let view = RoundImageView()
        view.image = UIImage(named: "speaker_placeholder.png")
        return view
    }()
    
    var micStatus: UIImageView = {
        let view = RoundImageView()
        view.isHidden = true
        view.backgroundColor = UIColor(hex: Colors.Black)
        return view
    }()
    
    var roleView: UIStackView = {
        let view = UIStackView()
        return view
    }()
    
    var roleIcon: UIImageView = {
        let view = UIImageView()
        view.width(constant: 16)
            .height(constant: 16)
            .active()
        view.isHidden = true
        return view
    }()
    
    var roleName: UILabel = {
        let view = UILabel()
        view.font = font
        view.numberOfLines = 1
        view.text = "邀请Ta"
        view.textColor = UIColor(hex: Colors.Gray)
        return view
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(handleTap(_:))))
        addSubview(avatar)
        addSubview(micStatus)
        addSubview(roleView)
        roleView.addArrangedSubview(roleIcon)
        roleView.addArrangedSubview(roleName)
        roleView.spacing = 5
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func render() {
        avatar
            .width(constant: SpeakerView.avatarWidth)
            .height(constant: SpeakerView.avatarWidth)
            .marginTop(anchor: topAnchor, constant: SpeakerView.padding)
            .centerX(anchor: centerXAnchor, constant: 0)
            .active()
        
        micStatus
            .width(constant: 20)
            .height(constant: 20)
            .marginTrailing(anchor: avatar.trailingAnchor, constant: 0)
            .marginBottom(anchor: avatar.bottomAnchor, constant: 0)
            .active()
        
        roleView
            .marginTop(anchor: avatar.bottomAnchor, constant: 10)
            .marginLeading(anchor: avatar.leadingAnchor, constant: 0, relation: .greaterOrEqual)
            .marginBottom(anchor: bottomAnchor, constant: SpeakerView.padding, relation: .greaterOrEqual)
            .centerX(anchor: avatar.centerXAnchor, constant: 0)
            .active()
    }
    
    @objc func handleTap(_ sender: UITapGestureRecognizer? = nil) {
        if isInvite {
            delegate?.onTapInInvite()
        } else {
            delegate?.onTap(member: model)
        }
    }
    
    static func sizeForItem(width: CGFloat) -> CGSize {
        return CGSize(width: width, height: padding + avatarWidth + 10 + font.lineHeight + padding )
    }
}
