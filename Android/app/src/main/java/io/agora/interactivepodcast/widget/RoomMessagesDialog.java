package io.agora.interactivepodcast.widget;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agora.data.model.Room;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMCustomMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.util.EMLog;

import java.util.Map;

import io.agora.baselibrary.base.DataBindBaseDialog;
import io.agora.interactivepodcast.R;
import io.agora.interactivepodcast.common.DemoHelper;
import io.agora.interactivepodcast.common.SoftKeyboardChangeHelper;
import io.agora.interactivepodcast.databinding.DialogRoomMessagesBinding;
import io.agora.interactivepodcast.msg.DemoMsgHelper;
import io.agora.interactivepodcast.utils.DemoConstants;
import io.agora.interactivepodcast.utils.Utils;

public class RoomMessagesDialog extends DataBindBaseDialog<DialogRoomMessagesBinding> {
    private static final String TAG = RoomMessagesDialog.class.getSimpleName();
    private static final String TAG_ROOM = "room";
    private Room room;
    private EMConversation conversation;
    ListAdapter adapter;
    boolean isBarrageMsg;
    private int barrageOriginMarginTop;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Window win = getDialog().getWindow();
        WindowManager.LayoutParams params = win.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        win.setAttributes(params);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Dialog_Bottom);
    }

    @Override
    public void iniBundle(@NonNull Bundle bundle) {
        room = (Room) bundle.getSerializable(TAG_ROOM);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_room_messages;
    }

    @Override
    public void iniView() {
    }

    @Override
    public void iniListener() {

    }

    public void show(@NonNull FragmentManager manager, @NonNull Room room) {
        Bundle intent = new Bundle();
        intent.putSerializable(TAG_ROOM, room);
        setArguments(intent);
        super.show(manager, TAG);
    }

    @Override
    public void iniData() {
        EMLog.e(TAG," iniData() ");
        addSoftKeyboardListener();
        conversation = EMClient.getInstance().chatManager().getConversation(room.getChatroomId(), EMConversation.EMConversationType.ChatRoom, true);
        adapter = new ListAdapter(getContext(), conversation);
        mDataBinding.listview.setLayoutManager(new LinearLayoutManager(getContext()));
        mDataBinding.listview.setAdapter(adapter);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setSize(0, (int) Utils.dip2px(getContext(), 4));
        itemDecoration.setDrawable(drawable);
        mDataBinding.listview.addItemDecoration(itemDecoration);
        mDataBinding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(messageViewListener != null){
                    if(TextUtils.isEmpty(mDataBinding.editText.getText())){
                        Toast.makeText(getContext(), "文字内容不能为空！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    messageViewListener.onMessageSend(mDataBinding.editText.getText().toString(), isBarrageMsg);
                    mDataBinding.editText.setText("");
                }
            }
        });
