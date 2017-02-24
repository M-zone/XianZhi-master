package com.zmz.rxzhihu.model;

import java.util.List;

/**
 * Created by Mzone on 16/4/23 11:28
 * mzonez@foxmail.com
 * <p/>
 * 知乎专栏列表
 */
public class DailySections
{

    public List<DailySectionsInfo> data;

    public class DailySectionsInfo
    {

        public String description;

        public int id;

        public String name;

        public String thumbnail;
    }
}
