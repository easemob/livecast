package io.agora.interactivepodcast.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.bumptech.glide.Glide;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.util.EMLog;

import java.util.List;

import io.agora.baselibrary.base.DataBindBaseActivity;
import io.agora.baselibrary.base.OnItemClickListener;
import io.agora.baselibrary.util.ToastUtile;
import io.agora.interactivepodcast.R;
import io.agora.interactivepodcast.adapter.ChatRoomListsnerAdapter;
import io.agora.interactivepodcast.adapter.ChatRoomSeatUserAdapter;
import io.agora.interactivepodcast.common.ThreadManager;
import io.agora.interactivepodcast.data.ChatRoomPresenter;
import io.agora.interactivepodcast.databinding.ActivityChatRoomBinding;
import io.agora.interactivepodcast.msg.DemoMsgHelper;
import io.agora.interactivepodcast.msg.OnCustomMsgReceiveListener;
import io.agora.interactivepodcast.msg.OnMsgCallBack;
import io.agora.interactivepodcast.utils.Utils;
import io.agora.interactivepodcast.viewmodels.ChatRoomManager;
import io.agora.interactivepodcast.widget.HandUpDialog;
import io.agora.interactivepodcast.widget.InviteMenuDialog;
import io.agora.interactivepodcast.widget.InvitedMenuDialog;
import io.agora.interactivepodcast.widget.RoomMessagesView;
import io.agora.interactivepodcast.widget.UserSeatMenuDialog;
import io.agora.rtc.IRtcEngineEventHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * 聊天室
 * 1. 查询Room表，房间是否存在，不存在就退出。
 * 2. 查询Member表
 * 2.1. 不存在就创建用户，并且用0加入到RTC，利用RTC分配一个唯一的uid，并且修改member的streamId值，这里注意，rtc分配的uid是int类型，需要进行（& 0xffffffffL）转换成long类型。
 * 2.2. 存在就返回member对象，利用streamId加入到RTC。
 *
 * @author chenhengfei@agora.io
 */
public class ChatRoomActivity extends DataBindBaseActivity<ActivityChatRoomBinding> implements View.OnClickListener, RoomEventCallback,ChatRoomPresenter.OnChatRoomListener, OnCustomMsgReceiveListener {

    private static final String TAG = ChatRoomActivity.class.getSimpleName();

    private static final String TAG_ROOM = "room";

    private ChatRoomSeatUserAdapter mSpeakerAdapter;
    private ChatRoomListsnerAdapter mListenerAdapter;
    private ChatRoomPresenter chatRoomPresenter;
    private String chatRoomId;

