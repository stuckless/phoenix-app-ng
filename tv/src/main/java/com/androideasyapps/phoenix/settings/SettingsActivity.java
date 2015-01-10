package com.androideasyapps.phoenix.settings;

import android.app.Activity;
import android.os.Bundle;

import com.androideasyapps.phoenix.R;

/**
 * Created by seans on 10/01/15.
 */
public class SettingsActivity extends Activity {
    public SettingsActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
    }
}
