package com.android.settings.cmremix.animation;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.provider.Settings;


import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.android.internal.util.cmremix.AwesomeAnimationHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.android.internal.logging.MetricsProto.MetricsEvent;

public class AnimationSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

	  private ListPreference mPowerMenuAnimations;
  
      private int[] mAnimations;
      private String[] mAnimationsStrings;
      private String[] mAnimationsNum;

	  protected Context mContext;

      protected ContentResolver mContentRes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.cmremix_animation_settings);
	    mContext = getActivity().getApplicationContext();
		mContentRes = getActivity().getContentResolver();

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.CMREMIX;
    }

}
