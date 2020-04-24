package com.boardtek.selection.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.boardtek.appcenter.AppCenter;
import com.boardtek.selection.datamodel.Selection;
import com.boardtek.selection.datamodel.SelectionProgramItem;
import com.boardtek.selection.db.SelectionDao;
import com.boardtek.selection.db.SelectionRoomDatabase;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoadSingleData extends Worker {

    private SelectionDao selectionDao;
    private String TAG = LoadSingleData.class.getSimpleName();

    public LoadSingleData(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        selectionDao = SelectionRoomDatabase.getDatabase(getApplicationContext()).selectionDao();
    }



    @NonNull
    @Override
    public Result doWork() {


        String url = getInputData().getString("url");
        String programId = getInputData().getString("programId");
        Boolean isTestMode = getInputData().getBoolean("isTestMode",false);
        Log.d(TAG,url+"programId="+programId);
        OkHttpClient okHttpClient = new OkHttpClient();
        FormBody formBody = new FormBody.Builder()
                .add("programId",programId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();


        try {
            Response response = okHttpClient.newCall(request).execute();
            Log.d(TAG,response.body().string());
            String json = response.body().string();
            SelectionProgramItem item = new Gson().fromJson(json,SelectionProgramItem.class);
            if(isTestMode)
                selectionDao.insertItem(getSelection_(item));
            else
                selectionDao.insertItem(getSelection_(item));
            return Result.success();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    @NotNull
    private Selection getSelection(SelectionProgramItem selectionProgramItem) {
        return new Selection(
                selectionProgramItem.getProgramId(),
                selectionProgramItem.getHour(),
                selectionProgramItem.isAutoAddVersion(),
                selectionProgramItem.isPause(),
                selectionProgramItem.getMinute(),
                selectionProgramItem.getRemark(),
                selectionProgramItem.getSetDate(),
                selectionProgramItem.getSetName(),
                selectionProgramItem.getVendorTitle(),
                new Gson().toJson(selectionProgramItem.getData_pp()),
                new Gson().toJson(selectionProgramItem.getData_content()),
                AppCenter.getSystemTime()
        );
    }

    //TestMode
    @NotNull
    private Selection getSelection_(SelectionProgramItem selectionProgramItem) {
        return new Selection(
                selectionProgramItem.getProgramId()+"_",
                selectionProgramItem.getHour(),
                selectionProgramItem.isAutoAddVersion(),
                selectionProgramItem.isPause(),
                selectionProgramItem.getMinute(),
                selectionProgramItem.getRemark(),
                selectionProgramItem.getSetDate(),
                selectionProgramItem.getSetName(),
                selectionProgramItem.getVendorTitle(),
                new Gson().toJson(selectionProgramItem.getData_pp()),
                new Gson().toJson(selectionProgramItem.getData_content()),
                AppCenter.getSystemTime()
        );
    }

}
