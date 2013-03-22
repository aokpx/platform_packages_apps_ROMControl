
package com.aokp.romcontrol.fragments;

import java.io.File;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.util.Helpers;

public class Advanced extends AOKPPreferenceFragment {

    private static final String TAG = "Advanced";
    private static final String HARDWARE_SETTINGS = "hardware_category";
    private static final String KEY_HARDWARE_KEYS = "hardware_keys";

    CheckBoxPreference mXlog;
    private PreferenceCategory mHardware;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_advanced);
        PreferenceScreen prefs = getPreferenceScreen();
        mHardware = (PreferenceCategory) prefs.findPreference(HARDWARE_SETTINGS);

        mXlog = (CheckBoxPreference)findPreference("xlog");
        mXlog.setChecked(!new File("/system/bin/logcat").exists());
        if (mXlog.isChecked()) {
            mXlog.setSummary("Logging disabled, please enable before requesting support via XDA");
        }

        // Only show the hardware keys config on a device that does not have a navbar
        IWindowManager windowManager = IWindowManager.Stub.asInterface(
                ServiceManager.getService(Context.WINDOW_SERVICE));

        final boolean hasNavBarByDefault = getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);

        if (hasNavBarByDefault) {
            // Let's assume they don't have hardware keys
            mHardware.removePreference(findPreference(KEY_HARDWARE_KEYS));
            prefs.removePreference(mHardware);
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

