/*
 * Copyright (C) 2015 The CyanogenMod project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.notification;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;

import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.util.omni.PackageUtils;

import java.util.List;
import java.util.ArrayList;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.DropDownPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import org.cyanogenmod.internal.logging.CMMetricsLogger;

import java.util.ArrayList;
import java.util.List;

public class NotificationManagerSettings extends SettingsPreferenceFragment
        implements Indexable, Preference.OnPreferenceChangeListener {

    private static final String TAG = NotificationManagerSettings.class.getSimpleName();

    private static final String KEY_LOCK_SCREEN_NOTIFICATIONS = "lock_screen_notifications";
    private static final String CATEGORY_WEATHER = "weather_category";
    private static final String WEATHER_ICON_PACK = "weather_icon_pack";
    private static final String DEFAULT_WEATHER_ICON_PACKAGE = "org.omnirom.omnijaws";
    private static final String WEATHER_SERVICE_PACKAGE = "org.omnirom.omnijaws";
    private static final String LOCK_CLOCK_PACKAGE="com.cyanogenmod.lockclock";
    private static final String CUSTOM_HEADER_IMAGE = "status_bar_custom_header";
    private static final String DAYLIGHT_HEADER_PACK = "daylight_header_pack";
    private static final String DEFAULT_HEADER_PACKAGE = "com.android.systemui";

    private ListPreference mDaylightHeaderPack;
    private SwitchPreference mCustomHeaderImage;

    private PreferenceCategory mWeatherCategory;
    private ListPreference mWeatherIconPack;

    private boolean mSecure;
    private int mLockscreenSelectedValue;
    private DropDownPreference mLockscreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.notification_manager_settings);
        mSecure = new LockPatternUtils(getActivity()).isSecure(UserHandle.myUserId());
        initLockscreenNotifications();
        PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        // weather icon
        mWeatherCategory = (PreferenceCategory) prefSet.findPreference(CATEGORY_WEATHER);
        if (mWeatherCategory != null && !isOmniJawsServiceInstalled()) {
            prefSet.removePreference(mWeatherCategory);
        } else {
            String settingHeaderPackage = Settings.System.getString(getContentResolver(),
                    Settings.System.STATUS_BAR_WEATHER_ICON_PACK);
            if (settingHeaderPackage == null) {
                settingHeaderPackage = DEFAULT_WEATHER_ICON_PACKAGE;
            }
            mWeatherIconPack = (ListPreference) findPreference(WEATHER_ICON_PACK);
            mWeatherIconPack.setEntries(getAvailableWeatherIconPacksEntries());
            mWeatherIconPack.setEntryValues(getAvailableWeatherIconPacksValues());

            int valueIndex = mWeatherIconPack.findIndexOfValue(settingHeaderPackage);
            if (valueIndex == -1) {
                // no longer found
                settingHeaderPackage = DEFAULT_WEATHER_ICON_PACKAGE;
                Settings.System.putString(getContentResolver(),
                        Settings.System.STATUS_BAR_WEATHER_ICON_PACK, settingHeaderPackage);
                valueIndex = mWeatherIconPack.findIndexOfValue(settingHeaderPackage);
            }
            mWeatherIconPack.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
            mWeatherIconPack.setSummary(mWeatherIconPack.getEntry());
            mWeatherIconPack.setOnPreferenceChangeListener(this);
        }

        // header image packs
        final boolean customHeaderImage = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_CUSTOM_HEADER, 0) == 1;
        mCustomHeaderImage = (SwitchPreference) findPreference(CUSTOM_HEADER_IMAGE);
        mCustomHeaderImage.setChecked(customHeaderImage);

        String imageHeaderPackage = Settings.System.getString(getContentResolver(),
                Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK);
        if (imageHeaderPackage == null) {
            imageHeaderPackage = DEFAULT_HEADER_PACKAGE;
        }
        mDaylightHeaderPack = (ListPreference) findPreference(DAYLIGHT_HEADER_PACK);
        List<String> entries = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        getAvailableHeaderPacks(entrieshp, values);
        mDaylightHeaderPack.setEntries(entries.toArray(new String[entries.size()]));
        mDaylightHeaderPack.setEntryValues(values.toArray(new String[values.size()]));

        int valueIndexHeader = mDaylightHeaderPack.findIndexOfValue(imageHeaderPackage);
        if (valueIndexHeader == -1) {
            // no longer found
            imageHeaderPackage = DEFAULT_HEADER_PACKAGE;
            Settings.System.putString(getContentResolver(),
                    Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK, imageHeaderPackage);
            valueIndexHeader = mDaylightHeaderPack.findIndexOfValue(imageHeaderPackage);
        }
        mDaylightHeaderPack.setValueIndex(valueIndexHeader >= 0 ? valueIndexHeader : 0);
        mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntry());
        mDaylightHeaderPack.setOnPreferenceChangeListener(this);
        mDaylightHeaderPack.setEnabled(customHeaderImage);
    }

    // === Lockscreen (public / private) notifications ===

    private void initLockscreenNotifications() {
        mLockscreen = (DropDownPreference) findPreference(KEY_LOCK_SCREEN_NOTIFICATIONS);
        if (mLockscreen == null) {
            Log.i(TAG, "Preference not found: " + KEY_LOCK_SCREEN_NOTIFICATIONS);
            return;
        }

        mLockscreen.addItem(R.string.lock_screen_notifications_summary_show,
                R.string.lock_screen_notifications_summary_show);
        if (mSecure) {
            mLockscreen.addItem(R.string.lock_screen_notifications_summary_hide,
                    R.string.lock_screen_notifications_summary_hide);
        }
        mLockscreen.addItem(R.string.lock_screen_notifications_summary_disable,
                R.string.lock_screen_notifications_summary_disable);
        updateLockscreenNotifications();
        mLockscreen.setCallback(new DropDownPreference.Callback() {
            @Override
            public boolean onItemSelected(int pos, Object value) {
                final int val = (Integer) value;
                if (val == mLockscreenSelectedValue) {
                    return true;
                }
                final boolean enabled = val != R.string.lock_screen_notifications_summary_disable;
                final boolean show = val == R.string.lock_screen_notifications_summary_show;
                Settings.Secure.putInt(getContentResolver(),
                        Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS, show ? 1 : 0);
                Settings.Secure.putInt(getContentResolver(),
                        Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, enabled ? 1 : 0);
                mLockscreenSelectedValue = val;
                return true;
            }
        });
    }

    private void updateLockscreenNotifications() {
        if (mLockscreen == null) {
            return;
        }
        final boolean enabled = getLockscreenNotificationsEnabled();
        final boolean allowPrivate = !mSecure || getLockscreenAllowPrivateNotifications();
        mLockscreenSelectedValue = !enabled ? R.string.lock_screen_notifications_summary_disable :
                allowPrivate ? R.string.lock_screen_notifications_summary_show :
                        R.string.lock_screen_notifications_summary_hide;
        mLockscreen.setSelectedValue(mLockscreenSelectedValue);
    }

    private boolean getLockscreenNotificationsEnabled() {
        return Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, 0) != 0;
    }

    private boolean getLockscreenAllowPrivateNotifications() {
        return Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS, 0) != 0;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                            boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.notification_manager_settings;
                    result.add(sir);

                    return result;
                }
            };

    @Override
    protected int getMetricsCategory() {
        return CMMetricsLogger.NOTIFICATION_MANAGER_SETTINGS;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mCustomHeaderImage) {
            final boolean value = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_CUSTOM_HEADER, value ? 1 : 0);
            mDaylightHeaderPack.setEnabled(value);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getContentResolver();
        if (preference == mWeatherIconPack) {
            String value = (String) newValue;
            Settings.System.putString(getContentResolver(),
                    Settings.System.STATUS_BAR_WEATHER_ICON_PACK, value);
            int valueIndex = mWeatherIconPack.findIndexOfValue(value);
            mWeatherIconPack.setSummary(mWeatherIconPack.getEntries()[valueIndex]);
            return true;
        } else if (preference == mDaylightHeaderPack) {
            String value = (String) newValue;
            Settings.System.putString(getContentResolver(),
                    Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK, value);
            int valueIndex = mDaylightHeaderPack.findIndexOfValue(value);
            mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntries()[valueIndex]);
            return true;
        }
        return false;
    }

    private boolean isOmniJawsServiceInstalled() {
        return PackageUtils.isAvailableApp(WEATHER_SERVICE_PACKAGE, getActivity());
    }

    private boolean isLockClockInstalled() {
        return PackageUtils.isAvailableApp(LOCK_CLOCK_PACKAGE, getActivity());
    }

    private String[] getAvailableWeatherIconPacksValues() {
        List<String> headerPacks = new ArrayList<String>();
        Intent i = new Intent();
        PackageManager packageManager = getPackageManager();
        i.setAction("org.omnirom.WeatherIconPack");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                headerPacks.add(0, r.activityInfo.name);
            } else {
                headerPacks.add(r.activityInfo.name);
            }
        }
        if (isLockClockInstalled()) {
            headerPacks.add(LOCK_CLOCK_PACKAGE ".weather");
            headerPacks.add(LOCK_CLOCK_PACKAGE ".weather_color");
            headerPacks.add(LOCK_CLOCK_PACKAGE ".weather_vclouds");
        }
        return headerPacks.toArray(new String[headerPacks.size()]);
    }

    private String[] getAvailableWeatherIconPacksEntries() {
        List<String> headerPacks = new ArrayList<String>();
        Intent i = new Intent();
        PackageManager packageManager = getPackageManager();
        i.setAction("org.omnirom.WeatherIconPack");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                headerPacks.add(0, label);
            } else {
                headerPacks.add(label);
            }
        }
        if (isLockClockInstalled()) {
            headerPacks.add("LockClock (white)");
            headerPacks.add("LockClock (color)");
            headerPacks.add("LockClock (vclouds)");
        }
        return headerPacks.toArray(new String[headerPacks.size()]);
    }

    private void getAvailableHeaderPacks(List<String> entries, List<String> values) {
        Intent i = new Intent();
        PackageManager packageManager = getPackageManager();
        i.setAction("org.omnirom.DaylightHeaderPack");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            if (packageName.equals(DEFAULT_HEADER_PACKAGE)) {
                values.add(0, packageName);
            } else {
                values.add(packageName);
            }
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            if (packageName.equals(DEFAULT_HEADER_PACKAGE)) {
                entries.add(0, label);
            } else {
                entries.add(label);
            }
        }
        i.setAction("org.omnirom.DaylightHeaderPack1");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            values.add(packageName  + "/" + r.activityInfo.name);
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (label == null) {
                label = packageName;
            }
            entries.add(label);
        }
    }
}
