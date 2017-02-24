package com.zmz.rxzhihu.ui.fragment;

import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.zmz.rxzhihu.R;
import com.zmz.rxzhihu.adapter.AbsRecyclerViewAdapter;
import com.zmz.rxzhihu.adapter.SectionsAdapter;
import com.zmz.rxzhihu.base.LazyFragment;
import com.zmz.rxzhihu.model.DailySections;
import com.zmz.rxzhihu.network.RetrofitHelper;
import com.zmz.rxzhihu.ui.activity.SectionsDetailsActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Mzone on 16/4/23 14:09
 * <p/>
 * 知乎专栏列表
 */
public class SectionsFragment extends LazyFragment
{

    @Bind(R.id.recycle)
    RecyclerView mRecyclerView;

    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private List<DailySections.DailySectionsInfo> sectionsInfos = new ArrayList<>();

    public static SectionsFragment newInstance()
    {

        SectionsFragment mSectionsFragment = new SectionsFragment();
        return mSectionsFragment;
    }


    @Override
    public int getLayoutId()
    {

        return R.layout.fragment_sections;
    }

    @Override
    public void initViews()
    {

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {

            @Override
            public void onRefresh()
            {

                getSections();
            }
        });
        showProgress();
    }

    private void getSections()
    {

        RetrofitHelper.getLastZhiHuApi().getZhiHuSections()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DailySections>()
                {

                    @Override
                    public void call(DailySections dailySections)
                    {

                        if (dailySections != null)
                        {
                            List<DailySections.DailySectionsInfo> data = dailySections.data;
                            if (data != null && data.size() > 0)
                            {
                                sectionsInfos.clear();
                                sectionsInfos.addAll(data);
                                finishGetSections();
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    }
                }, new Action1<Throwable>()
                {

                    @Override
                    public void call(Throwable throwable)
                    {

                        mSwipeRefreshLayout.post(new Runnable()
                        {

                            @Override
                            public void run()
                            {

                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });

                        Snackbar.make(mRecyclerView, "加载失败,请重新下拉刷新数据.", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void finishGetSections()
    {

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        SectionsAdapter mAdapter = new SectionsAdapter(mRecyclerView, sectionsInfos);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new AbsRecyclerViewAdapter.OnItemClickListener()
        {

            @Override
            public void onItemClick(int position, AbsRecyclerViewAdapter.ClickableViewHolder holder)
            {

                DailySections.DailySectionsInfo dailySectionsInfo = sectionsInfos.get(position);
                SectionsDetailsActivity.luancher(getActivity(), dailySectionsInfo.id);
            }
        });
    }

    public void showProgress()
    {

        mSwipeRefreshLayout.postDelayed(new Runnable()
        {

            @Override
            public void run()
            {

                mSwipeRefreshLayout.setRefreshing(true);
                getSections();
            }
        }, 500);
    }
}
