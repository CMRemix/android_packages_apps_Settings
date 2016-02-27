/*
 * Copyright (C) 2015 crDroid Android
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

package com.android.settings.cmremix;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.cmremix.utils.SeekBarPreference;

import com.android.internal.logging.MetricsLogger;

public class LockScreenSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "LockScreenSettings";
    private static final String LOCK_CLOCK_FONTS = "lock_clock_fonts";
    private static final String PREF_LS_BOUNCER = "lockscreen_bouncer";
    private static final String LOCKSCREEN_SECURITY_ALPHA = "lockscreen_security_alpha";
    private static final String LOCKSCREEN_ALPHA = "lockscreen_alpha";	

    private ListPreference mLockClockFonts;
    private ListPreference mLsBouncer;
    private SeekBarPreference mLsSecurityAlpha;
    private SeekBarPreference mLsAlpha;
    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFingerprintVib;

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.APPLICATION;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.cmremix_lockscreen);
        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mLockClockFonts = (ListPreference) findPreference(LOCK_CLOCK_FONTS);
        mLockClockFonts.setValue(String.valueOf(Settings.System.getInt(
                resolver, Settings.System.LOCK_CLOCK_FONTS, 4)));
        mLockClockFonts.setSummary(mLockClockFonts.getEntry());
        mLockClockFonts.setOnPreferenceChangeListener(this);

	mLsAlpha = (SeekBarPreference) findPreference(LOCKSCREEN_ALPHA);
            float alpha = Settings.System.getFloat(resolver,
                   Settings.System.LOCKSCREEN_ALPHA, 0.45f);
            mLsAlpha.setValue((int)(100 * alpha));
            mLsAlpha.setOnPreferenceChangeListener(this);

	 mLsBouncer = (ListPreference) findPreference(PREF_LS_BOUNCER);
        mLsBouncer.setOnPreferenceChangeListener(this);
        int lockbouncer = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_BOUNCER, 0);
        mLsBouncer.setValue(String.valueOf(lockbouncer));
        updateBouncerSummary(lockbouncer);
	
	  mLsSecurityAlpha = (SeekBarPreference) findPreference(LOCKSCREEN_SECURITY_ALPHA);
        float alpha2 = Settings.System.getFloat(resolver,
                Settings.System.LOCKSCREEN_SECURITY_ALPHA, 0.75f);
        mLsSecurityAlpha.setValue((int)(100 * alpha2));
        mLsSecurityAlpha.setOnPreferenceChangeListener(this);

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SwitchPreference) prefScreen.findPreference("fingerprint_success_vib");
        if (!mFingerprintManager.isHardwareDetected()){
            prefScreen.removePreference(mFingerprintVib);
        }
    }

    private void updateBouncerSummary(int value) {
         Resources res = getResources();
  
         if (value == 0) {
             // stock bouncer
             mLsBouncer.setSummary(res.getString(R.string.ls_bouncer_on_summary));
         } else if (value == 1) {
             // bypass bouncer
             mLsBouncer.setSummary(res.getString(R.string.ls_bouncer_off_summary));
         } else {
             String type = null;
             switch (value) {
                 case 2:
                     type = res.getString(R.string.ls_bouncer_dismissable);
                     break;
                 case 3:
                     type = res.getString(R.string.ls_bouncer_persistent);
                     break;
                 case 4:
                     type = res.getString(R.string.ls_bouncer_all);
                     break;
             }
             // Remove title capitalized formatting
             type = type.toLowerCase();
             mLsBouncer.setSummary(res.getString(R.string.ls_bouncer_summary, type));
         }
     }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mLockClockFonts) {
            Settings.System.putInt(resolver, Settings.System.LOCK_CLOCK_FONTS,
                    Integer.valueOf((String) newValue));
            mLockClockFonts.setValue(String.valueOf(newValue));
            mLockClockFonts.setSummary(mLockClockFonts.getEntry());
            return true;
	}  else if (preference == mLsAlpha) {
             int alpha = (Integer) newValue;
             Settings.System.putFloat(resolver,
                     Settings.System.LOCKSCREEN_ALPHA, alpha / 100.0f);
             return true;
        } else if (preference == mLsSecurityAlpha) {
            int alpha2 = (Integer) newValue;
            Settings.System.putFloat(resolver,
                    Settings.System.LOCKSCREEN_SECURITY_ALPHA, alpha2 / 100.0f);
            return true;
         } else if (preference == mLsBouncer) {
            int lockbouncer = Integer.valueOf((String) newValue);
            Settings.Secure.putInt(resolver, Settings.Secure.LOCKSCREEN_BOUNCER, lockbouncer);
            updateBouncerSummary(lockbouncer);
            return true;
        }
        return false;
    }
}