    private OnItemClickListener<Member> onitemSpeaker = new OnItemClickListener<Member>() {
        @Override
        public void onItemClick(@NonNull Member data, View view, int position, long id) {
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
    };

    private OnItemClickListener<Member> onitemListener = new OnItemClickListener<Member>() {
        @Override
        public void onItemClick(@NonNull Member data, View view, int position, long id) {
            if (!isOwner()) {
                return;
            }

            showUserInviteDialog(data);
        }
    };

    public static Intent newIntent(Context context, Room mRoom) {
        Intent intent = new Intent(context, ChatRoomActivity.class);
        intent.putExtra(TAG_ROOM, mRoom);
        return intent;
    }

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

            Member member = RoomManager.Instance(ChatRoomActivity.this).getMine();
            if (member == null) {
                return;
            }

            long streamId = uid & 0xffffffffL;
            member.setStreamId(streamId);
            onRTCRoomJoined();
        }
    };

    @Override
    protected void iniBundle(@NonNull Bundle bundle) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat_room;
    }

    @Override
    protected void iniView() {
        mSpeakerAdapter = new ChatRoomSeatUserAdapter(null, onitemSpeaker);
        mListenerAdapter = new ChatRoomListsnerAdapter(null, onitemListener);
        mDataBinding.rvSpeakers.setLayoutManager(new GridLayoutManager(this, 4));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mDataBinding.rvListeners.setLayoutManager(linearLayoutManager);
        mDataBinding.rvSpeakers.setAdapter(mSpeakerAdapter);
        mDataBinding.rvListeners.setAdapter(mListenerAdapter);
        mDataBinding.singleBarrageView.initBarrage();
    }

    @Override
    protected void iniListener() {
        RtcManager.Instance(this).addHandler(mIRtcEngineEventHandler);
        RoomManager.Instance(this).addRoomEventCallback(this);
        mDataBinding.ivMin.setOnClickListener(this);
        mDataBinding.ivMore.setOnClickListener(this);
        mDataBinding.ivExit.setOnClickListener(this);
        mDataBinding.ivNews.setOnClickListener(this);
        mDataBinding.ivAudio.setOnClickListener(this);
        mDataBinding.ivChat.setOnClickListener(this);
        mDataBinding.ivHandUp.setOnClickListener(this);
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

//        setUserBaseInfo();
        UserManager.Instance(this).getUserLiveData().observe(this, tempUser -> {
            if (tempUser == null) {
                return;
            }

            Member temp = RoomManager.Instance(ChatRoomActivity.this).getMine();
            if (temp == null) {
                return;
            }

            temp.setUser(tempUser);
//            setUserBaseInfo();
        });
        chatRoomId = mRoom.getChatroomId();
        preJoinRoom(mRoom);
        DemoMsgHelper.getInstance().init(chatRoomId);
        chatRoomPresenter = new ChatRoomPresenter(this, chatRoomId);
        EMClient.getInstance().chatManager().addMessageListener(chatRoomPresenter);
        chatRoomPresenter.setOnChatRoomListener(this);
        DemoMsgHelper.getInstance().setOnCustomMsgReceiveListener(this);
        onMessageListInit();
    }

    protected void onMessageListInit(){
        mDataBinding.roomMessagesView.init(chatRoomId);
        mDataBinding.roomMessagesView.setMessageViewListener(new RoomMessagesView.MessageViewListener() {
            @Override
            public void onMessageSend(String content, boolean isBarrageMsg) {
                chatRoomPresenter.sendTxtMsg(content, isBarrageMsg, new OnMsgCallBack() {
                    @Override
                    public void onSuccess(EMMessage message) {
                        //刷新消息列表
                        mDataBinding.roomMessagesView.refreshSelectLast();
                        if(isBarrageMsg) {
                            mDataBinding.singleBarrageView.addData(message);
                        }
                    }
                });
            }

            @Override
            public void onItemClickListener(final EMMessage message) {
                String clickUsername = message.getFrom();
//                showUserDetailsDialog(clickUsername);
            }

            @Override
            public void onHiderBottomBar() {
                mDataBinding.bottomBar.setVisibility(View.VISIBLE);
            }
        });
        mDataBinding.roomMessagesView.setVisibility(View.VISIBLE);
        mDataBinding.bottomBar.setVisibility(View.VISIBLE);
//        if(!chatroom.getAdminList().contains(EMClient.getInstance().getCurrentUser())
//                && !chatroom.getOwner().equals(EMClient.getInstance().getCurrentUser())) {
//            userManagerView.setVisibility(View.INVISIBLE);
//        }
//        isMessageListInited = true;
//        updateUnreadMsgView();
//        showMemberList();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EMClient.getInstance().chatManager().removeMessageListener(chatRoomPresenter);
    }

