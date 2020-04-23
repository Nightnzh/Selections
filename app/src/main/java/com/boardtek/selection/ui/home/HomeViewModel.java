package com.boardtek.selection.ui.home;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.boardtek.selection.Repository;
import com.boardtek.selection.datamodel.Selection;
import com.boardtek.selection.worker.LoadSingleData;

import java.net.NetworkInterface;
import java.util.concurrent.ExecutionException;


public class HomeViewModel extends AndroidViewModel {

    private String TAG = HomeViewModel.class.getSimpleName();
    private MutableLiveData<Selection> selectionMutableLiveData;
    private Repository repository;

    private WorkManager workManager;
    String url = "http://192.168.50.98/system_mvc/controller.php?s=dev,007459,500,laminationProgram,pp_program&action=mobile_programData";
    private int programId;
    private Data dataInput;
    private OneTimeWorkRequest loadSingleData;

    public LiveData<WorkInfo> getSingleState() {
        return singleState;
    }

    private LiveData<WorkInfo> singleState;


    public void setProgramId(int programId) {
        this.programId = programId;
        dataInput = new Data.Builder()
                .putString("url",url)
                .putString("programId",String.valueOf(programId))
                .build();
        loadSingleData = new OneTimeWorkRequest.Builder(LoadSingleData.class)
                .setInputData(dataInput)
                .addTag("Load")
                .build();
        singleState = workManager.getWorkInfoByIdLiveData(loadSingleData.getId());
    }

    public HomeViewModel(@NonNull Application application) {
        super(application);

        workManager = WorkManager.getInstance(getApplication());
        repository = new Repository(application);
        selectionMutableLiveData = new MutableLiveData<>();

        //****
        setProgramId(0);

        Log.d(TAG, "init");
    }

    public MutableLiveData<Selection> getSelectionMutableLiveData() {
        return selectionMutableLiveData;
    }


    //TODO:確認有無網路
    //    true:線上查詢
    //    false:本地查詢

    //TODO::
    void setItemFromId(int id){
        if(!isConnected()){
            workManager.enqueue(loadSingleData);
        } else {
            repository.getSelectionFromId(id, selection -> {
                selectionMutableLiveData.postValue(selection);
            });
        }
    }

    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo networkInfo =  cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}