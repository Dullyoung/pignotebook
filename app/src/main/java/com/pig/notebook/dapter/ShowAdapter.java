package com.pig.notebook.dapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pig.notebook.ChangeActivity;
import com.pig.notebook.R;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShowAdapter extends RecyclerView.Adapter<ShowAdapter.ShowHolder> {
    private Context context;
    private List<String> content;
    private List<String> create_time;
    private List<Boolean> isCheck;
    private List<String> titleList;
    private int lastNumber;
    boolean hideCheckBox=true;
    int CheckNumber = 0;
    Random random = new Random(System.currentTimeMillis());

    public void setConfig(Context context, List<String> content, List<String> create_time,List<String> titleList) {
        this.context = context;
        this.content = content;
        this.titleList=titleList;
        this.create_time = create_time;
        isCheck = new ArrayList<>();
        for (int i = 0; i < content.size(); i++) {
            isCheck.add(false);
        }

    }

    public List<Boolean> getIsCheck() {
        return isCheck;
    }

    @NonNull
    @Override
    public ShowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, null);
        ShowHolder holder = new ShowHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull final ShowHolder holder, final int position) {

        if (hideCheckBox) {
            holder.checkBox.setVisibility(View.GONE);
        } else {
            holder.checkBox.setVisibility(View.VISIBLE);
            for (int i = 0; i < isCheck.size(); i++) {
                if (isCheck.get(i)) {
                    holder.checkBox.setChecked(true);
                } else {
                    holder.checkBox.setChecked(false);
                }
            }

        }

        int number = random.nextInt(4);
        if (lastNumber == number) {
            number = number >= 3 ? 0 : number + 1;//连续两次相同，那就把第二次的=1，如果为3就变成0
        }
        lastNumber = number;
        switch (number) {
            case 0:
                holder.item_layout.setBackgroundResource(R.drawable.item_bg_0);
                break;
            case 1:
                holder.item_layout.setBackgroundResource(R.drawable.item_bg_1);
                break;
            case 2:
                holder.item_layout.setBackgroundResource(R.drawable.item_bg_2);
                break;
            case 3:
                holder.item_layout.setBackgroundResource(R.drawable.item_bg_3);
                break;
        }
        long timestamp = Long.parseLong(create_time.get(getItemCount() - position - 1));
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss"); //设置格式
        String timeText = format.format(timestamp);
        holder.time.setText(timeText);
        if (titleList.get(getItemCount()-position-1).equals("")){
            holder.title.setText("标题");
        }else {
            holder.title.setText(titleList.get(getItemCount()-position-1));
        }

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isCheck.set(position, isChecked);
                if (getIsAllCheck()) {
                    EventBus.getDefault().post("AllCheck");
                } else {
                    EventBus.getDefault().post("NotAllCheck");
                }
            }
        });
        holder.content.setText(content.get(getItemCount() - position - 1));
        holder.item_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.checkBox.getVisibility() == View.VISIBLE) {
                    if (holder.checkBox.isChecked()) {
                        isCheck.set(position, false);
                        holder.checkBox.setChecked(false);
                    } else {
                        isCheck.set(position, true);
                        holder.checkBox.setChecked(true);
                    }

                } else {
                    Intent intent =
                            new Intent(context, ChangeActivity.class);
                    intent.putExtra("position", getItemCount() - position - 1);
                    context.startActivity(intent);
                }
            }
        });
        holder.item_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                EventBus.getDefault().post("ToolBarShow");
                hideCheckBox = false;
                notifyDataSetChanged();
                return true;
            }
        });
    }

    public void setHideCheck(boolean hideCheck) {

        hideCheckBox = hideCheck;
        CancelCheckAll();
    }

    public boolean getIsAllCheck() {
        hideCheckBox=false;
        int yes = 0;
        for (int i = 0; i < isCheck.size(); i++) {
            if (isCheck.get(i)) {
                yes++;
            }
        }
        if (yes == isCheck.size()) {
            return true;
        } else {
            return false;
        }
    }

    public void CheckAll() {
        hideCheckBox=false;
        for (int i = 0; i < getItemCount(); i++) {
            isCheck.set(i, true);
        }
        notifyDataSetChanged();
    }

    public void CancelCheckAll() {
        hideCheckBox=true;
        for (int i = 0; i < getItemCount(); i++) {
            isCheck.set(i, false);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return content.size();
    }

    public class ShowHolder extends RecyclerView.ViewHolder {
        public TextView content, time,title;
        private RelativeLayout item_layout;
        private CheckBox checkBox;

        public ShowHolder(@NonNull View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.tv0);
            checkBox = itemView.findViewById(R.id.checkbox);
            item_layout = itemView.findViewById(R.id.item_layout);
            content = itemView.findViewById(R.id.tv1);
            time = itemView.findViewById(R.id.tv2);
        }
    }
}
