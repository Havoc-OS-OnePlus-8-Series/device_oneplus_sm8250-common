/*
 * Copyright (C) 2021 Yet Another AOSP Project
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
package org.havoc.device.DeviceSettings.ModeSwitch;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class DolbySwitch {
    private static final String PACKAGE_NAME = "com.dolby.daxservice";
    private static final String CLASS_NAME = "com.dolby.daxservice.DaxService";

    private Context mContext;
    private ComponentName mComponentName;

    public DolbySwitch(Context context) {
        mContext = context;
        mComponentName = new ComponentName(PACKAGE_NAME, CLASS_NAME);
    }

    public boolean isCurrentlyEnabled() {
        return mContext.getPackageManager().getComponentEnabledSetting(mComponentName)
                == COMPONENT_ENABLED_STATE_DEFAULT;
    }

    public static boolean isCurrentlyEnabled(Context context, ComponentName componentName) {
        return context.getPackageManager().getComponentEnabledSetting(componentName)
                == COMPONENT_ENABLED_STATE_DEFAULT;
    }

    public void setEnabled(boolean enabled) {
        Intent daxService = new Intent();
        daxService.setComponent(mComponentName);
        if (enabled) {
            // enable service component and start service
            mContext.getPackageManager().setComponentEnabledSetting(mComponentName,
                    COMPONENT_ENABLED_STATE_DEFAULT, 0);
            mContext.startService(daxService);
            return;
        }
        // disable service component and stop service
        mContext.stopService(daxService);
        mContext.getPackageManager().setComponentEnabledSetting(mComponentName,
                COMPONENT_ENABLED_STATE_DISABLED, 0);
    }
}