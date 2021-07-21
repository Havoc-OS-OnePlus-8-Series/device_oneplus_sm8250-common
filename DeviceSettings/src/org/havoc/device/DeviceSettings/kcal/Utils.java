/*
 * Copyright (C) 2018 The Xiaomi-SDM660 Project
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
 * limitations under the License
 */

package org.havoc.device.DeviceSettings.kcal;

public interface Utils {
    String PREF_ENABLED = "kcal_enabled";
    String PREF_RED = "color_red";
    String PREF_GREEN = "color_green";
    String PREF_BLUE = "color_blue";
    String PREF_SATURATION = "saturation";
    String PREF_VALUE = "value";
    String PREF_CONTRAST = "contrast";
    String PREF_HUE = "hue";

    int RED_DEFAULT = 256;
    int GREEN_DEFAULT = 256;
    int BLUE_DEFAULT = 256;
    int SATURATION_DEFAULT = 127;
    int SATURATION_OFFSET = 128;
    int VALUE_DEFAULT = 127;
    int VALUE_OFFSET = 128;
    int CONTRAST_DEFAULT = 127;
    int CONTRAST_OFFSET = 128;
    int HUE_DEFAULT = 0;

    String COLOR_FILE = "/sys/module/msm_drm/parameters";
    String COLOR_FILE_RED = COLOR_FILE + "/kcal_red";
    String COLOR_FILE_GREEN = COLOR_FILE + "/kcal_green";
    String COLOR_FILE_BLUE = COLOR_FILE + "/kcal_blue";
    String COLOR_FILE_SAT = COLOR_FILE + "/kcal_sat";
    String COLOR_FILE_HUE = COLOR_FILE + "/kcal_hue";
    String COLOR_FILE_VAL = COLOR_FILE + "/kcal_val";
    String COLOR_FILE_CONT = COLOR_FILE + "/kcal_cont";
}
