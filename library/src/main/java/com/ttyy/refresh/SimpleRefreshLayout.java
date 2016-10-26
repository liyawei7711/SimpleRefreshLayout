package com.ttyy.refresh;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;

import com.ttyy.refresh.footer.SinaFooterRefreshView;
import com.ttyy.refresh.header.SinaHeaderRefreshView;

/**
 * Author: hjq
 * Date  : 2016/10/12
 * Class : SimpleRefreshLayout
 * Desc  : 简单的上拉/下拉刷新父控件 不支持回弹
 */
public class SimpleRefreshLayout extends FrameLayout {

    /**
     * 状态 下拉刷新
     */
    private static final int PULL_DOWN_REFRESH = 1;
    /**
     * 状态 上拉加载
     */
    private static final int PULL_UP_LOAD = 2;

    /**
     * 是否正在刷新
     */
    boolean isRefreshing;
    /**
     * 是否正在加载
     */
    boolean isLoading;
    /**
     * 是否允许加载更多
     */
    boolean isEnableLoadMore = true;
    /**
     * 当前状态位
     */
    int state = -1;

    FrameLayout mHeaderLayout;
    int mHeaderRefreshHeight;
    int mHeaderMaxHeight;
    IHeader mIHeader;

    FrameLayout mFooterLayout;
    int mFooterLoadingHeight;
    int mFooterMaxHeight;
    IFooter mIFooter;

    View mChildView;
    RefreshListener mRefreshListener;


    DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(10);
    float mTouchY;
    int mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    public SimpleRefreshLayout(Context context) {
        super(context);
        init(null);
    }

    public SimpleRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SimpleRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    void init(AttributeSet attrs) {
        float density = getContext().getResources().getDisplayMetrics().density;
        mFooterLoadingHeight = mHeaderRefreshHeight = (int) (80 * density);
        mFooterMaxHeight = mHeaderMaxHeight = (int) (mHeaderRefreshHeight * 1.5f);

        if (attrs != null) {

        }
    }

