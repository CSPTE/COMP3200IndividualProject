package com.example.finalversion.ui.slideshow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalversion.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ProfileLBFragment extends Fragment {

    private Context profileLBContext;
    private String username;

    private Button addProfileButton;
    private Button messageButton;
    private LinearLayout backgroundPicture;
    private ImageView profilePic;
    private TextView usernameTextView;
    private TableLayout taskTable;
    private LinearLayout infoLayout;

    private ArrayList<TaskLB> taskData = new ArrayList<TaskLB>();
    private int weeklyScore;
    private int monthlyScore;
    private int alltimeScore;

    public static ProfileLBFragment newInstance() {
        return new ProfileLBFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        profileLBContext = this.getContext();
        return inflater.inflate(R.layout.fragment_profile_l_b, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        username = ProfileLBFragmentArgs.fromBundle(getArguments()).getUsername();

        addProfileButton = view.findViewById(R.id.profileLB_add_friend);
        messageButton = view.findViewById(R.id.profileLB_message);
        backgroundPicture = view.findViewById(R.id.profileLB_background);
        profilePic = view.findViewById(R.id.profileLB_pic);
        usernameTextView = view.findViewById(R.id.profileLB_username);
        infoLayout = view.findViewById(R.id.profileLB_point_information);
        taskTable = view.findViewById(R.id.table_layoutLB);

        //Add friend
        addProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = "Add " + username + " as friend. Coming soon!";
                Toast.makeText(profileLBContext, message, Toast.LENGTH_LONG).show();
            }
        });

        //Message user
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = "Message " + username + ". Coming soon!";
                Toast.makeText(profileLBContext, message, Toast.LENGTH_LONG).show();
            }
        });

        //Profile Info
        infoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder fileAlreadyExsists = new AlertDialog.Builder(profileLBContext);
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

        //TODO: Profile pic & background pic
        //Set username
        usernameTextView.setText(username);

        FirebaseApp.initializeApp(profileLBContext);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        getData(db);
        //setTable
    }

    private void setTable(){
        for (TaskLB t : taskData){
            setTableRow(t.getTask(), true, t.getStreak(), t.getPercentage(),t.getScore());
        }
        setTableFooter(alltimeScore, monthlyScore, weeklyScore);
    }

    private void getData(FirebaseFirestore db){
        // Get all documents from the collection
        db.collection("profiletable").document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Get the scores
                        alltimeScore = document.getLong("alltime score").intValue();
                        monthlyScore = document.getLong("monthly score").intValue();
                        weeklyScore = document.getLong("weekly score").intValue();

                        // Create a TaskLB object for each task
                        int taskCount = 0;
                        while (document.contains("task" + (taskCount + 1))) {
                            taskCount++;
                            TaskLB taskLB = new TaskLB();
                            taskLB.setStreak(document.getLong("streak" + taskCount).intValue());
                            taskLB.setPercentage(document.getLong("percentage" + taskCount).intValue());
                            taskLB.setScore(document.getLong("points" + taskCount).intValue());
                            taskLB.setTask(document.getString("task" + taskCount));
                            taskData.add(taskLB);
                            // Add the task to a list or store it in some other data structure
                        }
                        setTable();
                    } else {
                        // Handle the case where the document does not exist
                    }
                } else {
                    // Handle the case where the query failed
                }
            }
        });
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

}