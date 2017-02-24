package com.zmz.rxzhihu.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.zmz.rxzhihu.utils.NightModeHelper;

import butterknife.ButterKnife;

/**
 * Created by 11 on 2016/3/31.
 * <p/>
 * 普通Activity基类
 */
public abstract class AbsBaseActivity extends AppCompatActivity
{

    public NightModeHelper mNightModeHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        //设置布局内容
        setContentView(getLayoutId());
        //初始化黄油刀控件绑定框架
        ButterKnife.bind(this);
        //初始化控件
        initViews(savedInstanceState);
        //初始化ToolBar
        initToolBar();
        //初始化日夜间模式切换帮助类
        mNightModeHelper = new NightModeHelper(this);
    }

    @Override
    protected void onDestroy()
    {

        super.onDestroy();
        ButterKnife.unbind(this);
    }

    public abstract int getLayoutId();

    public abstract void initViews(Bundle savedInstanceState);

    public abstract void initToolBar();
}
