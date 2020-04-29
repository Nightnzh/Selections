package com.boardtek.selection.ui.v;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.boardtek.selection.R;
import com.boardtek.selection.databinding.ActionHeaderLayoutBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Map;

public class ShowActionResponse {

    public static void createAndShow(Context context, String name, String url, Map<String,String> posts, String message){
        View header = LayoutInflater.from(context).inflate(R.layout.action_header_layout,null);
        ActionHeaderLayoutBinding actionHeaderLayoutBinding = ActionHeaderLayoutBinding.bind(header);
        actionHeaderLayoutBinding.tActionTittle.setText("【NAME】: " + name);
        actionHeaderLayoutBinding.tActionUrl.setText("【URL】: "+"\n"+url);
        if(posts == null)
            actionHeaderLayoutBinding.tActionPost.setVisibility(View.GONE);
        else {
            actionHeaderLayoutBinding.tActionPost.setVisibility(View.VISIBLE);
            //StringBuilder post = new StringBuilder();
            actionHeaderLayoutBinding.tActionPost.setText("【POST】:"+"\n"+posts.toString());
        }

        AlertDialog alertDialog;

        if(message.length()>1000) {
            alertDialog = new MaterialAlertDialogBuilder(context)
                    .setCustomTitle(actionHeaderLayoutBinding.getRoot())
                    .setMessage("【Response】:" + "\n" + message.substring(0,999)+"...")
                    .setPositiveButton("OK",null)
                    .setNegativeButton("Show All",(dialog, which) -> {showAllResponse(context,message);})
                    .create();
        } else {
            alertDialog = new MaterialAlertDialogBuilder(context)
                    .setCustomTitle(actionHeaderLayoutBinding.getRoot())
                    .setMessage("【Response】:" + "\n" + message)
                    .setPositiveButton("OK",null)
                    .create();
        }

        alertDialog.show();
    }

    private static void showAllResponse(Context context,String message){
        new AlertDialog.Builder(context)
                .setTitle("All Response:")
                .setMessage(message)
                .setPositiveButton("OK",null)
                .show();
    }

}
