package com.frank.picturepicker.picturewatcher.impl;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Frank on 2018/5/28.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 图片查看器的 Adapter
 */
public class PictureWatcherAdapter extends PagerAdapter {

    private List<? extends View> mViews;

    PictureWatcherAdapter(List<? extends View> children) {
        this.mViews = children;
    }

    /**
     * 获取子级布局的数量
     */
    @Override
    public int getCount() {
        return mViews.size();
    }

    /**
     * 判断某个 View 对象是否为当前被添加到 ViewPager 容器中的对象
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    /**
     * 实例化 ViewPager 容器中指定的 position 位置需要显示的 View 对象
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mViews.get(position);
        container.addView(view);
        return view;
    }

    /**
     * 在ViewPager中移除指定的 position 位置的 view 对象
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = mViews.get(position);
        container.removeView(view);
    }

}
