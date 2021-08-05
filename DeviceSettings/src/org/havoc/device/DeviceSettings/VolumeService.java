/*
* Copyright (C) 2016 The OmniROM Project
* Copyright (C) 2021 The Havoc-OS Project
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

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.provider.Settings;

public class VolumeService {

    private static final int NO_CHANGE_VOLUME = 0;
    private static final int MUTE_VOLUME = 1;
    private static final int RESTORE_VOLUME = 2;

    private static final String MUTE_KEY = "device_settings_mute";
    private static final String VOLUME_SPEAKER_KEY = "device_settings_volume_speaker";

    public static void setEnabled(Context context, boolean enabled) {
        Settings.System.putInt(context.getContentResolver(), MUTE_KEY, enabled ? 1 : 0);
        AudioManager mAudioManager = context.getSystemService(AudioManager.class);
        changeMediaVolume(mAudioManager, context);
    }

    private static boolean isCurrentlyEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), MUTE_KEY, 0) == 1;
    }

    private static boolean isSilentMode(AudioManager mAudioManager) {
        return mAudioManager.getRingerModeInternal() == mAudioManager.RINGER_MODE_SILENT;
    }

    private static boolean isSpeakerOutput(Context context) {
        MediaRouter mr = (MediaRouter) context.getSystemService(Context.MEDIA_ROUTER_SERVICE);
        MediaRouter.RouteInfo ri = mr.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO);
        String mSpeakerOutput = context.getResources().getString(
                com.android.internal.R.string.default_audio_route_name);
        return ri.getName().equals(mSpeakerOutput);
    }

    private static void saveVolume(Context context, int volume) {
        Settings.System.putInt(context.getContentResolver(), VOLUME_SPEAKER_KEY, volume);
    }

    private static int getSavedVolume(Context context) {
        return Settings.System.getInt(context.getContentResolver(), VOLUME_SPEAKER_KEY, 0);
    }

    private static int getMediaVolume(AudioManager mAudioManager) {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    private static void setMediaVolume(AudioManager mAudioManager, int volume) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }
    
    private static int shouldChangeMediaVolume(boolean isCurrentlyEnabled, boolean isSpeakerOutput, int currentVolume, int savedVolume, boolean isSilentMode) {
        if (isCurrentlyEnabled) {
            if (!isSpeakerOutput) {
                return NO_CHANGE_VOLUME;
            } else if (isSilentMode) {
                return currentVolume != 0 ? MUTE_VOLUME : NO_CHANGE_VOLUME;
            } else {
                return (currentVolume == 0 && savedVolume != 0) ? RESTORE_VOLUME : NO_CHANGE_VOLUME;
            }
        }
        return (currentVolume == 0 && savedVolume != 0 && isSilentMode) ? RESTORE_VOLUME : NO_CHANGE_VOLUME;
    }
    
    public static void changeMediaVolume(AudioManager mAudioManager, Context context) {
        int currentVolume = getMediaVolume(mAudioManager);
        int savedVolume = getSavedVolume(context);
        int shouldChange = shouldChangeMediaVolume(isCurrentlyEnabled(context), isSpeakerOutput(context), currentVolume, savedVolume, isSilentMode(mAudioManager));
        if (shouldChange == MUTE_VOLUME) {
             saveVolume(context, currentVolume);
             setMediaVolume(mAudioManager, 0);
        } else if (shouldChange == RESTORE_VOLUME) { 
             setMediaVolume(mAudioManager, savedVolume);
             saveVolume(context, 0); 
        }
    }
}
