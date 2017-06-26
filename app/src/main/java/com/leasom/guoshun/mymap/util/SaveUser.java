package com.leasom.guoshun.mymap.util;


import com.leasom.guoshun.mymap.App;

/**
 * Created by rr on 2017/4/21.
 */
// 记录登录状态
public class SaveUser {

    private static SaveUser instance = new SaveUser();
    public SaveUser(){}
    public static SaveUser getInstance() {
        return instance;
    }

    private String me = "meID";
    private String you = "youID";
    private String meicon = "meIcon";
    private String youicon = "youIcon";
    private String citycode = "cityCode";

    public void saveMeId(String id) {
        SharePrefUtil.saveString(App.getContext(),me,id);
    }
    public void saveYouId(String id) {
        SharePrefUtil.saveString(App.getContext(),you,id);
    }
    public void saveMeIcon(String id) {
        SharePrefUtil.saveString(App.getContext(),meicon,id);
    }
    public void saveYouIcon(String id) {
        SharePrefUtil.saveString(App.getContext(),youicon,id);
    }
    public void saveCityCode(String id) {
        SharePrefUtil.saveString(App.getContext(),citycode,id);
    }
    public String getMeId(){
        String beanStr = SharePrefUtil.getString(App.getContext(),me,"");
        if(beanStr.equals("")){
            return "";
        }
        return beanStr;
    }
    public String getYouId(){
        String beanStr = SharePrefUtil.getString(App.getContext(),you,"");
        if(beanStr.equals("")){
            return "";
        }
        return beanStr;
    }
    public String getMeIcon(){
        String beanStr = SharePrefUtil.getString(App.getContext(),meicon,"");
        if(beanStr.equals("")){
            return "";
        }
        return beanStr;
    }
    public String getYouIcon(){
        String beanStr = SharePrefUtil.getString(App.getContext(),youicon,"");
        if(beanStr.equals("")){
            return "";
        }
        return beanStr;
    }
    public String getCityCode(){
        String beanStr = SharePrefUtil.getString(App.getContext(),citycode,"");
        if(beanStr.equals("")){
            return "";
        }
        return beanStr;
    }
}
