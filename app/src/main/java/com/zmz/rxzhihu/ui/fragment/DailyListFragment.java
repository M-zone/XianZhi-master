package com.zmz.rxzhihu.ui.fragment;

import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.zmz.rxzhihu.R;
import com.zmz.rxzhihu.adapter.AutoLoadOnScrollListener;
import com.zmz.rxzhihu.adapter.DailyListAdapter;
import com.zmz.rxzhihu.adapter.MainViewPagerAdapter;
import com.zmz.rxzhihu.base.LazyFragment;
import com.zmz.rxzhihu.db.DailyDao;
import com.zmz.rxzhihu.model.DailyBean;
import com.zmz.rxzhihu.model.DailyDetail;
import com.zmz.rxzhihu.model.DailyListBean;
import com.zmz.rxzhihu.model.TopDailys;
import com.zmz.rxzhihu.network.RetrofitHelper;
import com.zmz.rxzhihu.utils.LogUtil;
import com.zmz.rxzhihu.utils.NetWorkUtil;
import com.zmz.rxzhihu.widget.CircleIndicator;
import com.zmz.rxzhihu.widget.CircleProgressView;
import com.zmz.rxzhihu.widget.refresh.HeaderViewRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.R.attr.alpha;

/**
 * Created by 11 on 2016/4/1.
 * <p/>
 * 日报列表界面
 */
public class DailyListFragment extends LazyFragment implements Runnable {

    @Bind(R.id.daily_recycle)
    RecyclerView mRecyclerView;

    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.circle_progress)
    CircleProgressView mCircleProgressView;

//    @Bind(R.id.refresh_btn)
//    FloatingActionButton mRefreshBtn;

    public static final String TAG = DailyListFragment.class.getSimpleName();

    private List<DailyBean> dailys = new ArrayList<>();

    private String currentTime = "";

    private DailyListAdapter mAdapter;

    private AutoLoadOnScrollListener mAutoLoadOnScrollListener;

    private MainViewPagerAdapter mMainViewPagerAdapter;

    private ViewPager mViewPager;

    private CircleIndicator mCircleIndicator;

    private int mPagerPosition = 0;

    private boolean mIsUserTouched = false;

    private int size;

    private static final int START_ALPHA = 0;
    private static final int END_ALPHA = 255;

    private Toolbar toolbar;

    private Timer mTimer;

    private BannerTask mTimerTask;
    private int fadingHeight = 250;

    private LinearLayoutManager mLinearLayoutManager;

    private HeaderViewRecyclerAdapter mHeaderViewRecyclerAdapter;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            if (msg.what == 0) {
                getLatesDailys(true);
            } else if (msg.what == 1) {
                hideProgress();
                mSwipeRefreshLayout.setRefreshing(false);
                finishGetDaily();
            }
        }
    };

    public static DailyListFragment newInstance() {

        return new DailyListFragment();
    }

    @Override
    public int getLayoutId() {

        return R.layout.fragment_daily_list;
    }

    @Override
    public void initViews() {

        /*mSwipeRefreshLayout.setTitleAndHead(, header);
        mToolbar.getBackground().setAlpha(0);*/
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                mHandler.sendEmptyMessageDelayed(0, 1000);
            }
        });


        mAdapter = new DailyListAdapter(getActivity(), dailys);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        mAutoLoadOnScrollListener = new AutoLoadOnScrollListener(mLinearLayoutManager) {

            @Override
            public void onLoadMore(int currentPage) {

                loadMoreDaily(currentTime);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);
                //int firstPos = (recyclerView == null || recyclerView.getChildCount() == 0 ? 0 : recyclerView.getChildAt(0).getTop());

                    Log.e(TAG, "onScrolled: dy" + dy);
                Log.e(TAG, "onScrolled: alpha" + alpha);

                mSwipeRefreshLayout.setEnabled(mLinearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);
            }
        };
        mRecyclerView.addOnScrollListener(mAutoLoadOnScrollListener);
        mHeaderViewRecyclerAdapter = new HeaderViewRecyclerAdapter(mAdapter);
        View headView = LayoutInflater.from(getActivity()).inflate(R.layout.recycle_head_layout, mRecyclerView, false);
        mViewPager = (ViewPager) headView.findViewById(R.id.main_view_pager);
        mCircleIndicator = (CircleIndicator) headView.findViewById(R.id.pager_indicator);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                    mIsUserTouched = true;
                    mSwipeRefreshLayout.setEnabled(false);
                } else if (action == MotionEvent.ACTION_UP) {
                    mIsUserTouched = false;
                } else if (action == MotionEvent.ACTION_CANCEL) {
                    mSwipeRefreshLayout.setEnabled(true);
                }
                return false;
            }
        });
        mHeaderViewRecyclerAdapter.addHeaderView(headView);
        getLatesDailys(false);
    }