    // 相当于 Activity的onCreate
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mChildView == null) {
            mChildView = getChildAt(0);
        }

        if (mChildView == null) {
            return;
        }

        if (mHeaderLayout == null) {
            mHeaderLayout = new FrameLayout(getContext());
            mHeaderLayout.setBackgroundColor(Color.parseColor("#cccccc"));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
            params.gravity = Gravity.TOP;
            addView(mHeaderLayout, params);

            // 默认下拉刷新LoadingView
            mIHeader = new SinaHeaderRefreshView(getContext());
            mHeaderLayout.addView((View) mIHeader);
        }

        if (mFooterLayout == null) {
            mFooterLayout = new FrameLayout(getContext());
            mFooterLayout.setBackgroundColor(Color.parseColor("#cccccc"));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
            params.gravity = Gravity.BOTTOM;
            addView(mFooterLayout, params);

            // 默认上拉刷新LoadingView
            mIFooter = new SinaFooterRefreshView(getContext());
            mFooterLayout.addView((View) mIFooter);
        }

        mChildView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    // 相当于 Activity的onDestroy
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mChildView != null) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTouchY = ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dy = ev.getY() - mTouchY;
                    if (dy > 0 && !canChildScrollDown()) {
                        // 触发下拉刷新
                        state = PULL_DOWN_REFRESH;
                        return true;
                    } else if (dy < 0 && !canChildScrollUp() && isEnableLoadMore) {
                        // 触发上拉刷新
                        state = PULL_UP_LOAD;
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 子View添加手势操作
     */
    GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (isRefreshing && distanceY > mTouchSlop) {
                // 结束下拉刷新
                finishPullDownRefreshing();
            } else if (isLoading && distanceY < -mTouchSlop) {
                // 结束上拉加载
                finishPullUpLoading();
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    });

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isRefreshing || isLoading || mChildView == null) {
            // 控制避免同时可以上拉/下拉刷新
            return super.onTouchEvent(ev);
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                float dy = ev.getY() - mTouchY;
                if (state == PULL_DOWN_REFRESH) {
                    // 下拉刷新
                    dy = Math.min(mHeaderMaxHeight * 2, dy);
                    dy = Math.max(0, dy);

                    float offsetY = decelerateInterpolator.getInterpolation(dy / mHeaderMaxHeight / 2) * dy / 2;
                    mChildView.setTranslationY(offsetY);

                    mHeaderLayout.getLayoutParams().height = (int) offsetY;
                    mHeaderLayout.requestLayout();

                    if(mIHeader != null){
                        float percent = offsetY / mHeaderRefreshHeight;
                        mIHeader.onPullingDown(percent, mHeaderMaxHeight, mHeaderRefreshHeight);
                    }

                } else if (state == PULL_UP_LOAD) {
                    // 上拉加载
                    dy = Math.min(mFooterMaxHeight * 2, Math.abs(dy));
                    dy = Math.max(0, dy);

                    float offsetY = -decelerateInterpolator.getInterpolation(dy / mFooterMaxHeight / 2) * dy / 2;
                    mChildView.setTranslationY(offsetY);

                    mFooterLayout.getLayoutParams().height = (int) -offsetY;
                    mFooterLayout.requestLayout();

                    if(mIFooter != null){
                        float percent = -offsetY / mFooterLoadingHeight;
                        mIFooter.onPullingUp(percent, mFooterMaxHeight, mFooterLoadingHeight);
                    }
                }

                return true;
            case MotionEvent.ACTION_UP:
                if (state == PULL_DOWN_REFRESH) {
                    // 下拉刷新
                    if (mChildView.getTranslationY() > (mHeaderRefreshHeight - mTouchSlop)) {
                        // 刷新
                        animateChildView(mHeaderRefreshHeight);
                        isRefreshing = true;

                        if(mIHeader != null){
                            mIHeader.onRefreshing(mHeaderMaxHeight, mHeaderRefreshHeight);
                        }

                        if(mRefreshListener != null){
                            mRefreshListener.onRefresh(this, mChildView);
                        }

                    } else {
                        animateChildView(0);
                    }
                } else if (state == PULL_UP_LOAD) {
                    // 上拉加载
                    if (Math.abs(mChildView.getTranslationY()) > (mFooterLoadingHeight - mTouchSlop)) {
                        // 刷新
                        animateChildView(-mFooterLoadingHeight);
                        isLoading = true;

                        if(mIFooter != null){
                            mIFooter.onLoading(mFooterMaxHeight, mFooterLoadingHeight);
                        }

                        if(mRefreshListener != null){
                            mRefreshListener.onLoadMore(this, mChildView);
                        }

                    } else {
                        animateChildView(0);
                    }
                }
                return true;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * SwipeRefreshLayout canScrollUp 源码改编
     * Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     * 用来判断是否可以下拉
     * <p>
     *
     * Negative to check scrolling up, positive to check scrolling down.
     * Up/Down 只是相对于坐标系的Y方向 Y方向正方向为up Y方向负方向为down
     *
     * @return boolean
     */
    protected boolean canChildScrollDown() {
        if (mChildView == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (mChildView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mChildView;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mChildView, -1) || mChildView.getScrollY() > 0;
            }
        } else {
            // e1.getY - e2.getY
            // -1 下拉
            // 1  上拉
            // 此处判断是否可以在垂直方向上继续向下滑动
            // 所以实际上判断的是View是否可以继续下拉
            return ViewCompat.canScrollVertically(mChildView, -1);
        }
    }

    /**
     * SwipeRefreshLayout canScrollUp 源码改编
     * Whether it is possible for the child view of this layout to
     * scroll down. Override this if the child view is a custom view.
     * 判断是否可以上拉
     *
     * @return
     */
    protected boolean canChildScrollUp() {
        if (mChildView == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (mChildView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mChildView;
                return absListView.getChildCount() > 0
                        && (absListView.getLastVisiblePosition() < absListView.getChildCount() - 1
                        || absListView.getChildAt(absListView.getChildCount() - 1).getBottom() > absListView.getPaddingBottom());
            } else {
                return ViewCompat.canScrollVertically(mChildView, 1) || mChildView.getScrollY() < 0;
            }
        } else {
            // 是否可以上拉
            return ViewCompat.canScrollVertically(mChildView, 1);
        }
    }

    /**
     * 开始动画
     *
     * @param endValue
     */
    private void animateChildView(float endValue) {
        animateChildView(endValue, 300);
    }

    /**
     * 开始动画
     *
     * @param endValue
     * @param duration
     */
    private void animateChildView(float endValue, long duration) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(mChildView, "translationY", mChildView.getTranslationY(), endValue);
        oa.setDuration(duration);
        oa.setInterpolator(new DecelerateInterpolator());
        oa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = (int) Math.abs(mChildView.getTranslationY());
                if (state == PULL_DOWN_REFRESH) {

                    mHeaderLayout.getLayoutParams().height = height;
                    mHeaderLayout.requestLayout();
                    if(mIHeader != null){
                        mIHeader.onPullingRelease((float) height / mHeaderRefreshHeight, mHeaderMaxHeight, mHeaderRefreshHeight);
                    }

                } else if (state == PULL_UP_LOAD) {

                    mFooterLayout.getLayoutParams().height = height;
                    mFooterLayout.requestLayout();
                    if(mIFooter != null){
                        mIFooter.onPullingRelease((float) height / mFooterLoadingHeight, mFooterMaxHeight, mFooterLoadingHeight);
                    }

                }
            }
        });
        oa.start();
    }

    /**
     * 结束刷新
     */
    public void finishRefreshing() {
        if (isRefreshing) {
            finishPullDownRefreshing();
        } else if (isLoading) {
            finishPullUpLoading();
        }
    }

    /**
     * 完成下拉刷新
     */
    private void finishPullDownRefreshing() {
        isRefreshing = false;
        animateChildView(0);
    }

    /**
     * 完成上拉加载
     */
    private void finishPullUpLoading() {
        isLoading = false;
        animateChildView(0);
    }

    /**
     * 是否允许可以上拉加载更多
     * @param isEnableLoadMore
     */
    public void setEnableLoadMore(boolean isEnableLoadMore){
        this.isEnableLoadMore = isEnableLoadMore;
    }

    /**
     * 设置自定义的Header LoadingView
     * @param mIHeader
     */
    public void setHeaderView(IHeader mIHeader){
        mHeaderLayout.removeAllViews();
        mHeaderLayout.addView((View) mIHeader);
        this.mIHeader = mIHeader;
    }

    /**
     * 设置自定义的Footer LoadingView
     * @param mIFooter
     */
    public void setFooterView(IFooter mIFooter){
        mFooterLayout.removeAllViews();
        mFooterLayout.addView((View) mIFooter);
        this.mIFooter = mIFooter;
    }

    /**
     * 刷新侦听
     * @param refreshListener
     * @param <T>
     */
    public <T extends View> void setRefreshListener(RefreshListener<T> refreshListener){
        mRefreshListener = refreshListener;
    }

    /**
     * 刷新状态监听器
     * @param <T>
     */
    public interface RefreshListener<T extends View>{

        /**
         * 下拉刷新
         * @param view
         * @param refreshView
         */
        void onRefresh(View view, T refreshView);

        /**
         * 上拉加载更多
         * @param view
         * @param refreshView
         */
        void onLoadMore(View view, T refreshView);

    }
}
