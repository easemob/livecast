//
//  MemberView.swift
//  InteractivePodcast
//
//  Created by XUCH on 2021/3/9.
//

import Foundation
import UIKit

class ListenerView: BaseUICollectionViewCell<Member> {
    fileprivate static let padding: CGFloat = 10
    fileprivate static let avatarWidth: CGFloat = 36
    
    weak var delegate: RoomControlDelegate?
    
    override var model: Member! {
        didSet {
            avatar.image = UIImage(named: model.user.getLocalAvatar())
        }
    }
    
    var avatar: UIImageView = {
        let view = RoundImageView()
        return view
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(handleTap(_:))))
        backgroundColor = .clear
        addSubview(avatar)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func render() {
        avatar
            .width(constant: ListenerView.avatarWidth)
            .height(constant: ListenerView.avatarWidth)
            .marginLeading(anchor: leadingAnchor, constant: 16)
            .centerY(anchor: centerYAnchor)
            .active()
    }
    
    @objc func handleTap(_ sender: UITapGestureRecognizer? = nil) {
        delegate?.onTap(member: model)
    }
    
    static func sizeForItem(width: CGFloat) -> CGSize {
        return CGSize(width: width, height: padding + avatarWidth + padding)
    }
}
