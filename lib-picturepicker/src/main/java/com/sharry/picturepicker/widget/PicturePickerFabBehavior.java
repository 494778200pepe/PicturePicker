package com.sharry.picturepicker.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * FloatingActionButton 的 CoordinateLayout 的 Behavior 动画
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/9/18 16:25
 */
public class PicturePickerFabBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {

    private AnimatorSet mAppearAnimatorSet;
    private AnimatorSet mDismissAnimatorSet;

    /**
     * 设置依赖的控件
     */
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        return dependency instanceof RecyclerView;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild,
                                       View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }


    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target,
                               int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        // 上滑
        if (dyConsumed > 0 && dyUnconsumed == 0) {
            setAnimator(child, true);
        }

        // 到了边界还在上滑
        if (dyConsumed == 0 && dyUnconsumed > 0) {
            setAnimator(child, true);
        }

        // 下滑
        if (dyConsumed < 0 && dyUnconsumed == 0) {
            setAnimator(child, false);
        }

        // 到了边界, 还在下滑
        if (dyConsumed == 0 && dyUnconsumed < 0) {
            setAnimator(child, false);
        }

    }

    /**
     * 处理动画效果
     */
    private void setAnimator(FloatingActionButton target, final boolean isUp) {
        if (getAppearAnimator(target).isRunning() || getDismissAnimator(target).isRunning()) return;
        if (isUp) {// 处理上滑显示
            if (target.getVisibility() == View.INVISIBLE) {
                getAppearAnimator(target).start();
            }
        } else {// 处理下滑消失
            if (target.getVisibility() == View.VISIBLE) {
                getDismissAnimator(target).start();
            }
        }
    }

    /**
     * 获取呈现动画
     */
    private AnimatorSet getAppearAnimator(final FloatingActionButton target) {
        if (mAppearAnimatorSet == null) {
            mAppearAnimatorSet = new AnimatorSet();
            mAppearAnimatorSet.playTogether(
                    ObjectAnimator.ofFloat(target, "scaleX", 0f, 1f),
                    ObjectAnimator.ofFloat(target, "scaleY", 0f, 1f)
            );
            mAppearAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    target.setVisibility(View.VISIBLE);
                }
            });
            mAppearAnimatorSet.setDuration(200);
        }
        return mAppearAnimatorSet;
    }

    /**
     * 获取消失动画
     */
    private AnimatorSet getDismissAnimator(final FloatingActionButton target) {
        // 当且仅当处于 上滑状态, Animator动画结束, 且fab为可见状态时才执行下列方法
        if (mDismissAnimatorSet == null) {
            mDismissAnimatorSet = new AnimatorSet();
            mDismissAnimatorSet.playTogether(
                    ObjectAnimator.ofFloat(target, "scaleX", 1f, 0f),
                    ObjectAnimator.ofFloat(target, "scaleY", 1f, 0f)
            );
            mDismissAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    target.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    target.setVisibility(View.INVISIBLE);
                }
            });
            mDismissAnimatorSet.setDuration(200);
        }
        return mDismissAnimatorSet;
    }
}
