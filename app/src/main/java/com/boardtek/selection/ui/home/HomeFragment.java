package com.boardtek.selection.ui.home;

import android.os.Bundle;
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

import com.boardtek.selection.R;
import com.boardtek.selection.databinding.EnterNumBinding;
import com.boardtek.selection.databinding.FragmentHomeBinding;
import com.boardtek.selection.datamodel.DataContent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private String TAG = HomeFragment.class.getSimpleName();
    private HomeViewModel homeViewModel;
    private FragmentHomeBinding fragmentHomeBinding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        fragmentHomeBinding = FragmentHomeBinding.inflate(getLayoutInflater());
        View root = fragmentHomeBinding.getRoot();
        //observer
        homeViewModel.getSelectionMutableLiveData().observe(getViewLifecycleOwner(),selection -> {
            fragmentHomeBinding.tContentProgramId.setText(getString(R.string.programid) +selection.getProgramId());
            fragmentHomeBinding.tContentVendor.setText(getString(R.string.vendor)+selection.getVendorTitle());
            fragmentHomeBinding.tContentIsPaused.setText(getString(R.string.ispaused)+selection.isPause());
            fragmentHomeBinding.tContentHour.setText(getString(R.string.hour)+selection.getHour());
            fragmentHomeBinding.tContentMinute.setText(getString(R.string.minute)+selection.getMinute());
            fragmentHomeBinding.tContentSetName.setText(getString(R.string.set_name)+selection.getSetName());
            fragmentHomeBinding.tContentSetDate.setText(getString(R.string.set_date)+"\n"+selection.getSetDate().substring(0,9)+"\n"+selection.getSetDate().substring(11));
            fragmentHomeBinding.bContentDataPp.setOnClickListener(this);
            fragmentHomeBinding.bContentDataContent.setOnClickListener(this);
        });


        homeViewModel.getSingleState().observe(getViewLifecycleOwner(),workInfo -> {
            Log.d(TAG,workInfo.getState().toString());
        });

        fragmentHomeBinding.fab.setOnClickListener(this);
        return root;
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
                            int id = Integer.parseInt(editText.getText().toString());
                            Log.d(TAG, String.valueOf(id));
                            homeViewModel.setProgramId(id);
                            homeViewModel.setItemFromId(id);
                        }
                    }).show();

        }
        if(v.getId() == R.id.b_content_data_content){
            String json = homeViewModel.getSelectionMutableLiveData().getValue().getData_content();
            List<DataContent> list = new Gson().fromJson(json,new TypeToken<List<DataContent>>(){}.getType());
            Log.d(TAG,list.toString());

        }
    }
}
