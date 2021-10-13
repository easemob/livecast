package io.agora.interactivepodcast.widget;

import androidx.annotation.NonNull;

import io.agora.baselibrary.base.DataBindBaseDialog;
import io.agora.interactivepodcast.common.OnResourceParseCallback;
import io.agora.interactivepodcast.common.ThreadManager;
import io.agora.interactivepodcast.common.enums.Status;
import io.agora.interactivepodcast.common.reponsitories.Resource;
import io.agora.interactivepodcast.databinding.DialogCreateRoomBinding;
import io.agora.interactivepodcast.utils.Utils;

public abstract class AppBaseDialog extends DataBindBaseDialog<DialogCreateRoomBinding> {
    /**
     * 解析Resource<T>
     * @param response
     * @param callback
     * @param <T>
     */
    public <T> void parseResource(Resource<T> response, @NonNull OnResourceParseCallback<T> callback) {
        if(response == null) {
            return;
        }
        if(response.status == Status.SUCCESS) {
            callback.hideLoading();
            callback.onSuccess(response.data);
        }else if(response.status == Status.ERROR) {
            ThreadManager.getInstance().runOnMainThread(()-> {
                callback.hideLoading();
                if(!callback.hideErrorMsg) {
//                    showToast(response.getMessage());
                }
                callback.onError(response.errorCode, response.getMessage());
            });

        }else if(response.status == Status.LOADING) {
            callback.onLoading();
        }
    }

//    public void showToast(final String toastContent){
//        Utils.showToast(this, toastContent);
//    }
//
//    public void showLongToast(final String toastContent){
//        Utils.showLongToast(this, toastContent);
//    }
}
