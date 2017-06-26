package com.leasom.guoshun.mymap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/6/26.
 */

public class Data {
    double x=34.745876;
    double y=113.735078;
    public Data(){

    }
    public Data( double x,double y) {
        this.y = y;
        this.x = x;
    }
    public static List<Data> getlist(){
        List<Data> list=new ArrayList<>();
        list.add(new Data(34.745876,113.735078));
        list.add(new Data(34.745876,113.735278));
        list.add(new Data(34.745876,113.735478));
        list.add(new Data(34.745876,113.735678));
        list.add(new Data(34.745876,113.735878));
        list.add(new Data(34.745876,113.736078));
        return list;
    }
}
