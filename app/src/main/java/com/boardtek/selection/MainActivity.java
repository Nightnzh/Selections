package com.boardtek.selection;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.boardtek.appcenter.AppCenter;
import com.boardtek.appcenter.NetworkInformation;
import com.boardtek.selection.databinding.SwitchTestBinding;
import com.boardtek.selection.datamodel.Action;
import com.boardtek.selection.adapter.actionAdapter.ActionAdapter;
import com.boardtek.selection.databinding.ActionLayoutBinding;
import com.boardtek.selection.databinding.ActivityMainBinding;
import com.boardtek.selection.ui.v.Loading;
import com.boardtek.selection.ui.v.ShowActionResponse;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding activityMainBinding;
    private MainViewModel mainViewModel;
    private Loading loading;
    private List<Action> actionList = new ArrayList<>();
    private ActionLayoutBinding actionLayoutBinding;
    private ActionAdapter actionAdapter;
    private AlertDialog actionDialog;
    private RequestQueue volleyQueue;
    private SwitchCompat switchTest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ViewModel
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        //ViewBinding
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        //Setting
        getSharedPreferences("Setting",MODE_PRIVATE).edit().putBoolean("TEST_MODE",false);

        //loadingView
        loading = new Loading(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        NetworkInformation.init(this);
        AppCenter.init(this);
        initHeader(navigationView);

        mainViewModel.loadDataState.observe(this,workInfo -> {

            Log.d(TAG,workInfo.getState().toString());

            if(workInfo.getState()== WorkInfo.State.SUCCEEDED){
                Loading.closeLoadingView();
            }

            if(workInfo.getState()== WorkInfo.State.RUNNING){
                Loading.showLoadingView();
            }
        });

        activityMainBinding.navView.getMenu().findItem(R.id.menu_item_actions).setOnMenuItemClickListener(this::onOptionsItemSelected);
        activityMainBinding.navView.getMenu().findItem(R.id.menu_item_test_mode).setOnMenuItemClickListener(this::onOptionsItemSelected);
        switchTest = (SwitchMaterial) activityMainBinding.navView.getMenu().findItem(R.id.menu_item_test_mode).getActionView();
        switchTest.setOnCheckedChangeListener(this::onCheckedChanged);

        // Action
        actionLayoutBinding = ActionLayoutBinding.inflate(getLayoutInflater());

        actionLayoutBinding.checkSelectAll.setOnCheckedChangeListener(this::onCheckedChanged);

        //init Volley
        volleyQueue = Volley.newRequestQueue(this);

        actionDialog = new MaterialAlertDialogBuilder(this)
                .setView(actionLayoutBinding.getRoot())
                .setIcon(R.drawable.ic_view)
                .setPositiveButton("OK",(dialog, which) -> {
                    for(Action action : actionList){
                        if(action.getChecked()){
                            createAndStartActionRequest(action);
                        }
                    }
                })
                .create();
    }

    @SuppressLint("SetTextI18n")
    void initHeader(NavigationView navigationView){
        //navigationView headerView
        View headerView = navigationView.getHeaderView(0);
//        TextView textView_appName =  (TextView) headerView.findViewById(R.id.t_header_app_name);
        TextView textView_user = headerView.findViewById(R.id.t_header_user);
        TextView textView_mobile = headerView.findViewById(R.id.t_mobileNum);
        TextView textView_NowTime = headerView.findViewById(R.id.t_header_time);
        TextView textView_WiFi = headerView.findViewById(R.id.t_header_wifi_name);
        TextView textView_testState = headerView.findViewById(R.id.t_headre_mode);
//        textView_appName.setText("壓機程式自動選用");
        textView_user.setText(AppCenter.uName + ":" + AppCenter.uId);
        textView_mobile.setText("Mobile: "+ AppCenter.mobileSn);
        AppCenter.addTimerPerSecondListener(() -> {
            textView_NowTime.setText(getString(R.string.System_time) +"\n"+AppCenter.getSystemTime());
        });
        if(NetworkInformation.isConnected(this))
            textView_WiFi.setText(NetworkInformation.getWifiRetek());
        textView_testState.setText(NetworkInformation.IP);
        //navigationView contentView

    }

    private void createAndStartActionRequest(Action action){
        StringRequest getActionRequest;
        if(action.getPosts()==null) {
            getActionRequest = new StringRequest(action.getUrl(),response -> {
                Log.d(TAG,response);
                ShowActionResponse.createAndShow(this,action.getName(),action.getUrl(),action.getPosts(),response);
            },error -> {
                Log.d(TAG,error.toString());
                ShowActionResponse.createAndShow(this,action.getName(),action.getUrl(),action.getPosts(),error.toString());
            });
        } else {
            getActionRequest = new StringRequest(Request.Method.POST,action.getUrl(),response -> {
                Log.d(TAG,response);
                ShowActionResponse.createAndShow(this,action.getName(),action.getUrl(),action.getPosts(),response);
            },error -> {
                Log.d(TAG,error.toString());
                ShowActionResponse.createAndShow(this,action.getName(),action.getUrl(),action.getPosts(),error.toString());
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    return action.getPosts();
                }
            };
        }
        volleyQueue.add(getActionRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, getResources().getResourceName(item.getItemId()));
        switch (item.getItemId()){
            case R.id.update_data:
                //WorkManager.getInstance().enqueue(loadData);
                if (NetworkInformation.isConnected(this))
                    mainViewModel.workManager
                            .beginUniqueWork("LoadAllData", ExistingWorkPolicy.REPLACE, mainViewModel.loadData)
                            .enqueue();
                else
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Error")
                            .setIcon(R.drawable.ic_wifi)
                            .setMessage("Network is not connection.\nPlease open the WIFI.")
                            .setPositiveButton("OK", null)
                            .show();
                return true;
            case R.id.menu_item_actions:

                boolean isTestMode = false;
                actionList.clear();
                Map<String,String> posts = new HashMap<String, String>();
                if(!isTestMode) {
                    posts.put("programId",this.getSharedPreferences("Setting",MODE_PRIVATE).getString("TEMP_ID","0"));
                    assert false;
                    actionList.add(new Action("GetAll", "http://192.168.50.98/system_mvc/controller.php?s=dev,007459,500,laminationProgram,pp_program&action=mobile_programData_all"));
                    actionList.add(new Action("FromProgramId","http://192.168.50.98/system_mvc/controller.php?s=dev,007459,500,laminationProgram,pp_program&action=mobile_programData",posts));
                } else {

                }
                Log.d(TAG,posts.toString());
                actionAdapter = new ActionAdapter(actionList);
                actionAdapter.setOnAllSelectedEvent(isAllSelected -> {
                    actionLayoutBinding.checkSelectAll.setOnCheckedChangeListener(null);
                    actionLayoutBinding.checkSelectAll.setChecked(isAllSelected);
                    actionLayoutBinding.checkSelectAll.setOnCheckedChangeListener(this::onCheckedChanged);
                });
                RecyclerView recycler = actionLayoutBinding.recyclerAction;
                recycler.setHasFixedSize(true);
                recycler.setLayoutManager(new LinearLayoutManager(this));
                recycler.setAdapter(actionAdapter);
//                mainViewModel.getActionListLiveData().postValue(actionList);

                actionLayoutBinding.checkSelectAll.setChecked(false);
                actionDialog.show();
                return true;
            case R.id.menu_item_test_mode:
                switchTest.setChecked(!switchTest.isChecked());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        Log.d(TAG,getResources().getResourceName(buttonView.getId()));
        if(buttonView.getId() == R.id.check_select_all) {
            if (isChecked) {
                actionAdapter.checkAll();
            } else {
                actionAdapter.cancelCheckAll();
            }
        } else if(buttonView.getId() == R.id.switch_test){
            if(switchTest.isChecked()){
                // TODO:正式區
                getSharedPreferences("Setting",MODE_PRIVATE).edit().putInt("MODE",MODE_OFFCAIL).apply();
            } else {
                // TODO:測試區
                getSharedPreferences("Setting",MODE_PRIVATE).edit().putInt("MODE",MODE_TEST).apply();
                

            }
        }
    }

    private static final int MODE_TEST = 1;
    private static final int MODE_OFFCAIL = 0;
}


//        mainViewModel.actionListLiveData.observe(this,actions -> {
//            if(actions==null)
//                return;
//
//            Log.d("@@@@@",actions.toString());
//            for (Action action : actions){
//
//                if(!action.getChecked()) {
//                    actionLayoutBinding.checkSelectAll.setChecked(false);
//                    return;
//                }else {
//                    actionLayoutBinding.checkSelectAll.setChecked(true);
//                }
//            }
//        });