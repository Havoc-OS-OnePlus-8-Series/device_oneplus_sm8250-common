/*
* Copyright (C) 2013 The OmniROM Project
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.preference.PreferenceManager;

import org.havoc.device.DeviceSettings.Doze.DozeUtils;
import org.havoc.device.DeviceSettings.ModeSwitch.*;
import org.havoc.device.DeviceSettings.Services.FPSInfoService;
import org.havoc.device.DeviceSettings.Utils.Utils;

public class Startup extends BroadcastReceiver {

    private static final String ONE_TIME_DOLBY = "dolby_init_disabled";

    @Override
    public void onReceive(final Context context, final Intent bootintent) {

        boolean enabled = false;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        enabled = sharedPrefs.getBoolean(DeviceSettings.KEY_HBM_SWITCH, false);
        if (enabled) {
            restore(HBMModeSwitch.getFile(), enabled);
        }
        enabled = sharedPrefs.getBoolean(DeviceSettings.KEY_FPS_INFO, false);
        if (enabled) {
            context.startService(new Intent(context, FPSInfoService.class));
        }
        enabled = sharedPrefs.getBoolean(DeviceSettings.KEY_GAME_SWITCH, false);
        if (enabled) {
            restore(GameModeSwitch.getFile(), enabled);
        }
        enabled = sharedPrefs.getBoolean(ONE_TIME_DOLBY, false);
        if (!enabled) {
            // we want to disable it by default, only once.
            DolbySwitch dolbySwitch = new DolbySwitch(context);
            dolbySwitch.setEnabled(false);
            sharedPrefs.edit().putBoolean(ONE_TIME_DOLBY, true).apply();
        }
        DozeUtils.checkDozeService(context);
        DeviceSettings.restoreVibStrengthSetting(context);
    }

    private void restore(String file, boolean enabled) {
        if (file == null) {
            return;
        }
        if (enabled) {
            Utils.writeValue(file, "1");
        }
    }

    private void restore(String file, String value) {
        if (file == null) {
            return;
        }
        Utils.writeValue(file, value);
    }
}