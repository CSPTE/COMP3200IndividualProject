package com.example.finalversion.taskSettings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.finalversion.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class BlankTaskSettingsFragment extends Fragment {

    private BlankTaskSettingsViewModel mViewModel;
    private ColorObject selectedColor;
    private Context cont;

    public static BlankTaskSettingsFragment newInstance() {
        return new BlankTaskSettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        cont = this.getContext();
        return inflater.inflate(R.layout.fragment_blank_task_settings, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(BlankTaskSettingsViewModel.class);
        //Use the ViewModel
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //ColorSpinner
        ColorList cl = new ColorList();
        selectedColor = cl.getDefaultColor();
        Spinner spinner = (Spinner) view.findViewById(R.id.colorSpinner);
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


        //Day Selector
        Button bOpenAlertDialog = view.findViewById(R.id.openAlertDialogButton);
        final TextView tvSelectedItemsPreview = view.findViewById(R.id.selectedItemPreview);
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
        EditText nameEditText = view.findViewById(R.id.taskNameInput);
        EditText incrementEditText = view.findViewById(R.id.taskIncrementInput);
        Button finishTask = view.findViewById(R.id.add_task);
        SwitchCompat habitSwitch = view.findViewById(R.id.habit_switch);
        TextView selectedDaysTextView = view.findViewById(R.id.selectedItemPreview);
        //SwitchCompat notificationSwitch = view.findViewById(R.id.notification_switch);
        //EditText notificationTimeEditText = view.findViewById(R.id.notification_time);

        finishTask.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                String taskName = String.valueOf(nameEditText.getText());
                String taskIncrementString = String.valueOf(incrementEditText.getText());
                int taskIncrement;
                String taskColor = selectedColor.getHex();
                boolean taskHabitToggle = habitSwitch.isChecked();
                String taskHabitDays = String.valueOf(selectedDaysTextView.getText());
                //boolean taskNotificationToggle = notificationSwitch.isChecked();
                //String taskNotificationTime = String.valueOf(notificationTimeEditText.getText());
                StringBuilder blanks = new StringBuilder();

                //Make some fields mandatory and correct formatting
                if (taskName.equals("")) {blanks.append("Task Name \n");}
                if (taskIncrementString.equals("")) {blanks.append("Number Of Increments \n");
                } else if (isInteger(taskIncrementString) == false) {blanks.append("Number Of Increments is not a number \n");
                } else {
                    taskIncrement = Integer.valueOf(taskIncrementString);
                    if (taskIncrement <= 0) {blanks.append("Number Of Increments is less than 1 \n");}
                }
                if (taskHabitToggle == true) {
                    if (taskHabitDays.equals("")){blanks.append("Select Days \n");}
                }
                /*
                if (taskNotificationToggle == true) {
                    if (taskNotificationTime.equals("")){blanks.append("Time Of Notification \n");
                    } else if (!(taskNotificationTime.matches("[0-9](:)[0-5][0-9]") ||
                            (taskNotificationTime.matches("[1][0-9](:)[0-5][0-9]")) ||
                            (taskNotificationTime.matches("[2][0-3](:)[0-5][0-9]")))){
                        blanks.append("Time Of Notification invalid \n");
                    }
                }
                 */

                if (blanks.toString().equals("")){
                    //TODO: Quick lookup for naming scheme
                    //Task Naming scheme: WorkoutTask.txt
                    //Subtask Naming scheme: WorkoutSitupsSubtazk.txt
                    //CalendarSelectedHabits: TrackedHabits.txt
                    //LogFile Naming scheme: WorkoutLog.txt
                    //FutureTask Naming scheme: WorkoutFuturetakk.txt

                    String fileName = taskName + "Task";
                    StringBuilder fileBody = new StringBuilder();
                    fileBody.append("Task name =" + taskName + "\n");
                    fileBody.append("Number Of Increments =0/" + taskIncrementString + "\n");
                    fileBody.append("Color Selected =" + taskColor + "\n");
                    fileBody.append("Habit Toggled =" + taskHabitToggle + "\n");
                    fileBody.append("Habit Days =" + taskHabitDays + "\n");
                    //fileBody.append("Notification Toggled =" + taskNotificationToggle + "\n");
                    //fileBody.append("Notification Time =" + taskNotificationTime + "\n");
                    fileBody.append("Last Completed =");
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

                        if(taskHabitToggle){
                            String logFileName = taskName + "Log";
                            String logFileBody = "FullLog";
                            try (FileOutputStream fosl = cont.openFileOutput(logFileName, Context.MODE_PRIVATE)) {
                                fosl.write(logFileBody.getBytes(StandardCharsets.UTF_8));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        NavHostFragment.findNavController(BlankTaskSettingsFragment.this)
                                .navigate(R.id.action_blankTaskSettingsFragment_to_nav_home);
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