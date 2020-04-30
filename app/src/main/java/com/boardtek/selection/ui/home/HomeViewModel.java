package com.boardtek.selection.ui.home;
import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import com.boardtek.selection.Repository;
import com.boardtek.selection.datamodel.Selection;
import com.boardtek.selection.net.NetInfo;
import com.boardtek.selection.worker.LoadSingleData;

import java.util.List;


public class HomeViewModel extends AndroidViewModel {

    private String TAG = HomeViewModel.class.getSimpleName();
    private MutableLiveData<Selection> selectionMutableLiveData;
    private Repository repository;
    private NetInfo netInfo;


    private WorkManager workManager;
//    OneTimeWorkRequest loadSingleData;
    LiveData<List<WorkInfo>> loadSingleState;

    //constructor
    public HomeViewModel(@NonNull Application application) {
        super(application);
        netInfo = new NetInfo(application);
        workManager = WorkManager.getInstance(getApplication());
        repository = new Repository(application);
        selectionMutableLiveData = new MutableLiveData<>();
        //SingleWorkState
        loadSingleState = workManager.getWorkInfosForUniqueWorkLiveData("Single");

        Log.d(TAG, "init");
    }


    //

    public MutableLiveData<Selection> getSelectionMutableLiveData() {
        return selectionMutableLiveData;
    }

//    void createWorkById(int id){
//        String url = "http://192.168.50.98/system_mvc/controller.php?s=dev,007459,500,laminationProgram,pp_program&action=mobile_programData";
//        Data in = new Data.Builder().putString("url",url).putString("programId", String.valueOf(id)).build();
//        loadSingleData = new OneTimeWorkRequest.Builder(LoadSingleData.class).setInputData(in).build();
//    }
}

