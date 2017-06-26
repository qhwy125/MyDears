package com.leasom.guoshun.mymap;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Connection;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.nearby.NearbyInfo;
import com.amap.api.services.nearby.NearbySearch;
import com.amap.api.services.nearby.NearbySearchFunctionType;
import com.amap.api.services.nearby.NearbySearchResult;
import com.amap.api.services.nearby.UploadInfo;
import com.bumptech.glide.Glide;
import com.leasom.guoshun.mymap.serverLocation.LocationService;
import com.leasom.guoshun.mymap.serverLocation.LocationStatusManager;
import com.leasom.guoshun.mymap.serverLocation.Utils;
import com.leasom.guoshun.mymap.util.SaveUser;
import com.leasom.guoshun.mymap.util.SelectPhoto;
import com.yuyh.library.imgsel.ImgSelActivity;

import java.util.List;

import cn.modificator.waterwave_progress.WaterWaveProgress;

import static com.leasom.guoshun.mymap.App.citycode;
import static com.leasom.guoshun.mymap.App.me;
import static com.leasom.guoshun.mymap.App.meicon;
import static com.leasom.guoshun.mymap.App.time;
import static com.leasom.guoshun.mymap.App.you;
import static com.leasom.guoshun.mymap.App.youicon;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,NearbySearch.NearbyListener, OfflineMapManager.OfflineMapDownloadListener, GeocodeSearch.OnGeocodeSearchListener {
    static final int ME_CODE=1;
    static final int YOU_CODE=2;
    private MapView mapView;
    AMap aMap;
    LatLonPoint  latLonPoint;
    public static final String RECEIVER_ACTION = "location_in_background";
    ImageView me_iv,you_iv,user_iv;

    TextView jl_tv;
    double x=34.745876;
    double y=113.735078;
    List<Data> listd;
    ImageView dw_iv,ld_iv,set_iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写


        initView();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVER_ACTION);
        registerReceiver(locationChangeBroadcastReceiver, intentFilter);
        //申请权限
        PermissionsResult();

        //设置头像
        if (!meicon.equals("")){
            Glide.with(this).load(meicon).into(me_iv);
        }
        if (!youicon.equals("")){
            Glide.with(this).load(youicon).into(you_iv);
        }
        //初次设置用户信息

        if (me.equals("")&&you.equals("")){
            showDialog();
        }


    }

    private void initView() {
        me_iv=(ImageView)findViewById(R.id.me_iv);
        you_iv=(ImageView)findViewById(R.id.you_iv);
        dw_iv=(ImageView)findViewById(R.id.dw_iv);
        ld_iv=(ImageView)findViewById(R.id.ld_iv);
        set_iv=(ImageView)findViewById(R.id.set_iv);
        me_iv.setOnClickListener(this);
        you_iv.setOnClickListener(this);
        dw_iv.setOnClickListener(this);
        ld_iv.setOnClickListener(this);
        set_iv.setOnClickListener(this);
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        aMap.getUiSettings().setZoomControlsEnabled(false);
        jl_tv=(TextView)findViewById(R.id.jl_tv);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.me_iv:
                //更改我的头像
                new SelectPhoto(this,ME_CODE);
                break;
            case R.id.you_iv:
                //更改你的头像
                new SelectPhoto(this,YOU_CODE);
                break;
            case R.id.ld_iv:
               //雷达查找身边的人
                startSerach();
                break;
            case R.id.dw_iv:
                //定位自己（开启服务）
                startService();
                break;
            case R.id.set_iv:
                //设置
                showDialog();
                break;

        }
    }

    //相册  回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data==null){
            return;
        }
       if (requestCode==1){
            Glide.with(this).load(data.getStringArrayListExtra(ImgSelActivity.INTENT_RESULT).get(0)).into(me_iv);
            new SaveUser().saveMeIcon(data.getStringArrayListExtra(ImgSelActivity.INTENT_RESULT).get(0));
            meicon=data.getStringArrayListExtra(ImgSelActivity.INTENT_RESULT).get(0);
            bMapme=null;
            addMarkerMe(mylocation);
        }
        if (requestCode==2){
            Glide.with(this).load(data.getStringArrayListExtra(ImgSelActivity.INTENT_RESULT).get(0)).into(you_iv);
            new SaveUser().saveYouIcon(data.getStringArrayListExtra(ImgSelActivity.INTENT_RESULT).get(0));
            youicon=data.getStringArrayListExtra(ImgSelActivity.INTENT_RESULT).get(0);
            bMap=null;
            addMarker(youlocation);
        }
    }
    //将View转换为Bitmap  覆盖物
    private Bitmap getViewBitmapMe() {
        View ViewMe;
        ViewMe=getLayoutInflater().inflate(R.layout.markerme, null);
        final ImageView iv=(ImageView)ViewMe.findViewById(R.id.icon);
        iv.setImageURI(Uri.parse(meicon));
        ViewMe.setDrawingCacheEnabled(true);
        ViewMe.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        ViewMe.layout(0, 0,
                ViewMe.getMeasuredWidth(),
                ViewMe.getMeasuredHeight());
        ViewMe.buildDrawingCache(true);
        Bitmap cacheBitmap = ViewMe.getDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
        return bitmap;
    }
    //将View转换为Bitmap  覆盖物
    private Bitmap getViewBitmapYou() {
        View addViewContent;
        addViewContent= getLayoutInflater().inflate(R.layout.markeryou, null);
        ImageView iv=(ImageView)addViewContent.findViewById(R.id.icon);
        iv.setImageURI(Uri.parse(youicon));
        addViewContent.setDrawingCacheEnabled(true);
        addViewContent.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        addViewContent.layout(0, 0,
                addViewContent.getMeasuredWidth(),
                addViewContent.getMeasuredHeight());
        addViewContent.buildDrawingCache();
        Bitmap cacheBitmap = addViewContent.getDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
        return bitmap;
    }

    //我的覆盖物
    Bitmap bMapme;
    BitmapDescriptor desme;
    Marker markerme;
    LatLng mylocation=null;  //上一次位置
    private void addMarkerMe(LatLng point) {
        if(mylocation==null){
            mylocation = point;
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 16));//设置中心位置
        }else  if (mylocation!=null&&mylocation.latitude!=point.latitude){  //如果位置改变了重新设置中心位置
            mylocation = point;
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 16));
        }
        if (bMapme==null){
            bMapme= getViewBitmapMe();
            desme = BitmapDescriptorFactory.fromBitmap(bMapme);
            markerme = aMap.addMarker(new MarkerOptions().position(point).icon(desme) .anchor(0.5f, 0.5f));
        }
        markerme.setPosition(point); //更新点标记位置
    }
    //你的覆盖物
    Bitmap bMap;
    BitmapDescriptor des;
    Marker marker;
    LatLng youlocation=null;  //上一次位置
    private void addMarker(LatLng point) {
        youlocation=point;
        if (bMap==null){
            bMap=getViewBitmapYou();
            des = BitmapDescriptorFactory.fromBitmap(bMap);
           marker= aMap.addMarker(new MarkerOptions().position(point).icon(des).anchor(0.5f, 0.5f));
        }
        marker.setPosition(point); //更新点标记位置
    }


    /**
     * 启动或者关闭定位服务
     *
     */
    public void startService() {
        //开启定位
        startLocationService();
        //关闭定位
        //stopLocationService();
        LocationStatusManager.getInstance().resetToInit(getApplicationContext());
    }
    private Connection mLocationServiceConn = null;
    /**
     * 开始定位服务
     */
    private void startLocationService(){
        Log.e("==>","time0="+time);
        getApplicationContext().startService(new Intent(this, LocationService.class));
    }
    /**
     * 关闭服务
     * 先关闭守护进程，再关闭定位服务
     */
    private void stopLocationService(){
        sendBroadcast(Utils.getCloseBrodecastIntent());
    }
    String scitycode;
    String city;
    boolean isload=true;
    private BroadcastReceiver locationChangeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(RECEIVER_ACTION)) {
                double longitude = intent.getDoubleExtra("longitude",0);
                double latiude = intent.getDoubleExtra("latiude",0);
                scitycode=intent.getStringExtra("citycode");
                city=intent.getStringExtra("cityname");
                /*Log.e("我在==>",intent.getStringExtra("cityname")+"\n"+
                                 intent.getStringExtra("district")+"\n"+
                                 intent.getStringExtra("address")+"\n"+
                                 intent.getStringExtra("street")
                );*/
                latLonPoint=new LatLonPoint(latiude,longitude);
                /*if (!me.equals("")){
                    listd=Data.getlist();
                    x=listd.get(Integer.parseInt(me)).x;
                    y=listd.get(Integer.parseInt(me)).y;
                    latLonPoint=new LatLonPoint(x,y);
                    addMarkerMe(new LatLng(x,y));
                }*/
                addMarkerMe(new LatLng(latiude,longitude));
                if (!scitycode.equals("")&&!me.equals("")&&citycode.equals("")&&isload){
                    isload=false;
                    downloadmap(scitycode,city);  //下载地图
                }
                uplonpoint();
                startSerach();
            }
        }
    };

    //上传位置
    UploadInfo loadInfo;
    private void uplonpoint() {
        if (loadInfo==null){
            //构造上传位置信息
            loadInfo = new UploadInfo();
            //设置上传位置的坐标系支持AMap坐标数据与GPS数据
            loadInfo.setCoordType(NearbySearch.AMAP);
            //设置上传用户id
            loadInfo.setUserID(me);
        }
        //设置上传数据位置,位置的获取推荐使用高德定位sdk进行获取
        loadInfo.setPoint(latLonPoint);
        //调用异步上传接口
        NearbySearch.getInstance(getApplicationContext())
                .uploadNearbyInfoAsyn(loadInfo);
    }
    //查询位置
    NearbySearch mNearbySearch;
    NearbySearch.NearbyQuery query;
    private void startSerach() {
        if (query==null){
            //获取附近实例（单例模式）
             mNearbySearch = NearbySearch.getInstance(getApplicationContext());
             //设置附近监听
            mNearbySearch.getInstance(getApplicationContext()).addNearbyListener(this);
            //设置搜索条件
            query = new NearbySearch.NearbyQuery();
            //设置搜索的坐标体系
            query.setCoordType(NearbySearch.AMAP);
            //设置搜索半径
            query.setRadius(10000);
            //设置查询的时间
            query.setTimeRange(10000);
            //设置查询的方式驾车还是距离
            query.setType(NearbySearchFunctionType.DRIVING_DISTANCE_SEARCH);
        }
        //设置搜索的中心点
        query.setCenterPoint(latLonPoint);
        //调用异步查询接口
        NearbySearch.getInstance(getApplicationContext())
                .searchNearbyInfoAsyn(query);
    }
    //周边检索的回调函数
    @Override
    public void onNearbyInfoSearched(NearbySearchResult nearbySearchResult,int resultCode) {
        //搜索周边附近用户回调处理
        if(resultCode == 1000){
            if (nearbySearchResult != null
                    && nearbySearchResult.getNearbyInfoList() != null
                    && nearbySearchResult.getNearbyInfoList().size() > 0) {
                LatLng melatlng =new LatLng(latLonPoint.getLatitude(),latLonPoint.getLongitude());
                LatLng youlatlng;
                for (NearbyInfo n:nearbySearchResult.getNearbyInfoList()){
                    /*Log.e("==>","周边搜索结果为size "+ nearbySearchResult.getNearbyInfoList().size() + "first:"
                            + n.getUserID() + "  "
                            + n.getDistance()+ "  "
                            + n.getDrivingDistance() + "  "
                            + n.getTimeStamp() + "  "
                            +n.getPoint().toString());*/

                    if (me.equals(n.getUserID())){
                        String[] str=n.getPoint().toString().split("[,]");
                        melatlng=new LatLng(Double.valueOf(str[0]),Double.valueOf(str[1]));
                        addMarker(melatlng);
                    }
                    if (you.equals(n.getUserID())){
                        String[] str=n.getPoint().toString().split("[,]");
                        youlatlng=new LatLng(Double.valueOf(str[0]),Double.valueOf(str[1]));
                        addMarker(youlatlng);
                        serachYouData(new LatLonPoint(Double.valueOf(str[0]),Double.valueOf(str[1])));
                        float distance = AMapUtils.calculateLineDistance(youlatlng,melatlng);
                        //Log.e("距离==>",distance+"");
                        jl_tv.setText((int) distance+"米");
                        if ((int) distance<=10){
                            Toast.makeText(this,"ta就在你身边",Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(this,"ta在这里",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } else {
                Toast.makeText(this,"ta未上传位置",Toast.LENGTH_SHORT).show();
               // Log.e("","周边搜索结果为空");
            }
        }
        else{
            Toast.makeText(this,"你俩有异常",Toast.LENGTH_SHORT).show();
            //Log.e("","周边搜索出现异常，异常码为："+resultCode);
        }
    }
    //上传位置的回调函数
    @Override
    public void onNearbyInfoUploaded(int i) {
       // Log.e("","上传信息，码为："+i);
    }



    //申请权限
    private void PermissionsResult() {
        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            int i = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (i != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0);
            }else{
                //权限已经申请  并启动服务
                startService();
            }
        }else {
            startService();
        }
    }
    //申请权限回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==0){
            startService();
        }
    }


    //用户信息设置
    EditText me_et,you_et,time_et;
    private void showDialog() {
        View view=getLayoutInflater().inflate(R.layout.dialog,null);
        final AlertDialog builder = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .show();

        me_et=(EditText)view.findViewById(R.id.me_et);
        you_et=(EditText)view.findViewById(R.id.you_et);
        time_et=(EditText)view.findViewById(R.id.time_et);
        if (!new SaveUser().getMeId().equals("")){
            me_et.setText(new SaveUser().getMeId());
        }
        if (!new SaveUser().getYouId().equals("")){
            you_et.setText(new SaveUser().getYouId());
        }
        if (!new SaveUser().getTime().equals("")){
            time_et.setText(new SaveUser().getTime());
        }
        view.findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (you_et.getText().toString().equals("")||me_et.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this,"请输入手机号！",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (you_et.getText().toString().length()!=11||me_et.getText().toString().length()!=11){
                    Toast.makeText(MainActivity.this,"请输入正确手机号！",Toast.LENGTH_SHORT).show();
                    return;
                }
                new SaveUser().saveMeId(me_et.getText().toString());
                new SaveUser().saveYouId(you_et.getText().toString());
                new SaveUser().saveTime(time_et.getText().toString());
                me=me_et.getText().toString();
                you=you_et.getText().toString();
                time=time_et.getText().toString();
                startService();
                builder.dismiss();
            }
        });
    }


    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (locationChangeBroadcastReceiver != null)
            unregisterReceiver(locationChangeBroadcastReceiver);
    }
    @Override
    public void onUserInfoCleared(int i) {
    }


    //下载进度
    @Override
    public void onDownload(int i, int i1, String s) {
        waveProgress.setProgress(i1);
        if (i1==100) {
            new SaveUser().saveCityCode(citycode);
            popupWindow.dismiss();
            Toast.makeText(MainActivity.this,"下载完成",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onCheckUpdate(boolean b, String s) {

    }
    @Override
    public void onRemove(boolean b, String s, String s1) {
    }

    //下载地图
    WaterWaveProgress waveProgress;
    PopupWindow popupWindow;
    private void downloadmap(final String citycode, String city) {
        //构造OfflineMapManager对象
        final OfflineMapManager amapManager = new OfflineMapManager(this, this);
        //ArrayList<OfflineMapCity> arr=amapManager.getOfflineMapCityList();
        WindowManager windowManager = getWindowManager();
        int width = windowManager.getDefaultDisplay().getWidth();
        int heigth = windowManager.getDefaultDisplay().getHeight();
        View view=getLayoutInflater().inflate(R.layout.dialog_loadmap,null);
        View viewbg=getLayoutInflater().inflate(R.layout.dialog_loadmap,null);
        final TextView ok_tv=(TextView)view.findViewById(R.id.ok_tv);
        TextView ps_tv=(TextView)view.findViewById(R.id.ps_tv);
        ImageView off_iv=(ImageView)view.findViewById(R.id.off_iv);
        ps_tv.setText("是否下载"+city+"地图？");
        waveProgress = (WaterWaveProgress)view.findViewById(R.id.waterWaveProgress1);
        waveProgress.setShowProgress(true);
        waveProgress.animateWave();

        popupWindow = new PopupWindow(view,(int)(width),(int)(heigth));
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //显示在屏幕中央
        popupWindow.showAtLocation(viewbg, Gravity.CENTER, 0, 40);
        ok_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ok_tv.setVisibility(View.GONE);
                //按照citycode下载
                try {

                    amapManager.downloadByCityCode(citycode);
                } catch (AMapException e) {
                    e.printStackTrace();
                }
            }
        });
        off_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }
    GeocodeSearch geocoderSearch;
    RegeocodeQuery queryy;
    public void serachYouData(LatLonPoint latlonpoint){
        queryy = new RegeocodeQuery(latlonpoint, 10, GeocodeSearch.AMAP);
        geocoderSearch= new GeocodeSearch(this);
        geocoderSearch.getFromLocationAsyn(queryy);
        geocoderSearch.setOnGeocodeSearchListener(this);
    }
    //查询位置信息
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
       /* Log.e("你在==>",regeocodeResult.getRegeocodeAddress().getCity()+"\n"+
                regeocodeResult.getRegeocodeAddress().getDistrict()+"\n"+
                regeocodeResult.getRegeocodeAddress().getTownship()+"\n"+
                regeocodeResult.getRegeocodeAddress().getFormatAddress()
            );
*/
    }
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
    }
}