//        closeView.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                hideSoftKeyBoard();
//                if(messageViewListener != null){
//                    messageViewListener.onHiderBottomBar();
//                }
//            }
//        });
        mDataBinding.switchMsgType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isBarrageMsg = isChecked;
            }
        });
        mDataBinding.listview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                hideSoftKeyBoard();
                hideKeyboard();
                if (mDataBinding.containerSend.getVisibility() == View.INVISIBLE) {
                    setShowInputView(true);
                }
                return false;
            }
        });
    }


    private void addSoftKeyboardListener() {
        if(getContext() instanceof Activity) {
            SoftKeyboardChangeHelper.setOnSoftKeyboardChangeListener((Activity) getContext(), new SoftKeyboardChangeHelper.OnSoftKeyboardChangeListener() {
                @Override
                public void keyboardShow(int height) {

                    ViewGroup parent = (ViewGroup) (mDataBinding.getRoot().getParent());
                    startAnimation(height, 100, new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int value = (int) animation.getAnimatedValue();

                            parent.scrollTo(0, value);
                        }
                    });

                    int childCount = parent.getChildCount();
                    for(int i = 0; i < childCount; i++) {
                        View child = parent.getChildAt(i);
                        if(child instanceof SingleBarrageView) {
                            child.setBackgroundColor(Color.parseColor("#01ffffff"));
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) child.getLayoutParams();
                            barrageOriginMarginTop = params.topMargin;
                            startAnimation(height - barrageOriginMarginTop * 3, 100, new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    params.topMargin = (int) animation.getAnimatedValue() + barrageOriginMarginTop;
                                    child.setLayoutParams(params);
                                }
                            });
                        }
//                        if(child instanceof ShowGiveGiftView) {
//                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) child.getLayoutParams();
//                            giftOiginMarginBottom = params.bottomMargin;
//                            startAnimation(giftOiginMarginBottom  - 10, 100, new ValueAnimator.AnimatorUpdateListener() {
//                                @Override
//                                public void onAnimationUpdate(ValueAnimator animation) {
//                                    params.bottomMargin = giftOiginMarginBottom - (int) animation.getAnimatedValue();
//                                    child.setLayoutParams(params);
//                                }
//                            });
//                        }
                    }
                }

                @Override
                public void keyboardHide(int height) {
                    ViewGroup parent = (ViewGroup) (mDataBinding.getRoot().getParent());
                    startAnimation(height, 100, new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int value = (int) animation.getAnimatedValue();
                            parent.scrollTo(0, height - value);
                        }
                    });
                    int childCount = parent.getChildCount();
                    for(int i = 0; i < childCount; i++) {
                        View child = parent.getChildAt(i);
                        if(child instanceof SingleBarrageView) {
                            child.setBackground(null);
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) child.getLayoutParams();
                            startAnimation(height - barrageOriginMarginTop * 3, 100, new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    params.topMargin = height - barrageOriginMarginTop * 2 -  (int) animation.getAnimatedValue();
                                    child.setLayoutParams(params);
                                }
                            });
                        }
//                        if(child instanceof ShowGiveGiftView) {
//                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) child.getLayoutParams();
//                            startAnimation(giftOiginMarginBottom - 10, 100, new ValueAnimator.AnimatorUpdateListener() {
//                                @Override
//                                public void onAnimationUpdate(ValueAnimator animation) {
//                                    params.bottomMargin = 10 + (int) animation.getAnimatedValue();
//                                    child.setLayoutParams(params);
//                                }
//                            });
//                        }
                    }
                    setShowInputView(false);
                    if(messageViewListener != null){
                        messageViewListener.onHiderBottomBar();
                    }
                }
            });
        }

    }

    /**
     * 开始动画
     * @param values
     * @param listener
     */
    private void startAnimation(int values, int duration, ValueAnimator.AnimatorUpdateListener listener) {
        ValueAnimator animator = ValueAnimator.ofInt(values);
        animator.addUpdateListener(listener);
        animator.setDuration(duration);
        animator.start();
    }

    public void hideKeyboard() {
        Utils.hideKeyboard(mDataBinding.getRoot());
    }

    public void setShowInputView(boolean showInputView){
        if(showInputView){
            mDataBinding.containerSend.setVisibility(View.VISIBLE);
        }else{
            mDataBinding.containerSend.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 隐藏输入框及软键盘
     */
    public void hideSoftKeyBoard() {
        hideKeyboard();
        setShowInputView(false);
    }

    private RoomMessagesView.MessageViewListener messageViewListener;
    public interface MessageViewListener{
        void onMessageSend(String content, boolean isBarrageMsg);
        void onItemClickListener(EMMessage message);
        void onHiderBottomBar();
    }

    public void setMessageViewListener(RoomMessagesView.MessageViewListener messageViewListener){
        this.messageViewListener = messageViewListener;
    }

    public void refresh(){
        if(adapter != null){
            adapter.refresh();
        }
    }

    public void refreshSelectLast(){
        if(adapter != null){
            adapter.refresh();
            mDataBinding.listview.smoothScrollToPosition(adapter.getItemCount()-1);
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<MyViewHolder>{

        private final Context context;
        EMMessage[] messages;


        public ListAdapter(Context context, EMConversation conversation){
            this.context = context;
            messages = conversation.getAllMessages().toArray(new EMMessage[0]);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_room_msgs_item, parent, false));
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final EMMessage message = messages[position];
            String from = message.getFrom();
//            String nickName = DemoHelper.getNickName(from);
            String nickName = DemoHelper.getNickName(from);
            boolean isSelf = EMClient.getInstance().getCurrentUser().equals(from);
            if(message.getBody() instanceof EMTextMessageBody) {
                boolean memberAdd = false;
                Map<String, Object> ext = message.ext();
                if(ext.containsKey(DemoConstants.MSG_KEY_MEMBER_ADD)) {
                    memberAdd = (boolean) ext.get(DemoConstants.MSG_KEY_MEMBER_ADD);
                }
                String content = ((EMTextMessageBody) message.getBody()).getMessage();
                if(memberAdd) {
                    showMemberAddMsg(holder.name, nickName, content);
                }else {
                    showText(holder.name, nickName, isSelf, content);
                }
            }else if(message.getBody() instanceof EMCustomMessageBody) {
                DemoMsgHelper msgHelper = DemoMsgHelper.getInstance();
               /* if(msgHelper.isGiftMsg(message)) {
                    showGiftMessage(holder.name, nickName, isSelf, message);
                }else if(msgHelper.isPraiseMsg(message)) {
                    showPraiseMessage(holder.name, nickName, isSelf, message);
                }else*/ if(msgHelper.isBarrageMsg(message)) {
                    showBarrageMessage(holder.name, nickName, isSelf, message);
                }
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideSoftKeyBoard();
                    if (messageViewListener != null) {
                        messageViewListener.onItemClickListener(message);
                    }
                }
            });
        }

        private void showMemberAddMsg(TextView name, String nickName, String content) {
            StringBuilder builder = new StringBuilder();
            builder.append(nickName).append(" ").append(context.getString(R.string.em_live_msg_member_add));
            SpannableString span = new SpannableString(builder.toString());
            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.white)), 0, nickName.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.gray)),
                    nickName.length() + 1, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            name.setText(span);
        }

        private void showText(TextView name, String nickName, boolean isSelf, String content) {
            StringBuilder builder = new StringBuilder();
            builder.append(nickName).append(":").append(content);
            SpannableString span = new SpannableString(builder.toString());
            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.common_white)), 0, nickName.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new ForegroundColorSpan(isSelf ? ContextCompat.getColor(getContext(), R.color.color_room_my_msg) : Color.parseColor("#FFC700")),
                    nickName.length() + 1, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            name.setText(span);
        }

