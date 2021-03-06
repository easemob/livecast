package io.agora.interactivepodcast.widget;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.agora.data.BaseError;
import com.agora.data.DataRepositroy;
import com.agora.data.manager.UserManager;
import com.agora.data.model.Room;
import com.agora.data.model.User;
import com.agora.data.observer.DataObserver;
import com.hyphenate.util.EMLog;

import java.util.Random;

import io.agora.baselibrary.util.ToastUtile;
import io.agora.interactivepodcast.R;
import io.agora.interactivepodcast.activity.ChatRoomActivity;
import io.agora.interactivepodcast.activity.ChatRoomTestActivity;
import io.agora.interactivepodcast.common.ThreadManager;
import io.agora.interactivepodcast.data.JsonCallback;
import io.agora.interactivepodcast.data.UserRepository;
import io.agora.interactivepodcast.data.model.ChatRoomResp;
import io.reactivex.android.schedulers.AndroidSchedulers;
import okhttp3.internal.http2.Header;

/**
 * 创建房间
 *
 * @author chenhengfei@agora.io
 */
public class CreateRoomDialog extends AppBaseDialog implements View.OnClickListener {
    private static final String TAG = CreateRoomDialog.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Window win = getDialog().getWindow();
        WindowManager windowManager = win.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams params = win.getAttributes();
        params.width = display.getWidth() * 4 / 5;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        win.setAttributes(params);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Dialog_Nomal);
    }

    @Override
    public void iniBundle(@NonNull Bundle bundle) {

    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_create_room;
    }

    @Override
    public void iniView() {

    }

    @Override
    public void iniListener() {
        mDataBinding.btCancel.setOnClickListener(this);
        mDataBinding.ivRefresh.setOnClickListener(this);
        mDataBinding.btCreate.setOnClickListener(this);
    }

    @Override
    public void iniData() {
        refreshName();
    }

    public void show(@NonNull FragmentManager manager) {
        super.show(manager, TAG);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btCancel) {
            dismiss();
        } else if (id == R.id.ivRefresh) {
            refreshName();
        } else if (id == R.id.btCreate) {
            create();
        }
    }

    public static String radomName() {
        return "Room " + String.valueOf(new Random().nextInt(999999));
    }

    private void refreshName() {
        mDataBinding.etInput.setText(radomName());
    }

    private void create()  {
        String roomName = mDataBinding.etInput.getText().toString();
        if (TextUtils.isEmpty(roomName)) {
            return;
        }

        User user = UserManager.Instance(requireContext()).getUserLiveData().getValue();
        if (user == null) {
            return;
        }

        Room mRoom = new Room();
        mRoom.setAnchorId(user);
        mRoom.setChannelName(roomName);



        UserRepository.getInstance().createChatRoom(roomName, this, new JsonCallback<ChatRoomResp>(ChatRoomResp.class) {
            @Override
            public void onError(int status, String errorMsg) {
                EMLog.e(TAG, "onError: "+errorMsg );
                ToastUtile.toastShort(requireContext(), errorMsg);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, ChatRoomResp chatRoomResp) {
                EMLog.e(TAG, "onSuccess: 聊天室 聊天室创建成功" );
                ThreadManager.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mDataBinding.btCreate.setEnabled(false);

                        mRoom.setChatroomId(chatRoomResp.getData().getId());
        //                mRoom.setOwner(UserRepository.getInstance().getCurrentUser());
                        DataRepositroy.Instance(requireContext())
                                .creatRoom(mRoom)
                                .observeOn(AndroidSchedulers.mainThread())
                                .compose(mLifecycleProvider.bindToLifecycle())
                                .subscribe(new DataObserver<Room>(requireContext()) {
                                    @Override
                                    public void handleError(@NonNull BaseError e) {
                                        mDataBinding.btCreate.setEnabled(true);
                                        ToastUtile.toastShort(requireContext(), e.getMessage());
                                    }

                                    @Override
                                    public void handleSuccess(@NonNull Room room) {
                                        mDataBinding.btCreate.setEnabled(true);
                                        room.setAnchorId(user);
//                                        Intent intent = ChatRoomActivity.newIntent(requireContext(), room);
                                        Intent intent = ChatRoomTestActivity.newIntent(requireContext(), room);
                                        startActivity(intent);
                                        dismiss();
                                    }
                                });
                    }
                });
            }
        });

    }
}
