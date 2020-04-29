package com.boardtek.selection.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;
import com.boardtek.selection.R;
import com.boardtek.selection.adapter.datacontent.DataContentAdapter;
import com.boardtek.selection.databinding.EnterNumBinding;
import com.boardtek.selection.databinding.FragmentHomeBinding;
import com.boardtek.selection.databinding.RecyclerLayoutBinding;
import com.boardtek.selection.datamodel.DataContent;
import com.boardtek.selection.datamodel.Selection;
import com.boardtek.selection.db.SelectionRoomDatabase;
import com.boardtek.selection.net.NetInfo;
import com.boardtek.selection.ui.v.Loading;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private String TAG = HomeFragment.class.getSimpleName();
    private HomeViewModel homeViewModel;
    private FragmentHomeBinding fragmentHomeBinding;
    private Loading loading;
    private NetInfo netInfo;
    private EnterNumBinding enterNumBinding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        netInfo = new NetInfo(requireActivity().getApplication());
        loading = new Loading(requireContext());
        fragmentHomeBinding = FragmentHomeBinding.inflate(getLayoutInflater());
        View root = fragmentHomeBinding.getRoot();
        //observer data to show on UI
        homeViewModel.getSelectionMutableLiveData().observe(getViewLifecycleOwner(), selection -> {
            if (selection == null) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Not Exit")
                        .setIcon(R.drawable.ic_404_error)
                        .setMessage("Your search is not exit,Please check your network.")
                        .show();
                return;
            }
            setSelection(selection);
        });
        fragmentHomeBinding.fab.setOnClickListener(this);

        // init id = 1
        homeViewModel.createWorkById(1);
        homeViewModel.loadSingleState.observe(
                getViewLifecycleOwner(),
                workInfos -> {
                    if (workInfos.size() != 0) {
                        Log.d(TAG, workInfos.get(0).getState().toString());
                        switch (workInfos.get(0).getState()) {
                            case RUNNING:
                                Loading.showLoadingView();
                                break;
                            case SUCCEEDED:
                                String id = workInfos.get(0).getOutputData().getString("programId");
                                this.requireActivity().getSharedPreferences("Setting", Context.MODE_PRIVATE).edit().putString("TEMP_ID", id).apply();
                                SelectionRoomDatabase.databaseWriteExecutor.execute(() -> {
                                    homeViewModel.getSelectionMutableLiveData().postValue(SelectionRoomDatabase.getDatabase(getContext()).selectionDao().getSelection(Integer.parseInt(id)));
                                });
                                Loading.closeLoadingView();
                                break;
                            case FAILED:
                                Loading.closeLoadingView();
                                new MaterialAlertDialogBuilder(requireContext())
                                        .setTitle("Not Exit")
                                        .setIcon(R.drawable.ic_404_error)
                                        .setMessage("Your search is not exit")
                                        .setPositiveButton("OK", null)
                                        .show();
                                break;
                        }
                    }
            }
        );
        return root;
    }


    @SuppressLint("SetTextI18n")
    private void setSelection(@NotNull Selection selection) {

        fragmentHomeBinding.tContentProgramId.setText(getString(R.string.programid) + selection.getProgramId());
        fragmentHomeBinding.tContentVendor.setText(getString(R.string.vendor) + selection.getVendorTitle());
        fragmentHomeBinding.tContentIsPaused.setText(getString(R.string.ispaused) + selection.isPause());
        fragmentHomeBinding.tContentHour.setText(getString(R.string.hour) + selection.getHour());
        fragmentHomeBinding.tContentMinute.setText(getString(R.string.minute) + selection.getMinute());
        fragmentHomeBinding.tContentSetName.setText(getString(R.string.set_name) + selection.getSetName());
        fragmentHomeBinding.tContentSetDate.setText(getString(R.string.set_date) + "\n" + selection.getSetDate().substring(0, 9) + "\n" + selection.getSetDate().substring(11));
        fragmentHomeBinding.tContentIsAutoVersion.setText(getString(R.string.isautoversion)+selection.isAutoAddVersion());
        fragmentHomeBinding.tContentRemark.setText(getString(R.string.remark)+selection.getRemark());
        fragmentHomeBinding.bContentDataPp.setOnClickListener(this);
        fragmentHomeBinding.bContentDataContent.setOnClickListener(this);
    }

    @Override
    public void onClick(@NotNull View v) {
        Log.d(TAG, getResources().getResourceName(v.getId()));

        if (v.getId() == R.id.fab) {
            enterNumBinding = EnterNumBinding.inflate(getLayoutInflater());
            EditText editText;
            editText = enterNumBinding.editText;
            new MaterialAlertDialogBuilder(this.requireContext())
                    .setIcon(R.drawable.ic_data)
                    .setTitle("ID")
                    .setView(enterNumBinding.getRoot())
                    .setPositiveButton("OK", (dialog, which) -> {
                        String id = editText.getText().toString();
                        if (editText.getText() != null && netInfo.isConnected()) {
                            Log.d(TAG, id);
                            homeViewModel.createWorkById(Integer.parseInt(id));
                            WorkManager.getInstance(requireContext()).beginUniqueWork("Single",ExistingWorkPolicy.REPLACE,homeViewModel.loadSingleData).enqueue();
                        } else if(!netInfo.isConnected()){
                            SelectionRoomDatabase.databaseWriteExecutor.execute(() -> {
                                homeViewModel.getSelectionMutableLiveData().postValue(SelectionRoomDatabase.getDatabase(getContext()).selectionDao().getSelection(Integer.parseInt(id)));
                            });
                        }
                    }).show();

        }
        if (v.getId() == R.id.b_content_data_content) {
            String json = homeViewModel.getSelectionMutableLiveData().getValue().getData_content();
            List<DataContent> list = new Gson().fromJson(json, new TypeToken<List<DataContent>>() {
            }.getType());
            assert list != null;
            Log.d(TAG, list.toString());
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

        if(v.getId() == R.id.b_content_data_pp){
            String json = homeViewModel.getSelectionMutableLiveData().getValue().getData_pp();
            new MaterialAlertDialogBuilder(requireContext())
                    .setMessage(json)
                    .show();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    //    public interface Cb {
//        void call(LiveData<WorkInfo> liveData_workInfo);
//    }
}

//this.requireActivity().getSharedPreferences("Setting", Context.MODE_PRIVATE).edit().putString("TEMP_ID",id).apply();