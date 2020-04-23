package com.boardtek.selection;

import android.annotation.SuppressLint;
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
import com.boardtek.selection.worker.LoadData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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

        String url = "http://192.168.50.98/system_mvc/controller.php?s=dev,007459,500,laminationProgram,pp_program&action=mobile_programData_all";
        Data inputData = new Data.Builder()
                .putString("url",url)
                .putBoolean("isTestMode",true)
                .build();

        OneTimeWorkRequest loadData = new OneTimeWorkRequest.Builder(LoadData.class)
                .addTag("LoadData")
                .setInputData(inputData)
                .build();


        switch (item.getItemId()){
            case R.id.update_data:
                //WorkManager.getInstance().enqueue(loadData);
                WorkManager.getInstance(this).enqueue(loadData);
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
