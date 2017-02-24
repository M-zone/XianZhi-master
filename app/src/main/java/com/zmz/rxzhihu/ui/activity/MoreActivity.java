package com.zmz.rxzhihu.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.zmz.rxzhihu.R;
import com.zmz.rxzhihu.base.AbsBaseActivity;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Mzone on 16/5/15 16:33
 */
public class MoreActivity extends AbsBaseActivity
{

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    public int getLayoutId()
    {

        return R.layout.activity_more;
    }

    @Override
    public void initViews(Bundle savedInstanceState)
    {

    }

    @Override
    public void initToolBar()
    {

        mToolbar.setTitle("更多选项");
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.more_btn_info)
    void startHotBitmapGGInfo()
    {

        startActivity(new Intent(MoreActivity.this, MzoneInfoActivity.class));
    }


    @OnClick(R.id.more_btn_feed_back)
    void startFeedBack()
    {

        startActivity(new Intent(MoreActivity.this, MessageActivity.class));
    }

    @OnClick(R.id.more_btn_setting)
    void startSetting()
    {

    }

    @OnClick(R.id.more_btn_about_app)
    void startAboutApp()
    {

        startActivity(new Intent(MoreActivity.this, AppAboutActivity.class));
    }
}
