package com.boardtek.selection.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.boardtek.appcenter.AppCenter;
import com.boardtek.appcenter.NetworkInformation;
import com.boardtek.selection.Constant;
import com.boardtek.selection.R;
import com.boardtek.selection.adapter.datacontent.DataContentAdapter;
import com.boardtek.selection.adapter.datapp.DataPpAdapter;
import com.boardtek.selection.databinding.DataContentLayoutBinding;
import com.boardtek.selection.databinding.DataPpLayoutBinding;
import com.boardtek.selection.databinding.EnterNumBinding;
import com.boardtek.selection.databinding.FragmentHomeBinding;
import com.boardtek.selection.datamodel.DataContent;
import com.boardtek.selection.datamodel.DataPp;
import com.boardtek.selection.datamodel.Selection;
import com.boardtek.selection.db.SelectionRoomDatabase;
import com.boardtek.selection.net.WifiReceiver;
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
    //Network
    private NetworkInformation networkInformation;
    //WIFI
    private WifiReceiver wifiReceiver;

    //


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        NetworkInformation.init(requireContext());
        loading = new Loading(requireContext());
        fragmentHomeBinding = FragmentHomeBinding.inflate(getLayoutInflater());
        View root = fragmentHomeBinding.getRoot();
        fragmentHomeBinding.fab.setOnClickListener(this);

        setOnlineText();

        return root;
    }

    private void setOnlineText() {
        if (NetworkInformation.isConnected(requireContext())) {
            fragmentHomeBinding.tOnline.setText(getString(R.string.online));
            fragmentHomeBinding.tOnline.setVisibility(View.GONE);
        } else {
            fragmentHomeBinding.tOnline.setText(getString(R.string.offline));
            fragmentHomeBinding.tOnline.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    private void setSelection(@NotNull Selection selection) {

        setOnlineText();
        fragmentHomeBinding.tContentProgramId.setText(getString(R.string.programid)  +selection.getProgramId());
        fragmentHomeBinding.tContentVendor.setText(getString(R.string.vendor) +"\n" +selection.getVendorTitle());
        fragmentHomeBinding.tContentIsPaused.setText(getString(R.string.ispaused) +"\n" +selection.isPause());
        fragmentHomeBinding.tContentHour.setText(getString(R.string.hour) +"\n" +selection.getHour());
        fragmentHomeBinding.tContentMinute.setText(getString(R.string.minute) +"\n" +selection.getMinute());
        fragmentHomeBinding.tContentSetName.setText(getString(R.string.set_name) +"\n" +selection.getSetName());
        fragmentHomeBinding.tContentSetDate.setText(getString(R.string.set_date)  +"\n" +selection.getSetDate());
        fragmentHomeBinding.tContentIsAutoVersion.setText(getString(R.string.isautoaddversion)+"\n"+selection.isAutoAddVersion());
        fragmentHomeBinding.tContentRemark.setText(getString(R.string.remark)+"\n"+selection.getRemark());
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
                                    .putString("mac",NetworkInformation.macAddress)
                                    .putString("uSn", String.valueOf(AppCenter.uSn))
                                    .putString("mobileSn", String.valueOf(AppCenter.mobileSn))
                                    .putInt("mode",Constant.mode)
                                    .build();
                            OneTimeWorkRequest loadSingleDataById = new OneTimeWorkRequest.Builder(LoadSingleData.class)
                                    .setInputData(in)
                                    .build();
                            WorkManager.getInstance(requireContext()).beginUniqueWork("Single",ExistingWorkPolicy.REPLACE,loadSingleDataById).enqueue();

                            requireActivity().getSharedPreferences("Setting",Context.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("onStopRun",false)
                                    .apply();

                        } else if(!NetworkInformation.isConnected(requireContext())){
                            setSelectionById(id);
                        }
                    }).show();

        }
        if (v.getId() == R.id.b_content_data_content) {
            String json = Objects.requireNonNull(homeViewModel.getSelectionMutableLiveData().getValue()).getData_content();
            if(json.equals("[]")) {
                Toast.makeText(requireContext(), R.string.n_null, Toast.LENGTH_SHORT).show();
                return;
            }
            List<DataContent> list = new Gson().fromJson(json, new TypeToken<List<DataContent>>() {}.getType());
            assert list != null;
            Log.d(TAG, list.toString());
            DataContentLayoutBinding dataContentLayoutBinding = DataContentLayoutBinding.inflate(getLayoutInflater());
            View view = dataContentLayoutBinding.getRoot();
            RecyclerView recyclerView = dataContentLayoutBinding.recyclerDataContent;
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(new DataContentAdapter(list));
            new MaterialAlertDialogBuilder(requireContext())
                    .setView(view)
                    .show();
        }

        if(v.getId() == R.id.b_content_data_pp){
            String json = Objects.requireNonNull(homeViewModel.getSelectionMutableLiveData().getValue()).getData_pp();
            if(json.equals("[]")) {
                Toast.makeText(requireContext(), getString(R.string.n_null), Toast.LENGTH_SHORT).show();
                return;
            }
            List<DataPp> list = new Gson().fromJson(json, new TypeToken<List<DataPp>>() {}.getType());

            DataPpLayoutBinding dataPpLayoutBinding = DataPpLayoutBinding.inflate(getLayoutInflater());
            View view = dataPpLayoutBinding.getRoot();
            RecyclerView recyclerView = dataPpLayoutBinding.recyclerDataPp;
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(new DataPpAdapter(list));
            new MaterialAlertDialogBuilder(requireContext())
                    .setView(view)
                    .show();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
        homeViewModel.getSelectionMutableLiveData().removeObservers(getViewLifecycleOwner());
        homeViewModel.loadSingleState.removeObservers(getViewLifecycleOwner());

        requireActivity().getSharedPreferences("Setting",Context.MODE_PRIVATE)
                .edit()
                .putBoolean("onStopRun",true)
                .apply();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        //observer data to show on UI
        homeViewModel.getSelectionMutableLiveData().observe(getViewLifecycleOwner(), selection -> {
//            Log.d("DFFFFFF",selection.toString());
            if (selection == null) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.notexit)
                        .setIcon(R.drawable.ic_404_error)
                        .setMessage((NetworkInformation.isConnected(requireContext())? getString(R.string.please_research) : getString(R.string.check_network)))
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
                                setSelectionById(id);
                                Loading.closeLoadingView();
                                break;
                            case FAILED:
                                Loading.closeLoadingView();
//                                String tempId = requireContext().getSharedPreferences("Setting",Context.MODE_PRIVATE).getString("TEMP_ID","0");
//                                setSelectionById(tempId);

                                boolean onStopRun = requireContext().getSharedPreferences("Setting",Context.MODE_PRIVATE).getBoolean("onStopRun",false);
                                if(onStopRun) return;
                                new MaterialAlertDialogBuilder(requireContext())
                                        .setTitle(R.string.not_exist)
                                        .setIcon(R.drawable.ic_404_error)
                                        .setMessage(getString(R.string.search_is_not_exit)+","+getString(R.string.please_research))
                                        .setPositiveButton(R.string.ok, null)
                                        .show();
                                break;
                        }

                }
        );
    }

    private void setSelectionById(String id) {
        if (Constant.mode == Constant.MODE_OFFICIAL) {
            SelectionRoomDatabase.databaseWriteExecutor.execute(() -> {
                homeViewModel.getSelectionMutableLiveData().postValue(
                        SelectionRoomDatabase.getDatabase(getContext()).dbDao().getSelection(Integer.parseInt(id))
                );
            });
        } else {
            SelectionRoomDatabase.databaseWriteExecutor.execute(() -> {
                homeViewModel.getSelectionMutableLiveData().postValue(
                        SelectionRoomDatabase.getDatabase(getContext()).dbDao().getSelectionTest(Integer.parseInt(id))
                );
            });
        }
    }
}
