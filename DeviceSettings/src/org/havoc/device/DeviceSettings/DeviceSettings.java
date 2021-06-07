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
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.view.Window;
import android.view.WindowManager;
import android.util.Log;
import android.widget.Toast;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;

import com.qualcomm.qcrilmsgtunnel.IQcrilMsgTunnel;

import org.havoc.device.DeviceSettings.Doze.DozeSettingsActivity;
import org.havoc.device.DeviceSettings.ModeSwitch.*;
import org.havoc.device.DeviceSettings.Preference.CustomSeekBarPreference;
import org.havoc.device.DeviceSettings.Preference.SwitchPreference;
import org.havoc.device.DeviceSettings.Preference.VibratorStrengthPreference;
import org.havoc.device.DeviceSettings.Services.VolumeService;
import org.havoc.device.DeviceSettings.Services.FPSInfoService;
import org.havoc.device.DeviceSettings.Utils.Utils;
import org.havoc.device.DeviceSettings.Utils.FileUtils;

public class DeviceSettings extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String KEY_MUTE_MEDIA = "mute_media";
    public static final String KEY_AUTO_HBM_SWITCH = "auto_hbm";
    public static final String KEY_AUTO_HBM_THRESHOLD = "auto_hbm_threshold";
    public static final String KEY_HBM_SWITCH = "hbm";
    public static final String KEY_FPS_INFO = "fps_info";
    public static final String KEY_FPS_INFO_POSITION = "fps_info_position";
    public static final String KEY_FPS_INFO_COLOR = "fps_info_color";
    public static final String KEY_FPS_INFO_TEXT_SIZE = "fps_info_text_size";
    public static final String KEY_NR_MODE_SWITCHER = "nr_mode_switcher";
    public static final String KEY_GAME_SWITCH = "game_mode";
    public static final String KEY_VIBSTRENGTH = "vib_strength";

    private static final String KEY_ENABLE_DOLBY_ATMOS = "enable_dolby_atmos";
    private static final String PREF_DOZE = "advanced_doze_settings";

    private static final String FILE_LEVEL = "/sys/devices/platform/soc/a8c000.i2c/i2c-3/3-005a/leds/vibrator/level";
    private static final long testVibrationPattern[] = {0,50};
    public static final String DEFAULT = "3";

    private Protocol mProtocol;
    private Vibrator mVibrator;
    private DolbySwitch mDolbySwitch;
    private Preference mDozeSettings;
    private static SwitchPreference mFpsInfo;
    private static ListPreference mFpsInfoPosition;
    private static ListPreference mFpsInfoColor;
    private static ListPreference mNrModeSwitcher;
    private static TwoStatePreference mHBMModeSwitch;
    private static TwoStatePreference mGameModeSwitch;
    private static TwoStatePreference mAutoHBMSwitch;
    private static TwoStatePreference mMuteMedia;
    private static TwoStatePreference mEnableDolbyAtmos;
    private VibratorStrengthPreference mVibratorStrengthPreference;
    private CustomSeekBarPreference mFpsInfoTextSizePreference;

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

        Intent mIntent = new Intent();
        mIntent.setClassName("com.qualcomm.qcrilmsgtunnel", "com.qualcomm.qcrilmsgtunnel.QcrilMsgTunnelService");
        getContext().bindService(mIntent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                IQcrilMsgTunnel tunnel = IQcrilMsgTunnel.Stub.asInterface(service);
                if (tunnel != null)
                    mProtocol = new Protocol(tunnel);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mProtocol = null;
            }
        }, getContext().BIND_AUTO_CREATE);

        mMuteMedia = (TwoStatePreference) findPreference(KEY_MUTE_MEDIA);
        mMuteMedia.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(DeviceSettings.KEY_MUTE_MEDIA, false));
        mMuteMedia.setOnPreferenceChangeListener(this);

        mAutoHBMSwitch = (TwoStatePreference) findPreference(KEY_AUTO_HBM_SWITCH);
        mAutoHBMSwitch.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(DeviceSettings.KEY_AUTO_HBM_SWITCH, false));
        mAutoHBMSwitch.setOnPreferenceChangeListener(this);

        mHBMModeSwitch = (TwoStatePreference) findPreference(KEY_HBM_SWITCH);
        mHBMModeSwitch.setEnabled(HBMModeSwitch.isSupported());
        mHBMModeSwitch.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(DeviceSettings.KEY_HBM_SWITCH, false));
        mHBMModeSwitch.setOnPreferenceChangeListener(this);

        mDolbySwitch = new DolbySwitch(this.getContext());
        mEnableDolbyAtmos = (TwoStatePreference) findPreference(KEY_ENABLE_DOLBY_ATMOS);
        mEnableDolbyAtmos.setChecked(mDolbySwitch.isCurrentlyEnabled());
        mEnableDolbyAtmos.setOnPreferenceChangeListener(this);

        mDozeSettings = (Preference)findPreference(PREF_DOZE);
        mDozeSettings.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), DozeSettingsActivity.class);
            startActivity(intent);
            return true;
        });

        mFpsInfo = (SwitchPreference) findPreference(KEY_FPS_INFO);
        mFpsInfo.setChecked(isFPSOverlayRunning());
        mFpsInfo.setOnPreferenceChangeListener(this);

        mFpsInfoPosition = (ListPreference) findPreference(KEY_FPS_INFO_POSITION);
        mFpsInfoPosition.setOnPreferenceChangeListener(this);

        mFpsInfoColor = (ListPreference) findPreference(KEY_FPS_INFO_COLOR);
        mFpsInfoColor.setOnPreferenceChangeListener(this);

        mFpsInfoTextSizePreference = (CustomSeekBarPreference) findPreference(KEY_FPS_INFO_TEXT_SIZE);
        mFpsInfoTextSizePreference.setOnPreferenceChangeListener(this);

        mNrModeSwitcher = (ListPreference) findPreference(KEY_NR_MODE_SWITCHER);
        mNrModeSwitcher.setOnPreferenceChangeListener(this);

        mGameModeSwitch = (TwoStatePreference) findPreference(KEY_GAME_SWITCH);
        if (GameModeSwitch.isSupported()) {
            mGameModeSwitch.setEnabled(true);
        } else {
            mGameModeSwitch.setEnabled(false);
            mGameModeSwitch.setSummary(getString(R.string.unsupported_feature));
        }
        mGameModeSwitch.setChecked(GameModeSwitch.isCurrentlyEnabled(this.getContext()));
        mGameModeSwitch.setOnPreferenceChangeListener(new GameModeSwitch());

        mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        mVibratorStrengthPreference =  (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
        if (Utils.fileWritable(FILE_LEVEL)) {
            mVibratorStrengthPreference.setValue(sharedPrefs.getInt(KEY_VIBSTRENGTH,
                Integer.parseInt(Utils.getFileValue(FILE_LEVEL, DEFAULT))));
            mVibratorStrengthPreference.setOnPreferenceChangeListener(this);
        } else {
            mVibratorStrengthPreference.setEnabled(false);
            mVibratorStrengthPreference.setSummary(getString(R.string.unsupported_feature));
        }
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
          if (preference == mMuteMedia) {
            Boolean enabled = (Boolean) newValue;
            VolumeService.setEnabled(getContext(), enabled);
        } else if (preference == mAutoHBMSwitch) {
            Boolean enabled = (Boolean) newValue;
            SharedPreferences.Editor prefChange = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            prefChange.putBoolean(KEY_AUTO_HBM_SWITCH, enabled).commit();
            Utils.enableService(getContext());
        } else if (preference == mHBMModeSwitch) {
            Boolean enabled = (Boolean) newValue;
            Utils.writeValue(HBMModeSwitch.getFile(), enabled ? "5" : "0");
            Intent hbmIntent = new Intent(this.getContext(),
                    org.havoc.device.DeviceSettings.Services.HBMModeService.class);
            if (enabled) {
                this.getContext().startService(hbmIntent);
            } else {
                this.getContext().stopService(hbmIntent);
            }
        } else if (preference == mEnableDolbyAtmos) {
            mDolbySwitch.setEnabled((Boolean) newValue);
        } else if (preference == mFpsInfo) {
            boolean enabled = (Boolean) newValue;
            Intent fpsinfo = new Intent(this.getContext(), FPSInfoService.class);
            if (enabled) {
                this.getContext().startService(fpsinfo);
            } else {
                this.getContext().stopService(fpsinfo);
            }
        } else if (preference == mFpsInfoPosition) {
            int position = Integer.parseInt(newValue.toString());
            Context mContext = getContext();
            if (FPSInfoService.isPositionChanged(mContext, position)) {
                FPSInfoService.setPosition(mContext, position);
                if (isFPSOverlayRunning()) {
                    restartFpsInfo(mContext);
                }
            }
        } else if (preference == mFpsInfoColor) {
            int color = Integer.parseInt(newValue.toString());
            Context mContext = getContext();
            if (FPSInfoService.isColorChanged(mContext, color)) {
                FPSInfoService.setColorIndex(mContext, color);
                if (isFPSOverlayRunning()) {
                    restartFpsInfo(mContext);
                }
            }
        } else if (preference == mFpsInfoTextSizePreference) {
            int size = Integer.parseInt(newValue.toString());
            Context mContext = getContext();
            if (FPSInfoService.isSizeChanged(mContext, size - 1)) {
                FPSInfoService.setSizeIndex(mContext, size - 1);
                if (isFPSOverlayRunning()) {
                    restartFpsInfo(mContext);
                }
            }
        } else if (preference == mNrModeSwitcher) {
            int mode = Integer.parseInt(newValue.toString());
            return setNrModeChecked(mode);
        } else if (preference == mVibratorStrengthPreference) {
    	    int value = Integer.parseInt(newValue.toString());
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            sharedPrefs.edit().putInt(KEY_VIBSTRENGTH, value).commit();
            Utils.writeValue(FILE_LEVEL, String.valueOf(value));
            mVibrator.vibrate(testVibrationPattern, -1);
        }
        return true;
    }

    public static boolean isHBMModeService(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DeviceSettings.KEY_HBM_SWITCH, false);
    }

    public static boolean isAUTOHBMEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DeviceSettings.KEY_AUTO_HBM_SWITCH, false);
    }

    public static void restoreVibStrengthSetting(Context context) {
        if (Utils.fileWritable(FILE_LEVEL)) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            int value = sharedPrefs.getInt(KEY_VIBSTRENGTH,
                Integer.parseInt(Utils.getFileValue(FILE_LEVEL, DEFAULT)));
            Utils.writeValue(FILE_LEVEL, String.valueOf(value));
        }
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

    private void restartFpsInfo(Context context) {
        Intent fpsinfo = new Intent(context, FPSInfoService.class);
        context.stopService(fpsinfo);
        context.startService(fpsinfo);
    }

    private boolean setNrModeChecked(int mode) {
        switch (mode) {
            case 0:
                return setNrModeChecked(Protocol.NR_5G_DISABLE_MODE_TYPE.NAS_NR5G_DISABLE_MODE_SA);
            case 1:
                return setNrModeChecked(Protocol.NR_5G_DISABLE_MODE_TYPE.NAS_NR5G_DISABLE_MODE_NSA);
            default:
                return setNrModeChecked(Protocol.NR_5G_DISABLE_MODE_TYPE.NAS_NR5G_DISABLE_MODE_NONE);
        }
    }

    private boolean setNrModeChecked(Protocol.NR_5G_DISABLE_MODE_TYPE mode) {
        if (mProtocol == null) {
            Toast.makeText(getContext(), R.string.service_not_ready, Toast.LENGTH_LONG).show();
            return false;
        }
        int index = SubscriptionManager.getSlotIndex(SubscriptionManager.getDefaultDataSubscriptionId());
        if (index == SubscriptionManager.INVALID_SIM_SLOT_INDEX) {
            Toast.makeText(getContext(), R.string.unavailable_sim_slot, Toast.LENGTH_LONG).show();
            return false;
        }
        new Thread(() -> mProtocol.setNrMode(index, mode)).start();
        return true;
    }
}