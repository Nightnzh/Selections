package com.boardtek.selection.worker;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.boardtek.appcenter.AppCenter;
import com.boardtek.selection.datamodel.Selection;
import com.boardtek.selection.datamodel.SelectionProgram;
import com.boardtek.selection.datamodel.SelectionProgramItem;
import com.boardtek.selection.db.SelectionDao;
import com.boardtek.selection.db.SelectionRoomDatabase;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/*
    1.透過Url讀取全部資料
    2.存進本地端資料庫
    **測試模式的資料屬性 ProgramID(PrimeyKey) 在後面加上"_"以區分正式與正式的資料!
*/

public class LoadData extends Worker {
    private String TAG = LoadData.class.getSimpleName();
    private SelectionDao selectionDao;

    public LoadData(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        selectionDao = SelectionRoomDatabase.getDatabase(context).selectionDao();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Result doWork() {
        String url = getInputData().getString("url");
        Boolean isTestMode = getInputData().getBoolean("isTestMode",false);
        Log.d(TAG,"URL:"+url);

        OkHttpClient okHttpClient = new OkHttpClient();
        assert url != null;
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {

            Response response = okHttpClient.newCall(request).execute();
            String json = response.body().string();
            Log.d(TAG,"JSON:"+json);
            SelectionProgram selectionProgram = new Gson().fromJson(json,SelectionProgram.class);
            selectionProgram.forEach(selectionProgramItem -> {
                Log.d(TAG,selectionProgramItem.toString());
                if(isTestMode)
                    selectionDao.insertItem(getSelection(selectionProgramItem));
                else
                    selectionDao.insertItem(getSelection_(selectionProgramItem));
            });

            return Worker.Result.success();
        } catch (IOException e) {
            e.printStackTrace();
            return Worker.Result.failure();
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


/*
    Worker 有資料大小限制
*/