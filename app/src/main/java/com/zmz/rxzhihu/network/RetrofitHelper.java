package com.zmz.rxzhihu.network;

import com.zmz.rxzhihu.base.RxZhihuApp;
import com.zmz.rxzhihu.model.DailyDetail;
import com.zmz.rxzhihu.model.DailyExtraMessage;
import com.zmz.rxzhihu.model.DailyListBean;
import com.zmz.rxzhihu.model.DailyComment;
import com.zmz.rxzhihu.model.DailyRecommend;
import com.zmz.rxzhihu.model.DailyTypeBean;
import com.zmz.rxzhihu.model.LuanchImageBean;
import com.zmz.rxzhihu.model.ThemesDetails;
import com.zmz.rxzhihu.utils.NetWorkUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Created by 11 on 2016/3/31.
 * <p/>
 * Retrofit管理类
 */
public class RetrofitHelper
{

    public static final String ZHIHU_DAILY_URL = "http://news-at.zhihu.com/api/4/";

    public static final String ZHIHU_LAST_URL = "http://news-at.zhihu.com/api/3/";

    private static OkHttpClient mOkHttpClient;

    private final ZhiHuDailyAPI mZhiHuApi;

    public static final int CACHE_TIME_LONG = 60 * 60 * 24 * 7;


    public static RetrofitHelper builder()
    {

        return new RetrofitHelper();
    }

    private RetrofitHelper()
    {

        initOkHttpClient();

        Retrofit mRetrofit = new Retrofit.Builder()
                .baseUrl(ZHIHU_DAILY_URL)
                .client(mOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        mZhiHuApi = mRetrofit.create(ZhiHuDailyAPI.class);
    }


    public static ZhiHuDailyAPI getLastZhiHuApi()
    {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ZHIHU_LAST_URL)
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        return retrofit.create(ZhiHuDailyAPI.class);
    }


    /**
     * 初始化OKHttpClient
     */
    private void initOkHttpClient()
    {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        if (mOkHttpClient == null)
        {
            synchronized (RetrofitHelper.class)
            {
                if (mOkHttpClient == null)
                {
                    //设置Http缓存
                    Cache cache = new Cache(new File(RxZhihuApp.getContext().getCacheDir(), "HttpCache"), 1024 * 1024 * 100);

                    mOkHttpClient = new OkHttpClient.Builder()
                            .cache(cache)
                            .addInterceptor(mRewriteCacheControlInterceptor)
                            .addNetworkInterceptor(mRewriteCacheControlInterceptor)
                            .addInterceptor(interceptor)
                            .retryOnConnectionFailure(true)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .build();
                }
            }
        }
    }

    private Interceptor mRewriteCacheControlInterceptor = new Interceptor()
    {

        @Override
        public Response intercept(Chain chain) throws IOException
        {

            Request request = chain.request();
            if (!NetWorkUtil.isNetworkConnected())
            {
                request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
            }
            Response originalResponse = chain.proceed(request);
            if (NetWorkUtil.isNetworkConnected())
            {
                String cacheControl = request.cacheControl().toString();
                return originalResponse.newBuilder().header("Cache-Control", cacheControl).removeHeader("Pragma").build();
            } else
            {
                return originalResponse.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + CACHE_TIME_LONG)
                        .removeHeader("Pragma").build();
            }
        }
    };

    /**
     * 知乎日报Api封装 方便直接调用
     **/

    public Observable<DailyListBean> getLatestNews()
    {

        return mZhiHuApi.getlatestNews();
    }

    public Observable<DailyListBean> getBeforeNews(String date)
    {

        return mZhiHuApi.getBeforeNews(date);
    }

    public Observable<DailyDetail> getNewsDetails(int id)
    {

        return mZhiHuApi.getNewsDetails(id);
    }

    public Observable<LuanchImageBean> getLuanchImage(String res)
    {

        return mZhiHuApi.getLuanchImage(res);
    }

    public Observable<DailyTypeBean> getDailyType()
    {

        return mZhiHuApi.getDailyType();
    }

    public Observable<ThemesDetails> getThemesDetailsById(int id)
    {

        return mZhiHuApi.getThemesDetailsById(id);
    }

    public Observable<DailyExtraMessage> getDailyExtraMessageById(int id)
    {

        return mZhiHuApi.getDailyExtraMessageById(id);
    }

    public Observable<DailyComment> getDailyLongCommentById(int id)
    {

        return mZhiHuApi.getDailyLongComment(id);
    }

    public Observable<DailyComment> getDailyShortCommentById(int id)
    {

        return mZhiHuApi.getDailyShortComment(id);
    }

    public Observable<DailyRecommend> getDailyRecommendEditors(int id)
    {

        return mZhiHuApi.getDailyRecommendEditors(id);
    }
}
