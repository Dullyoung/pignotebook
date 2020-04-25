package com.pig.notebook.dapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pig.notebook.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.NotificationViewHolder> {
    Context context;
    List<String> text;
    List<String> title;
    List<Integer> id;
    List<Long> time;

    public void setConfig(Context context, List<String> text, List<String> title, List<Integer> id, List<Long> time) {
        this.context=context;
        this.text=text;
        this.title=title;
        this.id=id;
        this.time=time;
    }


    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notify_item_layout, null);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.tv0.setText(id.get(position)+"");
        holder.tv1.setText(title.get(position));
        holder.tv2.setText(text.get(position));
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        holder.tv3.setText(format.format(time.get(position)));
    }

    @Override
    public int getItemCount() {
        return title.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tv0,tv1,tv2,tv3;
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tv0=itemView.findViewById(R.id.tv0);
            tv1=itemView.findViewById(R.id.tv1);
            tv2=itemView.findViewById(R.id.tv2);
            tv3=itemView.findViewById(R.id.tv3);
        }
    }
}