//    private void setUserBaseInfo() {
//        Member member = RoomManager.Instance(ChatRoomActivity.this).getMine();
//        if (member == null) {
//            return;
//        }
//
//        Glide.with(this)
//                .load(member.getUserId().getAvatarRes())
//                .placeholder(R.mipmap.default_head)
//                .error(R.mipmap.default_head)
//                .circleCrop()
//                .into(mDataBinding.ivUser);
//    }

    private void refreshVoiceView() {
        Member member = RoomManager.Instance(ChatRoomActivity.this).getMine();
        if (member == null) {
            return;
        }

        if (isOwner()) {
            mDataBinding.ivAudio.setVisibility(View.VISIBLE);
            if (member.getIsMuted() == 1) {
                mDataBinding.ivAudio.setImageResource(R.mipmap.icon_microphoneoff);
            } else if (member.getIsSelfMuted() == 1) {
                mDataBinding.ivAudio.setImageResource(R.mipmap.icon_microphoneoff);
            } else {
                mDataBinding.ivAudio.setImageResource(R.mipmap.icon_microphoneon);
            }
        } else {
            if (member.getIsSpeaker() == 0) {
                mDataBinding.ivAudio.setVisibility(View.GONE);
            } else {
                mDataBinding.ivAudio.setVisibility(View.VISIBLE);
                if (member.getIsMuted() == 1) {
                    mDataBinding.ivAudio.setImageResource(R.mipmap.icon_microphoneoff);
                } else if (member.getIsSelfMuted() == 1) {
                    mDataBinding.ivAudio.setImageResource(R.mipmap.icon_microphoneoff);
                } else {
                    mDataBinding.ivAudio.setImageResource(R.mipmap.icon_microphoneon);
                }
            }
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

                        ToastUtile.toastShort(ChatRoomActivity.this, R.string.error_room_not_exsit);
                        RoomManager.Instance(ChatRoomActivity.this).leaveRoom();
                        finish();
                    }

                    @Override
                    public void handleSuccess(@Nullable Room room) {
                        if (RoomManager.isLeaving) {
                            return;
                        }

                        if (room == null) {
                            ToastUtile.toastShort(ChatRoomActivity.this, R.string.error_room_not_exsit);
                            RoomManager.Instance(ChatRoomActivity.this).leaveRoom();
                            finish();
                            return;
                        }

                        Member mine = RoomManager.Instance(ChatRoomActivity.this).getMine();
                        if (mine == null) {
                            RoomManager.Instance(ChatRoomActivity.this).leaveRoom();
                            finish();
                            return;
                        }

                        RoomManager.Instance(ChatRoomActivity.this)
                                .getMember(mine.getUserId().getObjectId())
                                .observeOn(AndroidSchedulers.mainThread())
                                .compose(mLifecycleProvider.bindToLifecycle())
                                .subscribe(new DataMaybeObserver<Member>(ChatRoomActivity.this) {
                                    @Override
                                    public void handleError(@NonNull BaseError e) {
                                        if (RoomManager.isLeaving) {
                                            return;
                                        }

                                        ToastUtile.toastShort(ChatRoomActivity.this, R.string.error_room_not_exsit);
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
        mDataBinding.tvName.setText(room.getChannelName());
    }

    private void onLoadRoomMembers(@NonNull List<Member> members) {
        RoomManager.Instance(this).onLoadRoomMembers(members);

        for (Member member : members) {
            if (member.getIsSpeaker() == 0) {
                mListenerAdapter.addItem(member);
            } else {
                mSpeakerAdapter.addItem(member);
            }
        }
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
            mDataBinding.ivNews.setVisibility(View.VISIBLE);
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
            mDataBinding.ivNews.setVisibility(View.GONE);
//            mDataBinding.ivExit.setVisibility(View.INVISIBLE);
        }
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
            case R.id.ivChat:
               showInputView();
                break;
        }
    }

    private void showCloseRoomDialog() {
        if (isOwner()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.room_dialog_close_title)
                    .setMessage(R.string.room_dialog_close_message)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exitRoom(true);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("退出房间")
                    .setMessage("悄悄滴离开房间")
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exitRoom(true);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }

    }

    private void exitRoom(boolean leaveRoom) {
        if (leaveRoom) {
            if (isOwner()){//群主解散聊天室
                ChatRoomManager.getInstance().destroyChatRoom(chatRoomId, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        RoomManager.Instance(ChatRoomActivity.this).leaveRoom();
                    }

                    @Override
                    public void onError(int i, String s) { }

                    @Override
                    public void onProgress(int i, String s) { }
                });
            } else {//普通成员离开聊天室
                ChatRoomManager.getInstance().leaveChatRoom(EMClient.getInstance().getCurrentUser());
                RoomManager.Instance(ChatRoomActivity.this).leaveRoom();
            }
        } else {
            RoomManager.Instance(this).onEnterMinStatus();
        }

        finish();
        if (!leaveRoom) {
            overridePendingTransition(R.anim.chat_room_in, R.anim.chat_room_out);
        }
    }

    private void showInputView() {
        mDataBinding.bottomBar.setVisibility(View.GONE);
        mDataBinding.roomMessagesView.setShowInputView(true);
        mDataBinding.roomMessagesView.getInputView().requestFocus();
        mDataBinding.roomMessagesView.getInputView().requestFocusFromTouch();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.showKeyboard(mDataBinding.roomMessagesView.getInputView());
            }
        },200);

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

        mDataBinding.ivAudio.setEnabled(false);
        RoomManager.Instance(this)
                .toggleSelfAudio()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mLifecycleProvider.bindToLifecycle())
                .subscribe(new DataCompletableObserver(this) {
                    @Override
                    public void handleError(@NonNull BaseError e) {
                        ToastUtile.toastShort(ChatRoomActivity.this, e.getMessage());
                        mDataBinding.ivAudio.setEnabled(true);
                    }

                    @Override
                    public void handleSuccess() {
                        mDataBinding.ivAudio.setEnabled(true);
                        refreshVoiceView();
                    }
                });
    }

    private void refreshHandUpView() {
        if (RoomManager.Instance(this).isOwner()) {
            mDataBinding.ivHandUp.setVisibility(View.GONE);
        } else {
            mDataBinding.ivHandUp.setImageResource(R.mipmap.icon_un_handup);

            Member member = RoomManager.Instance(this).getMine();
            if (member == null) {
                return;
            }
            mDataBinding.ivHandUp.setVisibility(member.getIsSpeaker() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void toggleHandUp() {
        mDataBinding.ivHandUp.setEnabled(false);
        RoomManager.Instance(this)
                .requestHandsUp()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mLifecycleProvider.bindToLifecycle())
                .subscribe(new DataCompletableObserver(this) {
                    @Override
                    public void handleError(@NonNull BaseError e) {
                        ToastUtile.toastShort(ChatRoomActivity.this, e.getMessage());
                        mDataBinding.ivHandUp.setEnabled(true);
                    }

                    @Override
                    public void handleSuccess() {
                        refreshHandUpView();
                        mDataBinding.ivHandUp.setEnabled(true);
                        ToastUtile.toastShort(ChatRoomActivity.this, R.string.request_handup_success);
                    }
                });
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

    @Override
    public void onOwnerLeaveRoom(@NonNull Room room) {
        ToastUtile.toastShort(this, R.string.room_closed);
        finish();
    }

    @Override
    public void onLeaveRoom(@NonNull Room room) {

    }

    @Override
    public void onMemberJoin(@NonNull Member member) {
        if (member.getIsSpeaker() == 0) {
            mListenerAdapter.addItem(member);
        } else {
            if (isOwner(member)) {
                mSpeakerAdapter.addItem(member, 0);
            } else {
                mSpeakerAdapter.addItem(member);
            }
        }
    }

    @Override
    public void onMemberLeave(@NonNull Member member) {
        mSpeakerAdapter.deleteItem(member);
        mListenerAdapter.deleteItem(member);
    }

    @Override
    public void onRoleChanged(boolean isMine, @NonNull Member member) {
        if (member.getIsSpeaker() == 1) {
            mSpeakerAdapter.addItem(member);
            mListenerAdapter.deleteItem(member);
        } else {
            mSpeakerAdapter.deleteItem(member);
            mListenerAdapter.addItem(member);
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
            if (member.getIsMuted() == 1) {
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
        mDataBinding.ivNews.setCount(DataRepositroy.Instance(this).getHandUpListCount());
    }

    @Override
    public void onHandUpAgree(@NonNull Member member) {
        refreshHandUpView();

        if (isOwner()) {
            mDataBinding.ivNews.setCount(DataRepositroy.Instance(this).getHandUpListCount());
        }
    }

    @Override
    public void onHandUpRefuse(@NonNull Member member) {
        if (isMine(member)) {
            ToastUtile.toastShort(this, R.string.handup_refuse);
        }

        refreshHandUpView();

        if (isOwner()) {
            mDataBinding.ivNews.setCount(DataRepositroy.Instance(this).getHandUpListCount());
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

    private boolean isMine(Member member) {
        return RoomManager.Instance(this).isMine(member);
    }

    private boolean isOwner() {
        return RoomManager.Instance(this).isOwner();
    }

    private boolean isOwner(Member member) {
        return RoomManager.Instance(this).isOwner(member);
    }

    @Override
    protected void onDestroy() {
        RtcManager.Instance(this).removeHandler(mIRtcEngineEventHandler);
        RoomManager.Instance(this).removeRoomEventCallback(this);
        DemoMsgHelper.getInstance().removeCustomMsgLisenter();
        closeInviteDialog();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onChatRoomOwnerChanged(String chatRoomId, String newOwner, String oldOwner) {
    }

    @Override
    public void onChatRoomMemberAdded(String participant) {
        EMLog.e(TAG,participant+" 进入直播间");
    }

    @Override
    public void onChatRoomMemberExited(String participant) {

    }

    @Override
    public void onMessageReceived() {
        mDataBinding.roomMessagesView.refreshSelectLast();
    }

    @Override
    public void onMessageSelectLast() {

    }

    @Override
    public void onMessageChanged() {
    }


    /**
     * 礼物消息
     * @param message
     */
    @Override
    public void onReceiveGiftMsg(EMMessage message) { }

    /**
     * 点赞消息
     * @param message
     */
    @Override
    public void onReceivePraiseMsg(EMMessage message) { }

    /**
     * 弹幕消息
     * @param message
     */
    @Override
    public void onReceiveBarrageMsg(EMMessage message) {
        ThreadManager.getInstance().runOnMainThread(()-> {
            mDataBinding.singleBarrageView.addData(message);
        });

    }
}
