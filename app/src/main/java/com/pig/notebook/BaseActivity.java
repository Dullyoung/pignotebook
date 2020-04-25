package com.pig.notebook;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

public class BaseActivity extends AppCompatActivity {
    public String dir;

    public String fileName = "myBackground";
    View view;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        view = getWindow().getDecorView();
        dir = Environment.getExternalStorageDirectory() + getCacheDir().getAbsolutePath() + "/background/";



    }

    @Override
    protected void onResume() {
        super.onResume();
        ImageView i=new ImageView(this);
        if (new File(dir,fileName).exists()){
            SharedPreferences sp=getSharedPreferences("NoteData",MODE_PRIVATE);
            int alpha= sp.getInt("alpha",-1);
            if (alpha!=-1){
                Drawable drawable=Drawable.createFromPath(dir+fileName);
                i.setImageDrawable(drawable);
                i.setImageAlpha(alpha);
                //判断这个图片是不是添加过了
                ((ViewGroup) view).addView(i,0,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                return;
            }
        }
        view.setBackgroundColor(getColor(R.color.default_background_color));
        ((ViewGroup) view).removeView(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}