//    @OnClick(R.id.refresh_btn)
//    void refreshData()
//    {
//        //回到顶部
//       mLinearLayoutManager.scrollToPosition(1);
//    }


    public void getLatesDailys(final boolean isDownRefresh) {

        RetrofitHelper.builder().getLatestNews()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Action0() {

                    @Override
                    public void call() {

                        if (!isDownRefresh) {
                            showProgress();
                        }
                    }
                })
                .map(new Func1<DailyListBean, DailyListBean>() {

                    @Override
                    public DailyListBean call(DailyListBean dailyListBean) {

                        cacheAllDetail(dailyListBean.getStories());
                        return changeReadState(dailyListBean);
                    }
                })
                .subscribe(new Action1<DailyListBean>() {

                    @Override
                    public void call(DailyListBean dailyListBean) {


                        if (dailyListBean.getStories() == null) {
                            hideProgress();
                            mSwipeRefreshLayout.setRefreshing(false);
                            LogUtil.all("加载数据失败");
                        } else {
                            mAdapter.updateData(dailyListBean.getStories());
                            currentTime = dailyListBean.getDate();
                            if (dailyListBean.getStories().size() < 8) {
                                loadMoreDaily(DailyListFragment.this.currentTime);
                            }
                            List<TopDailys> tops = dailyListBean.getTop_stories();
                            mMainViewPagerAdapter = new MainViewPagerAdapter(getActivity(), tops);
                            mViewPager.setAdapter(mMainViewPagerAdapter);
                            mCircleIndicator.setViewPager(mViewPager);
                            size = tops.size();
                            mHandler.sendEmptyMessageDelayed(1, 2000);
                        }
                    }
                }, new Action1<Throwable>() {

                    @Override
                    public void call(Throwable throwable) {

                        hideProgress();
                        LogUtil.all("加载失败" + throwable.getMessage());
                    }
                });
    }

    private void showProgress() {

        mCircleProgressView.setVisibility(View.VISIBLE);
        mCircleProgressView.spin();
        mRecyclerView.setVisibility(View.GONE);
    }

    public void hideProgress() {

        mCircleProgressView.setVisibility(View.GONE);
        mCircleProgressView.stopSpinning();
        mRecyclerView.setVisibility(View.VISIBLE);
    }


    private void finishGetDaily() {
        mRecyclerView.setAdapter(mHeaderViewRecyclerAdapter);
        //mRefreshBtn.setVisibility(View.VISIBLE);
        startViewPagerRun();
    }

    /* private void loadMoreDaily(final String currentTime) {

        RetrofitHelper.builder().getBeforeNews(currentTime)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(this::changeReadState)
            .subscribe(dailyListBean -> {

              mAutoLoadOnScrollListener.setLoading(false);
              mAdapter.addData(dailyListBean.getStories());
              DailyListFragment.this.currentTime = dailyListBean.getDate();
            }, throwable -> {

              mAutoLoadOnScrollListener.setLoading(false);
              LogUtil.all("加载更多数据失败");
            });
      }*/
    private void loadMoreDaily(final String currentTime) {

        RetrofitHelper.builder().getBeforeNews(currentTime)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<DailyListBean, DailyListBean>() {

                    @Override
                    public DailyListBean call(DailyListBean dailyListBean) {

                        cacheAllDetail(dailyListBean.getStories());
                        return changeReadState(dailyListBean);
                    }
                })
                .subscribe(new Action1<DailyListBean>() {

                    @Override
                    public void call(DailyListBean dailyListBean) {

                        mAutoLoadOnScrollListener.setLoading(false);
                        mAdapter.addData(dailyListBean.getStories());
                        DailyListFragment.this.currentTime = dailyListBean.getDate();
                    }
                }, new Action1<Throwable>() {

                    @Override
                    public void call(Throwable throwable) {

                        mAutoLoadOnScrollListener.setLoading(false);
                        LogUtil.all("加载更多数据失败");
                    }
                });
    }


    /**
     * 改变点击已阅读状态
     *
     * @param dailyList
     * @return
     */
    public DailyListBean changeReadState(DailyListBean dailyList) {

        List<String> allReadId = new DailyDao(getActivity()).getAllReadNew();
        for (DailyBean daily : dailyList.getStories()) {
            daily.setDate(dailyList.getDate());
            for (String readId : allReadId) {
                if (readId.equals(daily.getId() + "")) {
                    daily.setRead(true);
                }
            }
        }
        return dailyList;
    }

    /**
     * 缓存数据
     *
     * @param dailys
     */
    private void cacheAllDetail(List<DailyBean> dailys) {

        if (NetWorkUtil.isWifiConnected()) {
            for (DailyBean daily : dailys) {
                cacheNewsDetail(daily.getId());
            }
        }
    }

    private void cacheNewsDetail(int newsId) {

        RetrofitHelper.builder().getNewsDetails(newsId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Action1<DailyDetail>() {

                    @Override
                    public void call(DailyDetail dailyDetail) {

                    }
                });
    }

    public void startViewPagerRun() {
        //执行ViewPager进行轮播
        mTimer = new Timer();
        mTimerTask = new BannerTask();
        mTimer.schedule(mTimerTask, 10000, 10000);
    }

    @Override
    public void run() {

        if (mPagerPosition == size - 1) {
            mViewPager.setCurrentItem(size - 1, false);
        } else {
            mViewPager.setCurrentItem(mPagerPosition);
        }
    }


    private class BannerTask extends TimerTask {

        @Override
        public void run() {

            if (!mIsUserTouched) {
                mPagerPosition = (mPagerPosition + 1) % size;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(DailyListFragment.this);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }
}
