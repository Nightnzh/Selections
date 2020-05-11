package com.boardtek.selection.adapter.datacontent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.boardtek.selection.R;
import com.boardtek.selection.datamodel.DataContent;
import java.util.List;

public class DataContentAdapter extends RecyclerView.Adapter<DataViewHolder> {

    private List<DataContent> list;

    public DataContentAdapter(List<DataContent> list){
        this.list = list;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyler_item_data_content_layout,parent,false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        holder.bindTo(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

class DataViewHolder extends RecyclerView.ViewHolder{
    private TextView tThermoTime;
    private TextView tThermo;
    private TextView tPressureTime;
    private TextView tPressure;
    public DataViewHolder(@NonNull View itemView) {
        super(itemView);
        tThermoTime = itemView.findViewById(R.id.t_thermo_time);
        tThermo = itemView.findViewById(R.id.t_thermo);
        tPressureTime = itemView.findViewById(R.id.t_pressure_time);
        tPressure = itemView.findViewById(R.id.t_pressure);
    }

    void bindTo(DataContent dataContent){
        tThermoTime.setText(dataContent.getPressureTime());
        tThermo.setText(dataContent.getThermo());
        tPressureTime.setText(dataContent.getPressureTime());
        tPressure.setText(dataContent.getPressure());
    }
}
