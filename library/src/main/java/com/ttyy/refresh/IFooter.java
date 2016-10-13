package com.ttyy.refresh;

import android.view.View;

/**
 * Author: hjq
 * Date  : 2016/10/13
 * Class : IFooter
 * Desc  : Edit By Hujinqi
 */
public interface IFooter {

    View getView();

    /**
     * 上拉
     * @param percentage
     * @param maxHeight
     * @param stdHeight
     */
    void onPullingUp(float percentage, int maxHeight, int stdHeight);

    /**
     * 上拉松开
     * @param percentage
     * @param maxHeight
     * @param stdHeight
     */
    void onPullingRelease(float percentage, int maxHeight, int stdHeight);

    /**
     * 正在加载更多
     * @param maxHeight
     * @param stdHeight
     */
    void onLoading(int maxHeight, int stdHeight);

}
