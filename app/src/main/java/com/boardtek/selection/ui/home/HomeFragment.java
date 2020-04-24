package com.boardtek.selection.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.boardtek.selection.R;
import com.boardtek.selection.adapter.DataContentAdapter;
import com.boardtek.selection.databinding.EnterNumBinding;
import com.boardtek.selection.databinding.FragmentHomeBinding;
import com.boardtek.selection.databinding.RecyclerLayoutBinding;
import com.boardtek.selection.datamodel.DataContent;
import com.boardtek.selection.datamodel.Selection;
import com.boardtek.selection.db.SelectionDao;
import com.boardtek.selection.db.SelectionRoomDatabase;
import com.boardtek.selection.ui.loading.Loading;
import com.boardtek.selection.worker.LoadSingleData;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private String TAG = HomeFragment.class.getSimpleName();
    private HomeViewModel homeViewModel;
    private FragmentHomeBinding fragmentHomeBinding;
    private Loading loading;

    private LiveData<WorkInfo> workInfoLiveData;
    private OneTimeWorkRequest loadSingleData;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        loading = new Loading(requireContext());
        fragmentHomeBinding = FragmentHomeBinding.inflate(getLayoutInflater());
        View root = fragmentHomeBinding.getRoot();
        //observer data to show on UI
        homeViewModel.getSelectionMutableLiveData().observe(getViewLifecycleOwner(), this::SetSelection);

        fragmentHomeBinding.fab.setOnClickListener(this);

//        workInfoLiveData.observe(getViewLifecycleOwner(),workInfo -> {
//
//        });

        return root;
    }

    @SuppressLint("SetTextI18n")
    private void SetSelection(Selection selection) {
        fragmentHomeBinding.tContentProgramId.setText(getString(R.string.programid) +selection.getProgramId());
        fragmentHomeBinding.tContentVendor.setText(getString(R.string.vendor)+selection.getVendorTitle());
        fragmentHomeBinding.tContentIsPaused.setText(getString(R.string.ispaused)+selection.isPause());
        fragmentHomeBinding.tContentHour.setText(getString(R.string.hour)+selection.getHour());
        fragmentHomeBinding.tContentMinute.setText(getString(R.string.minute)+selection.getMinute());
        fragmentHomeBinding.tContentSetName.setText(getString(R.string.set_name)+selection.getSetName());
        fragmentHomeBinding.tContentSetDate.setText(getString(R.string.set_date)+"\n"+selection.getSetDate().substring(0,9)+"\n"+selection.getSetDate().substring(11));
        fragmentHomeBinding.bContentDataPp.setOnClickListener(this);
        fragmentHomeBinding.bContentDataContent.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, getResources().getResourceName(v.getId()));
        if(v.getId() == R.id.fab){
            EnterNumBinding enterNumBinding = EnterNumBinding.inflate(getLayoutInflater());
            EditText editText = enterNumBinding.editText;
            new MaterialAlertDialogBuilder(this.requireContext())
                    .setIcon(R.drawable.ic_data)
                    .setTitle("ID")
                    .setView(enterNumBinding.getRoot())
                    .setPositiveButton("OK",(dialog, which) -> {
                        if (editText.getText()!= null){
                            String id = editText.getText().toString();
                            Log.d(TAG, String.valueOf(id));
                            String url = "http://192.168.50.98/system_mvc/controller.php?s=dev,007459,500,laminationProgram,pp_program&action=mobile_programData";
                            Data data = new Data.Builder().putString("url",url).putString("programId",id).build();
                            loadSingleData =  new OneTimeWorkRequest.Builder(LoadSingleData.class).setInitialDelay(1000, TimeUnit.SECONDS).setInputData(data).build();
                            WorkManager.getInstance(getContext())
                                    .beginUniqueWork("Single", ExistingWorkPolicy.REPLACE,loadSingleData)
                                    .enqueue().getState().observe(getViewLifecycleOwner(),state -> {
                                        Log.d("ABC",state.getClass().getSimpleName());
                                        if (state instanceof Operation.State.SUCCESS){
                                            new Handler().postDelayed(() -> {
                                                Loading.closeLoadingView();
                                            }, 1000);
                                            homeViewModel.getSelectionMutableLiveData().postValue(
                                                    SelectionRoomDatabase.getDatabase(getContext()).selectionDao().getSelection(Integer.parseInt(id)
                                                    ));
                                            //Log.d("ABC","SUCCESS");
                                        } else if(state instanceof Operation.State.IN_PROGRESS){
                                            Loading.showLoadingView();
                                        }
                            });
                        }
                    }).show();

        }
        if(v.getId() == R.id.b_content_data_content){
            String json = homeViewModel.getSelectionMutableLiveData().getValue().getData_content();
            List<DataContent> list = new Gson().fromJson(json,new TypeToken<List<DataContent>>(){}.getType());
            Log.d(TAG,list.toString());
            RecyclerLayoutBinding recyclerLayoutBinding = RecyclerLayoutBinding.inflate(getLayoutInflater());
            View view = recyclerLayoutBinding.getRoot();
            RecyclerView recyclerView = recyclerLayoutBinding.recycler;
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(new DataContentAdapter(list));
            new MaterialAlertDialogBuilder(requireContext())
                    .setView(view)
                    .show();
        }
    }

    public interface Cb {
        void call(LiveData<WorkInfo> liveData_workInfo);
    }

    //                            homeViewModel.createAndRunWorker(id, liveData_workInfo->{
//                                liveData_workInfo.observe(getViewLifecycleOwner(), workInfo -> {
//                                    if (workInfo==null)return;
//                                    Log.d(TAG, "ID:" +   workInfo.getId());
//
//                                    Log.d(TAG, workInfo.getState().toString());
//                                    if(workInfo.getState() == WorkInfo.State.SUCCEEDED){
//                                        Loading.closeLoadingView();
//                                    }
//
//                                    if(workInfo.getState() == WorkInfo.State.RUNNING){
//                                        Loading.closeLoadingView();
//                                    }
//                                });
//                            });
}
