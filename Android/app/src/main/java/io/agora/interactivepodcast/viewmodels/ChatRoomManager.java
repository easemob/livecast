package io.agora.interactivepodcast.viewmodels;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMChatRoomManager;
import com.hyphenate.chat.EMClient;


public class ChatRoomManager {
    private static ChatRoomManager instance;

    private EMChatRoomManager emChatRoomManager;

    private ChatRoomManager() {
         emChatRoomManager = EMClient.getInstance().chatroomManager();
    }

    public static ChatRoomManager getInstance() {
        if(instance == null) {
            synchronized (ChatRoomManager.class) {
                if(instance == null) {
                    instance = new ChatRoomManager();
                }
            }
        }
        return instance;
    }


    /**
     * 加入聊天室
     * @param roomId
     */
    public void joinChatRoom(String roomId, EMValueCallBack<EMChatRoom> callBack){
         emChatRoomManager.joinChatRoom(roomId,callBack);
    }

    /**
     * 离开聊天室
     * @param userName
     */
    public void leaveChatRoom(String userName){
        emChatRoomManager.leaveChatRoom(userName);
    }


    /**
     * 销毁聊天室
     * @param chatRoomId
     * @param callBack
     */
    public void destroyChatRoom(String chatRoomId, EMCallBack callBack){
        emChatRoomManager.asyncDestroyChatRoom(chatRoomId,callBack);
    }

}
