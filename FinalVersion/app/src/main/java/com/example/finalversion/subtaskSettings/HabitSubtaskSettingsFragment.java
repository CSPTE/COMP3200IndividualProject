package com.example.finalversion.subtaskSettings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.finalversion.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HabitSubtaskSettingsFragment extends Fragment {
    private Context cont;
    private String parent;
    private ArrayList<String> habitDays = new ArrayList<String>();
    private String habitDaysString = null;
    private ArrayList<String> subtaskHabitDays = new ArrayList<String>();
    private String subtaskHabitDaysString = null;

    public HabitSubtaskSettingsFragment() {
    }

    public static HabitSubtaskSettingsFragment newInstance() {
        HabitSubtaskSettingsFragment fragment = new HabitSubtaskSettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        cont = this.getContext();
        return inflater.inflate(R.layout.fragment_habit_subtask_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        parent = HabitSubtaskSettingsFragmentArgs.fromBundle(getArguments()).getHabitTaskParentName();

        //Day Selector
        Button bOpenAlertDialog = view.findViewById(R.id.openAlertDialogButtonSubtask);
        final TextView tvSelectedItemsPreview = view.findViewById(R.id.selectedItemPreviewSubtask);
        final String[] listItems = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        final boolean[] checkedItems = new boolean[listItems.length];
        final List<String> selectedItems = Arrays.asList(listItems);

        bOpenAlertDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvSelectedItemsPreview.setText(null);
                AlertDialog.Builder builder = new AlertDialog.Builder(cont);
                builder.setTitle("Select Days");
                builder.setIcon(R.drawable.ic_menu_calendar);

                builder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItems[which] = isChecked;
                        String currentItem = selectedItems.get(which);
                    }
                });
                builder.setCancelable(false);

                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < checkedItems.length; i++) {
                            if (checkedItems[i]) {
                                tvSelectedItemsPreview.setText(tvSelectedItemsPreview.getText() + selectedItems.get(i) + ", ");
                            }
                        }
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setNeutralButton("CLEAR ALL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < checkedItems.length; i++) {
                            checkedItems[i] = false;
                        }
                    }
                });

                builder.create();
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        //Done Button
        EditText nameEditText = view.findViewById(R.id.habitSubTaskNameInput);
        EditText incrementEditText = view.findViewById(R.id.habitSubTaskIncrementInput);
        TextView selectedDaysTextView = view.findViewById(R.id.selectedItemPreviewSubtask);
        Button finishTask = view.findViewById(R.id.add_habit_subtask);

        finishTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String taskName = String.valueOf(nameEditText.getText());
                String taskIncrementString = String.valueOf(incrementEditText.getText());
                int taskIncrement;
                String taskHabitDays = String.valueOf(selectedDaysTextView.getText());
                StringBuilder blanks = new StringBuilder();

                getParentDays(cont.getFilesDir());

                //Make some fields mandatory and correct formatting
                if (taskName.equals("")) {blanks.append("Task Name \n");}
                if (taskIncrementString.equals("")) {blanks.append("Number Of Increments \n");
                } else if (isInteger(taskIncrementString) == false) {blanks.append("Number Of Increments is not a number \n");
                } else {
                    taskIncrement = Integer.valueOf(taskIncrementString);
                    if (taskIncrement <= 0) {blanks.append("Number Of Increments is less than 1 \n");}
                }
                if (taskHabitDays.equals("")){
                    subtaskHabitDaysString = habitDaysString;
                } else {
                    subtaskHabitDaysString = taskHabitDays;
                    daysToArrayList();
                    boolean allPresent = true;
                    for (String subtaskDay : subtaskHabitDays){
                        boolean isPresent = false;
                        for (String day : habitDays){
                            if (subtaskDay.equals(day)){
                                isPresent = true;
                            }
                        }
                        if (!isPresent){
                            allPresent = false;
                        }
                    }
                    if (!allPresent){
                        blanks.append("Days Selected Not a Subset of Main Task \n");
                    }
                }

                if (blanks.toString().equals("")){
                    String fileName = taskName + parent + "Subtazk";
                    StringBuilder fileBody = new StringBuilder();
                    fileBody.append("Task name =" + taskName + "\n");
                    fileBody.append("Number Of Increments =0/" + taskIncrementString + "\n");
                    fileBody.append("Habit Days =" + subtaskHabitDaysString + "\n");
                    fileBody.append("Last Completed =");
                    String fileBodyString = fileBody.toString();

                    File subTaskFile = new File(cont.getFilesDir(), fileName);
                    if (!subTaskFile.exists()) {
                        try (FileOutputStream fos = cont.openFileOutput(fileName, Context.MODE_PRIVATE)) {
                            fos.write(fileBodyString.getBytes(StandardCharsets.UTF_8));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        NavHostFragment.findNavController(HabitSubtaskSettingsFragment.this)
                                .navigate(R.id.action_habitSubtaskSettingsFragment_to_nav_home);
                    } else {
                        AlertDialog.Builder fileAlreadyExsists = new AlertDialog.Builder(cont);
                        fileAlreadyExsists.setTitle("Task Already Exists");
                        fileAlreadyExsists.setMessage("A task with this name already exists. Please rename the task or delete the existing task");
                        fileAlreadyExsists.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        fileAlreadyExsists.create();
                        AlertDialog alertDialog = fileAlreadyExsists.create();
                        alertDialog.show();
                    }

                } else {
                    AlertDialog.Builder warning = new AlertDialog.Builder(cont);
                    warning.setTitle("Please fill out the following fields:");
                    warning.setMessage(blanks);
                    warning.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    warning.create();
                    AlertDialog alertDialog = warning.create();
                    alertDialog.show();
                }
            }
        });
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    private void getParentDays(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(parent +"Task.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Habit Days =.*")){
                                    String[] value = line.split("=");
                                    habitDaysString = value[1];
                                    String[] days = value[1].split(", ");
                                    for (String day : days){
                                        habitDays.add(day);
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
    }

    private void daysToArrayList(){
        subtaskHabitDays.clear();
        String[] days = subtaskHabitDaysString.split(", ");
        for (String day : days){
            subtaskHabitDays.add(day);
        }
    }
}