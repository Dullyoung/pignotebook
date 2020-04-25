package com.pig.notebook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

public class CreateActivity extends BaseActivity {
    private ImageView back, finish;
    private EditText editText,ed_title;
    private TextView time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        findView();

    }

    private void findView() {
        back = findViewById(R.id.back);
        time=findViewById(R.id.time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss"); //设置格式
        String timeText = format.format(System.currentTimeMillis());
        time.setText(timeText);
        ed_title=findViewById(R.id.title);
        finish = findViewById(R.id.finish);
        editText = findViewById(R.id.edit_text);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("NoteData", MODE_PRIVATE);
                String historyData = sharedPreferences.getString("dataJson", "");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                JSONArray jsonArray = null;
                try {
                    if (historyData.equals("")) {
                        jsonArray = new JSONArray();
                    } else {
                        jsonArray = new JSONArray(historyData);
                    }
                    JSONObject jsonObject = new JSONObject();
                    if (editText.getText().toString().trim().equals("")){
                        Toast.makeText(CreateActivity.this, "猪脑子还真啥也不记呀？", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonObject.put("title",ed_title.getText().toString().trim());
                    jsonObject.put("content", editText.getText());
                    jsonObject.put("time", ""+System.currentTimeMillis());
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    Toast.makeText(CreateActivity.this, "读取文件失败", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                editor.putString("dataJson", jsonArray.toString());
                editor.commit();
                finish();
            }
        });

    }
}
