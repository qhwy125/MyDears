package com.leasom.guoshun.mymap.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.yuyh.library.imgsel.ImageLoader;
import com.yuyh.library.imgsel.ImgSelActivity;
import com.yuyh.library.imgsel.ImgSelConfig;

/**
 * Created by Administrator on 2017/6/7.
 */

public class SelectPhoto {
    static final int ME_CODE=1;
    static final int YOU_CODE=2;
    static final int Z_PHOTO_CODE=1;
    static final int F_PHOTO_CODE=2;
    public SelectPhoto(Context context,int code){
        // 自由配置选项
        ImgSelConfig config = new ImgSelConfig.Builder(context, loader)
                // 是否多选, 默认true
                .multiSelect(false)
                // 是否记住上次选中记录, 仅当multiSelect为true的时候配置，默认为true
                .rememberSelected(false)
                // “确定”按钮背景色
                .btnBgColor(Color.GRAY)
                // “确定”按钮文字颜色
                .btnTextColor(Color.BLUE)
                // 使用沉浸式状态栏
                .statusBarColor(Color.parseColor("#000000"))
                // 标题
                .title("图片")
                // 标题文字颜色
                .titleColor(Color.WHITE)
                // TitleBar背景色
                .titleBgColor(Color.parseColor("#000000"))
                // 第一个是否显示相机，默认true
                .needCamera(true)
                // 裁剪大小。needCrop为true的时候配置
                .cropSize(1, 1, 200, 200)
                .needCrop(true)
                .build();
        // 跳转到图片选择器
        ImgSelActivity.startActivity((Activity)context, config, code);
    }
    // 自定义图片加载器
    private ImageLoader loader = new ImageLoader() {
        @Override
        public void displayImage(Context context, String path, ImageView imageView) {
            // TODO 在这边可以自定义图片加载库来加载ImageView，例如Glide、Picasso、ImageLoader等
            Glide.with(context).load(path).into(imageView);
        }
    };

}
