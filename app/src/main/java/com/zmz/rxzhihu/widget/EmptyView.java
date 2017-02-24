package com.zmz.rxzhihu.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.zmz.rxzhihu.R;

/**
 * Created by Mzone on 16/5/14 15:22
 */
public class EmptyView extends LinearLayout
{


    public EmptyView(Context context)
    {

        this(context, null);
    }

    public EmptyView(Context context, AttributeSet attrs)
    {

        super(context, attrs);
        init();
    }

    private void init()
    {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_empty_comment, null);
        addView(view);
    }
}
