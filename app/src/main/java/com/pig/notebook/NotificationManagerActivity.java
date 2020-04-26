package com.pig.notebook;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.service.notification.StatusBarNotification;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.pig.notebook.Service.NotificationService;
import com.pig.notebook.dapter.NotificationListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NotificationManagerActivity extends BaseActivity {
    NotificationManager manager;
    RecyclerView recyclerView;
    List<String> text;
    List<String> title;
    List<Integer> id;
    List<Long> time;
    TextView no_data;
    NotificationListAdapter adapter;
    StatusBarNotification[] notifications;
    int lastID = 1000;
    String TAG = "aaaa";
    Random random = new Random();

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Intent intent = new Intent(NotificationManagerActivity.this, NotificationService.class);
            Bundle bundle = (Bundle) msg.obj;
            intent.putExtras(bundle);
            startService(intent);
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setData();
                }
            }, 100);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_manager);

        random.setSeed(System.currentTimeMillis()+lastID);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        recyclerView = findViewById(R.id.notification_rv);
        adapter = new NotificationListAdapter();
        no_data = findViewById(R.id.no_data_item);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setData();
    }


    private void setData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notifications = manager.getActiveNotifications();
        }

        if (notifications.length == 0) {
            recyclerView.setVisibility(View.GONE);
            no_data.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.GONE);
        }
        text = new ArrayList<>();
        title = new ArrayList<>();
        id = new ArrayList<>();
        time = new ArrayList<>();

        for (int i = 0; i < notifications.length; i++) {
            StatusBarNotification notification = notifications[i];
            text.add(notification.getNotification().extras.get("android.text") + "");
            title.add(notification.getNotification().extras.get("android.title") + "");
            id.add(notification.getId());
            time.add(notification.getPostTime());
        }

        adapter.setConfig(this, text, title, id, time);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        findViewById(R.id.bt1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.cancelAll();
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setData();
                        stopService(new Intent(NotificationManagerActivity.this, NotificationService.class));
                    }
                }, 100);
            }
        });

        findViewById(R.id.bt0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateDialog createDialog = new CreateDialog(NotificationManagerActivity.this);

                createDialog.show();

            }
        });
    }


    public int getNextID() {

        lastID = random.nextInt(8888) + 1000;
        return lastID + 1;
    }


    private class CreateDialog extends Dialog {
        Context context;
        Point screenSize = new Point();
        TextView id, imp;
        EditText title, text;
        Switch s;
        int id_now;
        public CreateDialog(@NonNull Context context) {
            super(context, R.style.no_border_dialog);
            this.context = context;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindowManager().getDefaultDisplay().getSize(screenSize);
            int width = (int) screenSize.x * 4 / 5;
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_create_layout, null), params);
            setCanceledOnTouchOutside(false);
            id = findViewById(R.id.id_tv);
            imp = findViewById(R.id.tv_imp);
            imp.setText(getResources().getString(R.string.normal_tip));
            title = findViewById(R.id.ed_title);
            text = findViewById(R.id.ed_text);
            id_now=getNextID();

            text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        SendNotification();
                    }
                    return true;
                }
            });


            id.setText("" + id_now);
            findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancel();
                }
            });
            s = findViewById(R.id.s_imp);
            s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        imp.setText(getResources().getString(R.string.imp_tip));
                    } else {
                        imp.setText(getResources().getString(R.string.normal_tip));
                    }
                }
            });

            findViewById(R.id.sure).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendNotification();
                }
            });
        }

        private void SendNotification() {
            if (title.getText().toString().equals("") && text.getText().toString().equals("")) {
                Toast.makeText(context, "不能标题和内容均为空", Toast.LENGTH_SHORT).show();
                return;
            }
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("id", id_now + "");
            bundle.putString("title", title.getText().toString());
            bundle.putString("text", text.getText().toString());
            if (s.isChecked()) {
                bundle.putString("imp", "1");
            } else {
                bundle.putString("imp", "0");
            }
            message.obj = bundle;
            handler.sendMessage(message);
            cancel();


        }

    }

}
