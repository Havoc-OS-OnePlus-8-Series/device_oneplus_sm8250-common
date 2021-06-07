/*
 * Copyright (C) 2020 Havoc-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.havoc.device.DeviceSettings.Preference;

import android.content.Context;
import android.os.VibrationEffect;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.R;

import org.havoc.device.DeviceSettings.Utils.VibrationUtils;

public class SwitchPreference extends androidx.preference.SwitchPreference {

    private final Context mContext;

    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mContext = context;
    }

    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context,
                androidx.preference.R.attr.switchPreferenceStyle,
                android.R.attr.switchPreferenceStyle));
        setLayoutResource(R.layout.preference_material_settings);
    }

    public SwitchPreference(Context context) {
        this(context, null);
        setLayoutResource(R.layout.preference_material_settings);
    }

    @Override
    protected void performClick(View view) {
        super.performClick(view);
        VibrationUtils.doHapticFeedback(mContext, VibrationEffect.EFFECT_CLICK);
    }
}