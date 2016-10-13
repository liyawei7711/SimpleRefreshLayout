package com.ttyy.refresh.footer;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ttyy.refresh.IFooter;
import com.ttyy.refresh.R;

/**
 * Created by lcodecore on 2016/10/2.
 */
public class SinaFooterRefreshView extends FrameLayout implements IFooter {

    private ImageView refreshArrow;
    private ImageView loadingView;
    private TextView refreshTextView;
    private View rootView;

    public SinaFooterRefreshView(Context context) {
        this(context, null);
    }

    public SinaFooterRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SinaFooterRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (rootView == null) {
            rootView = View.inflate(getContext(), R.layout.view_sinaheader, null);
            refreshArrow = (ImageView) rootView.findViewById(R.id.iv_arrow);
            refreshTextView = (TextView) rootView.findViewById(R.id.tv);
            loadingView = (ImageView) rootView.findViewById(R.id.iv_loading);
            addView(rootView);
        }
    }

    public void setArrowResource(@DrawableRes int resId) {
        refreshArrow.setImageResource(resId);
    }

    public void setPullUpStr(String pullDownStr1) {
        pullUpStr = pullDownStr1;
    }

    public void setReleaseRefreshStr(String releaseRefreshStr1) {
        releaseRefreshStr = releaseRefreshStr1;
    }

    public void setRefreshingStr(String refreshingStr1) {
        refreshingStr = refreshingStr1;
    }

    private String pullUpStr = "上拉加载更多";
    private String releaseRefreshStr = "释放加载更多";
    private String refreshingStr = "正在加载";

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onPullingUp(float percentage, int maxHeight, int stdHeight) {
        refreshTextView.setText(pullUpStr);
        refreshArrow.setRotation((1 - percentage * stdHeight / maxHeight) * 180);
    }

    @Override
    public void onPullingRelease(float percentage, int maxHeight, int stdHeight) {
        if(percentage < 1){
            refreshTextView.setText(pullUpStr);
            refreshArrow.setRotation((1 - percentage * stdHeight / maxHeight) * 180);
            if(refreshArrow.getVisibility() == View.GONE){
                refreshArrow.setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoading(int maxHeight, int stdHeight) {
        refreshTextView.setText(refreshingStr);
        refreshArrow.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
    }
}
