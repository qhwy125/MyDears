package com.leasom.guoshun.mymap;

import android.app.Application;
import android.content.Context;

import com.leasom.guoshun.mymap.util.SaveUser;

/**
 * Created by Administrator on 2017/6/26.
 */
public class App  extends Application{
    public static String me="";
    public static String you="";
    public static String youicon="";
    public static String meicon="";
    public static String citycode="";
    public static String time="";
    private static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext=getApplicationContext();
        me=new SaveUser().getMeId();
        you=new SaveUser().getYouId();
        meicon=new SaveUser().getMeIcon();
        youicon=new SaveUser().getYouIcon();
        citycode=new SaveUser().getCityCode();
        time=new SaveUser().getTime();
    }
    public static Context getContext(){
        return mContext;
    }
}
