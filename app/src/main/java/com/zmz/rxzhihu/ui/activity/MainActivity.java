package com.zmz.rxzhihu.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.zmz.rxzhihu.R;
import com.zmz.rxzhihu.base.AbsBaseActivity;
import com.zmz.rxzhihu.ui.fragment.DailyListFragment;
import com.zmz.rxzhihu.ui.fragment.HotNewsFragment;
import com.zmz.rxzhihu.ui.fragment.SectionsFragment;
import com.zmz.rxzhihu.ui.fragment.ThemesDailyFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * 知乎日报主界面
 *
 * @HotBitmapgg
 */
public class MainActivity extends AbsBaseActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.bottom_navigation)
    AHBottomNavigation mAhBottomNavigation;
    private long mExitTime=0L;


    private List<Fragment> fragments = new ArrayList<>();

    private int currentTabIndex;


    @Override
    public int getLayoutId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        return R.layout.activity_main;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {

        fragments.add(DailyListFragment.newInstance());
        fragments.add(ThemesDailyFragment.newInstance());
        fragments.add(SectionsFragment.newInstance());
        fragments.add(HotNewsFragment.newInstance());

        showFragment(fragments.get(0));
        initBottomNav();
    }

    private void initBottomNav() {

        AHBottomNavigationItem item1 = new AHBottomNavigationItem("日报", R.drawable.ic_profile_answer, R.color.colorPrimary);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem("主题", R.drawable.ic_profile_article, R.color.colorPrimary);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem("专栏", R.drawable.ic_profile_column, R.color.colorPrimary);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem("文章", R.drawable.ic_profile_favorite, R.color.colorPrimary);

        mAhBottomNavigation.addItem(item1);
        mAhBottomNavigation.addItem(item2);
        mAhBottomNavigation.addItem(item3);
        mAhBottomNavigation.addItem(item4);

        mAhBottomNavigation.setBehaviorTranslationEnabled(true);
        mAhBottomNavigation.setAccentColor(getResources().getColor(R.color.colorPrimary));
        mAhBottomNavigation.setInactiveColor(getResources().getColor(R.color.nav_text_color_mormal));
        mAhBottomNavigation.setCurrentItem(0);

        mAhBottomNavigation.setBehaviorTranslationEnabled(true);
        mAhBottomNavigation.setDefaultBackgroundColor(getResources().getColor(R.color.bg_color));


        mAhBottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {

            @Override
            public void onTabSelected(int position, boolean wasSelected) {

                if (currentTabIndex != position) {
                    FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
                    trx.hide(fragments.get(currentTabIndex));
                    if (!fragments.get(position).isAdded()) {
                        trx.add(R.id.content, fragments.get(position));
                    }
                    trx.show(fragments.get(position)).commit();
                }
                currentTabIndex = position;
            }
        });
    }

    @Override
    public void initToolBar() {

        mToolbar.setTitle("先知");
         // mToolbar.getBackground().setAlpha(0);//toolbar透明度初始化为0
        setSupportActionBar(mToolbar);
    }
    @Override
    public boolean onKeyDown(int n, KeyEvent keyEvent) {
        if (n != 4) {
            return super.onKeyDown(n, keyEvent);
        }
        if (System.currentTimeMillis() - this.mExitTime >= 2000L) {
            Toast.makeText(this,"再按一次返回键退出",Toast.LENGTH_SHORT).show();
            this.mExitTime = System.currentTimeMillis();
            return true;
        }
        this.finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
//            case R.id.action_mode:
//                //切换日夜间模式
//                mNightModeHelper.toggle();
//                return true;

            case R.id.action_settings:
                //设置
                startActivity(new Intent(MainActivity.this, MoreActivity.class));
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    private void showFragment(Fragment fragment) {

        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
    }
}
