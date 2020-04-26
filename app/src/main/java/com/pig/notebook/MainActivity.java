package com.pig.notebook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pig.notebook.dapter.ShowAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageView create;
    private RecyclerView show_rv;
    private TextView no_data;
    private SwipeRefreshLayout refreshLayout;
    String TAG = "aaaa";
    private List<String> contentList;
    private List<String> timeList;
    private List<String> titleList;
    public String dir;

    public String fileName = "myBackground";
    RelativeLayout toolLayout;
    TextView cancel, checkAll;
    ShowAdapter showAdapter = new ShowAdapter();
    View view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        view = getWindow().getDecorView();

        dir = Environment.getExternalStorageDirectory() + getCacheDir().getAbsolutePath() + "/background/";
        findView();
        refresh();
        init();
        if (new File(dir, fileName).exists()) {
            ImageView i=new ImageView(this);
            Drawable drawable = Drawable.createFromPath(dir + fileName);
            i.setImageDrawable(drawable);
            SharedPreferences sp = getSharedPreferences("NoteData", MODE_PRIVATE);
            SharedPreferences.Editor editor=sp.edit();
            int alpha = sp.getInt("alpha", -1);
            if (alpha!=-1){
                i.setImageAlpha(alpha);
                ((ViewGroup) view).addView(i,0);
                editor.putBoolean("added",true);
                editor.commit();
            }
            view.setBackgroundResource(R.color.default_background_color);
        }


    }

    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
        ImageView i = new ImageView(this);
        //判断背景图片路径文件是否存在 不存在就用默认就好了
        if (new File(dir, fileName).exists()) {

            SharedPreferences sp = getSharedPreferences("NoteData", MODE_PRIVATE);
            int alpha = sp.getInt("alpha", -1);
            boolean added=sp.getBoolean("added",false);
            boolean changed=sp.getBoolean("changed",false);
            SharedPreferences.Editor editor=sp.edit();
            //恢复默认
            if (alpha==-1){
                if (added){
                    //添加过就移除image
                    ((ViewGroup) view).removeViewAt(0);
                    Log.i(TAG, "onResume: 移除image 设置默认背景");
                    view.setBackgroundResource(R.color.default_background_color);
                    editor.putBoolean("added",false);
                    editor.commit();
                }
                //没有添加过就用默认背景
                view.setBackgroundResource(R.color.default_background_color);
                Log.i(TAG, "onResume:  恢复默认");
                return;
            }
            // 1 已经添加过 图片没有改变 -》不做任何操作
            if (added&&!changed){
                Log.i(TAG, "onResume: +已经添加过");
                return;
            }
            // 2 添加过  但是自定义图片改变了
            // 不需要再添加 只需要改变Image的drawable
            if (added&&changed){
                editor.putBoolean("changed",false);
                editor.commit();
                ImageView imageView= ((ImageView)  ((ViewGroup) view).getChildAt(0));
                imageView.setImageAlpha(alpha);
                imageView.setImageDrawable(Drawable.createFromPath(dir+fileName));
                Log.i(TAG, "onResume: 添加过 图片改变了 "+((ViewGroup) view).getChildAt(0));
                return;
            }
            // 没添加过 添加图片到最底层
            Drawable drawable = Drawable.createFromPath(dir + fileName);
            i.setImageDrawable(drawable);
            i.setImageAlpha(alpha);
            ((ViewGroup) view).addView(i,0);
            editor.putBoolean("added",true);
            Log.i(TAG, "onResume: 没添加过");
            editor.commit();
            return;
        }


    }

    private void findView() {
        show_rv = findViewById(R.id.recycler_view);
        show_rv.getParent().requestDisallowInterceptTouchEvent(false);
        toolLayout = findViewById(R.id.tool_layout);
        cancel = findViewById(R.id.cancel);
        checkAll = findViewById(R.id.check_cancel_all);
        show_rv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        create = findViewById(R.id.create);
        no_data = findViewById(R.id.no_data_item);
        refreshLayout = findViewById(R.id.refresh);
        findViewById(R.id.notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNotification();
            }
        });
        findViewById(R.id.psw_manager).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));

            }
        });
        onClick();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getMessage(String s) {
        if (s.equals("ToolBarShow")) {
            toolLayout.setVisibility(View.VISIBLE);
        }
        if (s.equals("AllCheck")) {
            checkAll.setText("全不选");
        }
        if (s.equals("NotAllCheck")) {
            checkAll.setText("全选");
        }
    }

    private void onClick() {
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CreateActivity.class));
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAdapter.setHideCheck(true);
                showAdapter.CancelCheckAll();
                toolLayout.setVisibility(View.GONE);
            }
        });
        checkAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showAdapter.getIsAllCheck()) {
                    checkAll.setText("全选");
                    showAdapter.CancelCheckAll();
                } else {
                    checkAll.setText("全不选");
                    showAdapter.CheckAll();
                }
            }
        });


    }

    private void refresh() {
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.default_color));
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                show_rv.setVisibility(View.GONE);
                no_data.setVisibility(View.GONE);
                init();

            }
        });
    }

    SharedPreferences sp;

    private void init() {
        sp = getSharedPreferences("NoteData", MODE_PRIVATE);
        String data = sp.getString("dataJson", "");
        if (data.equals("") | data.equals("[]")) {
            show_rv.setVisibility(View.GONE);
            no_data.setVisibility(View.VISIBLE);
        } else {
            no_data.setVisibility(View.GONE);
            show_rv.setVisibility(View.VISIBLE);
            setRecyclerView(data);
        }
        refreshLayout.setRefreshing(false);

    }

    private void setRecyclerView(String data) {
        contentList = new ArrayList<>();
        timeList = new ArrayList<>();
        titleList = new ArrayList<>();
        String time;
        String content;
        String title;
        try {
            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                content = jsonObject.getString("content");
                time = jsonObject.getString("time");
                title = jsonObject.getString("title");
                contentList.add(content);
                timeList.add(time);
                titleList.add(title);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "数据读取失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        showAdapter.setConfig(this, contentList, timeList, titleList);
        show_rv.setAdapter(showAdapter);
        show_rv.scrollToPosition(0);
        refreshLayout.setRefreshing(false);

    }

    public void putToTop(View view) {
        int CheckNumber = 0;
        List<Boolean> checkList = showAdapter.getIsCheck();
        for (boolean b : checkList) {
            if (b) {
                CheckNumber++;
            }
        }
        if (CheckNumber == 0) {
            Toast.makeText(this, "猪猪虽然秃顶了\n但还是要选一个ヾ(≧O≦)〃嗷~", Toast.LENGTH_SHORT).show();
        }
        if (CheckNumber > 1) {
            Toast.makeText(this, "猪脑子有" + CheckNumber + "个秃顶嘛?\n只能选一个置顶呀！", Toast.LENGTH_SHORT).show();
        }
        if (CheckNumber == 1) {
            sp = getSharedPreferences("NoteData", MODE_PRIVATE);
            String data = sp.getString("dataJson", "");
            try {
                JSONArray jsonArray = new JSONArray(data);
                //最上面的便签
                int chooseObjectNumber = 0;
                for (int i = 0; i < checkList.size(); i++) {
                    if (checkList.get(i)) {
                        chooseObjectNumber = i;

                    }
                }
                //获取要更换的便签
                //标签中是最后创建的在最上面 在数组的最右 index越大
                JSONObject chooseObject = (JSONObject) jsonArray.get(jsonArray.length() - chooseObjectNumber - 1);
                //获取原来最大的那个object
                JSONObject changeObject = (JSONObject) jsonArray.get(jsonArray.length() - 1);
                //把选中的object放到数组最右
                jsonArray.put(jsonArray.length() - 1, chooseObject);
                //获取倒数第二大的ob;
                JSONObject secondObject;
                for (int i = 1; i <= chooseObjectNumber; i++) {
                    //获取倒数第二大的object
                    secondObject = (JSONObject) jsonArray.get(jsonArray.length() - i - 1);
                    //把之前最大的object放到倒数第二大的位置
                    jsonArray.put(jsonArray.length() - i - 1, changeObject);
                    //把倒数第二大的object变成下一次循环中最大的object
                    changeObject = secondObject;
                }
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("dataJson", jsonArray.toString());
                editor.commit();
                init();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    public void delete(View view) {
        List<Boolean> deleteList = showAdapter.getIsCheck();
        sp = getSharedPreferences("NoteData", MODE_PRIVATE);
        String data = sp.getString("dataJson", "");
        try {
            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < deleteList.size(); i++) {
                if (deleteList.get(i))
                    jsonArray.remove(deleteList.size() - i - 1);
            }
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("dataJson", jsonArray.toString());
            editor.commit();
            showAdapter.CancelCheckAll();
            showAdapter.setHideCheck(true);
            init();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        toolLayout.setVisibility(View.GONE);
    }


    long firstTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //如果开启了编辑状态
            if (toolLayout.getVisibility() == View.VISIBLE) {
                showAdapter.setHideCheck(true);
                showAdapter.CancelCheckAll();
                toolLayout.setVisibility(View.GONE);
                return true;
            }
            //正常状态下
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 5000 || secondTime - firstTime < 500) {
                Toast.makeText(this, "哈哈，又被困住了~", Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
                return true;
            } else {
                Toast.makeText(this, "骗你的小猪", Toast.LENGTH_SHORT).show();
                finish();
            }

        }
        return super.onKeyDown(keyCode, event);


    }


    private void goToNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (manager.areNotificationsEnabled()) {
                startActivity(new Intent(MainActivity.this, NotificationManagerActivity.class));
            } else {
                openNotificationSettingsForApp();
            }
        } else {
            startActivity(new Intent(MainActivity.this, NotificationManagerActivity.class));
        }
    }


    private void openNotificationSettingsForApp() {
        // Links to this app's notification settings.
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getApplicationContext().getPackageName());
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, getApplicationInfo().uid);
        //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
        intent.putExtra("app_package", getApplicationContext().getPackageName());
        intent.putExtra("app_uid", getApplicationInfo().uid);
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
