package com.example.finalversion.ui.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.finalversion.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ProfileFragment extends Fragment {
    private Context profileContext;

    private static final int SELECT_IMAGE_REQUEST_CODE = 1;
    private static final int SELECT_PROFILE_IMAGE_REQUEST_CODE = 2;
    final Uri[] imageUri = new Uri[1];
    final Uri[] profileImageUri = new Uri[1];
    private File backgroundImageFile;
    private File profileImageFile;
    private boolean reloadPage = false;

    private ArrayList<String> tasks = new ArrayList<String>();
    private final String selectedTasksFileName = "DisplayedHabits";
    private ArrayList<String> loadedHabits = new ArrayList<String>();

    private Button editProfileButton;
    private Button editTableButton;
    private LinearLayout backgroundPicture;
    private ImageView profilePic;
    private TextView username;
    private TableLayout taskTable;

    private LinearLayout infoLayout;

    private String currentUsername;
    private ArrayList<String> users = new ArrayList<>();
    private boolean dataReceived = false;
    private Map<String, Object> tableData = new HashMap<>();

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        profileContext = this.getContext();
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editProfileButton = view.findViewById(R.id.profile_edit_images);
        editTableButton = view.findViewById(R.id.profile_edit_table);
        backgroundPicture = view.findViewById(R.id.profile_background);
        profilePic = view.findViewById(R.id.profile_pic);
        username = view.findViewById(R.id.profile_username);
        infoLayout = view.findViewById(R.id.profile_point_information);
        taskTable = view.findViewById(R.id.table_layout);

        backgroundImageFile = new File(profileContext.getFilesDir(), "background_image.jpg");
        profileImageFile = new File(profileContext.getFilesDir(), "profile_image.jpg");

        //Default Load Table
        loadSelectedHabitsFromFileToVar(profileContext.getFilesDir());
        taskTable.removeAllViews();
        setTableHeader();
        int sendDataInt = 1;
        for (String hab : loadedHabits) {
            ArrayList<String> idealDates = new ArrayList<String>();
            int streakInt = 0;
            int percentage = 0;

            boolean habitual = getTaskType(profileContext.getFilesDir(), hab);
            String days = getTaskData(profileContext.getFilesDir(), hab);
            if(days != null){
                ArrayList<String> daysArray = daysToDaysArray(days); //Days To Array
                ArrayList<String> taskDates = getLogFile(profileContext.getFilesDir(), hab);
                if (!taskDates.isEmpty()){
                    idealDates = getIdealDates(taskDates, daysArray); //Perfect Performance
                    streakInt = calculateStreak(idealDates, taskDates); //Streak
                    percentage = calculatePercentage(idealDates, taskDates); //Percentage
                }
            }
            int taskPoints = getTaskPoints(profileContext.getFilesDir(), hab);

            tableData.put("task" + String.valueOf(sendDataInt), hab);
            tableData.put("streak" + String.valueOf(sendDataInt), streakInt);
            tableData.put("percentage" + String.valueOf(sendDataInt), percentage);
            tableData.put("points" + String.valueOf(sendDataInt), taskPoints);
            sendDataInt++;

            setTableRow(hab, habitual, streakInt, percentage, taskPoints);
        }
        int oldPointsAllTime = getcurrentPointsAllTime(profileContext.getFilesDir());
        int oldPointsMonth = getcurrentPointsMonth(profileContext.getFilesDir());
        int oldPointsWeek = getcurrentPointsWeek(profileContext.getFilesDir());

        tableData.put("weekly score", oldPointsWeek);
        tableData.put("monthly score", oldPointsMonth);
        tableData.put("alltime score", oldPointsAllTime);

        setTableFooter(oldPointsAllTime, oldPointsMonth, oldPointsWeek);

        //Set Edit Profile Button
        final String[] listItems = new String[]{"Profile Picture", "Background Picture", "Username"};
        final AtomicInteger checkedItem = new AtomicInteger(0);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseApp.initializeApp(profileContext);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                getData(db);
                String temp1 = "test1";

                AlertDialog.Builder builder = new AlertDialog.Builder(profileContext);
                builder.setTitle("Select which to change");
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setSingleChoiceItems(listItems, checkedItem.get(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkedItem.set(which);
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedOptionIndex = checkedItem.get();
                        String selectedOption = listItems[selectedOptionIndex];

                        if (selectedOption.equals("Profile Picture")) {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent, SELECT_PROFILE_IMAGE_REQUEST_CODE);
                        } else if (selectedOption.equals("Background Picture")) {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
                        } else if (selectedOption.equals("Username")) {
                            final EditText input = new EditText(profileContext);

                            // create a Dialog to show the input field
                            AlertDialog.Builder usernameBuilder = new AlertDialog.Builder(profileContext)
                                    .setTitle("Change username")
                                    .setView(input)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String newUsername = input.getText().toString();
                                            username.setText(newUsername);

                                            if (dataReceived == false){
                                                AlertDialog.Builder fileAlreadyExsists = new AlertDialog.Builder(profileContext);
                                                fileAlreadyExsists.setTitle("Please wait");
                                                fileAlreadyExsists.setMessage("We are fetching the cloud data. Please wait until it is finished");
                                                fileAlreadyExsists.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                });
                                                fileAlreadyExsists.create();
                                                AlertDialog alertDialog = fileAlreadyExsists.create();
                                                alertDialog.show();
                                                while(dataReceived == false)
                                                {
                                                    try {
                                                        Thread.sleep(1000);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }

                                            if (users.contains(newUsername)){
                                                AlertDialog.Builder fileAlreadyExsists = new AlertDialog.Builder(profileContext);
                                                fileAlreadyExsists.setTitle("Username taken");
                                                fileAlreadyExsists.setMessage("This username is already in use. Please use another one.");
                                                fileAlreadyExsists.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                });
                                                fileAlreadyExsists.create();
                                                AlertDialog alertDialog = fileAlreadyExsists.create();
                                                alertDialog.show();
                                            } else {
                                                try {
                                                    FileOutputStream outputStream = getContext().openFileOutput("username", Context.MODE_PRIVATE);
                                                    OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                                                    writer.write(newUsername);
                                                    writer.close();
                                                    outputStream.close();

                                                    getActivity().recreate();
                                                } catch (FileNotFoundException e) {
                                                    e.printStackTrace();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                sendData(db, newUsername, oldPointsWeek, oldPointsMonth, oldPointsAllTime);
                                                deleteData(db, currentUsername);
                                                sendTableData(db, newUsername);
                                                String temp2 = "test2";
                                            }

                                        }
                                    })
                                    .setNegativeButton("Cancel", null);
                            AlertDialog usernameDialog = usernameBuilder.create();
                            usernameDialog.show();
                        }
                        reloadPage = true;
                    }

                });
                builder.setNegativeButton("Cancel", null);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        //Profile Info
        infoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder fileAlreadyExsists = new AlertDialog.Builder(profileContext);
                fileAlreadyExsists.setTitle("Point Breakdown Per Task");
                fileAlreadyExsists.setMessage("Score = P + [(M + s + s + ...) x STR x CR] \n \nWhere: \nP = Previous points gained from this task \nM = 10 points for completing the Main task \ns = 1 point for completing each Subtask \nSTR = Your Streak on the given day \nCR = Your Completion Rate on the given day \n \nFor example: \nIf You had 100 points previously, and complete a task with 4 subtasks on a given day with STR = 2 and CR = 50%, your new score is: \n \nNew Score = 100 + [(10 + 4 x 1) x 2 x 0.5] \nNew Score =  114 \n \nYour User Score is calculated by adding together the points accumulated from each habitual, non-habitual and future tasks");
                fileAlreadyExsists.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                fileAlreadyExsists.create();
                AlertDialog alertDialog = fileAlreadyExsists.create();
                alertDialog.show();
            }
        });

        //Update Table
        findTasks(profileContext.getFilesDir());
        int numberOfChoices = tasks.size();
        String[] listItemsTable = new String[numberOfChoices];
        for (int i=0; i<numberOfChoices; i++){
            listItemsTable[i] = tasks.get(i);
        }
        final boolean[] checkedItemsTable = new boolean[listItemsTable.length];
        final List<String> selectedItemsTable = Arrays.asList(listItemsTable);

        editTableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder tableBuilder = new AlertDialog.Builder(profileContext);
                tableBuilder.setTitle("Select Publicly Viewable Tasks");
                tableBuilder.setIcon(R.mipmap.ic_launcher);

                tableBuilder.setMultiChoiceItems(listItemsTable, checkedItemsTable, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItemsTable[which] = isChecked;
                        String currentItem = selectedItemsTable.get(which);
                    }
                });
                tableBuilder.setCancelable(false);

                tableBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        taskTable.removeAllViews();
                        StringBuilder tasksToWrite = new StringBuilder();
                        setTableHeader();
                        int sendDataInt = 1;
                        for (int i = 0; i < checkedItemsTable.length; i++) {
                            if (checkedItemsTable[i]){
                                ArrayList<String> idealDates = new ArrayList<String>();
                                int streakInt = 0;
                                int percentage = 0;

                                boolean habitual = getTaskType(profileContext.getFilesDir(), tasks.get(i));
                                String days = getTaskData(profileContext.getFilesDir(), tasks.get(i));
                                ArrayList<String> daysArray = daysToDaysArray(days);
                                ArrayList<String> taskDates = getLogFile(profileContext.getFilesDir(), tasks.get(i));
                                if (!taskDates.isEmpty()) {
                                    idealDates = getIdealDates(taskDates, daysArray); //Perfect Performance
                                    streakInt = calculateStreak(idealDates, taskDates); //Streak
                                    percentage = calculatePercentage(idealDates, taskDates); //Percentage
                                }
                                int taskPoints = getTaskPoints(profileContext.getFilesDir(), tasks.get(i));

                                setTableRow(tasks.get(i), habitual, streakInt, percentage, taskPoints);
                                tableData.put("task" + String.valueOf(sendDataInt), tasks.get(i));
                                tableData.put("streak" + String.valueOf(sendDataInt), streakInt);
                                tableData.put("percentage" + String.valueOf(sendDataInt), percentage);
                                tableData.put("points" + String.valueOf(sendDataInt), taskPoints);
                                sendDataInt++;

                                tasksToWrite.append(tasks.get(i) + "\n");
                            }
                        }
                        int oldPointsAllTime = getcurrentPointsAllTime(profileContext.getFilesDir());
                        int oldPointsMonth = getcurrentPointsMonth(profileContext.getFilesDir());
                        int oldPointsWeek = getcurrentPointsWeek(profileContext.getFilesDir());

                        tableData.put("weekly score", oldPointsWeek);
                        tableData.put("monthly score", oldPointsMonth);
                        tableData.put("alltime score", oldPointsAllTime);

                        FirebaseApp.initializeApp(profileContext);
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        sendTableData(db, currentUsername);

                        setTableFooter(oldPointsAllTime, oldPointsMonth, oldPointsWeek);

                        String fileBodyString = tasksToWrite.toString();
                        File taskFile = new File(profileContext.getFilesDir(), selectedTasksFileName);
                        if (!taskFile.exists()) {
                            try (FileOutputStream fos = profileContext.openFileOutput(selectedTasksFileName, Context.MODE_PRIVATE)) {
                                fos.write(fileBodyString.getBytes(StandardCharsets.UTF_8));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            updateTaskFile(profileContext.getFilesDir(), fileBodyString);
                            loadSelectedHabitsFromFileToVar(profileContext.getFilesDir());
                        }
                    }
                });
                tableBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                tableBuilder.setNeutralButton("CLEAR ALL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < checkedItemsTable.length; i++) {
                            checkedItemsTable[i] = false;
                        }
                    }
                });

                tableBuilder.create();
                AlertDialog alertDialogTable = tableBuilder.create();
                alertDialogTable.show();
            }
        });
    }

    private void sendTableData(FirebaseFirestore db, String user){
        db.collection("profiletable")
                .document(user)
                .set(tableData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Log.d("TAG", "DocumentSnapshot successfully written!");
                        String temp = "alma";
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String temp2 = "korte";
                    }
                });
    }

    private void sendData(FirebaseFirestore db, String usernameLocal, int weeklyScoreLocal, int monthlyScoreLocal, int alltimeScoreLocal){
        Map<String, Object> data = new HashMap<>();
        data.put("username", usernameLocal);
        data.put("weeklyScore", weeklyScoreLocal);
        data.put("monthlyScore", monthlyScoreLocal);
        data.put("alltimeScore", alltimeScoreLocal);
        db.collection("scores")
                .document(usernameLocal)
                .set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Log.d("TAG", "DocumentSnapshot successfully written!");
                        String temp = "alma";
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String temp2 = "korte";
                    }
                });
    }

    private void deleteData(FirebaseFirestore db, String oldUser){
        db.collection("scores").document(oldUser)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        String success = "success delete";
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error deleting document", e);
                        String fail = "fail delete";
                    }
                });
        db.collection("profiletable").document(oldUser)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        String success = "success delete";
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error deleting document", e);
                        String fail = "fail delete";
                    }
                });
    }

    private void getData(FirebaseFirestore db){
        // Get all documents from the collection
        db.collection("scores")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            //scores.add(documentSnapshot.getData());
                            String user = documentSnapshot.getString("username");
                            users.add(user);
                        }
                        dataReceived = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String temp = "test";
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //Uri imageUri = data.getData();
            imageUri[0] = data.getData();

            if (imageUri[0] != null) {
                try {
                    InputStream inputStream = profileContext.getContentResolver().openInputStream(imageUri[0]);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                    backgroundPicture.setBackground(drawable);

                    FileOutputStream outputStream = new FileOutputStream(backgroundImageFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else if (requestCode == SELECT_PROFILE_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            profileImageUri[0] = data.getData();

            try {
                InputStream inputStream = profileContext.getContentResolver().openInputStream(profileImageUri[0]);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                profilePic.setImageDrawable(drawable);

                FileOutputStream outputStream = new FileOutputStream(profileImageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (reloadPage){
            getActivity().recreate();
            reloadPage = false;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // try to load the stored background image
        try {
            FileInputStream inputStream = getContext().openFileInput("background_image.jpg");
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
            FileInputStream inputStream = getContext().openFileInput("profile_image.jpg");
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
            FileInputStream inputStream = getContext().openFileInput("username");
            InputStreamReader reader = new InputStreamReader(inputStream);
            StringBuilder usernameBuilder = new StringBuilder();
            int character;
            while ((character = reader.read()) != -1) {
                usernameBuilder.append((char) character);
            }
            reader.close();
            inputStream.close();
            String loadedUsername = usernameBuilder.toString();
            currentUsername = loadedUsername;
            username.setText(loadedUsername);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //requireActivity().recreate();
    }

    private void findTasks(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(".*Task.txt")){
                    String taskToAdd = file.getName();
                    String newString = taskToAdd.replace("Task", "");
                    tasks.add(newString);
                }
            }
        }
    }

    private void setTableHeader(){
        TableRow row = new TableRow(getContext());
        //int textColorPrimary = ContextCompat.getColor(profileContext, R.color.textColorPrimary);
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        TypedArray arr =
                getActivity().obtainStyledAttributes(typedValue.data, new int[]{
                        android.R.attr.textColorPrimary});
        int primaryColor = arr.getColor(0, -1);

        TextView cell1 = new TextView(getContext());
        cell1.setText("Task Name");
        cell1.setGravity(Gravity.CENTER);
        cell1.setTextColor(primaryColor);
        cell1.setTypeface(null, Typeface.BOLD);
        row.addView(cell1);

        TextView cell2 = new TextView(getContext());
        cell2.setText("Streak");
        cell2.setGravity(Gravity.CENTER);
        cell2.setTextColor(primaryColor);
        cell2.setTypeface(null, Typeface.BOLD);
        row.addView(cell2);

        TextView cell3 = new TextView(getContext());
        cell3.setText("CR (%)");
        cell3.setGravity(Gravity.CENTER);
        cell3.setTextColor(primaryColor);
        cell3.setTypeface(null, Typeface.BOLD);
        row.addView(cell3);

        TextView cell4 = new TextView(getContext());
        cell4.setText("Points");
        cell4.setGravity(Gravity.CENTER);
        cell4.setTextColor(primaryColor);
        cell4.setTypeface(null, Typeface.BOLD);
        row.addView(cell4);

        taskTable.addView(row);
    }

    private void setTableFooter(int userPoints, int monthlyPoints, int weeklyPoints){
        TableRow row = new TableRow(getContext());
        //int textColorPrimary = ContextCompat.getColor(profileContext, R.color.textColorPrimary);
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        TypedArray arr =
                getActivity().obtainStyledAttributes(typedValue.data, new int[]{
                        android.R.attr.textColorPrimary});
        int primaryColor = arr.getColor(0, -1);

        TextView cell1 = new TextView(getContext());
        cell1.setText("Weekly Score");
        cell1.setGravity(Gravity.CENTER);
        cell1.setTextColor(primaryColor);
        cell1.setTypeface(null, Typeface.BOLD);
        row.addView(cell1);

        TextView cell2 = new TextView(getContext());
        cell2.setText("");
        cell2.setGravity(Gravity.CENTER);
        cell2.setTextColor(primaryColor);
        cell2.setTypeface(null, Typeface.BOLD);
        row.addView(cell2);

        TextView cell3 = new TextView(getContext());
        cell3.setText("");
        cell3.setGravity(Gravity.CENTER);
        cell3.setTextColor(primaryColor);
        cell3.setTypeface(null, Typeface.BOLD);
        row.addView(cell3);

        TextView cell4 = new TextView(getContext());
        cell4.setText(Integer.toString(weeklyPoints));
        cell4.setGravity(Gravity.CENTER);
        cell4.setTextColor(primaryColor);
        cell4.setTypeface(null, Typeface.BOLD);
        row.addView(cell4);

        taskTable.addView(row);

        TableRow row2 = new TableRow(getContext());
        TextView cell12 = new TextView(getContext());
        cell12.setText("Monthly Score");
        cell12.setGravity(Gravity.CENTER);
        cell12.setTextColor(primaryColor);
        cell12.setTypeface(null, Typeface.BOLD);
        row2.addView(cell12);

        TextView cell22 = new TextView(getContext());
        cell22.setText("");
        cell22.setGravity(Gravity.CENTER);
        cell22.setTextColor(primaryColor);
        cell22.setTypeface(null, Typeface.BOLD);
        row2.addView(cell22);

        TextView cell32 = new TextView(getContext());
        cell32.setText("");
        cell32.setGravity(Gravity.CENTER);
        cell32.setTextColor(primaryColor);
        cell32.setTypeface(null, Typeface.BOLD);
        row2.addView(cell32);

        TextView cell42 = new TextView(getContext());
        cell42.setText(Integer.toString(monthlyPoints));
        cell42.setGravity(Gravity.CENTER);
        cell42.setTextColor(primaryColor);
        cell42.setTypeface(null, Typeface.BOLD);
        row2.addView(cell42);

        taskTable.addView(row2);

        TableRow row3 = new TableRow(getContext());
        TextView cell13 = new TextView(getContext());
        cell13.setText("All-Time Score");
        cell13.setGravity(Gravity.CENTER);
        cell13.setTextColor(primaryColor);
        cell13.setTypeface(null, Typeface.BOLD);
        row3.addView(cell13);

        TextView cell23 = new TextView(getContext());
        cell23.setText("");
        cell23.setGravity(Gravity.CENTER);
        cell23.setTextColor(primaryColor);
        cell23.setTypeface(null, Typeface.BOLD);
        row3.addView(cell23);

        TextView cell33 = new TextView(getContext());
        cell33.setText("");
        cell33.setGravity(Gravity.CENTER);
        cell33.setTextColor(primaryColor);
        cell33.setTypeface(null, Typeface.BOLD);
        row3.addView(cell33);

        TextView cell43 = new TextView(getContext());
        cell43.setText(Integer.toString(userPoints));
        cell43.setGravity(Gravity.CENTER);
        cell43.setTextColor(primaryColor);
        cell43.setTypeface(null, Typeface.BOLD);
        row3.addView(cell43);

        taskTable.addView(row3);
    }

    @SuppressLint("SetTextI18n")
    private void setTableRow(String taskName, boolean habitual, int streak, int percentage, int points){
        TableRow row = new TableRow(getContext());

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        TypedArray arr =
                getActivity().obtainStyledAttributes(typedValue.data, new int[]{
                        android.R.attr.textColorPrimary});
        int primaryColor = arr.getColor(0, -1);

        TextView cell1 = new TextView(getContext());
        cell1.setText(taskName);
        cell1.setGravity(Gravity.CENTER);
        cell1.setTextColor(primaryColor);
        row.addView(cell1);

        TextView cell2 = new TextView(getContext());
        if (habitual){
            cell2.setText(Integer.toString(streak) + " day(s)");
        } else {
            cell2.setText("-");
        }
        cell2.setGravity(Gravity.CENTER);
        cell2.setTextColor(primaryColor);
        row.addView(cell2);

        TextView cell3 = new TextView(getContext());
        if (habitual){
            cell3.setText(Integer.toString(percentage) + "%");
        } else {
            cell3.setText("-");
        }
        cell3.setGravity(Gravity.CENTER);
        cell3.setTextColor(primaryColor);
        row.addView(cell3);

        TextView cell4 = new TextView(getContext());
        cell4.setText(Integer.toString(points));
        cell4.setGravity(Gravity.CENTER);
        cell4.setTextColor(primaryColor);
        row.addView(cell4);

        taskTable.addView(row);
    }

    private boolean getTaskType(File dir, String taskName){
        File[] files = dir.listFiles();
        boolean isItHabitual = false;
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskName +"Task.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();

                        //read each line
                        while (line != null) {
                            if (line.matches("Habit Toggled =.*")){
                                String[] value = line.split("=");
                                isItHabitual = Boolean.parseBoolean(value[1]);
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
        return isItHabitual;
    }

    private String getTaskData(File dir, String taskName){
        File[] files = dir.listFiles();
        boolean isItHabitual = false;
        String days = null;
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskName +"Task.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();

                        //read each line
                        while (line != null) {
                            if (line.matches("Habit Toggled =.*")){
                                String[] value = line.split("=");
                                isItHabitual = Boolean.parseBoolean(value[1]);
                            } else if (line.matches("Habit Days =.*")){
                                if (isItHabitual){
                                    String[] value = line.split("=");
                                    days = value[1];
                                } else {
                                    days = "Non-Habitual";
                                }
                            }
                            line = br.readLine();
                        }
                        br.close();
                        if (days == null){
                            days = "Non-Habitual";
                        }
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
        return days;
    }

    private ArrayList<String> daysToDaysArray(String days){
        ArrayList<String> daysArray = new ArrayList<String>();
        String[] value = days.split(", ");
        daysArray.addAll(Arrays.asList(value));
        return daysArray;
    }

    private ArrayList<String> getLogFile(File dir, String taskName){
        ArrayList<String> taskDates = new ArrayList<String>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskName + "Log.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (!line.matches("FullLog")){
                                taskDates.add(line);
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
        return taskDates;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ArrayList<String> getIdealDates(ArrayList<String> taskDates, ArrayList<String> daysArray){
        ArrayList<String> idealDates = new ArrayList<String>();

        //Strings to LocalDates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String firstDateString = taskDates.get(0);
        LocalDate firstDate = LocalDate.parse(firstDateString, formatter);

        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date currentDateTemp = new Date();
        String currentDateString = currentDateFormat.format(currentDateTemp);
        LocalDate currentDate = LocalDate.parse(currentDateString, formatter);

        //String currentDateString = "15-12-2022";
        //LocalDate currentDate = LocalDate.parse(currentDateString, formatter);

        for (LocalDate date = firstDate; date.isBefore(currentDate.plusDays(1)); date = date.plusDays(1)) {
            DayOfWeek dayNameUpperCase = date.getDayOfWeek();
            String dayNameLowerCase = dayNameUpperCase.toString().toLowerCase();
            String dayName = dayNameLowerCase.substring(0, 1).toUpperCase() + dayNameLowerCase.substring(1);
            if (daysArray.contains(dayName)){
                idealDates.add(date.toString());
            }
        }
        return idealDates;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private int calculateStreak(ArrayList<String> idealDates, ArrayList<String> taskDates){
        int streakInt = 0;
        for (String idealLocalDate : idealDates){
            String[] splitter = idealLocalDate.split("-");
            String idealDate = splitter[2] + "-" + splitter[1] + "-" + splitter[0];
            if (taskDates.contains(idealDate)){
                streakInt++;
            } else {
                streakInt = 0;
            }
        }
        return streakInt;
    }

    private int calculatePercentage(ArrayList<String> idealDates, ArrayList<String> taskDates){
        int percentage = 0;
        float divideBy = idealDates.size();
        float divide = taskDates.size();
        float result = (divide / divideBy) * 100;
        percentage = Math.round(result);
        return percentage;
    }

    private void updateTaskFile(File dir, String output){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("DisplayedHabits.txt")){
                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(output.getBytes(StandardCharsets.UTF_8));
                        fos.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
    }

    private void loadSelectedHabitsFromFileToVar(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("DisplayedHabits.txt")){
                    loadedHabits.clear();
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            loadedHabits.add(line);
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

    private int getTaskPoints(File dir, String taskName){
        int taskPoints = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskName +"Pointcard.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (!line.matches("Points =")){
                                String[] value = line.split("=");
                                taskPoints = Integer.parseInt(value[1]);
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
        return taskPoints;
    }

    private int getcurrentPointsAllTime(File dir){
        int oldPoints = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("AllTimeScore.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Points =.*")){
                                String[] value = line.split("=");
                                oldPoints = Integer.parseInt(value[1]);
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
        return oldPoints;
    }

    private int getcurrentPointsMonth(File dir){
        int oldPoints = 0;
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
                            if (line.matches("Points =.*")){
                                String[] value = line.split("=");
                                oldPoints = Integer.parseInt(value[1]);
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
        return oldPoints;
    }

    private int getcurrentPointsWeek(File dir){
        int oldPoints = 0;
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
                            if (line.matches("Points =.*")){
                                String[] value = line.split("=");
                                oldPoints = Integer.parseInt(value[1]);
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
        return oldPoints;
    }
}