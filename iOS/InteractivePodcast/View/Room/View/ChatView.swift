//
//  ChatView.swift
//  InteractivePodcast
//
//  Created by zliu on 2021/10/21.
//

import Foundation
import UIKit

class ChatView: UITableViewCell {
    
    static let reuseId = String(describing: ChatView.self)
    
    let nameLabel: UILabel = {
        let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.font = UIFont.systemFont(ofSize: 14)
        label.textAlignment = .right
        label.lineBreakMode = .byTruncatingMiddle
        return label
    }()
    
    let messageLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor(hex: Colors.White)
        label.translatesAutoresizingMaskIntoConstraints = false
        label.font = UIFont.systemFont(ofSize: 14)
        label.numberOfLines = 0
        return label
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        backgroundColor = .clear
        selectionStyle = .none
        contentView.addSubview(nameLabel)
        contentView.addSubview(messageLabel)
        let views = [
            "name":nameLabel,
            "message": messageLabel
        ]
        contentView.addConstraints(NSLayoutConstraint.constraints(withVisualFormat: "H:|-16-[name(80)]-8-[message]-16-|", options: [.alignAllTop], metrics: nil, views: views))
        contentView.addConstraints(NSLayoutConstraint.constraints(withVisualFormat: "V:|-13-[name]", options: [], metrics: nil, views: views))
        contentView.addConstraints(NSLayoutConstraint.constraints(withVisualFormat: "V:|-13-[message]|", options: [], metrics: nil, views: views))
    }
    func setup(name: String, message: String, isOwner: Bool) {
        nameLabel.text = "\(name)："
        messageLabel.text = message
        nameLabel.textColor = isOwner ? UIColor(hex: Colors.NameGold) : UIColor(hex: Colors.NameBlue)
    }
    
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
