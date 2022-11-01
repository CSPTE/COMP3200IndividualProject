package com.example.finalversion;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalversion.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private Context mainContext;

    private MenuItem darkMode;
    private String currentAppTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainContext = this;

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        Menu menu = navigationView.getMenu();
        darkMode = menu.findItem(R.id.nav_statistics);

        createThemeFile("Light");
        readAppTheme(mainContext.getFilesDir());
        if (currentAppTheme.equals("Dark")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        darkMode.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int currentNightMode = MainActivity.this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        // Night mode is not active on device
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        updateAppTheme(mainContext.getFilesDir(), "Dark");
                        break;
                    case Configuration.UI_MODE_NIGHT_YES:
                        // Night mode is active on device
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        updateAppTheme(mainContext.getFilesDir(), "Light");
                        break;
                }
                return false;
            }
        });


        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_statistics, R.id.nav_settings)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //createNotificationChannel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "NotificationsName";
            String description = "NotificationsDescription";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Channel1", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = MainActivity.this.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createThemeFile(String fileBodyString){
        File themeFile = new File(mainContext.getFilesDir(), "AppTheme");
        if (!themeFile.exists()) {
            try (FileOutputStream fos = mainContext.openFileOutput("AppTheme", Context.MODE_PRIVATE)) {
                fos.write(fileBodyString.getBytes(StandardCharsets.UTF_8));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateAppTheme(File dir, String fileBodyString){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("AppTheme.txt")){
                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(fileBodyString.getBytes(StandardCharsets.UTF_8));
                        fos.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
    }

    private void readAppTheme(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("AppTheme.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            currentAppTheme = line;
                            line = br.readLine();
                        }
                        br.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
    }
}