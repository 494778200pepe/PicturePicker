package com.sharry.lib.picturepicker;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.sharry.lib.picturepicker.toolbar.SToolbar;
import com.sharry.lib.picturepicker.toolbar.TextViewOptions;

import java.util.ArrayList;

/**
 * 图片选择器的 Activity
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.3
 * @since 2018/9/1 10:17
 */
public class PickerActivity extends AppCompatActivity implements PickerContract.IView,
        PictureAdapter.AdapterInteraction,
        FolderAdapter.AdapterInteraction,
        View.OnClickListener {

    /**
     * Constants.
     */
    public static final int REQUEST_CODE = 267;
    public static final String RESULT_EXTRA_PICKED_PICTURES = "result_intent_extra_picked_pictures";
    private static final String EXTRA_CONFIG = "start_intent_extra_config";

    /**
     * U can launch PickerActivity from here.
     * If U picked success, it will return picked data, U can got it like
     * {@code ArrayList<String> paths = data.getStringArrayListExtra(PickerActivity.RESULT_EXTRA_PICKED_PICTURES)}
     *
     * @param from     The Activity that request launch PickerActivity.
     * @param resultTo Result data will return to this instance.
     * @param config   Launch PickerActivity required data.
     */
    public static void launchActivityForResult(Activity from, Fragment resultTo, PickerConfig config) {
        Intent intent = new Intent(from, PickerActivity.class);
        intent.putExtra(PickerActivity.EXTRA_CONFIG, config);
        resultTo.startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * Presenter associated with this Activity.
     */
    private PickerContract.IPresenter mPresenter;

    /**
     * Views
     */
    private SToolbar mToolbar;
    private ProgressBar mProgressBar;
    private TextView mTvToolbarFolderName;
    private TextView mTvToolbarEnsure;
    private RecyclerView mRecyclePictures;
    private ViewGroup mMenuNavContainer;
    private ImageView mIvNavIndicator;
    private TextView mTvFolderName;
    private TextView mTvPreview;
    private RecyclerView mRecycleFolders;
    private FloatingActionButton mFab;

    /**
     * CoordinatorLayout behaviors.
     */
    private BottomSheetBehavior mBottomMenuBehavior;
    private PicturePickerFabBehavior mFabBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_picker_activity_picker);
        initTitle();
        initViews();
        initData();
    }

    @Override
    public void setToolbarBackgroundColor(int color) {
        mToolbar.setBackgroundColor(color);
    }

    @Override
    public void setToolbarBackgroundDrawable(int drawableId) {
        mToolbar.setBackgroundDrawableRes(drawableId);
    }

    @Override
    public void setToolbarScrollable(boolean isScrollable) {
        if (isScrollable) {
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
            mToolbar.setLayoutParams(params);
        }
    }

    @Override
    public void setPicturesBackgroundColor(int color) {
        mRecyclePictures.setBackgroundColor(color);
    }

    @Override
    public void setPicturesSpanCount(int spanCount) {
        mRecyclePictures.setLayoutManager(new GridLayoutManager(this, spanCount));
    }

    @Override
    public void setPicturesAdapter(@NonNull PickerConfig config,
                                   @NonNull ArrayList<MediaMeta> metas,
                                   @NonNull ArrayList<MediaMeta> userPickedMetas) {
        mRecyclePictures.setAdapter(new PictureAdapter(this, config,
                metas, userPickedMetas));
    }

    @Override
    public void setFolderAdapter(@NonNull ArrayList<FolderModel> allFolders) {
        mRecycleFolders.setAdapter(new FolderAdapter(this, allFolders));
    }

    @Override
    public void setFabColor(int color) {
        mFab.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    @Override
    public void setProgressBarVisible(boolean visible) {
        mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void switchFabVisibility(boolean isVisible) {
        if (isVisible) {
            mFab.show();
        } else {
            mFab.hide();
        }
    }

    @Override
    public void setPictureFolderText(@NonNull String folderName) {
        // 更新文件夹名称
        mTvFolderName.setText(folderName);
        mTvToolbarFolderName.setText(folderName);
    }

    @Override
    public void setToolbarEnsureText(@NonNull CharSequence content) {
        mTvToolbarEnsure.setText(content);
    }

    @Override
    public void setPreviewText(@NonNull CharSequence content) {
        mTvPreview.setText(content);
    }

    @Override
    public void notifyPickedPathsChanged() {
        RecyclerView.Adapter adapter;
        if ((adapter = mRecyclePictures.getAdapter()) != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDisplayPathsChanged() {
        RecyclerView.Adapter adapter;
        if ((adapter = mRecyclePictures.getAdapter()) != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDisplayPathsInsertToFirst() {
        RecyclerView.Adapter adapter;
        if ((adapter = mRecyclePictures.getAdapter()) != null) {
            adapter.notifyItemInserted(1);
        }
    }

    @Override
    public void notifyFolderDataSetChanged() {
        RecyclerView.Adapter adapter;
        if ((adapter = mRecycleFolders.getAdapter()) != null) {
            adapter.notifyItemInserted(1);
        }
    }

    @Override
    public void showMsg(@NonNull String msg) {
        Snackbar.make(mFab, msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void setResult(@NonNull ArrayList<MediaMeta> pickedPaths) {
        Intent intent = new Intent();
        intent.putExtra(PickerActivity.RESULT_EXTRA_PICKED_PICTURES, pickedPaths);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onPictureChecked(@NonNull MediaMeta checkedMeta) {
        return mPresenter.handlePictureChecked(checkedMeta);
    }

    @Override
    public void onPictureRemoved(@NonNull MediaMeta removedMeta) {
        mPresenter.handlePictureRemoved(removedMeta);
    }

    @Override
    public void onPictureClicked(@NonNull ImageView imageView, @NonNull String uri, int position) {
        mPresenter.handlePictureClicked(position, imageView);
    }

    @Override
    public void onCameraClicked() {
        mPresenter.handleCameraClicked();
    }

    @Override
    public void onFolderChecked(int position) {
        mPresenter.handleFolderChecked(position);
        mBottomMenuBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onClick(View v) {
        // 底部菜单按钮
        if (v.getId() == R.id.tv_folder_name) {
            mBottomMenuBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        // 预览按钮
        else if (v.getId() == R.id.tv_preview) {
            mPresenter.handlePreviewClicked();
        }
        // 确认按钮
        else if (v == mTvToolbarEnsure || v.getId() == R.id.fab) {
            mPresenter.handleEnsureClicked();
        }
    }

    @Override
    public void onBackPressed() {
        if (BottomSheetBehavior.STATE_COLLAPSED != mBottomMenuBehavior.getState()) {
            mBottomMenuBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    protected void initTitle() {
        // 初始化视图
        mToolbar = findViewById(R.id.toolbar);
        // 设置标题文本
        mToolbar.setTitleText(getString(R.string.picture_picker_picker_all_picture));
        mTvToolbarFolderName = mToolbar.getTitleText();
        // 添加图片确认按钮
        mToolbar.addRightMenuText(
                TextViewOptions.Builder()
                        .setText(getString(R.string.picture_picker_picker_ensure))
                        .setTextSize(15)
                        .setListener(this)
                        .build()
        );
        mTvToolbarEnsure = mToolbar.getRightMenuView(0);
    }

    protected void initViews() {
        // Pictures recycler view.
        mRecyclePictures = findViewById(R.id.recycle_pictures);

        // Bottom navigation menu.
        mMenuNavContainer = findViewById(R.id.rv_menu_nav_container);
        mIvNavIndicator = findViewById(R.id.iv_nav_indicator);
        mTvFolderName = findViewById(R.id.tv_folder_name);
        mTvPreview = findViewById(R.id.tv_preview);
        mRecycleFolders = findViewById(R.id.recycle_folders);
        mTvFolderName.setOnClickListener(this);
        mTvPreview.setOnClickListener(this);
        mRecycleFolders.setLayoutManager(new LinearLayoutManager(this));
        mRecycleFolders.setHasFixedSize(true);
        mBottomMenuBehavior = BottomSheetBehavior.from(findViewById(R.id.ll_bottom_menu));
        mBottomMenuBehavior.setBottomSheetCallback(new BottomMenuNavigationCallback());

        // Floating action bar.
        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(this);
        mFabBehavior = PicturePickerFabBehavior.from(mFab);

        // Progress bar.
        mProgressBar = findViewById(R.id.progress_bar);
    }

    protected void initData() {
        mPresenter = new PickerPresenter(this, this, (PickerConfig)
                getIntent().getParcelableExtra(EXTRA_CONFIG));
    }

    /**
     * Callback associated with bottom menu navigation bar.
     * Method will be invoked when menu scrolled.
     */
    private class BottomMenuNavigationCallback extends BottomSheetBehavior.BottomSheetCallback {

        private final Drawable indicatorDrawable;
        private final int bgCollapsedColor;
        private final int bgExpandColor;
        private final int textCollapsedColor;
        private final int textExpandColor;

        BottomMenuNavigationCallback() {
            indicatorDrawable = mIvNavIndicator.getDrawable();
            bgCollapsedColor = ContextCompat.getColor(PickerActivity.this,
                    R.color.picture_picker_picker_bottom_menu_nav_bg_collapsed_color);
            bgExpandColor = ContextCompat.getColor(PickerActivity.this,
                    R.color.picture_picker_picker_bottom_menu_navi_bg_expand_color);
            textCollapsedColor = ContextCompat.getColor(PickerActivity.this,
                    R.color.picture_picker_picker_bottom_menu_nav_text_collapsed_color);
            textExpandColor = ContextCompat.getColor(PickerActivity.this,
                    R.color.picture_picker_picker_bottom_menu_navi_text_expand_color);
        }

        @Override
        public void onStateChanged(@NonNull View view, int state) {
            mFabBehavior.setBehaviorValid(BottomSheetBehavior.STATE_COLLAPSED == state);
        }

        @Override
        public void onSlide(@NonNull View view, float fraction) {
            // Get background color associate with the bottom menu navigation bar.
            int bgColor = ColorUtil.gradualChanged(fraction, bgCollapsedColor, bgExpandColor);
            mMenuNavContainer.setBackgroundColor(bgColor);
            // Get text color associate with the bottom menu navigation bar.
            int textColor = ColorUtil.gradualChanged(fraction, textCollapsedColor, textExpandColor);
            // Set text drawable color before set text color with the purpose of decrease view draw.
            if (VersionUtil.isLollipop()) {
                indicatorDrawable.setTint(textColor);
            }
            // Set texts colors associate with the bottom menu.
            mTvFolderName.setTextColor(textColor);
            mTvPreview.setTextColor(textColor);
        }
    }

}