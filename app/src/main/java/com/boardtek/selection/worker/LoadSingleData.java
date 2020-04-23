package com.boardtek.selection.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.boardtek.selection.db.SelectionDao;
import com.boardtek.selection.db.SelectionRoomDatabase;

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
            return Result.success();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
