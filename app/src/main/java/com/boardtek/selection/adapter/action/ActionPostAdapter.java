package com.boardtek.selection.adapter.action;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.Stream;
import com.boardtek.selection.R;

import java.util.List;
import java.util.Map;

public class ActionPostAdapter extends RecyclerView.Adapter<ActionPostAdapter.SettingViewHolder> {

    private Map<String,String> map;
    private List<Map.Entry<String, String>> list;

    public ActionPostAdapter(Map<String, String> map) {
        this.map = map;
        this.list = Stream.of(map.entrySet()).toList();
    }

    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_action_item_setting_layout,parent,false);
        return new SettingViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onBindViewHolder(@NonNull SettingViewHolder holder, int position) {
        holder.bindTo(list.get(position));
    }

    class SettingViewHolder extends RecyclerView.ViewHolder{

        private TextView tPostName;
        private EditText ePost;

        public SettingViewHolder(@NonNull View itemView) {
            super(itemView);
            tPostName = itemView.findViewById(R.id.t_action_item_post_name);
            ePost = itemView.findViewById(R.id.e_action_item_post);
        }

        public void bindTo(Map.Entry<String, String> set){
            tPostName.setText(set.getKey());
            ePost.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    map.put(set.getKey(), String.valueOf(s));
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

        }
    }
}
