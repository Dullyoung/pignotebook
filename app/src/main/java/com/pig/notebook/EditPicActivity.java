package com.pig.notebook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditPicActivity extends AppCompatActivity {
    SeekBar fuzzy, alpha;
    ImageView bg;
    Bitmap myBit;
    public String dir;
    public String fileName = "myBackground";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         View view = getWindow().getDecorView();

        setContentView(R.layout.activity_edit_pic);
        fuzzy = findViewById(R.id.fuzzy_seekbar);
        alpha = findViewById(R.id.alpha_seekbar);

        bg = findViewById(R.id.background_preview);
        dir = Environment.getExternalStorageDirectory() + getCacheDir().getAbsolutePath() + "/background/";
        try {
            bg.setImageBitmap( BitmapFactory.decodeStream(new FileInputStream(dir+fileName)));
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "读取文件失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        findViewById(R.id.finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImageToSD();
                SharedPreferences sharedPreferences=getSharedPreferences("NoteData",MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putInt("alpha", (100-alpha.getProgress())*255/100);
//                editor.putBoolean("added",false);
                editor.putBoolean("changed",true);
                editor.commit();
                finish();
            }
        });
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        fuzzy.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(dir+fileName);
                    myBit = BitmapFactory.decodeStream(fis);
                    bg.setImageBitmap(blurBitmap(EditPicActivity.this, myBit, progress / 4));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        alpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int alpha= (100-progress)*255/100;
                bg.setImageAlpha(alpha);//0-255

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myBit != null) {
            myBit.recycle();
            myBit = null;
            System.gc();
        }
    }
    private Bitmap getViewBitmap(ImageView imageView) {
        BitmapDrawable bitmapDrawable= (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap=bitmapDrawable.getBitmap();
        return bitmap;
    }


    public void saveImageToSD( ) {
        File fileFolder = new File(dir);
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }
        File file = new File(fileFolder, fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            getViewBitmap(bg).compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //模糊
    public static Bitmap blurBitmap(Context context, Bitmap image, float blurRadius) {
        if (blurRadius == 0) {
            blurRadius = 0.01f;
        }
        // 计算图片缩小后的长宽
//            int width = Math.round(image.getWidth() * BITMAP_SCALE);
//            int height = Math.round(image.getHeight() * BITMAP_SCALE);
        int width = image.getWidth();
        int height = image.getHeight();
        // 将缩小后的图片做为预渲染的图片
        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        // 创建一张渲染后的输出图片
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
        // 创建RenderScript内核对象
        RenderScript rs = RenderScript.create(context);
        // 创建一个模糊效果的RenderScript的工具对象
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        // 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间
        // 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);

        // 设置渲染的模糊程度, 25f是最大模糊度
        blurScript.setRadius(blurRadius);

        // 设置blurScript对象的输入内存
        blurScript.setInput(tmpIn);
        // 将输出数据保存到输出内存中
        blurScript.forEach(tmpOut);

        // 将数据填充到Allocation中
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;

    }

}
