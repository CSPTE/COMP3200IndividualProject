package com.example.finalversion.futuretaskSettings;

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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.finalversion.R;
import com.example.finalversion.taskSettings.ColorList;
import com.example.finalversion.taskSettings.ColorObject;
import com.example.finalversion.taskSettings.ColorSpinnerAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FuturetaskSettingsFragment extends Fragment {
    private Context cont;
    private ColorObject selectedColor;
    private String dateToShow;

    public FuturetaskSettingsFragment() {
    }

    public static FuturetaskSettingsFragment newInstance() {
        return new FuturetaskSettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        cont = this.getContext();
        return inflater.inflate(R.layout.fragment_futuretask_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dateToShow = FuturetaskSettingsFragmentArgs.fromBundle(getArguments()).getDateOfTask();

        //ColorSpinner
        ColorList cl = new ColorList();
        selectedColor = cl.getDefaultColor();
        Spinner spinner = (Spinner) view.findViewById(R.id.colorSpinnerFutureTask);
        ColorSpinnerAdapter customAdapter = new ColorSpinnerAdapter(this.getContext(), cl.basicColors());
        spinner.setAdapter(customAdapter);
        spinner.setSelection(cl.colorPosition(selectedColor),false);
        AdapterView.OnItemSelectedListener selectedItemListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedColor = cl.basicColors().get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
        spinner.setOnItemSelectedListener(selectedItemListener);

        //Done Button
        EditText nameEditText = view.findViewById(R.id.taskNameInputFutureTask);
        EditText incrementEditText = view.findViewById(R.id.taskIncrementInputFutureTask);
        Button finishTask = view.findViewById(R.id.add_taskFutureTask);

        finishTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String taskName = String.valueOf(nameEditText.getText());
                String taskIncrementString = String.valueOf(incrementEditText.getText());
                int taskIncrement;
                String taskColor = selectedColor.getHex();
                StringBuilder blanks = new StringBuilder();

                //Make some fields mandatory and correct formatting
                if (taskName.equals("")) {blanks.append("Task Name \n");}
                if (taskIncrementString.equals("")) {blanks.append("Number Of Increments \n");
                } else if (isInteger(taskIncrementString) == false) {blanks.append("Number Of Increments is not a number \n");
                } else {
                    taskIncrement = Integer.valueOf(taskIncrementString);
                    if (taskIncrement <= 0) {blanks.append("Number Of Increments is less than 1 \n");}
                }

                if (blanks.toString().equals("")){
                    String fileName = taskName + "FutureTakk";
                    StringBuilder fileBody = new StringBuilder();
                    fileBody.append("Task name =" + taskName + "\n");
                    fileBody.append("Number Of Increments =0/" + taskIncrementString + "\n");
                    fileBody.append("Color Selected =" + taskColor + "\n");
                    fileBody.append("Date Of Task=" + dateToShow);
                    String fileBodyString = fileBody.toString();

                    File taskFile = new File(cont.getFilesDir(), fileName);
                    if (!taskFile.exists()) {
                        try (FileOutputStream fos = cont.openFileOutput(fileName, Context.MODE_PRIVATE)) {
                            fos.write(fileBodyString.getBytes(StandardCharsets.UTF_8));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        NavHostFragment.findNavController(FuturetaskSettingsFragment.this)
                                .navigate(R.id.action_futuretaskSettingsFragment_to_nav_gallery);
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
}