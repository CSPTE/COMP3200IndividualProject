package com.example.finalversion;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.RequiresApi;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //TODO: Quick lookup for naming scheme
    //Task Naming scheme: WorkoutTask.txt
    //Subtask Naming scheme: SitupsWorkoutSubtazk.txt
    //CalendarSelectedHabits: TrackedHabits.txt
    //LogFile Naming scheme: WorkoutLog.txt
    //FutureTask Naming scheme: WorkoutFuturetakk.txt
    //Background Image: background_image.jpg
    //Profile Picture: profile_image.jpg
    //Username: username.txt
    //ProfileSelectedHabits: DisplayedHabits.txt
    //Task Point File: WorkoutPointcard.txt
    //Weekly Point File: WeeklyScore.txt
    //Monthly Point File: MonthlyScore.txt
    //AllTime Point File: AllTimeScore.txt

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private Context mainContext;

    private MenuItem darkMode;
    private String currentAppTheme;

    @RequiresApi(api = Build.VERSION_CODES.O)
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

        View header = navigationView.getHeaderView(0);
        LinearLayout backgroundImage = header.findViewById(R.id.backgroundView);
        ImageView profileImage = header.findViewById(R.id.imageView);
        TextView username = header.findViewById(R.id.textView);
        generateUsername();
        setHeaderMain(backgroundImage, profileImage, username);

        createPointcardFiles();

        //Monthly Score
        SimpleDateFormat taskSimpleDateFormat = new SimpleDateFormat("MMMM");
        Date taskDate = new Date();
        String currentDateMonth = taskSimpleDateFormat.format(taskDate);
        boolean resetMonth = checkIfMonthNeedsResetting(mainContext.getFilesDir(), currentDateMonth);
        if (resetMonth){
            updateMonthPointFileDated(mainContext.getFilesDir(), 0, currentDateMonth);
        }

        //Weekly Score
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date currentDateTemp = new Date();
        String currentDateString = currentDateFormat.format(currentDateTemp);
        boolean resetWeek = checkIfWeekNeedsResetting(mainContext.getFilesDir(), currentDateString);
        if(resetWeek){
            updateWeekPointFileDated(mainContext.getFilesDir(), 0, currentDateString);
        }

        //Navigation
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_statistics, R.id.nav_settings, R.id.nav_profile)
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

    private void createPointcardFiles(){
        File weeklyFile = new File(mainContext.getFilesDir(), "WeeklyScore");
        if (!weeklyFile.exists()) {
            SimpleDateFormat taskSimpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date taskDate = new Date();
            String currentDate = taskSimpleDateFormat.format(taskDate);

            StringBuilder fileBody = new StringBuilder();
            fileBody.append("Start Date =" + currentDate + "\n");
            fileBody.append("Points =0" + "\n");
            String fileBodyString = fileBody.toString();
            try (FileOutputStream fos = mainContext.openFileOutput("WeeklyScore", Context.MODE_PRIVATE)) {
                fos.write(fileBodyString.getBytes(StandardCharsets.UTF_8));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File monthlyFile = new File(mainContext.getFilesDir(), "MonthlyScore");
        if (!monthlyFile.exists()) {
            SimpleDateFormat taskSimpleDateFormat = new SimpleDateFormat("MMMM");
            Date taskDate = new Date();
            String currentDate = taskSimpleDateFormat.format(taskDate);

            StringBuilder fileBody = new StringBuilder();
            fileBody.append("Start Date =" + currentDate + "\n");
            fileBody.append("Points =0" + "\n");
            String fileBodyString = fileBody.toString();
            try (FileOutputStream fos = mainContext.openFileOutput("MonthlyScore", Context.MODE_PRIVATE)) {
                fos.write(fileBodyString.getBytes(StandardCharsets.UTF_8));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File allTimeFile = new File(mainContext.getFilesDir(), "AllTimeScore");
        if (!allTimeFile.exists()) {
            StringBuilder fileBody = new StringBuilder();
            fileBody.append("Points =0" + "\n");
            String fileBodyString = fileBody.toString();
            try (FileOutputStream fos = mainContext.openFileOutput("AllTimeScore", Context.MODE_PRIVATE)) {
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void generateUsername(){
        File taskFile = new File(this.getFilesDir(), "username");
        if (!taskFile.exists()) {
            long unixTime = Instant.now().getEpochSecond();
            String username = "Traveller" + String.valueOf(unixTime);
            try (FileOutputStream fos = this.openFileOutput("username", Context.MODE_PRIVATE)) {
                fos.write(username.getBytes(StandardCharsets.UTF_8));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setHeaderMain(LinearLayout backgroundPicture, ImageView profilePic, TextView username){
        // try to load the stored background image
        try {
            FileInputStream inputStream = mainContext.openFileInput("background_image.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            backgroundPicture.setBackground(drawable);
            inputStream.close();
        } catch (FileNotFoundException e) {
            // file not found, do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }

        // try to load the stored profile image
        try {
            FileInputStream inputStream = mainContext.openFileInput("profile_image.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            profilePic.setImageBitmap(bitmap);
            inputStream.close();
        } catch (FileNotFoundException e) {
            // file not found, do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }

        //load username
        try {
            FileInputStream inputStream = mainContext.openFileInput("username");
            InputStreamReader reader = new InputStreamReader(inputStream);
            StringBuilder usernameBuilder = new StringBuilder();
            int character;
            while ((character = reader.read()) != -1) {
                usernameBuilder.append((char) character);
            }
            reader.close();
            inputStream.close();
            String loadedUsername = usernameBuilder.toString();
            username.setText(loadedUsername);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkIfMonthNeedsResetting(File dir, String currentMonth){
        boolean resetMonth = false;
        String fileMonth;
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("MonthlyScore.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Start Date =.*")){
                                String[] value = line.split("=");
                                fileMonth = value[1];
                                if(!currentMonth.equals(fileMonth)){
                                    resetMonth = true;
                                }
                            }
                            line = br.readLine();
                        }
                        br.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
        return resetMonth;
    }

    private void updateMonthPointFileDated(File dir, int newPoints, String currentDate){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("MonthlyScore.txt")){
                    try {
                        StringBuilder fileBody = new StringBuilder();
                        fileBody.append("Start Date =" + currentDate + "\n");
                        fileBody.append("Points =" + newPoints + "\n");
                        String fileBodyString = fileBody.toString();

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean checkIfWeekNeedsResetting(File dir, String currentMonth){
        boolean resetWeek = false;
        String fileMonth;
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("WeeklyScore.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Start Date =.*")){
                                String[] value = line.split("=");
                                fileMonth = value[1];

                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                                String firstDateString = fileMonth;
                                LocalDate firstDate = LocalDate.parse(firstDateString, formatter);

                                DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                                String firstDateString2 = currentMonth;
                                LocalDate firstDate2 = LocalDate.parse(firstDateString2, formatter2);

                                for (int j=0; j<7; j++) {
                                    firstDate = firstDate.plusDays(1);
                                }

                                if((firstDate.isBefore(firstDate2))){
                                    resetWeek = true;
                                }
                            }
                            line = br.readLine();
                        }
                        br.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
        return resetWeek;
    }

    private void updateWeekPointFileDated(File dir, int newPoints, String currentDate){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("WeeklyScore.txt")){
                    try {
                        StringBuilder fileBody = new StringBuilder();
                        fileBody.append("Start Date =" + currentDate + "\n");
                        fileBody.append("Points =" + newPoints + "\n");
                        String fileBodyString = fileBody.toString();

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
}