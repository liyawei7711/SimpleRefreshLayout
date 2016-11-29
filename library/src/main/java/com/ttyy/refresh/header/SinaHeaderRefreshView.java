package com.ttyy.refresh.header;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ttyy.refresh.IHeader;
import com.ttyy.refresh.R;

/**
 * Created by lcodecore on 2016/10/2.
 */
public class SinaHeaderRefreshView extends FrameLayout implements IHeader {

    private ImageView refreshArrow;
    private ImageView loadingView;
    private TextView refreshTextView;
    private View rootView;

    public SinaHeaderRefreshView(Context context) {
        this(context, null);
    }

    public SinaHeaderRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SinaHeaderRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (rootView == null) {
            rootView = View.inflate(getContext(), R.layout.view_sinaheader, this);
            refreshArrow = (ImageView) rootView.findViewById(R.id.iv_arrow);
            refreshTextView = (TextView) rootView.findViewById(R.id.tv);
            loadingView = (ImageView) rootView.findViewById(R.id.iv_loading);
        }
    }

    public void setArrowResource(int resId) {
        refreshArrow.setImageResource(resId);
    }

    public void setPullDownStr(String pullDownStr1) {
        pullDownStr = pullDownStr1;
    }

    public void setReleaseRefreshStr(String releaseRefreshStr1) {
        releaseRefreshStr = releaseRefreshStr1;
    }

    public void setRefreshingStr(String refreshingStr1) {
        refreshingStr = refreshingStr1;
    }

    private String pullDownStr = "下拉刷新";
    private String releaseRefreshStr = "释放刷新";
    private String refreshingStr = "正在刷新";

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onPullingDown(float percentage, int maxHeight, int stdHeight) {
        refreshTextView.setText(pullDownStr);
        refreshArrow.setRotation(percentage * stdHeight / maxHeight * 180);
    }

    @Override
    public void onPullingRelease(float percentage, int maxHeight, int stdHeight) {
        if(percentage < 1){
            refreshTextView.setText(pullDownStr);
            refreshArrow.setRotation(percentage * stdHeight / maxHeight * 180);
            if (refreshArrow.getVisibility() == GONE) {
                refreshArrow.setVisibility(VISIBLE);

                AnimationDrawable ad = (AnimationDrawable) loadingView.getDrawable();
                ad.stop();

                loadingView.setVisibility(GONE);
            }
        }
    }

    @Override
    public void onRefreshing(int maxHeight, int stdHeight) {
        refreshTextView.setText(refreshingStr);
        refreshArrow.setVisibility(GONE);
        loadingView.setVisibility(VISIBLE);

        AnimationDrawable ad = (AnimationDrawable) loadingView.getDrawable();
        ad.start();
    }
}
