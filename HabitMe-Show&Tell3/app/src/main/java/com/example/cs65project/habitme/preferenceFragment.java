package com.example.cs65project.habitme;

import android.os.Bundle;
import android.preference.PreferenceFragment;


public class preferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
    }
}
