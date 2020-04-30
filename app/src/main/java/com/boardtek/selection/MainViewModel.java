package com.boardtek.selection;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private String urlGetAll = "http://192.168.50.98/system_mvc/controller.php?s=dev,007459,500,laminationProgram,pp_program&action=mobile_programData_all";
    WorkManager workManager = WorkManager.getInstance(getApplication());
    LiveData<List<WorkInfo>> loadAllDataState = workManager.getWorkInfosForUniqueWorkLiveData("LoadAllData");

    public MainViewModel(@NonNull Application application) {
        super(application);
    }


//    private Data inputData = new Data.Builder().putString("url",urlGetAll).build();
//    OneTimeWorkRequest loadData = new OneTimeWorkRequest.Builder(LoadData.class).setInputData(inputData).build();
//    WorkManager workManager = WorkManager.getInstance(getApplication());
//    LiveData<WorkInfo> loadDataState =  workManager.getWorkInfoByIdLiveData(loadData.getId());
    //public MutableLiveData<List<Action>> actionListLiveData = new MutableLiveData<>();

}
