package io.agora.interactivepodcast.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.agora.data.manager.UserManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.util.EMLog;

import io.agora.baselibrary.base.DataBindBaseActivity;
import io.agora.interactivepodcast.R;
import io.agora.interactivepodcast.common.DemoHelper;
import io.agora.interactivepodcast.common.OnResourceParseCallback;
import io.agora.interactivepodcast.common.PreferenceManager;
import io.agora.interactivepodcast.data.UserRepository;
import io.agora.interactivepodcast.data.model.User;
import io.agora.interactivepodcast.databinding.ActivitySplashBinding;
import io.agora.interactivepodcast.viewmodels.LoginViewModel;

/**
 * 闪屏界面
 *
 * @author chenhengfei@agora.io
 */
public class SplashActivity extends AppBaseActivity {
    private ImageView ivIcon;
    private TextView tvWelcome;
    private LoginViewModel viewModel;


    @Override
    protected void iniBundle(@NonNull Bundle bundle) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void iniView() {
        ivIcon = findViewById(R.id.iv_icon);
        tvWelcome = findViewById(R.id.tv_welcome);
    }

    @Override
    protected void iniListener() {

    }

    @Override
    protected void iniData() {
        UserManager.Instance(this).loginIn();

//        startActivity(new Intent(this, RoomListActivity.class));
//        finish();
        //初始化demo user数据
//        UserRepository.getInstance().init(mContext);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        AlphaAnimation animation = new AlphaAnimation(0, 1f);
        animation.setDuration(500);
        ivIcon.startAnimation(animation);
        tvWelcome.startAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                skipToTarget();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

    private void skipToTarget() {
        //登录过
        if(EMClient.getInstance().isLoggedInBefore() && EMClient.getInstance().isSdkInited()){

            PreferenceManager.init(mContext, EMClient.getInstance().getCurrentUser());
            EMLog.e("SplashActivity", "skipToTarget  : "+ EMClient.getInstance().getCurrentUser());

            UserRepository.getInstance().getAdminToken(this);
            DemoHelper.saveUserId();
            DemoHelper.initDb();
            startActivity(new Intent(this, RoomListActivity.class));
            finish();
        }else {
            if(DemoHelper.isCanRegister()) {
//                startActivity(new Intent(this, LoginActivity.class));
//                finish();
            }else {
                //创建临时账号
                createRandomUser();
            }

        }
    }

    private void createRandomUser() {
        ProgressDialog pd = new ProgressDialog(mContext);
        pd.setMessage("请稍等...");
        pd.setCanceledOnTouchOutside(false);

        viewModel.getLoginObservable().observe(mContext, response -> {
            parseResource(response, new OnResourceParseCallback<User>() {
                @Override
                public void onSuccess(User data) {
                    EMLog.e("SplashActivity", "onSuccess: "+ data.getUsername());
                    skipToTarget();
                }

                @Override
                public void onLoading() {
                    super.onLoading();
                    pd.show();
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    pd.dismiss();
                }
            });
        });
        String userName = UserManager.Instance(this).getUserName();
        User user = new User();
        user.setUsername(userName);
        user.setId(userName);
        viewModel.login(user);
    }
}
