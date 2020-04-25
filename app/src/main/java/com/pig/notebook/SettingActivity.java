package com.pig.notebook;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;

public class SettingActivity extends BaseActivity {
    Switch aSwitch;
    SharedPreferences sp;
    EditText psw1, psw2;
    Button change;
    TextView diy_bg;
    final int PERMISSION_WRITE_AND_READ = 666;
    final int USE_PIC = 667;
    final int USE_CROP = 668;
    Uri finalUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        aSwitch = findViewById(R.id.switch_control);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        sp = getSharedPreferences("NoteData", MODE_PRIVATE);
        changeWord();
        diy_bg = findViewById(R.id.diy_bg);
        diy_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(SettingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {//如果有读写权限 打开相册
                    startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), USE_PIC);
                } else {//如果没有权限就申请
                    ActivityCompat.requestPermissions(SettingActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_WRITE_AND_READ);
                }
            }
        });
        findViewById(R.id.recovery_default).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               SharedPreferences sp=getSharedPreferences("NoteData",MODE_PRIVATE);
               SharedPreferences.Editor editor=sp.edit();
               editor.putInt("alpha",-1);
               editor.commit();
                Toast.makeText(SettingActivity.this, "主页背景重启生效", Toast.LENGTH_SHORT).show();
               finish();
            }
        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == USE_PIC) {
            if (resultCode==RESULT_OK){
                Point screenSize = new Point();
                getWindow().getWindowManager().getDefaultDisplay().getSize(screenSize);
                cutPhoto(data.getData(), screenSize.x, screenSize.y);
            }else {
                return;
            }

        }
        if (requestCode == USE_CROP) {
            if (resultCode == RESULT_OK){
                startActivity(new Intent(this,EditPicActivity.class));
            }

            if (resultCode == RESULT_CANCELED) {
                return;
            }
        }

    }

    String TAG = "aaaa";


    private void cutPhoto(Uri uri, int width, int height) {
        Intent cut = new Intent("com.android.camera.action.CROP");
        cut.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cut.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        cut.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        cut.putExtra("aspectX", width);
        cut.putExtra("aspectY", height);
        // outputX outputY 是裁剪图片宽高
        cut.putExtra("outputX", width);
        cut.putExtra("outputY", height);
        //cut.putExtra("return-data", true);//是否返回bitmap

        File f = new File(dir, fileName);
        if (!new File(dir).exists()) {
            new File(dir).mkdirs();
        }
        if (!f.exists()) {
            try {
                f.createNewFile();//创建一个空的jpg文件
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        finalUri = Uri.fromFile(f);//保存剪切后的照片必须用这个获取uri
        cut.putExtra(MediaStore.EXTRA_OUTPUT, finalUri);//根据uri保存图片或视频 mediaStore
        startActivityForResult(cut, USE_CROP);//剪切相册中已有的照片
    }


    private void changeWord() {
        aSwitch.setChecked(sp.getBoolean("needPsw", false));
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sp.edit();
                if (isChecked) {//需要密码启动
                    editor.putBoolean("needPsw", true);
                } else {//不需要密码启动
                    editor.putBoolean("needPsw", false);
                }
                editor.commit();
            }
        });
        psw1 = findViewById(R.id.first_ed);
        psw2 = findViewById(R.id.second_ed);
        change = findViewById(R.id.change_psw);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sp.edit();
                if (psw1.getText().toString().equals("")) {
                    Toast.makeText(SettingActivity.this, "密码不能为空嗷~", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!psw1.getText().toString().equals(psw2.getText().toString())) {
                    Toast.makeText(SettingActivity.this, "两次密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Toast.makeText(SettingActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                    editor.putString("psw", psw1.getText().toString());
                    editor.commit();
                }
            }
        });
        findViewById(R.id.goqq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SettingActivity.this, "复制了点小惊喜，\n快粘贴发送吧！", Toast.LENGTH_SHORT).show();
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText(null, "爸比爸比，我是小猪~");
                clipboardManager.setPrimaryClip(clipData);
                gotoQQ(SettingActivity.this, "664846453");
            }
        });
    }

    public static void gotoQQ(final Context context, String qq) {
        try {
            String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + qq;
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(context, "手机QQ未安装或该版本不支持", Toast.LENGTH_SHORT).show();
        }
    }
}
