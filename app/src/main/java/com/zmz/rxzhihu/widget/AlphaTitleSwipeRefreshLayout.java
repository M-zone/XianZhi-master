package com.zmz.rxzhihu.widget;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class AlphaTitleSwipeRefreshLayout extends SwipeRefreshLayout {
    public static final String TAG = "AlphaTitleSwipeRefreshLayout";
    private int mSlop;
    private View toolbar;
    private View headView;
    public AlphaTitleSwipeRefreshLayout(Context context) {
        super(context);
    }

    public AlphaTitleSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void init(Context context) {
        // mSlop = ViewConfiguration.get(context).getScaledDoubleTapSlop();
        mSlop = 10;
    }
    /**
     *
     * @param headLayout
     *            头部布局
     * @param imageview
     *            标题
     */
    public void setTitleAndHead(View toolbar, View headView) {
        this.toolbar = toolbar;
        this.headView = headView;
    }
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        float headHeight = headView.getMeasuredHeight()
                - toolbar.getMeasuredHeight();
        int alpha = (int) (((float) t / headHeight) * 255);
        if (alpha >= 255)
            alpha = 255;
        if (alpha <= mSlop)
            alpha = 0;
        toolbar.getBackground().setAlpha(alpha);

        super.onScrollChanged(l, t, oldl, oldt);
    }
}