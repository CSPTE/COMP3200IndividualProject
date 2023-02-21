package com.example.finalversion.ui.slideshow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalversion.R;
import com.example.finalversion.databinding.FragmentSlideshowBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SlideshowFragment extends Fragment implements LeaderboardAdapter.OnItemListener {

    private FragmentSlideshowBinding binding;
    private Context leaderboardContext;

    private Button selectLeaderboardButton;
    private RecyclerView leaderboardRecyclerView;
    public TextView currentlyDisplayingTextView;
    private Button loadPrevious10;
    private Button loadNext10;
    private Button resetWeeklyButton;
    private Button resetMonthlyButton;

    private int weeklyScoreLocal;
    private int monthlyScoreLocal;
    private int alltimeScoreLocal;
    private String usernameLocal;

    private String currentlyDisplayingText = "1-10";
    private String currentLeaderboard = "Weekly";

    private ArrayList<LeaderboardComponent> displayedUsers = new ArrayList<LeaderboardComponent>();
    private ArrayList<LeaderboardComponent> weeklyRanked = new ArrayList<LeaderboardComponent>();
    private ArrayList<LeaderboardComponent> monthlyRanked = new ArrayList<LeaderboardComponent>();
    private ArrayList<LeaderboardComponent> alltimeRanked = new ArrayList<LeaderboardComponent>();

    private boolean query = false;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        leaderboardContext = this.getContext();

        //final TextView textView = binding.textSlideshow;
        //slideshowViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Send Score
        FirebaseApp.initializeApp(leaderboardContext);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        getAllLocalScores(leaderboardContext.getFilesDir());
        sendData(db);

        //Connect to UI
        selectLeaderboardButton = view.findViewById(R.id.selectLeaderboard);
        leaderboardRecyclerView = view.findViewById(R.id.leaderboardRecyclerView);
        currentlyDisplayingTextView = view.findViewById(R.id.currentlyDisplayingLeaderboard);
        loadPrevious10 = view.findViewById(R.id.loadPreviousLeaderboard);
        loadNext10 = view.findViewById(R.id.leadNextLeaderboard);
        //resetWeeklyButton = view.findViewById(R.id.leaderboard_reset_weekly);
        //resetMonthlyButton = view.findViewById(R.id.leaderboard_reset_monthly);
        final TextView tvSelectedItemsPreview = view.findViewById(R.id.selectedHabitsPreviewLeaderboard);

        //Default Load
        tvSelectedItemsPreview.setText("Displaying Weekly Leaderboard");

        //Leaderboard Navigation
        loadPrevious10.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if (query == true){
                    String[] splitter = currentlyDisplayingText.split("-");
                    if (Integer.parseInt(splitter[0]) >= 11){
                        int newStart = Integer.parseInt(splitter[0]) - 10;
                        int newEnd = Integer.parseInt(splitter[1]) - 10;
                        currentlyDisplayingText = newStart + "-" + newEnd;
                        setLeaderboard(currentLeaderboard, currentlyDisplayingText);
                    }
                } else {
                    AlertDialog.Builder fileAlreadyExsists = new AlertDialog.Builder(leaderboardContext);
                    fileAlreadyExsists.setTitle("Please wait");
                    fileAlreadyExsists.setMessage("We are fetching the leaderboard data. Please wait until it is finished");
                    fileAlreadyExsists.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    fileAlreadyExsists.create();
                    AlertDialog alertDialog = fileAlreadyExsists.create();
                    alertDialog.show();
                }
            }
        });
        loadNext10.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                if (query == true){
                    String[] splitter = currentlyDisplayingText.split("-");
                    if (Integer.parseInt(splitter[1]) <= displayedUsers.size()){
                        int newStart = Integer.parseInt(splitter[0]) + 10;
                        int newEnd = Integer.parseInt(splitter[1]) + 10;
                        currentlyDisplayingText = newStart + "-" + newEnd;
                        setLeaderboard(currentLeaderboard, currentlyDisplayingText);
                    }
                } else {
                    AlertDialog.Builder fileAlreadyExsists = new AlertDialog.Builder(leaderboardContext);
                    fileAlreadyExsists.setTitle("Please wait");
                    fileAlreadyExsists.setMessage("We are fetching the leaderboard data. Please wait until it is finished");
                    fileAlreadyExsists.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    fileAlreadyExsists.create();
                    AlertDialog alertDialog = fileAlreadyExsists.create();
                    alertDialog.show();
                }
            }
        });

        //Select Table
        final String[] listItems = new String[]{"Weekly Leaderboard", "Monthly Leaderboard", "All-Time Leaderboard"};
        final AtomicInteger checkedItem = new AtomicInteger(0);
        selectLeaderboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (query == true){
                    tvSelectedItemsPreview.setText(null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(leaderboardContext);
                    builder.setTitle("Select leaderboard to display");
                    builder.setIcon(R.mipmap.ic_launcher);
                    builder.setSingleChoiceItems(listItems, checkedItem.get(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkedItem.set(which);
                        }
                    });

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int selectedOptionIndex = checkedItem.get();
                            String selectedOption = listItems[selectedOptionIndex];

                            if (selectedOption.equals("Weekly Leaderboard")) {
                                tvSelectedItemsPreview.setText("Displaying Weekly Leaderboard");
                                currentLeaderboard = "Weekly";
                                currentlyDisplayingText = "1-10";
                                setLeaderboard(currentLeaderboard, currentlyDisplayingText);

                            } else if (selectedOption.equals("Monthly Leaderboard")) {
                                tvSelectedItemsPreview.setText("Displaying Monthly Leaderboard");
                                currentLeaderboard = "Monthly";
                                currentlyDisplayingText = "1-10";
                                setLeaderboard(currentLeaderboard, currentlyDisplayingText);

                            } else if (selectedOption.equals("All-Time Leaderboard")) {
                                tvSelectedItemsPreview.setText("Displaying All-Time Leaderboard");
                                currentLeaderboard = "AllTime";
                                currentlyDisplayingText = "1-10";
                                setLeaderboard(currentLeaderboard, currentlyDisplayingText);

                            }
                        }

                    });
                    builder.setNegativeButton("Cancel", null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    AlertDialog.Builder fileAlreadyExsists = new AlertDialog.Builder(leaderboardContext);
                    fileAlreadyExsists.setTitle("Please wait");
                    fileAlreadyExsists.setMessage("We are fetching the leaderboard data. Please wait until it is finished");
                    fileAlreadyExsists.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    fileAlreadyExsists.create();
                    AlertDialog alertDialog = fileAlreadyExsists.create();
                    alertDialog.show();
                }

            }
        });

        //Resetting Leaderboards (Admin)
        /*
        resetWeeklyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetWeekly(db);
            }
        });

        resetMonthlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetMonthly(db);
            }
        });
         */

    }

    private void resetMonthly(FirebaseFirestore db) {
        CollectionReference collectionRef = db.collection("scores");
        WriteBatch batch = db.batch();

        collectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    DocumentReference docRef = documentSnapshot.getReference();
                    batch.update(docRef, "monthlyScore", 0);
                }

                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Batch write completed
                    }
                });
            }
        });
    }

    private void resetWeekly(FirebaseFirestore db) {
        CollectionReference collectionRef = db.collection("scores");
        WriteBatch batch = db.batch();

        collectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    DocumentReference docRef = documentSnapshot.getReference();
                    batch.update(docRef, "weeklyScore", 0);
                }

                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Batch write completed
                    }
                });
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setLeaderboard(String whichLeaderboard, String frame){
        if (currentLeaderboard.equals("Weekly")){
            displayedUsers = weeklyRanked;
        } else if (currentLeaderboard.equals("Monthly")){
            displayedUsers = monthlyRanked;
        } else if (currentLeaderboard.equals("AllTime")){
            displayedUsers = alltimeRanked;
        }

        //initTestComponents();

        currentlyDisplayingTextView.setText(frame);
        String[] splitter = frame.split("-");
        int start = Integer.parseInt(splitter[0]);
        int end = Integer.parseInt(splitter[1]);
        ArrayList<LeaderboardComponent> displayedNow = new ArrayList<LeaderboardComponent>();
        while (start <= end){
            if (displayedUsers.size() >= start){
                displayedNow.add(displayedUsers.get(start - 1));
            }
            start++;
        }

        LeaderboardAdapter leaderboardAdapter = new LeaderboardAdapter(displayedNow, this, leaderboardContext, currentLeaderboard);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(leaderboardContext.getApplicationContext(), 1);
        leaderboardRecyclerView.setLayoutManager(layoutManager);
        leaderboardRecyclerView.setAdapter(leaderboardAdapter);
    }

    @Override
    public void onItemClick(int position, String dayText) {
        //String message = "View the profile of " + dayText + ". Coming soon!";
        //Toast.makeText(leaderboardContext, message, Toast.LENGTH_LONG).show();
        SlideshowFragmentDirections.ActionNavSlideshowToProfileLBFragment action = SlideshowFragmentDirections.actionNavSlideshowToProfileLBFragment(dayText);
        NavHostFragment.findNavController(SlideshowFragment.this).navigate(action);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getData(FirebaseFirestore db){
        List<User> users = new ArrayList<>();
        displayedUsers.clear();
        weeklyRanked.clear();
        monthlyRanked.clear();
        alltimeRanked.clear();
        // Get all documents from the collection
        db.collection("scores")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            //scores.add(documentSnapshot.getData());
                            User user = new User(documentSnapshot.getString("username"), documentSnapshot.getLong("weeklyScore"), documentSnapshot.getLong("monthlyScore"), documentSnapshot.getLong("alltimeScore"));
                            users.add(user);
                        }

                        List<User> weeklySortedUsers = new ArrayList<>(users);
                        Collections.sort(weeklySortedUsers, new WeeklyScoreComparator());

                        List<User> monthlySortedUsers = new ArrayList<>(users);
                        Collections.sort(monthlySortedUsers, new MonthlyScoreComparator());

                        List<User> alltimeSortedUsers = new ArrayList<>(users);
                        Collections.sort(alltimeSortedUsers, new AlltimeScoreComparator());

                        for (User user : users) {
                            int weeklyPosition = Collections.binarySearch(weeklySortedUsers, user, new WeeklyScoreComparator()) + 1;
                            int monthlyPosition = Collections.binarySearch(monthlySortedUsers, user, new MonthlyScoreComparator()) + 1;
                            int alltimePosition = Collections.binarySearch(alltimeSortedUsers, user, new AlltimeScoreComparator()) + 1;

                            if (weeklyPosition < 0) {
                                weeklyPosition = -(weeklyPosition + 1);
                            }

                            if (monthlyPosition < 0) {
                                monthlyPosition = -(monthlyPosition + 1);
                            }

                            if (alltimePosition < 0) {
                                alltimePosition = -(alltimePosition + 1);
                            }

                            LeaderboardComponent leaderboardComponent = new LeaderboardComponent(String.valueOf(weeklyPosition), String.valueOf(monthlyPosition), String.valueOf(alltimePosition), user.getUsername(), user.getWeeklyScore(), user.getMonthlyScore(), user.getAlltimeScore());
                            //displayedUsers.add(leaderboardComponent);
                            weeklyRanked.add(leaderboardComponent);
                            monthlyRanked.add(leaderboardComponent);
                            alltimeRanked.add(leaderboardComponent);
                            String temp = "test";
                        }

                        /*
                        for (LeaderboardComponent lc : displayedUsers) {
                            weeklyRanked.add(lc);
                            monthlyRanked.add(lc);
                            alltimeRanked.add(lc);
                        }

                         */



                        Collections.sort(weeklyRanked, new WeeklyScoreComparatorLB());
                        Collections.sort(monthlyRanked, new MonthlyScoreComparatorLB());
                        Collections.sort(alltimeRanked, new AlltimeScoreComparatorLB());

                        int previousScoreWeekly = 1;
                        int iWeekly = 0;
                        int countWeekly = 0;
                        for (LeaderboardComponent lc : weeklyRanked){
                            if (iWeekly == 0){
                                iWeekly++;
                                lc.setWeeklyRank("#" + String.valueOf(iWeekly));
                                countWeekly++;
                                previousScoreWeekly = Integer.parseInt(lc.getWeeklyScore());
                            } else {
                                if (Integer.parseInt(lc.getWeeklyScore()) == previousScoreWeekly){
                                    lc.setWeeklyRank("#" + String.valueOf(iWeekly));
                                    countWeekly++;
                                } else {
                                    countWeekly++;
                                    iWeekly = countWeekly;
                                    lc.setWeeklyRank("#" + String.valueOf(iWeekly));
                                    previousScoreWeekly = Integer.parseInt(lc.getWeeklyScore());
                                }
                            }
                        }

                        int previousScoreMonthly = 1;
                        int iMonthly = 0;
                        int countMonthly = 0;
                        for (LeaderboardComponent lc : monthlyRanked){
                            if (iMonthly == 0){
                                iMonthly++;
                                lc.setMonthlyRank("#" + String.valueOf(iMonthly));
                                countMonthly++;
                                previousScoreMonthly = Integer.parseInt(lc.getMonthlyScore());
                            } else {
                                if (Integer.parseInt(lc.getMonthlyScore()) == previousScoreMonthly){
                                    lc.setMonthlyRank("#" + String.valueOf(iMonthly));
                                    countMonthly++;
                                } else {
                                    countMonthly++;
                                    iMonthly = countMonthly;
                                    lc.setMonthlyRank("#" + String.valueOf(iMonthly));
                                    previousScoreMonthly = Integer.parseInt(lc.getMonthlyScore());
                                }
                            }
                        }

                        int previousScoreAllTime = 1;
                        int iAllTime = 0;
                        int countAllTime = 0;
                        for (LeaderboardComponent lc : alltimeRanked){
                            if (iAllTime == 0){
                                iAllTime++;
                                lc.setAllTimeRank("#" + String.valueOf(iAllTime));
                                countAllTime++;
                                previousScoreAllTime = Integer.parseInt(lc.getAllTimeScore());
                            } else {
                                if (Integer.parseInt(lc.getAllTimeScore()) == previousScoreAllTime){
                                    lc.setAllTimeRank("#" + String.valueOf(iAllTime));
                                    countAllTime++;
                                } else {
                                    countAllTime++;
                                    iAllTime = countAllTime;
                                    lc.setAllTimeRank("#" + String.valueOf(iAllTime));
                                    previousScoreAllTime = Integer.parseInt(lc.getAllTimeScore());
                                }
                            }
                        }
                        query = true;
                        String temp = "test";
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String temp = "test";
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        setLeaderboard(currentLeaderboard, currentlyDisplayingText);
                    }
                });
    }

    private void sendData(FirebaseFirestore db){
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
                        getData(db);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String temp2 = "korte";
                    }
                });

        DocumentReference documentRef = db.collection("profiletable").document(usernameLocal);

        documentRef.update("weekly score", weeklyScoreLocal, "monthly score", monthlyScoreLocal, "alltime score", alltimeScoreLocal)
                .addOnSuccessListener(aVoid -> {
                    // Update successful
                })
                .addOnFailureListener(e -> {
                    // Update failed
                });
    }

    private void getAllUsersTest() {
        for(int i = 1; i <= 21; i++) {
            //LeaderboardComponent lc = new LeaderboardComponent(Integer.toString(i), "TheCaptain", i*100);
            //displayedUsers.add(lc);
            String temp = "test";
        }
    }

    private void initTestComponents(){
        /*
        //WeeklyHash - Increase
        weeklyHash.put("Alma", 100);
        weeklyHash.put("Banan", 200);
        weeklyHash.put("Citrom", 300);
        weeklyHash.put("Dio", 400);
        weeklyHash.put("Eper", 500);
        weeklyHash.put("Fuge", 600);
        weeklyHash.put("Grepfrut", 700);
        weeklyHash.put("Homoktovis", 800);
        weeklyHash.put("Idared", 900);
        weeklyHash.put("Josta", 1000);
        weeklyHash.put("Kokeny", 1100);
        weeklyHash.put("Licsi", 1200);
        weeklyHash.put("Malna", 1300);
        weeklyHash.put("Narancs", 1400);
        weeklyHash.put("Oszibarack", 1500);
        weeklyHash.put("Papaja", 1600);
        weeklyHash.put("Ribiszke", 1700);
        weeklyHash.put("Sargadinnye", 1800);
        weeklyHash.put("Tojasdinnye", 1900);
        weeklyHash.put("Uborka", 2000);
        weeklyHash.put("Vadcseresznye", 2100);

        //MonthlyHash - Decrease
        int i = 2100;
        for(String gyumolcs : weeklyHash.keySet()){
            monthlyHash.put(gyumolcs, i);
            i = i - 100;
        }

        //AllTimeHash - Equal
        for(String gyumolcs : weeklyHash.keySet()){
            alltimeHash.put(gyumolcs, 1000);
        }
         */
    }

    private void getAllLocalScores(File dir){
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
                                alltimeScoreLocal = Integer.parseInt(value[1]);
                            }
                            line = br.readLine();
                        }
                        br.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                } else if (fileName.matches("MonthlyScore.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Points =.*")){
                                String[] value = line.split("=");
                                monthlyScoreLocal = Integer.parseInt(value[1]);
                            }
                            line = br.readLine();
                        }
                        br.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                } else if (fileName.matches("WeeklyScore.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Points =.*")){
                                String[] value = line.split("=");
                                weeklyScoreLocal = Integer.parseInt(value[1]);
                            }
                            line = br.readLine();
                        }
                        br.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                } else if (fileName.matches("username.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            usernameLocal = line;
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