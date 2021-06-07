/*
* Copyright (C) 2016 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.havoc.device.DeviceSettings;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.view.Window;
import android.view.WindowManager;
import android.util.Log;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;

import org.havoc.device.DeviceSettings.FileUtils;
import org.havoc.device.DeviceSettings.doze.DozeSettingsActivity;
import org.havoc.device.DeviceSettings.FPSInfoService;

public class DeviceSettings extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String KEY_HBM_SWITCH = "hbm";
    public static final String KEY_AUTO_HBM_SWITCH = "auto_hbm";
    public static final String KEY_AUTO_HBM_THRESHOLD = "auto_hbm_threshold";
    public static final String KEY_FPS_INFO = "fps_info";
    public static final String KEY_VIBSTRENGTH = "vib_strength";
    public static final String KEY_SETTINGS_PREFIX = "device_setting_";

    private static final String PREF_DOZE = "advanced_doze_settings";

    private Preference mDozeSettings;
    private static SwitchPreference mFpsInfo;
    private static TwoStatePreference mHBMModeSwitch;
    private static TwoStatePreference mAutoHBMSwitch;

    private VibratorStrengthPreference mVibratorStrength;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        addPreferencesFromResource(R.xml.main);

        Resources res = getResources();
        Window win = getActivity().getWindow();

        win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        win.setNavigationBarColor(res.getColor(R.color.primary_color));
        win.setNavigationBarDividerColor(res.getColor(R.color.primary_color));

        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        mVibratorStrength = (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
        if (mVibratorStrength == null || !VibratorStrengthPreference.isSupported()) {
            getPreferenceScreen().removePreference((Preference) findPreference("vibrator"));
        }

        mHBMModeSwitch = (TwoStatePreference) findPreference(KEY_HBM_SWITCH);
        mHBMModeSwitch.setEnabled(HBMModeSwitch.isSupported());
        mHBMModeSwitch.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(DeviceSettings.KEY_HBM_SWITCH, false));
        mHBMModeSwitch.setOnPreferenceChangeListener(this);

        mAutoHBMSwitch = (TwoStatePreference) findPreference(KEY_AUTO_HBM_SWITCH);
        mAutoHBMSwitch.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(DeviceSettings.KEY_AUTO_HBM_SWITCH, false));
        mAutoHBMSwitch.setOnPreferenceChangeListener(this);

        mDozeSettings = (Preference)findPreference(PREF_DOZE);
        mDozeSettings.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), DozeSettingsActivity.class);
            startActivity(intent);
            return true;
        });

        mFpsInfo = (SwitchPreference) findPreference(KEY_FPS_INFO);
        mFpsInfo.setChecked(isFPSOverlayRunning());
        mFpsInfo.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        mHBMModeSwitch.setChecked(HBMModeSwitch.isCurrentlyEnabled(this.getContext()));
        mFpsInfo.setChecked(isFPSOverlayRunning());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
          if (preference == mFpsInfo) {
            boolean enabled = (Boolean) newValue;
            Intent fpsinfo = new Intent(this.getContext(), FPSInfoService.class);
            if (enabled) {
                this.getContext().startService(fpsinfo);
            } else {
                this.getContext().stopService(fpsinfo);
            }
        } else if (preference == mAutoHBMSwitch) {
            Boolean enabled = (Boolean) newValue;
            SharedPreferences.Editor prefChange = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            prefChange.putBoolean(KEY_AUTO_HBM_SWITCH, enabled).commit();
            Utils.enableService(getContext());
        } else if (preference == mHBMModeSwitch) {
            Boolean enabled = (Boolean) newValue;
            Utils.writeValue(HBMModeSwitch.getFile(), enabled ? "5" : "0");
            Intent hbmIntent = new Intent(this.getContext(),
                    org.havoc.device.DeviceSettings.HBMModeService.class);
            if (enabled) {
                this.getContext().startService(hbmIntent);
            } else {
                this.getContext().stopService(hbmIntent);
            }
        }
        return true;
    }

    public static boolean isHBMModeService(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DeviceSettings.KEY_HBM_SWITCH, false);
    }

    public static boolean isAUTOHBMEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DeviceSettings.KEY_AUTO_HBM_SWITCH, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isFPSOverlayRunning() {
        ActivityManager am = (ActivityManager) getContext().getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                am.getRunningServices(Integer.MAX_VALUE))
            if (FPSInfoService.class.getName().equals(service.service.getClassName()))
                return true;
        return false;
   }
}
