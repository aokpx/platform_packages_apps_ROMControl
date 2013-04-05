
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

public class Advanced extends AOKPPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "Advanced";
    private static final String HARDWARE_SETTINGS = "hardware_category";
    private static final String KEY_HARDWARE_KEYS = "hardware_keys";
    private static final String KEY_VIBECONTROL = "vibecontrol";
    private static final String VIBECONTROL_PATH = "/sys/devices/virtual/timed_output/vibrator/vibe_strength";

    ListPreference mVibeControl;
    CheckBoxPreference mXlog;
    private PreferenceCategory mHardware;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_advanced);
        PreferenceScreen prefs = getPreferenceScreen();
        mHardware = (PreferenceCategory) prefs.findPreference(HARDWARE_SETTINGS);
        mVibeControl = (ListPreference) prefs.findPreference(
                KEY_VIBECONTROL);
        mXlog = (CheckBoxPreference)findPreference("xlog");

        final boolean hasVibeControl = new File(VIBECONTROL_PATH).exists();

        if (hasVibeControl) {
            String vibeStrength;
            int vibeNum;
            vibeStrength = Helpers.readOneLine(VIBECONTROL_PATH);
            vibeNum = Integer.valueOf(vibeStrength);
            mVibeControl.setValue(Integer.toString(vibeNum));
                if (vibeNum != 90) {
                    mVibeControl.setSummary(mVibeControl.getEntry());
                }
            mVibeControl.setOnPreferenceChangeListener(this);
        } else {
            mHardware.removePreference(findPreference(KEY_VIBECONTROL));
        }

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
            if (!hasVibeControl) {
                prefs.removePreference(mHardware);
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mVibeControl) {
            int value = Integer.valueOf((String) newValue);
            int index = mVibeControl.findIndexOfValue((String) newValue);
            mVibeControl.setSummary(
                    mVibeControl.getEntries()[index]);
                    CMDProcessor.runSuCommand("busybox echo " + value + " > "
                    + VIBECONTROL_PATH);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mXlog) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            if (checked) {
                Helpers.getMount("rw");
                CMDProcessor.runSuCommand("mv /system/bin/logcat /system/bin/logcat.bck");
                Helpers.getMount("ro");
                mXlog.setSummary("Logging disabled, please enable before requesting support via XDA");
            } else {
                Helpers.getMount("rw");
                CMDProcessor.runSuCommand("mv /system/bin/logcat.bck /system/bin/logcat");
                Helpers.getMount("ro");
                mXlog.setSummary(R.string.xlog_summary);
            }
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}

