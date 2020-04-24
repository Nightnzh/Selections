package com.boardtek.selection.ui.loading;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.boardtek.selection.R;
import com.boardtek.selection.databinding.LoadingLayoutBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Loading {

    private static AlertDialog loadingDialog;
    private LoadingLayoutBinding binding;
    private static String tittle = "...";

    public Loading(Context context){
        View view = LayoutInflater.from(context).inflate(R.layout.loading_layout,null);
        binding = LoadingLayoutBinding.bind(view);
        loadingDialog = new MaterialAlertDialogBuilder(context)
                .setView(binding.getRoot())
                .setCancelable(false)
                .create();
    }

    public static void showLoadingView(){
        loadingDialog.show();
    }

    public static void closeLoadingView(){
        loadingDialog.dismiss();
    }

}
