package io.agora.interactivepodcast.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agora.data.BaseError;
import com.agora.data.DataRepositroy;
import com.agora.data.RoomEventCallback;
import com.agora.data.manager.RoomManager;
import com.agora.data.manager.RtcManager;
import com.agora.data.manager.UserManager;
import com.agora.data.model.Member;
import com.agora.data.model.Room;
import com.agora.data.model.User;
import com.agora.data.observer.DataCompletableObserver;
import com.agora.data.observer.DataMaybeObserver;
import com.agora.data.observer.DataObserver;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;
import java.util.List;

import io.agora.baselibrary.base.BaseActivity;
import io.agora.baselibrary.base.OnItemClickListener;
import io.agora.baselibrary.util.ToastUtile;
import io.agora.interactivepodcast.R;
import io.agora.interactivepodcast.adapter.ChatRoomListsnerAdapter;
import io.agora.interactivepodcast.adapter.ChatRoomSeatUserAdapter;
import io.agora.interactivepodcast.common.LiveDataBus;
import io.agora.interactivepodcast.common.ThreadManager;
import io.agora.interactivepodcast.data.ChatRoomPresenter;
import io.agora.interactivepodcast.msg.DemoMsgHelper;
import io.agora.interactivepodcast.msg.OnCustomMsgReceiveListener;
import io.agora.interactivepodcast.msg.OnMsgCallBack;
import io.agora.interactivepodcast.utils.DemoConstants;
import io.agora.interactivepodcast.utils.Utils;
import io.agora.interactivepodcast.viewmodels.ChatRoomManager;
import io.agora.interactivepodcast.widget.CountImageView;
import io.agora.interactivepodcast.widget.ExitPopupWindow;
import io.agora.interactivepodcast.widget.HandUpDialog;
import io.agora.interactivepodcast.widget.InviteMenuDialog;
import io.agora.interactivepodcast.widget.InvitedMenuDialog;
import io.agora.interactivepodcast.widget.MyGridLayoutManager;
import io.agora.interactivepodcast.widget.RoomMessagesView;
import io.agora.interactivepodcast.widget.SingleBarrageView;
import io.agora.interactivepodcast.widget.UserSeatMenuDialog;
import io.agora.rtc.IRtcEngineEventHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ChatRoomTestActivity extends BaseActivity implements View.OnClickListener, ChatRoomPresenter.OnChatRoomListener, OnCustomMsgReceiveListener, RoomEventCallback {

    private static final String TAG = ChatRoomTestActivity.class.getSimpleName();

    private static final String TAG_ROOM = "room";

    private ChatRoomSeatUserAdapter mSpeakerAdapter;
    private ChatRoomListsnerAdapter mListenerAdapter;
    private ChatRoomPresenter chatRoomPresenter;
    private String chatRoomId;
    private ImageView ivMin,ivMore,ivExit,ivAudio,ivChat,ivHandUp;
    private CountImageView ivNews;
    private RecyclerView rvSpeakers,rvListeners;
    private SingleBarrageView singleBarrageView;
    private RoomMessagesView roomMessagesView;
    private View bottomBar;
    private TextView tvName;

    public static Intent newIntent(Context context, Room mRoom) {
        Intent intent = new Intent(context, ChatRoomTestActivity.class);
        intent.putExtra(TAG_ROOM, mRoom);
        return intent;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        exitRoom(true);
    }

    private OnItemClickListener<Member> onitemSpeaker = new OnItemClickListener<Member>() {
        @Override
        public void onItemClick(@NonNull Member data, View view, int position, long id) {
            User user = data.getUserId();
            EMLog.d(TAG,"setUserInfo  : name = "+user.getName());
            if (user.getName().contains("号麦")) {
                if (isOwner()) {
                    inviteDialog();
                }
            } else {
                if (isOwner()) {
                    if (isMine(data)) {
                        return;
                    }
                } else {
                    if (!isMine(data)) {
                        return;
                    }
                }
                showUserMenuDialog(data);
            }
        }
    };

    /**
     * 点击加号邀请上台
     */
     private void inviteDialog(){
         if (mListenerAdapter != null && mListenerAdapter.getItemCount() > 0) {
             int max = mListenerAdapter.getItemCount(), min = 0;
             int index = (int) (Math.random() * (max - min) + min);
             EMLog.d(TAG,"inviteDialog  : index = "+index);
             Member member = mListenerAdapter.getItemData(index);
             showUserInviteDialog(member);
         } else {
             ToastUtile.toastLong(ChatRoomTestActivity.this, "暂无听众可以邀请!");
         }
     }

    private OnItemClickListener<Member> onitemListener = new OnItemClickListener<Member>() {
        @Override
        public void onItemClick(@NonNull Member data, View view, int position, long id) {
            if (!isOwner()) {
                return;
            }
            showUserInviteDialog(data);
        }
    };

    private final IRtcEngineEventHandler mIRtcEngineEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onError(int err) {
            super.onError(err);
        }
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            if (RoomManager.isLeaving) {
                return;
            }
            Member member = RoomManager.Instance(ChatRoomTestActivity.this).getMine();
            if (member == null) {
                return;
            }
            long streamId = uid & 0xffffffffL;
            member.setStreamId(streamId);
            onRTCRoomJoined();
        }
    };

    @Override
    protected void iniBundle(@NonNull Bundle bundle) { }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat_room_test;
    }

    @Override
    protected void iniView() {
        mSpeakerAdapter = new ChatRoomSeatUserAdapter(null, onitemSpeaker);
        mListenerAdapter = new ChatRoomListsnerAdapter(null, onitemListener);
        rvSpeakers = findViewById(R.id.rvSpeakers);
        MyGridLayoutManager gridLayoutManager = new MyGridLayoutManager(this, 4);
        rvSpeakers.setLayoutManager(gridLayoutManager);
        rvSpeakers.setAdapter(mSpeakerAdapter);

        rvListeners = findViewById(R.id.rvListeners);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvListeners.setLayoutManager(linearLayoutManager);
        rvListeners.setAdapter(mListenerAdapter);

        singleBarrageView = findViewById(R.id.singleBarrageView);
        singleBarrageView.initBarrage();

        roomMessagesView = findViewById(R.id.roomMessagesView);
        ivMin = findViewById(R.id.ivMin);
        ivMore = findViewById(R.id.ivMore);
        tvName = findViewById(R.id.tvName);
        ivExit = findViewById(R.id.ivExit);
        ivNews = findViewById(R.id.ivNews);
        ivAudio = findViewById(R.id.ivAudio);
        bottomBar = findViewById(R.id.bottomBar);
        ivChat = findViewById(R.id.ivChat);
        ivHandUp = findViewById(R.id.ivHandUp);
    }

    @Override
    protected void iniListener() {
        RtcManager.Instance(this).addHandler(mIRtcEngineEventHandler);
        RoomManager.Instance(this).addRoomEventCallback(this);
        ivMin.setOnClickListener(this);
        ivMore.setOnClickListener(this);
        ivExit.setOnClickListener(this);
        ivNews.setOnClickListener(this);
        ivAudio.setOnClickListener(this);
        ivChat.setOnClickListener(this);
        bottomBar.setOnClickListener(this);
        ivHandUp.setOnClickListener(this);
    }

    @Override
    protected void iniData() {
        User mUser = UserManager.Instance(this).getUserLiveData().getValue();
        if (mUser == null) {
            ToastUtile.toastShort(this, "please login in");
            finish();
            return;
        }

        Room mRoom = (Room) getIntent().getExtras().getSerializable(TAG_ROOM);

        Member mMember = new Member(mUser);
        mMember.setRoomId(mRoom);
        RoomManager.Instance(this).onJoinRoom(mRoom, mMember);

        if (isOwner()) {
            mMember.setIsSpeaker(1);
        } else {
            mMember.setIsSpeaker(0);
        }

        UserManager.Instance(this).getUserLiveData().observe(this, tempUser -> {
            if (tempUser == null) {
                return;
            }
            Member temp = RoomManager.Instance(ChatRoomTestActivity.this).getMine();
            if (temp == null) {
                return;
            }

            temp.setUser(tempUser);
        });
        chatRoomId = mRoom.getChatroomId();

        LiveDataBus.get().with(DemoConstants.NETWORK_CONNECTED, Boolean.class).observe(this, event -> {
            if(event != null && event) {
                EMLog.e(TAG, "断网重连后 重新加入聊天室");

                ChatRoomManager.getInstance().joinChatRoom(chatRoomId, new EMValueCallBack<EMChatRoom>() {
                    @Override
                    public void onSuccess(EMChatRoom emChatRoom) {
                        EMLog.e(TAG, "断网重连后 加入聊天室 onSuccess");
                    }

                    @Override
                    public void onError(int i, String s) {
                        EMLog.e(TAG,"断网重连后 重新加入聊天室 onError " + "msg : "+s+"  code : "+i);
                    }
                });
            }
        });

        preJoinRoom(mRoom);
        DemoMsgHelper.getInstance().init(chatRoomId);
        chatRoomPresenter = new ChatRoomPresenter(this, chatRoomId);
        EMClient.getInstance().chatManager().addMessageListener(chatRoomPresenter);
        chatRoomPresenter.setOnChatRoomListener(this);
        DemoMsgHelper.getInstance().setOnCustomMsgReceiveListener(this);
        onMessageListInit();
    }

    protected void onMessageListInit(){
        roomMessagesView.init(chatRoomId);
        roomMessagesView.setMessageViewListener(new RoomMessagesView.MessageViewListener() {
            @Override
            public void onMessageSend(String content, boolean isBarrageMsg) {
                chatRoomPresenter.sendTxtMsg(content, isBarrageMsg, new OnMsgCallBack() {
                    @Override
                    public void onSuccess(EMMessage message) {
                        EMLog.e(TAG,"onMessageListInit  :  onMessageSend   content : "+content +" : isBarrageMsg = "+isBarrageMsg);
                        //刷新消息列表
                        roomMessagesView.refreshSelectLast();
                        if(isBarrageMsg) {
                            singleBarrageView.addData(message);
                        }
                    }
                });
            }

            @Override
            public void onItemClickListener(final EMMessage message) {
            }

            @Override
            public void onHiderBottomBar() {
                bottomBar.setVisibility(View.VISIBLE);
            }
        });
        roomMessagesView.setVisibility(View.VISIBLE);
        bottomBar.setVisibility(View.VISIBLE);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivMin:
                exitRoom(false);
                break;
            case R.id.ivMore:
            case R.id.ivExit:
                showCloseRoomDialog();
                break;
            case R.id.ivNews:
                gotoHandsUpList();
                break;
            case R.id.ivAudio:
                toggleAudio();
                break;
            case R.id.ivHandUp:
                toggleHandUp();
                break;
            case R.id.bottomBar:
            case R.id.ivChat:
                showInputView();
                break;
        }
    }

    private void exitRoom(boolean leaveRoom) {
        if (leaveRoom) {
            if (isOwner()){//群主解散聊天室
                ChatRoomManager.getInstance().destroyChatRoom(chatRoomId, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        RoomManager.Instance(ChatRoomTestActivity.this).leaveRoom();
                    }

                    @Override
                    public void onError(int i, String s) { }

                    @Override
                    public void onProgress(int i, String s) { }
                });
            } else {//普通成员离开聊天室
                ChatRoomManager.getInstance().leaveChatRoom(EMClient.getInstance().getCurrentUser());
                RoomManager.Instance(ChatRoomTestActivity.this).leaveRoom();
            }
        } else {
            RoomManager.Instance(this).onEnterMinStatus();
        }

        finish();
        if (!leaveRoom) {
            overridePendingTransition(R.anim.chat_room_in, R.anim.chat_room_out);
        }
    }


    //TODO 改成dialog弹窗
    private void showCloseRoomDialog() {
        ExitPopupWindow exitPopupWindow = new ExitPopupWindow(this);
//        exitPopupWindow.showAtLocation(this.findViewById(R.id.chatroom), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        exitPopupWindow.showPopupWindow(ivMore);
        exitPopupWindow.setOnItemClickListener(new ExitPopupWindow.OnItemClickListener() {
            @Override
            public void onExitClick() {
                exitRoom(true);
            }
        });
    }

    private void gotoHandsUpList() {
        Room mRoom = RoomManager.Instance(this).getRoom();
        if (mRoom == null) {
            return;
        }
        new HandUpDialog().show(getSupportFragmentManager(), mRoom);
    }

    private void toggleAudio() {
        if (!RoomManager.Instance(this).isOwner()) {
            Member member = RoomManager.Instance(this).getMine();
            if (member == null) {
                return;
            }

            if (member.getIsMuted() == 1) {
                ToastUtile.toastShort(this, R.string.member_muted);
                return;
            }
        }

        ivAudio.setEnabled(false);
        RoomManager.Instance(this)
                .toggleSelfAudio()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mLifecycleProvider.bindToLifecycle())
                .subscribe(new DataCompletableObserver(this) {
                    @Override
                    public void handleError(@NonNull BaseError e) {
                        ToastUtile.toastShort(ChatRoomTestActivity.this, e.getMessage());
                        ivAudio.setEnabled(true);
                    }

                    @Override
                    public void handleSuccess() {
                        ivAudio.setEnabled(true);
                        refreshVoiceView();
                    }
                });
    }

    private void refreshVoiceView() {
        Member member = RoomManager.Instance(ChatRoomTestActivity.this).getMine();
        if (member == null) {
            return;
        }

        if (isOwner()) {
            ivAudio.setVisibility(View.VISIBLE);
            if (member.getIsMuted() == 1) {
                ivAudio.setImageResource(R.mipmap.icon_microphoneoff);
            } else if (member.getIsSelfMuted() == 1) {
                ivAudio.setImageResource(R.mipmap.icon_microphoneoff);
            } else {
                ivAudio.setImageResource(R.mipmap.icon_microphoneon);
            }
        } else {
            if (member.getIsSpeaker() == 0) {
                ivAudio.setVisibility(View.GONE);
            } else {
                ivAudio.setVisibility(View.VISIBLE);
                if (member.getIsMuted() == 1) {
                    ivAudio.setImageResource(R.mipmap.icon_microphoneoff);
                } else if (member.getIsSelfMuted() == 1) {
                    ivAudio.setImageResource(R.mipmap.icon_microphoneoff);
                } else {
                    ivAudio.setImageResource(R.mipmap.icon_microphoneon);
                }
            }
        }
    }


    private void toggleHandUp() {
        ivHandUp.setEnabled(false);
        RoomManager.Instance(this)
                .requestHandsUp()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mLifecycleProvider.bindToLifecycle())
                .subscribe(new DataCompletableObserver(this) {
                    @Override
                    public void handleError(@NonNull BaseError e) {
                        ToastUtile.toastShort(ChatRoomTestActivity.this, e.getMessage());
                        ivHandUp.setEnabled(true);
                    }

                    @Override
                    public void handleSuccess() {
                        refreshHandUpView();
                        ivHandUp.setEnabled(true);
                        ToastUtile.toastShort(ChatRoomTestActivity.this, R.string.request_handup_success);
                    }
                });
    }


    private void refreshHandUpView() {
        if (RoomManager.Instance(this).isOwner()) {
            ivHandUp.setVisibility(View.GONE);
        } else {
            ivHandUp.setImageResource(R.mipmap.icon_un_handup);
            Member member = RoomManager.Instance(this).getMine();
            if (member == null) {
                return;
            }
            ivHandUp.setVisibility(member.getIsSpeaker() == 0 ? View.VISIBLE : View.GONE);
        }
    }


    private void showInputView() {
        bottomBar.setVisibility(View.GONE);
        roomMessagesView.setShowInputView(true);
        roomMessagesView.getInputView().requestFocus();
        roomMessagesView.getInputView().requestFocusFromTouch();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.showKeyboard(roomMessagesView.getInputView());
            }
        },150);
    }

    private UserSeatMenuDialog mUserSeatMenuDialog;

    private void showUserMenuDialog(Member data) {
        if (mUserSeatMenuDialog != null && mUserSeatMenuDialog.isShowing()) {
            return;
        }
        mUserSeatMenuDialog = new UserSeatMenuDialog();
        mUserSeatMenuDialog.show(getSupportFragmentManager(), data);
    }


    private InviteMenuDialog mInviteMenuDialog;
    private void showUserInviteDialog(Member data) {
        if (mInviteMenuDialog != null && mInviteMenuDialog.isShowing()) {
            return;
        }
        mInviteMenuDialog = new InviteMenuDialog();
        mInviteMenuDialog.show(getSupportFragmentManager(), data);
    }

    private void onRTCRoomJoined() {
        joinRoom();
    }

    private void joinRoom() {
        RoomManager.Instance(this)
                .joinRoom()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mLifecycleProvider.bindToLifecycle())
                .subscribe(new DataObserver<Member>(this) {
                    @Override
                    public void handleError(@NonNull BaseError e) {

                    }

                    @Override
                    public void handleSuccess(@NonNull Member member) {
                        if (RoomManager.isLeaving) {
                            return;
                        }
                        getMembers();
                        onJoinRoomEnd();
                    }
                });
    }

    private void onJoinRoomEnd() {
        refreshVoiceView();
        refreshHandUpView();
        if (isOwner()) {
            ivNews.setVisibility(View.VISIBLE);
//            mDataBinding.ivExit.setVisibility(View.VISIBLE);

            Member member = RoomManager.Instance(this).getMine();
            if (member == null) {
                return;
            }

            if (member.getIsSpeaker() == 1) {
                RoomManager.Instance(this).startLivePlay();
            } else {
                RoomManager.Instance(this).stopLivePlay();
            }
        } else {
            ivNews.setVisibility(View.GONE);
            RoomManager.Instance(this)
                    .toggleSelfAudio()
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(mLifecycleProvider.bindToLifecycle())
                    .subscribe(new DataCompletableObserver(this) {
                        @Override
                        public void handleError(@NonNull BaseError e) {
                            ToastUtile.toastShort(ChatRoomTestActivity.this, e.getMessage());
                            ivAudio.setEnabled(true);
                        }

                        @Override
                        public void handleSuccess() {
                            ivAudio.setEnabled(true);
                        }
                    });
//            mDataBinding.ivExit.setVisibility(View.INVISIBLE);
        }
    }

    private void preJoinRoom(Room room) {
        onLoadRoom(room);

        RoomManager.Instance(this)
                .getRoom(room)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mLifecycleProvider.bindToLifecycle())
                .subscribe(new DataMaybeObserver<Room>(this) {
                    @Override
                    public void handleError(@NonNull BaseError e) {
                        if (RoomManager.isLeaving) {
                            return;
                        }

                        ToastUtile.toastShort(ChatRoomTestActivity.this, R.string.error_room_not_exsit);
                        RoomManager.Instance(ChatRoomTestActivity.this).leaveRoom();
                        finish();
                    }

                    @Override
                    public void handleSuccess(@Nullable Room room) {
                        if (RoomManager.isLeaving) {
                            return;
                        }

                        if (room == null) {
                            ToastUtile.toastShort(ChatRoomTestActivity.this, R.string.error_room_not_exsit);
                            RoomManager.Instance(ChatRoomTestActivity.this).leaveRoom();
                            finish();
                            return;
                        }

                        Member mine = RoomManager.Instance(ChatRoomTestActivity.this).getMine();
                        if (mine == null) {
                            RoomManager.Instance(ChatRoomTestActivity.this).leaveRoom();
                            finish();
                            return;
                        }

                        RoomManager.Instance(ChatRoomTestActivity.this)
                                .getMember(mine.getUserId().getObjectId())
                                .observeOn(AndroidSchedulers.mainThread())
                                .compose(mLifecycleProvider.bindToLifecycle())
                                .subscribe(new DataMaybeObserver<Member>(ChatRoomTestActivity.this) {
                                    @Override
                                    public void handleError(@NonNull BaseError e) {
                                        if (RoomManager.isLeaving) {
                                            return;
                                        }

                                        ToastUtile.toastShort(ChatRoomTestActivity.this, R.string.error_room_not_exsit);
                                        finish();
                                    }

                                    @Override
                                    public void handleSuccess(@Nullable Member member) {
                                        if (RoomManager.isLeaving) {
                                            return;
                                        }
                                        joinRTCRoom();
                                    }
                                });
                    }
                });
    }

    private void joinRTCRoom() {
        Room room = RoomManager.Instance(this).getRoom();
        if (room == null) {
            return;
        }

        Member member = RoomManager.Instance(this).getMine();
        if (member == null) {
            return;
        }

        int userId = 0;
        if (member.getStreamId() != null) {
            userId = member.getStreamId().intValue();
        }

        String objectId = room.getObjectId();
        RtcManager.Instance(this).joinChannel(objectId, userId);
    }

    private void getMembers() {
        Room room = RoomManager.Instance(this).getRoom();
        if (room == null) {
            return;
        }

        DataRepositroy.Instance(this)
                .getMembers(room)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mLifecycleProvider.bindToLifecycle())
                .subscribe(new DataObserver<List<Member>>(this) {
                    @Override
                    public void handleError(@NonNull BaseError e) {

                    }

                    @Override
                    public void handleSuccess(@NonNull List<Member> members) {
                        if (RoomManager.isLeaving) {
                            return;
                        }

                        onLoadRoomMembers(members);
                    }
                });
    }

    private void onLoadRoom(Room room) {
        tvName.setText(room.getChannelName());
    }

    private void onLoadRoomMembers(@NonNull List<Member> members) {
        RoomManager.Instance(this).onLoadRoomMembers(members);

        EMLog.d(TAG,"onLoadRoomMembers :  "+members.size());
        List<Member> memberList = new ArrayList<>();
        for (Member member : members) {
            if (member.getIsSpeaker() == 0) {
                meListenerFirst(member);
            } else {
                memberList.add(member);
                meSpeakerFirst(member);
            }
        }

        //TODO
        // 1、已在房间的人员加载房间人数时，最后追加一个加号，
        // 2、当有新人加入加入房间时，在最后-1的插入新加入成员。原来的顺序不做变动
        // 3、超过8时移除最后一个加号
        // 八个以内 自动添加到8个，超过8个之后最后面添加一个
        handleMembers();
    }

    private void handleMembers(){
        //目前总发言人数
        int itemCount = mSpeakerAdapter.getItemCount();
        EMLog.d(TAG,"handleMembers  itemCount :  "+itemCount);
        int count = 0;
        List<Integer> addMembers = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            Member member = mSpeakerAdapter.getItemData(i);
            if (member != null && !TextUtils.isEmpty(member.getObjectId())) {
                count++;
            } else {
                mSpeakerAdapter.deleteItem(count);
                addMembers.add(i);
            }
        }

        EMLog.d(TAG," 删除后的长度 :  "+mSpeakerAdapter.getItemCount());

        int num = count;
        EMLog.d(TAG,"handleMembers  :  itemCount "+itemCount+"；； num = "+num);
        if(num < 8){
            for (int i = num; i < 8; i++) {
                User mUser = new User();
                int temp = i+1;
                mUser.setName(temp + "号麦");
                Member member = new Member(mUser);
                mSpeakerAdapter.addEndItem(member);
            }
        } else if(num == 8){

        } else if (num < 16){
            User mUser = new User();
            mUser.setName(num + 1 + "号麦");
            Member member = new Member(mUser);
            mSpeakerAdapter.addEndItem(member);
        }
    }


    private boolean isMine(Member member) {
        return RoomManager.Instance(this).isMine(member);
    }

    private boolean isOwner(Member member) {
        return RoomManager.Instance(this).isOwner(member);
    }

    private boolean isOwner() {
        return RoomManager.Instance(this).isOwner();
    }


    @Override
    public void onChatRoomOwnerChanged(String chatRoomId, String newOwner, String oldOwner) { }

    @Override
    public void onChatRoomMemberAdded(String participant) { }

    @Override
    public void onChatRoomMemberExited(String participant) { }

    @Override
    public void onMessageReceived() {
        roomMessagesView.refreshSelectLast();
    }

    @Override
    public void onMessageSelectLast() { }

    @Override
    public void onMessageChanged() { }

    @Override
    public void onReceiveGiftMsg(EMMessage message) { }

    @Override
    public void onReceivePraiseMsg(EMMessage message) { }

    @Override
    public void onReceiveBarrageMsg(EMMessage message) {
        ThreadManager.getInstance().runOnMainThread(()-> {
            singleBarrageView.addData(message);
        });

    }

    @Override
    public void onOwnerLeaveRoom(@NonNull Room room) {
        ToastUtile.toastShort(this, R.string.room_closed);
        finish();
    }

    @Override
    public void onLeaveRoom(@NonNull Room room) { }

    @Override
    public void onMemberJoin(@NonNull Member member) {
        if (member.getIsSpeaker() == 0) {
            meListenerFirst(member);
        } else {
//            meSpeakerFirst(member);
            EMLog.e(TAG,"onMemberJoin : "+member.getUserId().getName());
//            if (isMine(member)) {
                mSpeakerAdapter.addItem(member, 1);
//            } else {
//                //最后的位置改为 第二个位置
//                mSpeakerAdapter.addItem(member,1);
//            }
            handleMembers();
        }
    }

    private void meSpeakerFirst(Member member){
//        if (isMine(member)) {
//            mSpeakerAdapter.addItem(member, 0);
//        } else {
            mSpeakerAdapter.addItem(member);
//        }
    }

    private void meListenerFirst(Member member){
        if (isMine(member)) {
            mListenerAdapter.addItem(member, 0);
        } else {
            mListenerAdapter.addItem(member);
        }
    }

    @Override
    public void onMemberLeave(@NonNull Member member) {
        EMLog.e(TAG,"onMemberLeave : "+member.getUserId().getName());
        mSpeakerAdapter.deleteItem(member);
        mListenerAdapter.deleteItem(member);
        handleMembers();
    }

    @Override
    public void onRoleChanged(boolean isMine, @NonNull Member member) {
        if (member.getIsSpeaker() == 1) {
//            meSpeakerFirst(member);
            //TODO 角色变化时，自己则显示在首位，非自己显示在第二位置。
            EMLog.e(TAG,"onRoleChanged : "+member.getUserId().getName());
//            if (isMine(member)) {
                mSpeakerAdapter.addItem(member, 1);
//            } else {
//                //最后的位置改为 第二个位置
//                mSpeakerAdapter.addItem(member,1);
//            }
            handleMembers();
            mListenerAdapter.deleteItem(member);
        } else {
            mSpeakerAdapter.deleteItem(member);
            handleMembers();
            meListenerFirst(member);
        }

        refreshVoiceView();
        refreshHandUpView();

        if (!isMine && isMine(member)) {
            if (member.getIsSpeaker() == 0) {
                ToastUtile.toastShort(this, R.string.member_speaker_to_listener);
            }
        }
    }

    @Override
    public void onAudioStatusChanged(boolean isMine, @NonNull Member member) {
        refreshVoiceView();

        if (!isMine && isMine(member)) {
            int isSelfMuted = member.getIsSelfMuted();
            if (member.getIsSelfMuted() == 1) {
                ToastUtile.toastShort(this, "打开麦克风");
            } else if (member.getIsMuted() == 1) {
                ToastUtile.toastShort(this, R.string.member_muted);
            }
        }

        int index = mSpeakerAdapter.indexOf(member);
        if (index >= 0) {
            mSpeakerAdapter.update(index, member);
        }
    }

    @Override
    public void onReceivedHandUp(@NonNull Member member) {
       ivNews.setCount(DataRepositroy.Instance(this).getHandUpListCount());
    }

    @Override
    public void onHandUpAgree(@NonNull Member member) {
        refreshHandUpView();
        if (isOwner()) {
            ivNews.setCount(DataRepositroy.Instance(this).getHandUpListCount());
        }
    }

    @Override
    public void onHandUpRefuse(@NonNull Member member) {
        if (isMine(member)) {
            ToastUtile.toastShort(this, R.string.handup_refuse);
        }
        refreshHandUpView();
        if (isOwner()) {
            ivNews.setCount(DataRepositroy.Instance(this).getHandUpListCount());
        }
    }

    @Override
    public void onReceivedInvite(@NonNull Member member) {
        showInviteDialog();
    }

    @Override
    public void onInviteAgree(@NonNull Member member) {
    }

    @Override
    public void onInviteRefuse(@NonNull Member member) {
        if (isOwner()) {
            ToastUtile.toastShort(this, getString(R.string.invite_refuse, member.getUserId().getName()));
        }
    }

    @Override
    public void onEnterMinStatus() {

    }

    @Override
    public void onRoomError(int error) {
        showErrorDialog(getString(R.string.error_room_default, String.valueOf(error)));
    }

    private AlertDialog errorDialog = null;
    private void showErrorDialog(String msg) {
        if (errorDialog != null && errorDialog.isShowing()) {
            return;
        }
        errorDialog = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exitRoom(true);
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        EMLog.e(TAG, "   onDestroy");
        RtcManager.Instance(this).removeHandler(mIRtcEngineEventHandler);
        RoomManager.Instance(this).removeRoomEventCallback(this);
        DemoMsgHelper.getInstance().removeCustomMsgLisenter();
        EMClient.getInstance().chatManager().removeMessageListener(chatRoomPresenter);
        closeInviteDialog();
        super.onDestroy();
    }

    private InvitedMenuDialog inviteDialog;
    private void showInviteDialog() {
        if (inviteDialog != null && inviteDialog.isShowing()) {
            return;
        }
        Room room = RoomManager.Instance(this).getRoom();
        if (room == null) {
            return;
        }
        inviteDialog = new InvitedMenuDialog();
        inviteDialog.show(getSupportFragmentManager(), room.getAnchorId());
    }

    private void closeInviteDialog() {
        if (inviteDialog != null && inviteDialog.isShowing()) {
            inviteDialog.dismiss();
        }
    }

}