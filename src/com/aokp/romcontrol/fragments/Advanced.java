
package com.aokp.romcontrol.fragments;

import java.io.File;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.util.Helpers;

public class Advanced extends AOKPPreferenceFragment {

    private static final String TAG = "Advanced";

    CheckBoxPreference mXlog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_advanced);

        mXlog = (CheckBoxPreference)findPreference("xlog");
        mXlog.setChecked(!new File("/system/bin/logcat").exists());
        if (mXlog.isChecked()) {
            mXlog.setSummary("Logging disabled, please enable before requesting support via XDA");
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mXlog) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            if (checked) {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/bin/logcat /system/bin/logcat.bck");
                Helpers.getMount("ro");
                mXlog.setSummary("Logging disabled, please enable before requesting support via XDA");
            } else {
                Helpers.getMount("rw");
                new CMDProcessor().su
                    .runWaitFor("mv /system/bin/logcat.bck /system/bin/logcat");
                Helpers.getMount("ro");
                mXlog.setSummary(R.string.xlog_summary);
            }
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}

