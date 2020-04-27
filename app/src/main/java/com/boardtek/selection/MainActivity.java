package com.boardtek.selection;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;
import com.boardtek.appcenter.AppCenter;
import com.boardtek.appcenter.NetworkInformation;
import com.boardtek.selection.databinding.ActivityMainBinding;
import com.boardtek.selection.databinding.AppBarMainBinding;
import com.boardtek.selection.ui.loading.Loading;
import com.boardtek.selection.worker.LoadData;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private MainViewModel mainViewModel;
    private Loading loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ViewModel
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        //ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        binding.navView.getMenu().findItem(R.id.menu_item_actions).setOnMenuItemClickListener(this::onOptionsItemSelected);


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, String.valueOf(item.getItemId()));

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
                CharSequence [] actions = new CharSequence[]{"A","B"};
                boolean[] bs = {false,false};
                List<Boolean> listBoolean = Arrays.asList(false,false);
                AlertDialog dialogBuilder = new MaterialAlertDialogBuilder(this)
                        .setTitle("Actions")
                        .setIcon(R.drawable.ic_view)
                        .setMultiChoiceItems(actions,bs,(dialog, which, isChecked) -> {

                        }).show();
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

}
