package com.sharry.picturepicker.watcher.impl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Transition;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.sharry.picturepicker.R;
import com.sharry.picturepicker.support.loader.PictureLoader;
import com.sharry.picturepicker.watcher.manager.WatcherConfig;
import com.sharry.picturepicker.widget.CheckedIndicatorView;
import com.sharry.picturepicker.widget.DraggableViewPager;
import com.sharry.picturepicker.widget.photoview.OnPhotoTapListener;
import com.sharry.picturepicker.widget.photoview.PhotoView;
import com.sharry.picturepicker.widget.toolbar.AppBarHelper;
import com.sharry.picturepicker.widget.toolbar.CommonToolbar;
import com.sharry.picturepicker.widget.toolbar.Style;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 图片查看器的 Activity, 主题设置为背景透明效果更佳
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.3
 * @since 2018/9/22 23:24
 */
@SuppressLint("NewApi")
public class PictureWatcherActivity extends AppCompatActivity implements
        PictureWatcherContract.IView,
        DraggableViewPager.OnPagerChangedListener {

    // 启动时的 Extra
    public static final String START_EXTRA_CONFIG = "start_intent_extra_config";
    public static final String START_EXTRA_SHARED_ELEMENT = "start_intent_extra_shared_element";

    // 返回时的 Extra
    public static final String RESULT_EXTRA_PICKED_PICTURES = "result_extra_picked_pictures";// 返回的图片
    public static final String RESULT_EXTRA_IS_PICKED_ENSURE = "result_extra_is_picked_ensure";// 是否是确认选择

    // Presenter
    private PictureWatcherContract.IPresenter mPresenter = new PictureWatcherPresenter();

    private TextView mTvTitle;
    private CheckedIndicatorView mCheckIndicator;
    private DraggableViewPager mViewPager;

    // 底部选中容器
    private LinearLayout mLlBottomPreviewContainer;
    private RecyclerView mBottomPreviewPictures;
    private TextView mTvEnsure;
    private ArrayList<PhotoView> mPhotoViews = new ArrayList<>();// 图片对象
    private WatcherPagerAdapter mWatcherPagerAdapter;// ViewPager 的 Adapter

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter.attach(this);
        parseIntent();
        setContentView(R.layout.libpicturepicker_activity_picture_watcher);
        initTitle();
        initViews();
        initData();
    }

    protected void parseIntent() {
        mPresenter.init(
                (WatcherConfig) getIntent().getParcelableExtra(START_EXTRA_CONFIG),
                getIntent().getBooleanExtra(START_EXTRA_SHARED_ELEMENT, false)
        );
    }

    protected void initTitle() {
        AppBarHelper.with(this).setStatusBarStyle(Style.TRANSPARENT).apply();
        CommonToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setAdjustToTransparentStatusBar(true);
        toolbar.addLeftIcon(0, R.drawable.libpicturepicker_common_arrow_right_white, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.handleBackPressed();
            }
        });
        toolbar.setTitleGravity(Gravity.LEFT);
        mTvTitle = toolbar.getTitleText();
        mTvTitle.setTextSize(20);
        // 添加右部的索引
        mCheckIndicator = new CheckedIndicatorView(this);
        mCheckIndicator.setVisibility(View.INVISIBLE);
        toolbar.addRightView(1, mCheckIndicator, 25, 25, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.handleToolbarCheckedIndicatorClick(mCheckIndicator.isChecked());
            }
        });
    }

    protected void initViews() {
        // 1. 初始化 ViewPager
        mViewPager = findViewById(R.id.view_pager);
        mWatcherPagerAdapter = new WatcherPagerAdapter(mPhotoViews);
        mViewPager.setAdapter(mWatcherPagerAdapter);
        mViewPager.setOnPagerChangedListener(this);
        // 2. 初始化底部菜单
        mLlBottomPreviewContainer = findViewById(R.id.ll_bottom_container);
        mBottomPreviewPictures = findViewById(R.id.recycle_pictures);
        mBottomPreviewPictures.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        mTvEnsure = findViewById(R.id.tv_ensure);
        mTvEnsure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.handleEnsureClick();
            }
        });
    }

    protected void initData() {
        mPresenter.start();
    }

    @Override
    public void setWindowEnterTransitions(Transition enterTransition) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(enterTransition);
    }

    @Override
    public void setWindowReturnTransitions(Transition returnTransition) {
        getWindow().setReturnTransition(returnTransition);
    }

    @Override
    public void setSharedElementEnterTransition(Transition enterTransition) {
        getWindow().setSharedElementEnterTransition(enterTransition);
    }

    @Override
    public void setSharedElementReturnTransition(Transition returnTransition) {
        getWindow().setSharedElementReturnTransition(returnTransition);
    }

    @Override
    public void setToolbarCheckedIndicatorVisibility(boolean isShowCheckedIndicator) {
        mCheckIndicator.setVisibility(isShowCheckedIndicator ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setToolbarCheckedIndicatorColors(int indicatorBorderCheckedColor, int indicatorBorderUncheckedColor,
                                                 int indicatorSolidColor, int indicatorTextColor) {
        mCheckIndicator.setBorderColor(indicatorBorderCheckedColor, indicatorBorderUncheckedColor);
        mCheckIndicator.setSolidColor(indicatorSolidColor);
        mCheckIndicator.setTextColor(indicatorTextColor);
    }

    @Override
    public void setPreviewAdapter(WatcherPreviewAdapter adapter) {
        mBottomPreviewPictures.setAdapter(adapter);
    }

    @Override
    public void createPhotoViews(ArrayList<String> pictureUris) {
        for (String uri : pictureUris) {
            PhotoView photoView = new PhotoView(this);
            photoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
                @Override
                public void onPhotoTap(ImageView view, float x, float y) {
                    onBackPressed();
                }
            });
            mPhotoViews.add(photoView);
        }
        mWatcherPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void bindSharedElementView(int position, String sharedKey) {
        // Postpone the shared element enter transition.
        postponeEnterTransition();
        // 绑定共享元素
        final PhotoView sharedElement = mPhotoViews.get(position);
        sharedElement.setTransitionName(sharedKey);
        sharedElement.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return true;
            }
        });
    }

    @Override
    public void notifySharedElementChanged(int sharedPosition, String sharedKey) {
        mViewPager.setSharedElementPosition(sharedPosition);
        mPhotoViews.get(sharedPosition).setTransitionName(sharedKey);
    }

    @Override
    public void notifyBottomPicturesRemoved(String removedPath, int removedIndex) {
        mBottomPreviewPictures.getAdapter().notifyItemRemoved(removedIndex);
    }

    @Override
    public void notifyBottomPictureAdded(String insertPath, int addedIndex) {
        mBottomPreviewPictures.getAdapter().notifyItemInserted(addedIndex);
    }

    @Override
    public void displayPictureAt(ArrayList<String> pictureUris, int curPosition) {
        mViewPager.setCurrentItem(curPosition);
        // 加载当前的位置的图片
        PhotoView curView = mPhotoViews.get(curPosition);
        if (curView != null && curView.getDrawable() == null) {
            PictureLoader.load(this, pictureUris.get(curPosition), curView);
        }
        // 加载前一个
        int beforeIndex = curPosition - 1;
        PhotoView beforeView = beforeIndex >= 0 ? mPhotoViews.get(beforeIndex) : null;
        if (beforeView != null && beforeView.getDrawable() == null) {
            PictureLoader.load(this, pictureUris.get(beforeIndex), beforeView);
        }
        // 加载后一个
        int afterIndex = curPosition + 1;
        PhotoView afterView = afterIndex < pictureUris.size() ? mPhotoViews.get(afterIndex) : null;
        if (afterView != null && afterView.getDrawable() == null) {
            PictureLoader.load(this, pictureUris.get(afterIndex), afterView);
        }
    }

    @Override
    public void setToolbarIndicatorChecked(boolean isChecked) {
        mCheckIndicator.setChecked(isChecked);
    }

    @Override
    public void displayToolbarIndicatorText(CharSequence indicatorText) {
        mCheckIndicator.setText(indicatorText);
    }

    @Override
    public void onPagerChanged(int position) {
        mPresenter.handlePagerChanged(position);
    }

    @Override
    public void displayPreviewEnsureText(CharSequence content) {
        mTvEnsure.setText(content);
    }

    @Override
    public void displayToolbarLeftText(CharSequence content) {
        mTvTitle.setText(content);
    }

    @Override
    public void setBottomPreviewVisibility(boolean nowVisible, final boolean destVisible) {
        if (nowVisible == destVisible) return;
        int startY = nowVisible ? 0 : mLlBottomPreviewContainer.getHeight();
        int endY = nowVisible ? mLlBottomPreviewContainer.getHeight() : 0;
        ObjectAnimator animator = ObjectAnimator.ofFloat(mLlBottomPreviewContainer,
                "translationY", startY, endY);
        animator.setDuration(200);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mLlBottomPreviewContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mLlBottomPreviewContainer.setVisibility(destVisible ? View.VISIBLE : View.INVISIBLE);
            }
        });
        animator.start();
    }

    @Override
    public void previewPicturesSmoothScrollToPosition(int position) {
        mBottomPreviewPictures.smoothScrollToPosition(position);
    }

    @Override
    public void showMsg(String msg) {
        Snackbar.make(mBottomPreviewPictures, msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void finish() {
        // 设置回调
        Intent intent = new Intent();
        intent.putExtra(RESULT_EXTRA_PICKED_PICTURES, mPresenter.getUserPicked());
        intent.putExtra(RESULT_EXTRA_IS_PICKED_ENSURE, mPresenter.isEnsurePressed());
        setResult(RESULT_OK, intent);
        super.finish();
        // 当前 Activity 关闭时, 使用淡入淡出的动画
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}