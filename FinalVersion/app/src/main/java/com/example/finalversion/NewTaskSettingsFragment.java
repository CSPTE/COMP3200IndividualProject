package com.example.finalversion;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class NewTaskSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}