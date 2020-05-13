package com.boardtek.selection;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.boardtek.appcenter.AppCenter;
import com.boardtek.appcenter.NetworkInformation;
import com.boardtek.selection.adapter.language.LanguageAdapter;
import com.boardtek.selection.databinding.LanguageLayoutBinding;
import com.boardtek.selection.databinding.NavHeaderMainBinding;
import com.boardtek.selection.datamodel.Action;
import com.boardtek.selection.adapter.action.ActionAdapter;
import com.boardtek.selection.databinding.ActionLayoutBinding;
import com.boardtek.selection.databinding.ActivityMainBinding;
import com.boardtek.selection.db.SelectionRoomDatabase;
import com.boardtek.selection.net.WifiReceiver;
import com.boardtek.selection.ui.v.Loading;
import com.boardtek.selection.ui.v.ShowActionResponse;
import com.boardtek.selection.worker.LoadAllData;
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
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding activityMainBinding;
    private NavHeaderMainBinding navHeaderMainBinding;
    private MainViewModel mainViewModel;
    private Loading loading;
    private List<Action> actionList = new ArrayList<>();
    private ActionLayoutBinding actionLayoutBinding;
    private ActionAdapter actionAdapter;
    private AlertDialog actionDialog;
    private AlertDialog languageDialog;
    private RequestQueue volleyQueue;
    private SwitchCompat switchTest;
    private NavController navController;

    //WIFI
    private WifiReceiver wifiReceiver;

    //language
    LanguageLayoutBinding languageLayoutBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Language
        Constant.lang = getSharedPreferences("Setting", Context.MODE_PRIVATE).getString("Language",Locale.US.getCountry());
        Locale myLocale =  Constant.lang.equals(Locale.US.getCountry()) ? Locale.US :  Locale.TAIWAN ;
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

        //ViewModel
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        //ViewBinding
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        navHeaderMainBinding = NavHeaderMainBinding.bind(activityMainBinding.navView.getHeaderView(0));

        setContentView(activityMainBinding.getRoot().getRootView());


        //Network
        NetworkInformation.init(this);
        //AppCenter
        AppCenter.init(this);

        // Setting
        Constant.mode = getSharedPreferences("Setting",MODE_PRIVATE).getInt("MODE",Constant.MODE_OFFICIAL);
        activityMainBinding.navView.getMenu().findItem(R.id.menu_item_language).setOnMenuItemClickListener(this::onOptionsItemSelected);
        activityMainBinding.navView.getMenu().findItem(R.id.menu_item_actions).setOnMenuItemClickListener(this::onOptionsItemSelected);
        activityMainBinding.navView.getMenu().findItem(R.id.menu_item_test_mode).setOnMenuItemClickListener(this::onOptionsItemSelected);
        switchTest = (SwitchMaterial) activityMainBinding.navView.getMenu().findItem(R.id.menu_item_test_mode).getActionView();
        switchTest.setOnCheckedChangeListener(this::onCheckedChanged);

        //D30 D31顯示
        Log.d("depSn", String.valueOf(AppCenter.depSn));
        if(AppCenter.depSn == 60 || AppCenter.depSn == 156){
            activityMainBinding.navView.getMenu().findItem(R.id.menu_item_actions).setVisible(true);
            activityMainBinding.navView.getMenu().findItem(R.id.menu_item_test_mode).setVisible(true);
        }

        //loadingView
        loading = new Loading(this);

        Toolbar toolbar = activityMainBinding.include.toolbar;
        setSupportActionBar(toolbar);
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        DrawerLayout drawer = activityMainBinding.drawerLayout;
//        NavigationView navigationView = findViewById(R.id.nav_view);
        NavigationView navigationView = activityMainBinding.navView;

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setDrawerLayout(drawer)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        initDrawerHeader(navigationView);


        // Action
        actionLayoutBinding = ActionLayoutBinding.inflate(getLayoutInflater());
        actionLayoutBinding.checkSelectAll.setOnCheckedChangeListener(this::onCheckedChanged);

        //init Volley
        volleyQueue = Volley.newRequestQueue(this);

        actionDialog = new MaterialAlertDialogBuilder(this)
                .setView(actionLayoutBinding.getRoot())
                .setIcon(R.drawable.ic_view)
                .setPositiveButton("RUN",(dialog, which) -> {
                    for(Action action : actionList){
                        if(action.getChecked()){
                            createAndStartActionRequest(action);
                        }
                    }
                })
                .create();


        //language
        languageLayoutBinding = LanguageLayoutBinding.inflate(getLayoutInflater());
        languageLayoutBinding.reyclerView.setHasFixedSize(true);
        languageLayoutBinding.reyclerView.setLayoutManager(new LinearLayoutManager(this));
        languageLayoutBinding.reyclerView.setAdapter(new LanguageAdapter(this));
        languageDialog = new MaterialAlertDialogBuilder(this)
                .setView(languageLayoutBinding.getRoot())
                .create();

    }

    @Override
    protected void onStart() {
        super.onStart();

        mainViewModel.loadAllDataState.observe(this,workInfos -> {
            if(workInfos.size() == 0) return;
            WorkInfo workInfo = workInfos.get(0);
            Log.d(TAG,workInfo.getState().toString());
            if(workInfo.getState()== WorkInfo.State.SUCCEEDED){
                Loading.closeLoadingView();
            } else if(workInfo.getState()== WorkInfo.State.RUNNING){
                Loading.showLoadingView();
            } else if(workInfo.getState()== WorkInfo.State.FAILED){
                Loading.closeLoadingView();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    void initDrawerHeader(NavigationView navigationView){
        //navigationView headerView
        View headerView = navigationView.getHeaderView(0);
//        TextView textView_appName =  (TextView) headerView.findViewById(R.id.t_header_app_name);
        TextView textView_user = headerView.findViewById(R.id.t_header_user);
        TextView textView_mobile = headerView.findViewById(R.id.t_mobileNum);
        TextView textView_NowTime = headerView.findViewById(R.id.t_header_time);
        TextView textView_wifi = headerView.findViewById(R.id.t_header_wifi_name);
        TextView textView_mode = headerView.findViewById(R.id.t_headre_mode);
//        textView_appName.setText("壓機程式自動選用");
        textView_user.setText(AppCenter.uName + ":" + AppCenter.uId);
        textView_mobile.setText(getString(R.string.moblie)+ AppCenter.mobileSn);
        AppCenter.addTimerPerSecondListener(() -> {
            textView_NowTime.setText(getString(R.string.System_time) +"\n"+AppCenter.getSystemTime());
        });
        if(NetworkInformation.isConnected(this))
            textView_wifi.setText(NetworkInformation.getWifiRetek());
        if(Constant.mode == Constant.MODE_OFFICIAL) {
            textView_mode.setText(R.string.official);
            switchTest.setChecked(false);
        }
        else if(Constant.mode == Constant.MODE_TEST) {
            textView_mode.setText(R.string.test);
            switchTest.setChecked(true);
        }
        //navigationView contentView

    }

    private void createAndStartActionRequest(Action action){
        StringRequest getActionRequest;
        if(action.getPosts()==null) {
            getActionRequest = new StringRequest(action.getUrl(), response -> {
                        Log.d(TAG,new String(response.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
                        ShowActionResponse.createAndShow(this,action.getName(),action.getUrl(),action.getPosts(),
                        new String(response.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
            },error -> {
                Log.d(TAG,error.toString());
                ShowActionResponse.createAndShow(this,action.getName(),action.getUrl(),action.getPosts(),error.toString());
            });
        } else {
            getActionRequest = new StringRequest(Request.Method.POST,action.getUrl(),response -> {
                Log.d(TAG,new String(response.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
                ShowActionResponse.createAndShow(this,action.getName(),action.getUrl(),action.getPosts(),new String(response.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
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
            //同步資料
            case R.id.update_data:
                if (NetworkInformation.isConnected(this)) {

                    new MaterialAlertDialogBuilder(this)
                            .setTitle(R.string.synchronize_data)
                            .setIcon(R.drawable.ic_syncronization)
                            .setMessage(R.string.ask_for_a_lot_data)
                            .setPositiveButton(getString(R.string.cancel),null)
                            .setNegativeButton(getString(R.string.ok),(dialog, which) -> {
                                Data in = new Data.Builder()
                                        .putString("url",Constant.getUrl(Constant.mode,Constant.ACTION_GET_ALL))
                                        .putString("mac",NetworkInformation.macAddress)
                                        .putString("uSn", String.valueOf(AppCenter.uSn))
                                        .putString("mobileSn", String.valueOf(AppCenter.mobileSn))
                                        .putInt("mode",Constant.mode)
                                        .build();
                                OneTimeWorkRequest loadAllData = new OneTimeWorkRequest.Builder(LoadAllData.class)
                                        .setInputData(in)
                                        .build();

                                mainViewModel.workManager
                                        .beginUniqueWork("LoadAllData", ExistingWorkPolicy.REPLACE, loadAllData)
                                        .enqueue();
                            }).show();

                }
                else
                    new MaterialAlertDialogBuilder(this)
                            .setTitle(R.string.error)
                            .setIcon(R.drawable.ic_wifi)
                            .setMessage(R.string.check_network)
                            .setPositiveButton(R.string.ok, null)
                            .show();
                return true;
            case R.id.delete_data:
                String ms = "";
                if(Constant.lang.equals(Locale.US.getCountry())){
                    ms = (Constant.mode == Constant.MODE_OFFICIAL ? "Are you sure to delete all data of " + getString(R.string.official) : "Are you sure to delete all data of "+ getString(R.string.test)) + " ?";
                } else {
                    ms = (Constant.mode == Constant.MODE_OFFICIAL ? "你確定要刪除"  + getString(R.string.official) : "你確定要刪除" + getString(R.string.test)) + "的全部資料媽?";
                }

                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.delete_local_data)
                        .setIcon(R.drawable.ic_can)
                        .setMessage(ms)
                        .setPositiveButton(R.string.cancel,null)
                        .setNegativeButton(getString(R.string.ok),(dialog, which) -> {
                            if(Constant.mode == Constant.MODE_OFFICIAL){
                                SelectionRoomDatabase.databaseWriteExecutor.execute(() -> {
                                    SelectionRoomDatabase.getDatabase(this).dbDao().deleteAll();
                                });
                                Toast.makeText(this, R.string.delete_all, Toast.LENGTH_SHORT).show();
                            } else {
                                SelectionRoomDatabase.databaseWriteExecutor.execute(() -> {
                                    SelectionRoomDatabase.getDatabase(this).dbDao().deleteTestAll();
                                });
                                Toast.makeText(this, R.string.delete_all, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();

                return true;
                //Action
            case R.id.menu_item_actions:
                actionList.clear();
                Map<String,String> posts = new HashMap<String, String>();
                posts.put("programId",this.getSharedPreferences("Setting",MODE_PRIVATE).getString("TEMP_ID","0"));

//                posts.put("mobileSn", String.valueOf(AppCenter.mobileSn));
//                posts.put("uSn", String.valueOf(AppCenter.uSn));
//                posts.put("mac", NetworkInformation.macAddress);

                actionList.add(new Action(Constant.ACTION_GET_ALL, Constant.getUrl( Constant.mode,Constant.ACTION_GET_ALL)));
                actionList.add(new Action(Constant.ACTION_BY_PROGRAM_ID,Constant.getUrl(Constant.mode,Constant.ACTION_BY_PROGRAM_ID),posts));
                Log.d(TAG,posts.toString());
                actionAdapter = new ActionAdapter(actionList);
                actionAdapter.setOnAllSelectedEvent(isAllSelected -> {
                    actionLayoutBinding.checkSelectAll.setOnCheckedChangeListener(null);
                    actionLayoutBinding.checkSelectAll.setChecked(isAllSelected);
                    actionLayoutBinding.checkSelectAll.setOnCheckedChangeListener(this::onCheckedChanged);
                });
                RecyclerView recycler = actionLayoutBinding.recyclerAction;
                recycler.setHasFixedSize(false);
                recycler.setLayoutManager(new LinearLayoutManager(this));
                recycler.setAdapter(actionAdapter);
//                mainViewModel.getActionListLiveData().postValue(actionList);

                actionLayoutBinding.checkSelectAll.setChecked(false);

                actionDialog.show();
                actionDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

                return true;
            case R.id.menu_item_test_mode:
                switchTest.setChecked(!switchTest.isChecked());
                return true;
            case R.id.menu_item_language:
                languageDialog.show();
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
            actionDialog.getCurrentFocus().clearFocus();
            if (isChecked) {
                actionAdapter.checkAll();
            } else {
                actionAdapter.cancelCheckAll();
            }
        } else if(buttonView.getId() == R.id.switch_test){
            if(switchTest.isChecked()){
                getSharedPreferences("Setting",MODE_PRIVATE).edit().putInt("MODE",Constant.MODE_TEST).apply();
                Constant.mode = Constant.MODE_TEST;
                Log.d("MODE", String.valueOf(Constant.mode));
                navHeaderMainBinding.tHeadreMode.setText(R.string.test);
            } else {
                getSharedPreferences("Setting",MODE_PRIVATE).edit().putInt("MODE",Constant.MODE_OFFICIAL).apply();
                Constant.mode = Constant.MODE_OFFICIAL;
                Log.d("MODE", String.valueOf(Constant.mode));
                navHeaderMainBinding.tHeadreMode.setText(R.string.official);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //註冊Wifi接收廣播
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        wifiReceiver = new WifiReceiver();
        wifiReceiver.setOnNetworkChangeListener(this::OnNetworkChange);
        registerReceiver(wifiReceiver, filter);

    }

    @SuppressLint("SetTextI18n")
    public void OnNetworkChange(Boolean isConnected, WifiInfo wifiInfo) {



        if (isConnected) {
            NetworkInformation.init(this);
            String ssid = wifiInfo == null ? "" : wifiInfo.getSSID();
            navHeaderMainBinding.tHeaderWifiName.setText(ssid);
        } else {
            NetworkInformation.clear();
            navHeaderMainBinding.tHeaderWifiName.setText("null");
        }
        navController.popBackStack();
        navController.navigate(R.id.nav_home);

    }

    //暫停, 註銷廣播
    @Override
    protected void onPause() {
        super.onPause();
        if (wifiReceiver!=null) unregisterReceiver(wifiReceiver);
    }
}

