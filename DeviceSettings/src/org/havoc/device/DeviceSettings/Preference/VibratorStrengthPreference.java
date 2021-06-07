/*
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
package org.havoc.device.DeviceSettings.Preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import org.havoc.device.DeviceSettings.Preference.CustomSeekBarPreference;
import org.havoc.device.DeviceSettings.R;

public class VibratorStrengthPreference extends CustomSeekBarPreference {

    public VibratorStrengthPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.reset) {
            Toast.makeText(getContext(), getContext().getString(R.string.custom_seekbar_default_value_to_set, getTextValue(mDefaultValue)),
                    Toast.LENGTH_LONG).show();
        } else if (id == R.id.minus) {
            setValue(mValue - mInterval, true);
        } else if (id == R.id.plus) {
            setValue(mValue + mInterval, true);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int newValue = getLimitedValue(mMinValue + (progress * mInterval));
        if (mTrackingTouch && !mContinuousUpdates) {
            mTrackingValue = newValue;
            updateValueViews();
        } else if (mValue != newValue) {
            if (!callChangeListener(newValue)) {
                mSeekBar.setProgress(getSeekValue(mValue));
                return;
            }
            changeValue(newValue);
            persistInt(newValue);

            mValue = newValue;
            updateValueViews();
        }
    }
}