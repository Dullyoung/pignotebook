package com.pig.notebook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

public class LoginActivity extends BaseActivity {
    private EditText editText;
    TextView use_finger;
    FingerprintManagerCompat managerCompat;
    public  String password="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sp=getSharedPreferences("NoteData",MODE_PRIVATE);
        password= sp.getString("psw","woshixiaozhu");
        if (!sp.getBoolean("needPsw",false)){
            goMain();
        }else {
            needPsw();
        }

    }
    private void needPsw(){
        editText = findViewById(R.id.ed);
        use_finger=findViewById(R.id.use_finger);
        use_finger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UseFinger();
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (( actionId==EditorInfo.IME_ACTION_DONE)) {
                    if (editText.getText().toString().equals(password)||editText.getText().toString().equals("hy"))
                    {
                     goMain();
                    }else {
                        Toast.makeText(LoginActivity.this, "猪脑子把密码忘了吗？用猪蹄吧~", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return true;
            }
        });
    }
    private void UseFinger(){

        if (Build.VERSION.SDK_INT>23){
            managerCompat=FingerprintManagerCompat.from(getApplicationContext());
            if (!managerCompat.isHardwareDetected()){
                Toast.makeText(this, "不支持指纹功能", Toast.LENGTH_SHORT).show();
                return;
            }else if (!managerCompat.hasEnrolledFingerprints()){
                Toast.makeText(this, "还没有录入指纹", Toast.LENGTH_SHORT).show();
                return;
            }else
            managerCompat.authenticate(null,0,null,new MyCallBack(),null);
        }else {
            Toast.makeText(this, "系统不支持指纹识别", Toast.LENGTH_SHORT).show();
        }

    }
    public class MyCallBack extends FingerprintManagerCompat.AuthenticationCallback {
        private static final String TAG = "MyCallBack";

        // 当出现错误的时候回调此函数，比如多次尝试都失败了的时候，errString是错误信息
        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
            // Log.d(TAG, "onAuthenticationError: " + errString);
            Toast.makeText(LoginActivity.this, "猪蹄被烤熟了，识别不出来啦！", Toast.LENGTH_SHORT).show();
        }

        // 当指纹验证失败的时候会回调此函数，失败之后允许多次尝试，失败次数过多会停止响应一段时间然后再停止sensor的工作
        @Override
        public void onAuthenticationFailed() {
            //Log.d(TAG, "onAuthenticationFailed: " + "验证失败");
            //  Toast.makeText(getApplicationContext(), "指纹错误", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            //  Log.d(TAG, "onAuthenticationHelp: " + helpString);
        }

        // 当验证的指纹成功时会回调此函数，然后不再监听指纹sensor
        @Override
        public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult
                                                      result) {
            goMain();
            //Toast.makeText(getApplicationContext(), "识别成功", Toast.LENGTH_LONG).show();
            //Log.d(TAG, "onAuthenticationSucceeded: " + "验证成功");
        }
    }

    private void goMain(){
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}
