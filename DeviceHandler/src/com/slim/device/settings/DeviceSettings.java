/*
 * Copyright (C) 2016 The CyanogenMod Project
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

package com.slim.device.settings;

import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import com.slim.device.SRGBModeSwitch;
import com.slim.device.DCIModeSwitch;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.util.Log;
import android.text.TextUtils;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.slim.device.KernelControl;
import com.slim.device.R;
import com.slim.device.util.FileUtils;

public class DeviceSettings extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String KEY_SRGB_SWITCH = "srgb";
    public static final String KEY_DCI_SWITCH = "dci";
    public static final String KEYCODE_SLIDER_TOP = "keycode_top_position";
    public static final String KEYCODE_SLIDER_MIDDLE = "keycode_middle_position";
    public static final String KEYCODE_SLIDER_BOTTOM = "keycode_bottom_position";
    private static final String KEY_CATEGORY_GRAPHICS = "graphics";

    private TwoStatePreference mSliderSwap;
    private ListPreference mSliderTop;
    private ListPreference mSliderMiddle;
    private ListPreference mSliderBottom;

    private static final String SPECTRUM_KEY = "spectrum";
    private static final String SPECTRUM_SYSTEM_PROPERTY = "persist.spectrum.profile";

    private TwoStatePreference mSRGBModeSwitch;
    private TwoStatePreference mDCIModeSwitch;
    private ListPreference mSpectrum;

@Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.main, rootKey);

        mSliderSwap = (TwoStatePreference) findPreference("button_swap");
        mSliderSwap.setOnPreferenceChangeListener(this);

        mSliderTop = (ListPreference) findPreference(KEYCODE_SLIDER_TOP);
        mSliderTop.setOnPreferenceChangeListener(this);

        mSliderMiddle = (ListPreference) findPreference(KEYCODE_SLIDER_MIDDLE);
        mSliderMiddle.setOnPreferenceChangeListener(this);

        mSliderBottom = (ListPreference) findPreference(KEYCODE_SLIDER_BOTTOM);
        mSliderBottom.setOnPreferenceChangeListener(this);

        mSRGBModeSwitch = (TwoStatePreference) findPreference(KEY_SRGB_SWITCH);
        mSRGBModeSwitch.setEnabled(SRGBModeSwitch.isSupported());
        mSRGBModeSwitch.setChecked(SRGBModeSwitch.isCurrentlyEnabled(this.getContext()));
        mSRGBModeSwitch.setOnPreferenceChangeListener(new SRGBModeSwitch());

        mDCIModeSwitch = (TwoStatePreference) findPreference(KEY_DCI_SWITCH);
        mDCIModeSwitch.setEnabled(DCIModeSwitch.isSupported());
        mDCIModeSwitch.setChecked(DCIModeSwitch.isCurrentlyEnabled(this.getContext()));
        mDCIModeSwitch.setOnPreferenceChangeListener(new DCIModeSwitch());

	mSpectrum = (ListPreference) findPreference(SPECTRUM_KEY);
        if( mSpectrum != null ) {
            mSpectrum.setValue(SystemProperties.get(SPECTRUM_SYSTEM_PROPERTY, "0"));
            mSpectrum.setOnPreferenceChangeListener(this);
        }
    }

    private void setSummary(ListPreference preference, String file) {
        String keyCode;
        if ((keyCode = FileUtils.readOneLine(file)) != null) {
            preference.setValue(keyCode);
            preference.setSummary(preference.getEntry());
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String file;
	final String key = preference.getKey();
	String strvalue;

	if (SPECTRUM_KEY.equals(key)) {
	    Boolean value;
            strvalue = (String) newValue;
            SystemProperties.set(SPECTRUM_SYSTEM_PROPERTY, strvalue);
            return true;
	}

        if (preference == mSliderTop) {
            file = KernelControl.KEYCODE_SLIDER_TOP;
        } else if (preference == mSliderMiddle) {
            file = KernelControl.KEYCODE_SLIDER_MIDDLE;
        } else if (preference == mSliderBottom) {
            file = KernelControl.KEYCODE_SLIDER_BOTTOM;
        } else if (preference == mSliderSwap) {
            Boolean value = (Boolean) newValue;
            FileUtils.writeLine(KernelControl.SLIDER_SWAP_NODE, value ? "1" : "0");
            return true;
        } else {
            return false;
        }

        FileUtils.writeLine(file, (String) newValue);
        setSummary((ListPreference) preference, file);

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Remove padding around the listview
            getListView().setPadding(0, 0, 0, 0);

        setSummary(mSliderTop, KernelControl.KEYCODE_SLIDER_TOP);
        setSummary(mSliderMiddle, KernelControl.KEYCODE_SLIDER_MIDDLE);
        setSummary(mSliderBottom, KernelControl.KEYCODE_SLIDER_BOTTOM);
    }
}
