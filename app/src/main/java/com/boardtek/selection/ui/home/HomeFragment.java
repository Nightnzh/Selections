package com.boardtek.selection.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.boardtek.appcenter.NetworkInformation;
import com.boardtek.selection.Constant;
import com.boardtek.selection.R;
import com.boardtek.selection.adapter.datacontent.DataContentAdapter;
import com.boardtek.selection.databinding.EnterNumBinding;
import com.boardtek.selection.databinding.FragmentHomeBinding;
import com.boardtek.selection.databinding.RecyclerLayoutBinding;
import com.boardtek.selection.datamodel.DataContent;
import com.boardtek.selection.datamodel.Selection;
import com.boardtek.selection.db.SelectionRoomDatabase;
import com.boardtek.selection.ui.v.Loading;
import com.boardtek.selection.worker.LoadSingleData;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private String TAG = HomeFragment.class.getSimpleName();
    private HomeViewModel homeViewModel;
    private FragmentHomeBinding fragmentHomeBinding;
    private Loading loading;
    private EnterNumBinding enterNumBinding;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        NetworkInformation.init(requireContext());
        loading = new Loading(requireContext());
        fragmentHomeBinding = FragmentHomeBinding.inflate(getLayoutInflater());
        View root = fragmentHomeBinding.getRoot();
        fragmentHomeBinding.fab.setOnClickListener(this);

        return root;
    }


    @SuppressLint("SetTextI18n")
    private void setSelection(@NotNull Selection selection) {

        if(Constant.mode == Constant.MODE_OFFICIAL) {
            fragmentHomeBinding.tContentMode.setText("OFFICIAL");
        }else {
            fragmentHomeBinding.tContentMode.setText("TEST");
        }
        fragmentHomeBinding.tContentProgramId.setText(getString(R.string.programid) + selection.getProgramId());
        fragmentHomeBinding.tContentVendor.setText(getString(R.string.vendor) + selection.getVendorTitle());
        fragmentHomeBinding.tContentIsPaused.setText(getString(R.string.ispaused) + selection.isPause());
        fragmentHomeBinding.tContentHour.setText(getString(R.string.hour) + selection.getHour());
        fragmentHomeBinding.tContentMinute.setText(getString(R.string.minute) + selection.getMinute());
        fragmentHomeBinding.tContentSetName.setText(getString(R.string.set_name) + selection.getSetName());
        fragmentHomeBinding.tContentSetDate.setText(getString(R.string.set_date)  + selection.getSetDate());
        fragmentHomeBinding.tContentIsAutoVersion.setText(getString(R.string.isautoaddversion)+selection.isAutoAddVersion());
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
//                    .setTitle("ID")
                    .setView(enterNumBinding.getRoot())
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        String id = editText.getText().toString();
                        if(id.equals("")) return;
                        if (NetworkInformation.isConnected(requireContext())) {
                            Log.d(TAG, id);
                            Data in = new Data.Builder()
                                    .putString("url",Constant.getUrl(Constant.mode,Constant.ACTION_BY_PROGRAM_ID))
                                    .putString("programId",id)
                                    .putInt("mode",Constant.mode)
                                    .build();
                            OneTimeWorkRequest loadSingleDataById = new OneTimeWorkRequest.Builder(LoadSingleData.class)
                                    .setInputData(in)
                                    .build();
                            WorkManager.getInstance(requireContext()).beginUniqueWork("Single",ExistingWorkPolicy.REPLACE,loadSingleDataById).enqueue();
                        } else if(!NetworkInformation.isConnected(requireContext())){
                            if (Constant.mode == Constant.MODE_OFFICIAL) {
                                SelectionRoomDatabase.databaseWriteExecutor.execute(() -> {
                                    homeViewModel.getSelectionMutableLiveData().postValue(SelectionRoomDatabase.getDatabase(getContext()).selectionDao().getSelection(Integer.parseInt(id)));
                                });
                            }else {
                                SelectionRoomDatabase.databaseWriteExecutor.execute(() -> {
                                    homeViewModel.getSelectionMutableLiveData().postValue(SelectionRoomDatabase.getDatabase(getContext()).selectionDao().getSelectionTest(Integer.parseInt(id)));
                                });
                            }
                        }
                    }).show();

        }
        if (v.getId() == R.id.b_content_data_content) {
            String json = Objects.requireNonNull(homeViewModel.getSelectionMutableLiveData().getValue()).getData_content();
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
            String json = Objects.requireNonNull(homeViewModel.getSelectionMutableLiveData().getValue()).getData_pp();
            new MaterialAlertDialogBuilder(requireContext())
                    .setMessage(json)
                    .show();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        homeViewModel.getSelectionMutableLiveData().removeObservers(getViewLifecycleOwner());
        homeViewModel.loadSingleState.removeObservers(getViewLifecycleOwner());
    }

    @Override
    public void onStart() {
        super.onStart();

        //observer data to show on UI
        homeViewModel.getSelectionMutableLiveData().observe(getViewLifecycleOwner(), selection -> {
            if (selection == null) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.notexit)
                        .setIcon(R.drawable.ic_404_error)
                        .setMessage(R.string.check_network)
                        .show();
                return;
            }
            this.requireActivity().getSharedPreferences("Setting", Context.MODE_PRIVATE).edit().putString("TEMP_ID", selection.getProgramId()).apply();
            setSelection(selection);
        });


        homeViewModel.loadSingleState.observe(
                getViewLifecycleOwner(),
                workInfos -> {
                    if (workInfos.size() == 0) return;
                        Log.d(TAG, "loadSingleState::" + workInfos.get(0).getState().toString());
                        switch (workInfos.get(0).getState()) {
                            case RUNNING:
                                Loading.showLoadingView();
                                break;
                            case SUCCEEDED:
                                String id = workInfos.get(0).getOutputData().getString("programId");
//                            this.requireActivity().getSharedPreferences("Setting", Context.MODE_PRIVATE).edit().putString("TEMP_ID", id).apply();
                                if(Constant.mode == Constant.MODE_OFFICIAL) {
                                    SelectionRoomDatabase.databaseWriteExecutor.execute(() -> {
                                        homeViewModel.getSelectionMutableLiveData().postValue(
                                                SelectionRoomDatabase.getDatabase(getContext()).selectionDao().getSelection(Integer.parseInt(id))
                                        );
                                    });
                                } else {
                                    SelectionRoomDatabase.databaseWriteExecutor.execute(() -> {
                                        homeViewModel.getSelectionMutableLiveData().postValue(
                                                SelectionRoomDatabase.getDatabase(getContext()).selectionDao().getSelectionTest(Integer.parseInt(id))
                                        );
                                    });
                                }
                                Loading.closeLoadingView();
                                break;
                            case FAILED:
                                Loading.closeLoadingView();
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle(R.string.not_exist)
                                    .setIcon(R.drawable.ic_404_error)
                                    //.setMessage("Your search is not exist")
                                    .setPositiveButton(R.string.ok, null)
                                    .show();
                                break;
                        }

                }
        );
    }

}
