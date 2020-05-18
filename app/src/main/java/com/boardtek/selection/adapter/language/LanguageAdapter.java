package com.boardtek.selection.adapter.language;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.boardtek.selection.MainActivity;
import com.boardtek.selection.R;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

    private List<String> list = Arrays.asList("US", "Chinese");

    public LanguageAdapter(Context context) {
        this.context = context;
    }

    private Context context;

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.language_recycler_item,parent,false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        switch (list.get(position)){
            case "US":
                holder.bindTo(R.drawable.us,"US");
                holder.itemView.setOnClickListener(v -> {
                    context.getSharedPreferences("Setting",MODE_PRIVATE).edit().putString("Language",Locale.US.getCountry()).apply();
                    Intent restart = new Intent(context, MainActivity.class);
                    ((MainActivity)context).finish();
                    context.startActivity(restart);
                });
                break;
            case "Chinese":
                holder.bindTo(R.drawable.tw,"中文");
                holder.itemView.setOnClickListener(v -> {
                    context.getSharedPreferences("Setting",MODE_PRIVATE).edit().putString("Language",Locale.TAIWAN.getCountry()).apply();

                    Intent restart = new Intent(context, MainActivity.class);
                    ((MainActivity)context).finish();
                    context.startActivity(restart);
                });
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + list.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class LanguageViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        private TextView textView;
        public LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_language);
            textView = itemView.findViewById(R.id.t_language);
        }

        void bindTo(int id,String name){
            imageView.setImageResource(id);
            textView.setText(name);
        }
    }
}

