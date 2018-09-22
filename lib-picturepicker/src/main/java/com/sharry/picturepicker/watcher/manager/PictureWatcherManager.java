package com.sharry.picturepicker.watcher.manager;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import android.view.View;

import com.sharry.picturepicker.watcher.impl.PictureWatcherActivity;
import com.sharry.picturepicker.support.loader.IPictureLoader;
import com.sharry.picturepicker.support.loader.PictureLoader;
import com.sharry.picturepicker.support.permission.PermissionsCallback;
import com.sharry.picturepicker.support.permission.PermissionsManager;
import com.sharry.picturepicker.support.utils.VersionUtil;

import java.util.ArrayList;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import static com.sharry.picturepicker.watcher.impl.PictureWatcherActivity.START_EXTRA_SHARED_ELEMENT;

/**
 * Created by Sharry on 2018/6/19.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.0
 * Description: 图片查看器的管理类
 */
public class PictureWatcherManager {

    public static final String TAG = PictureWatcherManager.class.getSimpleName();

    public static PictureWatcherManager with(@NonNull Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return new PictureWatcherManager(activity);
        } else {
            throw new IllegalArgumentException("PictureWatcherManager.with -> Context can not cast to Activity");
        }
    }

    private String[] mPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private Activity mActivity;
    private WatcherConfig mConfig;
    private PictureWatcherFragment mWatcherFragment;
    private View mTransitionView;

    private PictureWatcherManager(Activity activity) {
        this.mActivity = activity;
        this.mConfig = new WatcherConfig();
        this.mWatcherFragment = getCallbackFragment(mActivity);
    }

    /**
     * 选择的最大阈值
     */
    public PictureWatcherManager setThreshold(int threshold) {
        mConfig.threshold = threshold;
        return this;
    }

    /**
     * 需要展示的 URI
     */
    public PictureWatcherManager setPictureUri(@NonNull String uri) {
        ArrayList<String> pictureUris = new ArrayList<>();
        pictureUris.add(uri);
        setPictureUris(pictureUris, 0);
        return this;
    }

    /**
     * 需要展示的 URI 集合
     *
     * @param pictureUris 数据集合
     * @param position    展示的位置
     */
    public PictureWatcherManager setPictureUris(@NonNull ArrayList<String> pictureUris, int position) {
        mConfig.pictureUris = pictureUris;
        mConfig.position = position;
        return this;
    }

    /**
     * 设置用户已经选中的图片, 相册会根据 Path 比较, 在相册中打钩
     *
     * @param pickedPictures 已选中的图片
     */
    public PictureWatcherManager setUserPickedSet(@NonNull ArrayList<String> pickedPictures) {
        mConfig.userPickedSet = pickedPictures;
        return this;
    }

    /**
     * 设置共享元素
     */
    public PictureWatcherManager setSharedElement(View transitionView) {
        mTransitionView = transitionView;
        return this;
    }

    /**
     * 设置选择索引的边框颜色
     *
     * @param textColorId 边框的颜色 ID
     */
    public PictureWatcherManager setIndicatorTextColorRes(@ColorRes int textColorId) {
        return setIndicatorTextColor(ContextCompat.getColor(mActivity, textColorId));
    }

    /**
     * 设置选择索引的边框颜色
     *
     * @param textColor 边框的颜色
     */
    public PictureWatcherManager setIndicatorTextColor(@ColorInt int textColor) {
        mConfig.indicatorTextColor = textColor;
        return this;
    }

    /**
     * 设置选择索引的边框颜色
     *
     * @param solidColorId 边框的颜色 ID
     */
    public PictureWatcherManager setIndicatorSolidColorRes(@ColorRes int solidColorId) {
        return setIndicatorSolidColor(ContextCompat.getColor(mActivity, solidColorId));
    }

    /**
     * 设置选择索引的边框颜色
     *
     * @param solidColor 边框的颜色
     */
    public PictureWatcherManager setIndicatorSolidColor(@ColorInt int solidColor) {
        mConfig.indicatorSolidColor = solidColor;
        return this;
    }

    /**
     * 设置选择索引的边框颜色
     *
     * @param checkedColorId   选中的边框颜色
     * @param uncheckedColorId 未选中的边框颜色
     */
    public PictureWatcherManager setIndicatorBorderColorRes(@ColorRes int checkedColorId, @ColorRes int uncheckedColorId) {
        return setIndicatorBorderColor(ContextCompat.getColor(mActivity, checkedColorId),
                ContextCompat.getColor(mActivity, uncheckedColorId));
    }

    /**
     * 设置选择索引的边框颜色
     *
     * @param checkedColor   选中的边框颜色的 Res Id
     * @param uncheckedColor 未选中的边框颜色的Res Id
     */
    public PictureWatcherManager setIndicatorBorderColor(@ColorInt int checkedColor, @ColorInt int uncheckedColor) {
        mConfig.indicatorBorderCheckedColor = checkedColor;
        mConfig.indicatorBorderUncheckedColor = uncheckedColor;
        return this;
    }

    /**
     * 设置图片加载方案
     */
    public PictureWatcherManager setPictureLoader(IPictureLoader loader) {
        PictureLoader.setPictureLoader(loader);
        return this;
    }

    /**
     * 调用图片查看器的方法
     */
    public void start() {
        start(null);
    }

    /**
     * 调用图片查看器, 一般用于相册
     */
    public void start(final WatcherCallback callback) {
        // 1. 验证是否实现了图片加载器
        if (PictureLoader.getPictureLoader() == null) {
            throw new UnsupportedOperationException("PictureLoader.load -> please invoke setPictureLoader first");
        }
        // 2. 请求权限
        PermissionsManager.getManager(mActivity)
                .request(mPermissions)
                .execute(new PermissionsCallback() {
                    @Override
                    public void onResult(boolean granted) {
                        if (!granted) return;
                        if (callback != null) startForResultActual(callback);
                        else startActual();
                    }
                });
    }

    /**
     * 真正的执行 Activity 的启动(无回调)
     */
    private void startActual() {
        startForResultActual(null);
    }

    /**
     * 真正的执行 Activity 的启动(有回调)
     */
    private void startForResultActual(final WatcherCallback callback) {
        mWatcherFragment.setPickerCallback(callback);
        Intent intent = new Intent(mActivity, PictureWatcherActivity.class);
        intent.putExtra(PictureWatcherActivity.START_EXTRA_CONFIG, mConfig);
        // 5.0 以上的系统使用 Transition 跳转
        if (VersionUtil.isLollipop()) {
            ActivityOptions options = null;
            if (mTransitionView != null) {
                // 共享元素
                intent.putExtra(START_EXTRA_SHARED_ELEMENT, true);
                String transitionKey = mConfig.pictureUris.get(mConfig.position);
                mTransitionView.setTransitionName(transitionKey);
                options = ActivityOptions.makeSceneTransitionAnimation(
                        mActivity, Pair.create(mTransitionView, transitionKey));
            } else {
                options = ActivityOptions.makeSceneTransitionAnimation(mActivity);
            }
            mWatcherFragment.startActivityForResult(intent, PictureWatcherFragment.REQUEST_CODE_PICKED, options.toBundle());
        } else {
            mWatcherFragment.startActivityForResult(intent, PictureWatcherFragment.REQUEST_CODE_PICKED);
            mActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    /**
     * 获取用于回调的 Fragment
     */
    private PictureWatcherFragment getCallbackFragment(Activity activity) {
        PictureWatcherFragment pictureWatcherFragment = findCallbackFragment(activity);
        if (pictureWatcherFragment == null) {
            pictureWatcherFragment = PictureWatcherFragment.newInstance();
            FragmentManager fragmentManager = activity.getFragmentManager();
            fragmentManager.beginTransaction().add(pictureWatcherFragment, TAG).commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return pictureWatcherFragment;
    }

    /**
     * 在 Activity 中通过 TAG 去寻找我们添加的 Fragment
     */
    private PictureWatcherFragment findCallbackFragment(Activity activity) {
        return (PictureWatcherFragment) activity.getFragmentManager().findFragmentByTag(TAG);
    }

}
