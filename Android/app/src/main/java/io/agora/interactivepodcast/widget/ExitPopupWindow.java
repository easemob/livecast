package io.agora.interactivepodcast.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import io.agora.interactivepodcast.R;
import io.agora.interactivepodcast.utils.ScreenUtils;

public class ExitPopupWindow extends PopupWindow {

    private View contentView;
    private OnItemClickListener mListener;


    public ExitPopupWindow(Activity context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.popup_window_layout, null);
        int h = context.getWindowManager().getDefaultDisplay().getHeight();
        int w = context.getWindowManager().getDefaultDisplay().getWidth();
        // 设置SelectPicPopupWindow的View
        this.setContentView(contentView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(w);
        // 设置SelectPicPopupWindow弹出窗体的高
//        this.setHeight((ViewGroup.LayoutParams.MATCH_PARENT - ScreenUtils.dp2px(context,50f))/2);
        this.setHeight(ScreenUtils.getScreenHeight(context)/3);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        // 刷新状态
        this.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);
        // mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        // 设置SelectPicPopupWindow弹出窗体动画效果
//        this.setAnimationStyle(R.style.AnimationPreview);
        RelativeLayout powerLayout = (RelativeLayout) contentView.findViewById(R.id.rl_power);
        powerLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mListener != null) {
                    mListener.onExitClick();
                    dismiss();
                }
            }
        });

    }

    /**
     * 定义一个接口，公布出去 在Activity中操作按钮的单击事件
     */
    public interface OnItemClickListener {
        void onExitClick();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    /**
     * 显示popupWindow
     *
     * @param parent
     */
    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            // 以下拉方式显示popupwindow
//            this.showAsDropDown(parent, parent.getLayoutParams().width , parent.getLayoutParams().height);
            contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int xOffset = parent.getWidth();
            this.showAsDropDown(parent,xOffset,0);
//            this.showAtLocation(contentView, Gravity.BOTTOM,0,ScreenUtils.getScreenHeight(parent.getContext())/2);
        } else {
            this.dismiss();
        }
    }

    @Override
    public void showAsDropDown(View anchor) {
        if(Build.VERSION.SDK_INT >= 24) {
            Rect rect = new Rect();
            anchor.getGlobalVisibleRect(rect);
            DisplayMetrics outMetrics = new DisplayMetrics();
            Context context = anchor.getContext();
            ((Activity) context).getWindowManager().getDefaultDisplay().getRealMetrics(outMetrics);
            int h = outMetrics.heightPixels - rect.bottom;
            setHeight(h);
        }
        super.showAsDropDown(anchor);
    }

}
