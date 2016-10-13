package com.ttyy.refresh;

import android.view.View;

/**
 * Author: hjq
 * Date  : 2016/10/13
 * Class : IHeader
 * Desc  : Edit By Hujinqi
 */
public interface IHeader {

    View getView();

    /**
     * 正在下拉
     * @param percentage
     * @param maxHeight
     * @param stdHeight
     */
    void onPullingDown(float percentage, int maxHeight, int stdHeight);

    /**
     * 下拉到刷新点并且松开
     * @param percentage
     * @param maxHeight
     * @param stdHeight
     */
    void onPullingRelease(float percentage, int maxHeight, int stdHeight);

    /**
     * 正在刷新
     * @param maxHeight
     * @param stdHeight
     */
    void onRefreshing(int maxHeight, int stdHeight);

}
