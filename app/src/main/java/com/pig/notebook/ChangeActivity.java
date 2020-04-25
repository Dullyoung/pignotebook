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

public class ChangeActivity extends BaseActivity {
    private ImageView back, finish;
    private EditText editText,title;
    TextView time;
    private SharedPreferences sp;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change);

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        finish = findViewById(R.id.finish);
        editText = findViewById(R.id.ed);
        time=findViewById(R.id.time);
        title=findViewById(R.id.title);
        position = getIntent().getIntExtra("position", -1);
        if (position != -1) {
            getData();
        } else {
            Toast.makeText(this, "哎呀，猪脑子丢了！", Toast.LENGTH_SHORT).show();
        }
    }

    JSONArray jsonArray;

    private void getData() {
        sp = getSharedPreferences("NoteData", MODE_PRIVATE);

        String history = sp.getString("dataJson", "");
        if (history.equals("")) {
            Toast.makeText(this, "小猪被外星人抓走了", Toast.LENGTH_SHORT).show();
        } else {
            try {
                jsonArray = new JSONArray(history);
                JSONObject jsonObject = (JSONObject) jsonArray.get(position);
                editText.setText(jsonObject.getString("content"));
                title.setText(jsonObject.getString("title"));
                long timestamp = Long.parseLong(jsonObject.getString("time"));
                SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss"); //设置格式
                String timeText = format.format(timestamp);
                time.setText(timeText);
                editText.setSelection(editText.getText().length());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    if (editText.getText().toString().trim().equals("")){
                        Toast.makeText(ChangeActivity.this, "猪脑子需要记点东西哦！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonObject.put("content", editText.getText());
                    jsonObject.put("time", System.currentTimeMillis());
                    jsonObject.put("title",title.getText().toString().trim());
                    jsonArray.put(position, jsonObject);
                    SharedPreferences.Editor editor=sp.edit();
                    editor.putString("dataJson",jsonArray.toString());
                    editor.commit();
                    finish();
                } catch (JSONException e) {
                    Toast.makeText(ChangeActivity.this, "哎呀，猪脑子丢了！", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

}
