package com.boardtek.selection.adapter.action;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.Stream;
import com.boardtek.selection.R;
import com.boardtek.selection.datamodel.Action;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ActionAdapter extends RecyclerView.Adapter<ActionAdapter.ActionViewHolder> {

    private String TAG = ActionAdapter.class.getSimpleName();
    private List<Action> list;
    private onAllSelectedEvent onAllSelectedEvent;

    public interface onAllSelectedEvent {
        void call(boolean isAllSelected);
    }

    public ActionAdapter(List<Action> list){
        this.list = list;
    }

    public void setOnAllSelectedEvent(onAllSelectedEvent onAllSelectedEvent) {
        this.onAllSelectedEvent = onAllSelectedEvent;
    }

    @NonNull
    @Override
    public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_action_item_layout,parent,false);
        return new ActionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActionViewHolder holder, int position) {
        Log.d(TAG, String.valueOf(position));
        holder.bindTo(list.get(position),position);
        holder.checkBoxItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG,  holder.itemView.getId() + String.valueOf(isChecked));
            if(isChecked){
                list.get(position).setChecked(true);
            }else {
                list.get(position).setChecked(false);
            }
            if (onAllSelectedEvent != null)
                onAllSelectedEvent.call(Stream.of(list).allMatch(Action::getChecked) );

        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void checkAll(){
        for(Action action : list){
            action.setChecked(true);
        }
        notifyDataSetChanged();
    }

    public void cancelCheckAll(){
        for(Action action : list){
            action.setChecked(false);
        }
        notifyDataSetChanged();
    }


    //ViewHolder
    class ActionViewHolder extends RecyclerView.ViewHolder{

        private TextView tIndex;
        private CheckBox checkBoxItem;
        private RecyclerView recyclerPost;
        private ImageView iPostSetting;

        public ActionViewHolder(@NonNull View itemView) {
            super(itemView);
            tIndex = itemView.findViewById(R.id.t_action_index);
            checkBoxItem = itemView.findViewById(R.id.c_action);
            iPostSetting = itemView.findViewById(R.id.i_action_setting);
            recyclerPost = itemView.findViewById(R.id.recycler_action_post);
        }

        void bindTo(Action action,int position){
            final Boolean[] vs = {false};
            //bind
            tIndex.setText(position+1+".");
            String actionName = action.getName().substring(action.getName().indexOf("=")).substring(1);
            checkBoxItem.setText(actionName);
            if(action.getChecked())
                checkBoxItem.setChecked(true);
            else
                checkBoxItem.setChecked(false);

            //recyclerView post
            if(action.getPosts()==null) {
                iPostSetting.setVisibility(View.INVISIBLE);
                return;
            }

            recyclerPost.setHasFixedSize(true);
            recyclerPost.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            recyclerPost.setAdapter(new ActionPostAdapter(action.getPosts()));

            recyclerPost.setVisibility(View.GONE);

            iPostSetting.setOnClickListener(v -> {
                if(vs[0]) {
                    recyclerPost.setVisibility(View.GONE);
                    vs[0] = false;
                }else {
                    recyclerPost.setVisibility(View.VISIBLE);
                    vs[0] = true;
                }
            });
        }

    }

}
