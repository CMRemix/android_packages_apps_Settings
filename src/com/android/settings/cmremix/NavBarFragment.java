/*Copyright (C) 2015 The ResurrectionRemix Project
     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
*/
package com.android.settings.cmremix;
import android.content.ContentResolver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.cmremix.SeekBarPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.cmremix.utils.DeviceUtils;
import com.android.settings.cmremix.utils.TelephonyUtils;
import org.cyanogenmod.internal.util.ScreenType;

import android.util.Log;
import android.view.IWindowManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;

import java.util.List;

import cyanogenmod.hardware.CMHardwareManager;
import cyanogenmod.providers.CMSettings;

public class NavBarFragment extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "NavBarFragment";
    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.CMREMIX;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.cmremix_navbar);

    }

    public boolean onPreferenceChange(Preference preference, Object newValue){
        return false;
    }
}
