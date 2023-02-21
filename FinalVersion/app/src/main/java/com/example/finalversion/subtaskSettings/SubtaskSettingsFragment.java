package com.example.finalversion.subtaskSettings;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.finalversion.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SubtaskSettingsFragment extends Fragment {
    private Context cont;
    private String parent;

    private boolean edit;
    private String previousName;
    private String unacceptableWord = "honorificabilitudinitatibusz";

    public SubtaskSettingsFragment() {
    }

    public static SubtaskSettingsFragment newInstance() {
        SubtaskSettingsFragment fragment = new SubtaskSettingsFragment();
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
        return inflater.inflate(R.layout.fragment_subtask_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        parent = SubtaskSettingsFragmentArgs.fromBundle(getArguments()).getTaskParentName();
        edit = SubtaskSettingsFragmentArgs.fromBundle(getArguments()).getEdit();
        previousName = SubtaskSettingsFragmentArgs.fromBundle(getArguments()).getPreviousName();

        //Done Button
        EditText nameEditText = view.findViewById(R.id.subTaskNameInput);
        EditText incrementEditText = view.findViewById(R.id.subTaskIncrementInput);
        Button finishTask = view.findViewById(R.id.add_subtask);
        if (!previousName.equals("honorificabilitudinitatibusz")){
            nameEditText.setText(previousName);
        }

        finishTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String taskName = String.valueOf(nameEditText.getText());
                String taskIncrementString = String.valueOf(incrementEditText.getText());
                int taskIncrement;
                StringBuilder blanks = new StringBuilder();

                //check for symbols
                if (taskName.matches(".*\\W.*")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(cont);
                    builder.setMessage("Symbols are not allowed in the input.")
                            .setPositiveButton("OK", null)
                            .create()
                            .show();
                } else {

                //Make some fields mandatory and correct formatting
                if (taskName.equals("")) {blanks.append("Task Name \n");}
                if (taskIncrementString.equals("")) {blanks.append("Number Of Increments \n");
                } else if (isInteger(taskIncrementString) == false) {blanks.append("Number Of Increments is not a number \n");
                } else {
                    taskIncrement = Integer.valueOf(taskIncrementString);
                    if (taskIncrement <= 0) {blanks.append("Number Of Increments is less than 1 \n");}
                }

                if (blanks.toString().equals("")){
                    String fileName = taskName + parent + "Subtazk";
                    StringBuilder fileBody = new StringBuilder();
                    fileBody.append("Task name =" + taskName + "\n");
                    fileBody.append("Number Of Increments =0/" + taskIncrementString + "\n");
                    fileBody.append("Last Completed =");
                    String fileBodyString = fileBody.toString();

                    File subTaskFile = new File(cont.getFilesDir(), fileName);
                    if ((!subTaskFile.exists()) || (previousName.equals(taskName))) {
                        try (FileOutputStream fos = cont.openFileOutput(fileName, Context.MODE_PRIVATE)) {
                            fos.write(fileBodyString.getBytes(StandardCharsets.UTF_8));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if((!previousName.equals("honorificabilitudinitatibusz")) && (!previousName.equals(taskName))){
                            deleteTask(cont.getFilesDir(), previousName);
                        }

                        NavHostFragment.findNavController(SubtaskSettingsFragment.this)
                                .navigate(R.id.action_subtaskSettingsFragment_to_nav_home);
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
                }}
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

    private void deleteTask(File dir, String taskName){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskName + parent +"Subtazk.txt")){
                    boolean isItDeleted = file.delete();
                }
            }
        }
    }
}