//        private void showGiftMessage(TextView name, String nickName, boolean isSelf, EMMessage message) {
//            GiftBean bean = DemoHelper.getGiftById(DemoMsgHelper.getInstance().getMsgGiftId(message));
//            int num = DemoMsgHelper.getInstance().getMsgGiftNum(message);
//            String giftName = "礼物";
//            if(bean != null) {
//                giftName = bean.getName();
//            }
//            String content = context.getString(R.string.em_live_msg_gift, nickName, giftName, num);
//            SpannableString span = new SpannableString(content);
//            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.white)), 0, nickName.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.gray)),
//                    nickName.length() + 1, nickName.length() + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            span.setSpan(new ForegroundColorSpan(Color.parseColor("#ff68ff95")),
//                    nickName.length() + 4, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            name.setText(span);
//        }

        private void showPraiseMessage(TextView name, String nickName, boolean isSelf, EMMessage message) {
            String content = context.getString(R.string.em_live_msg_like, nickName, DemoMsgHelper.getInstance().getMsgPraiseNum(message));
            SpannableString span = new SpannableString(content);
            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.white)), 0, nickName.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.gray)),
                    nickName.length() + 1, nickName.length() + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new ForegroundColorSpan(Color.parseColor("#ff68ff95")),
                    nickName.length() + 3, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            name.setText(span);
        }

        private void showBarrageMessage(TextView name, String nickName, boolean isSelf, EMMessage message) {
            showText(name, nickName, isSelf, DemoMsgHelper.getInstance().getMsgBarrageTxt(message));
        }

        @Override
        public int getItemCount() {
            return messages.length;
        }

        public void refresh(){
            messages = conversation.getAllMessages().toArray(new EMMessage[0]);
            ((Activity)getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

    }


    private class MyViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView content;
        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            content = (TextView) itemView.findViewById(R.id.content);
        }
    }
}
