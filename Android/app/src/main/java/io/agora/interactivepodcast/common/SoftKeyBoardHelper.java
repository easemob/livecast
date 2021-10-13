package io.agora.interactivepodcast.common;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

public class SoftKeyBoardHelper {
    public interface OnSoftKeyboardChangeListener {
        void keyBoardShow(int height );
        void keyBoardHide(int height);
    }

    private static class HeightWrapper {
        int height;
    }

    public static void setListener(Activity activity, final OnSoftKeyboardChangeListener listener) {
        final View rootView = activity.getWindow().getDecorView();
        final HeightWrapper wrapper = new HeightWrapper();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int height = r.height();
                if (wrapper.height == 0) {
                    wrapper.height = height;
                } else {
                    int diff = wrapper.height - height;
                    if (diff > 200) {
                        if (listener != null) {
                            listener.keyBoardShow(diff);
                        }
                        wrapper.height = height;
                    } else if (diff < -200) {
                        if (listener != null) {
                            listener.keyBoardHide(diff);
                        }
                        wrapper.height = height;
                    }
                }
            }
        });
    }
}
