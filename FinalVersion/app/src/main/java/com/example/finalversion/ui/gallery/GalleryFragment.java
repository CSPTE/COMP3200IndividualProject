package com.example.finalversion.ui.gallery;

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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.finalversion.databinding.FragmentGalleryBinding;
import com.example.finalversion.ui.home.HomeFragment;
import com.example.finalversion.ui.home.HomeFragmentDirections;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GalleryFragment extends Fragment implements CalendarAdapter.OnItemListener {

    private FragmentGalleryBinding binding;
    private Context galleryContext;
    ArrayList<String> habitualTasks = new ArrayList<String>();

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;

    private int numberOfSelected;
    private final String selectedTasksFileName = "TrackedHabits";
    private ArrayList<String> loadedHabits = new ArrayList<String>();
    private HashMap<String, ArrayList<String>> daysAndTasksOnThem = new HashMap<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        galleryContext = this.getContext();

        //final TextView textView = binding.textGallery;
        //galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Day Selector
        Button bOpenAlertDialog = view.findViewById(R.id.selectHabits);
        final TextView tvSelectedItemsPreview = view.findViewById(R.id.selectedHabitsPreview);

        //loads previously selected tasks
        loadSelectedHabitsFromFileToVar(galleryContext.getFilesDir());
        for (String hab : loadedHabits) {
                tvSelectedItemsPreview.setText(tvSelectedItemsPreview.getText() + hab + ", ");
        }

        //resume selection
        findHabitualTaskNames(galleryContext.getFilesDir());
        int numberOfChoices = habitualTasks.size();
        String[] listItems = new String[numberOfChoices];
        for (int i=0; i<numberOfChoices; i++){
            listItems[i] = habitualTasks.get(i);
        }
        final boolean[] checkedItems = new boolean[listItems.length];
        final List<String> selectedItems = Arrays.asList(listItems);

        bOpenAlertDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvSelectedItemsPreview.setText(null);
                AlertDialog.Builder builder = new AlertDialog.Builder(galleryContext);
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
                        numberOfSelected = 0;
                        StringBuilder tasksToWrite = new StringBuilder();
                        for (int i = 0; i < checkedItems.length; i++) {
                            if (checkedItems[i]) {
                                numberOfSelected++;
                                //String temp = selectedItems.get(i);
                                tasksToWrite.append(selectedItems.get(i) + "\n");
                            }
                        }
                        String fileBodyString = tasksToWrite.toString();

                        if (numberOfSelected < 5){
                            for (int i = 0; i < checkedItems.length; i++) {
                                if (checkedItems[i]) {
                                    tvSelectedItemsPreview.setText(tvSelectedItemsPreview.getText() + selectedItems.get(i) + ", ");
                                }
                            }
                            //makes or updates files to store selected habits
                            File taskFile = new File(galleryContext.getFilesDir(), selectedTasksFileName);
                            if (!taskFile.exists()) {
                                try (FileOutputStream fos = galleryContext.openFileOutput(selectedTasksFileName, Context.MODE_PRIVATE)) {
                                    fos.write(fileBodyString.getBytes(StandardCharsets.UTF_8));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                updateTaskFile(galleryContext.getFilesDir(), fileBodyString);
                                loadSelectedHabitsFromFileToVar(galleryContext.getFilesDir());
                            }

                            //Refresh, incredibly inefficiently the app
                            initDaysAndTasksOnThem();
                            for (String task : loadedHabits){
                                findDaysForTheTasks(galleryContext.getFilesDir(), task);
                            }
                            selectedDate = selectedDate.minusMonths(1);
                            setMonthView();
                            initDaysAndTasksOnThem();
                            for (String task : loadedHabits){
                                findDaysForTheTasks(galleryContext.getFilesDir(), task);
                            }
                            selectedDate = selectedDate.plusMonths(1);
                            setMonthView();

                        } else {
                            AlertDialog.Builder tooManyTasks = new AlertDialog.Builder(galleryContext);
                            tooManyTasks.setTitle("Too Many Tasks");
                            tooManyTasks.setMessage("You selected too many tasks. Select up to 4");
                            tooManyTasks.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            tooManyTasks.create();
                            AlertDialog alertDialog = tooManyTasks.create();
                            alertDialog.show();
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

        //Calendar
        //get what day each task is on
        initDaysAndTasksOnThem();
        for (String task : loadedHabits){
            findDaysForTheTasks(galleryContext.getFilesDir(), task);
        }

        initWidgets(view);
        selectedDate = LocalDate.now();
        setMonthView();
        Button goBackAMonth = view.findViewById(R.id.previousMonth);
        goBackAMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initDaysAndTasksOnThem();
                for (String task : loadedHabits){
                    findDaysForTheTasks(galleryContext.getFilesDir(), task);
                }
                selectedDate = selectedDate.minusMonths(1);
                setMonthView();
            }
        });
        Button goForwardAMonth = view.findViewById(R.id.nextMonth);
        goForwardAMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initDaysAndTasksOnThem();
                for (String task : loadedHabits){
                    findDaysForTheTasks(galleryContext.getFilesDir(), task);
                }
                selectedDate = selectedDate.plusMonths(1);
                setMonthView();
            }
        });
    }

    private void findHabitualTaskNames(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(".*Task.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Habit Toggled =.*")){
                                String[] value = line.split("=");
                                boolean isHabit = Boolean.parseBoolean(value[1]);
                                if (isHabit){
                                    String taskToAdd = file.getName();
                                    String newString = taskToAdd.replace("Task", "");
                                    habitualTasks.add(newString);
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

    private void initWidgets(View view)
    {
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        monthYearText = view.findViewById(R.id.monthYearTV);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setMonthView()
    {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);
        String monthYearString = monthYearText.getText().toString();

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this, daysAndTasksOnThem, galleryContext, monthYearString);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(galleryContext.getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ArrayList<String> daysInMonthArray(LocalDate date)
    {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for(int i = 1; i <= 42; i++)
        {
            if(i <= dayOfWeek || i > daysInMonth + dayOfWeek)
            {
                daysInMonthArray.add("");
            }
            else
            {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return  daysInMonthArray;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String monthYearFromDate(LocalDate date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String monthFromDate(LocalDate date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M");
        return date.format(formatter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String yearFromDate(LocalDate date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        return date.format(formatter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onItemClick(int position, String dayText, LinearLayout calendarCircle, HashMap<String, String> habitColors)
    {
        //check if day exits
        if(!dayText.equals(""))
        {
            //check if day in past or future
            SimpleDateFormat day = new SimpleDateFormat("dd");
            Date date = new Date();
            String currentDay = day.format(date);
            SimpleDateFormat month = new SimpleDateFormat("M");
            Date date2 = new Date();
            String currentMonth = month.format(date2);
            SimpleDateFormat year = new SimpleDateFormat("yyyy");
            Date date3 = new Date();
            String currentYear = year.format(date3);
            if((Integer.parseInt(yearFromDate(selectedDate)) < Integer.parseInt(currentYear)) ||
                    ((Integer.parseInt(yearFromDate(selectedDate)) == Integer.parseInt(currentYear)) && (Integer.parseInt(monthFromDate(selectedDate)) < Integer.parseInt(currentMonth))) ||
                    ((Integer.parseInt(yearFromDate(selectedDate)) == Integer.parseInt(currentYear)) && (Integer.parseInt(monthFromDate(selectedDate)) == Integer.parseInt(currentMonth)) && (Integer.parseInt(dayText) <= Integer.parseInt(currentDay)))) {

                //get tasks to show and nr of them
                ArrayList<String> tasksToDisplay;
                int taskCount;
                if(position % 7 == 0) {
                    //Its Sunday
                    tasksToDisplay = daysAndTasksOnThem.get("Sunday");
                    taskCount = tasksToDisplay.size();
                } else if(position % 7 == 1) {
                    //Its Monday
                    tasksToDisplay = daysAndTasksOnThem.get("Monday");
                    taskCount = tasksToDisplay.size();
                } else if(position % 7 == 2) {
                    //Its Tuesday
                    tasksToDisplay = daysAndTasksOnThem.get("Tuesday");
                    taskCount = tasksToDisplay.size();
                } else if(position % 7 == 3) {
                    //Its Wednesday
                    tasksToDisplay = daysAndTasksOnThem.get("Wednesday");
                    taskCount = tasksToDisplay.size();
                } else if(position % 7 == 4) {
                    //Its Thursday
                    tasksToDisplay = daysAndTasksOnThem.get("Thursday");
                    taskCount = tasksToDisplay.size();
                } else if(position % 7 == 5) {
                    //Its Friday
                    tasksToDisplay = daysAndTasksOnThem.get("Friday");
                    taskCount = tasksToDisplay.size();
                } else if(position % 7 == 6) {
                    //Its Saturday
                    tasksToDisplay = daysAndTasksOnThem.get("Saturday");
                    taskCount = tasksToDisplay.size();
                } else {
                    //Should never be used, here only so its initialised for sure
                    tasksToDisplay = daysAndTasksOnThem.get("Sunday");
                    taskCount = tasksToDisplay.size();
                }

                String[] listItems = new String[taskCount];
                for(int i = 0; i < taskCount; i++) {
                    listItems[i] = tasksToDisplay.get(i);
                }
                boolean[] checkedItems = new boolean[taskCount];
                ArrayList<String> colors = new ArrayList<String>();
                colors = getColorsIfCompleted(dayText, tasksToDisplay, habitColors);
                for(int i = 0; i < taskCount; i++) {
                    if (colors.get(i).length() == 7){
                        checkedItems[i] = true;
                    } else if (colors.get(i).length() == 9){
                        checkedItems[i] = false;
                    }
                }
                List<String> selectedItems = Arrays.asList(listItems);

                AlertDialog.Builder builder = new AlertDialog.Builder(galleryContext);
                builder.setTitle("Tasks on this day");
                builder.setIcon(R.drawable.ic_notification_icon);

                builder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        dialog.dismiss();
                    }
                });
                builder.setCancelable(false);
                builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create();
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                AlertDialog.Builder fileAlreadyExsists = new AlertDialog.Builder(galleryContext);
                fileAlreadyExsists.setTitle("Tasks planned for this day");
                fileAlreadyExsists.setIcon(R.drawable.ic_notification_icon);

                ArrayList<String> futures = new ArrayList<String>();
                String dateOfSelection = dayText + "-" + monthFromDate(selectedDate) + "-" + yearFromDate(selectedDate);
                getFutureTasks(galleryContext.getFilesDir(), futures, dateOfSelection);
                String[] listItems = new String[futures.size()];
                for(int i = 0; i < futures.size(); i++) {
                    listItems[i] = futures.get(i);
                }
                final boolean[] checkedItems = new boolean[listItems.length];
                final List<String> selectedItems = Arrays.asList(listItems);

                fileAlreadyExsists.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItems[which] = isChecked;
                        String currentItem = selectedItems.get(which);
                    }
                });
                fileAlreadyExsists.setCancelable(false);
                fileAlreadyExsists.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String sendText = dayText + "-" + monthFromDate(selectedDate) + "-" + yearFromDate(selectedDate);
                        GalleryFragmentDirections.ActionNavGalleryToFuturetaskSettingsFragment action = GalleryFragmentDirections.actionNavGalleryToFuturetaskSettingsFragment(sendText);
                        NavHostFragment.findNavController(GalleryFragment.this).navigate(action);
                    }
                });
                fileAlreadyExsists.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                fileAlreadyExsists.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < checkedItems.length; i++) {
                            if (checkedItems[i]) {
                                deleteFutureTask(galleryContext.getFilesDir(),listItems[i]);
                            }
                            selectedDate = selectedDate.minusMonths(1);
                            setMonthView();
                            selectedDate = selectedDate.plusMonths(1);
                            setMonthView();
                        }
                    }
                });
                fileAlreadyExsists.create();
                AlertDialog alertDialog = fileAlreadyExsists.create();
                alertDialog.show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ArrayList<String> getColorsIfCompleted(String textExists, ArrayList<String> passHabits, HashMap<String, String> habitColors){
        ArrayList<String> colorsToPass = new ArrayList<String>();
        for(String habit : passHabits){
            colorsToPass.add(getTransparency(galleryContext.getFilesDir(), habit, habitColors.get(habit), textExists));
        }
        return colorsToPass;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTransparency(File dir, String taskText, String color, String dayText){
        File[] files = dir.listFiles();
        String finalColor = null;
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskText +"Log.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        String dayTested = dayText + "-" + monthFromDate(selectedDate) + "-" + yearFromDate(selectedDate);
                        //read each line
                        boolean dayFound = false;
                        //read each line
                        while (line != null) {
                            if (line.matches(dayTested)){
                                dayFound = true;
                            }
                            line = br.readLine();
                        }
                        if (dayFound == true) {
                            finalColor = color;
                        } else {
                            String[] splitter = color.split("#");
                            String background = "#00" + splitter[1];
                            finalColor = background;
                        }
                        br.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
        return finalColor;
    }

    private void updateTaskFile(File dir, String output){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("TrackedHabits.txt")){
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
                if (fileName.matches("TrackedHabits.txt")){
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

    private void initDaysAndTasksOnThem(){
        daysAndTasksOnThem.put("Sunday", new ArrayList<>());
        daysAndTasksOnThem.put("Monday", new ArrayList<>());
        daysAndTasksOnThem.put("Tuesday", new ArrayList<>());
        daysAndTasksOnThem.put("Wednesday", new ArrayList<>());
        daysAndTasksOnThem.put("Thursday", new ArrayList<>());
        daysAndTasksOnThem.put("Friday", new ArrayList<>());
        daysAndTasksOnThem.put("Saturday", new ArrayList<>());
    }

    private void findDaysForTheTasks(File dir, String taskName){
        File[] files = dir.listFiles();
        //StringBuilder foundFiles = new StringBuilder();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskName +"Task.txt")){
                    //foundFiles.append(fileName);
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Habit Days =.*")) {
                                String[] value = line.split("=");
                                String[] days = value[1].split(", ");
                                for (String day : days){
                                    Objects.requireNonNull(daysAndTasksOnThem.get(day)).add(taskName);
                                    String temp = "Check";
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

    private void getFutureTasks(File dir, ArrayList<String> hash, String date){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(".*FutureTakk.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Date Of Task=.*")){
                                String[] value = line.split("=");
                                String dated = value[1];
                                if (dated.equals(date)){
                                    String[] taskNameSplitter = fileName.split("FutureTakk");
                                    String taskName = taskNameSplitter[0];
                                    hash.add(taskName);
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

    private void deleteFutureTask(File dir, String taskName){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskName +"FutureTakk.txt")){
                    boolean isItDeleted = file.delete();
                }
                if (fileName.matches(".*" + taskName +"Subtazk.txt")){
                    boolean isItDeleted = file.delete();
                }
            }
        }
    }
}