package com.boardtek.selection.adapter.datapp;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.boardtek.selection.R;
import com.boardtek.selection.datamodel.DataPp;

import java.util.List;

public class DataPpAdapter extends RecyclerView.Adapter<DataPpAdapter.DataPpViewHolder> {

    private List<DataPp> list;

    public DataPpAdapter(List<DataPp> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public DataPpViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_data_pp_layout,parent,false);
        return new DataPpViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataPpViewHolder holder, int position) {
        holder.bindTo(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class DataPpViewHolder extends RecyclerView.ViewHolder{

        private TextView tTypeTittle;
        private TextView tModelTittle;

        public DataPpViewHolder(@NonNull View itemView) {
            super(itemView);
            tModelTittle = itemView.findViewById(R.id.t_data_pp_model_tittle);
            tTypeTittle = itemView.findViewById(R.id.t_data_pp_type_tittle);
        }

        public void bindTo(DataPp dataPp){
            tTypeTittle.setText(dataPp.getTypeTitle());
            tModelTittle.setText(dataPp.getModelTitle());
        }
    }
}
