/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.car.settings.datetime;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertThrows;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.car.settings.CarSettingsRobolectricTestRunner;
import com.android.car.settings.common.FragmentController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowApplication;

import java.util.List;

@RunWith(CarSettingsRobolectricTestRunner.class)
public class AutoDatetimeTogglePreferenceControllerTest {

    private static final String PREFERENCE_KEY = "auto_datetime_switch_entry";

    private ShadowApplication mApplication;
    private Context mContext;
    private SwitchPreference mPreference;
    private AutoDatetimeTogglePreferenceController mController;

    @Before
    public void setUp() {
        mApplication = ShadowApplication.getInstance();
        mContext = RuntimeEnvironment.application;
        mController = new AutoDatetimeTogglePreferenceController(mContext, PREFERENCE_KEY,
                mock(FragmentController.class));
        mPreference = new SwitchPreference(mContext);
        mPreference.setKey(mController.getPreferenceKey());
    }

    @Test
    public void testUpdateState_unchecked() {
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.AUTO_TIME, 0);
        mController.updateState(mPreference);
        assertThat(mPreference.isChecked()).isFalse();
    }

    @Test
    public void testUpdateState_checked() {
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.AUTO_TIME, 1);
        mController.updateState(mPreference);
        assertThat(mPreference.isChecked()).isTrue();
    }

    @Test
    public void testUpdateState_wrongPreferenceType() {
        assertThrows(IllegalArgumentException.class,
                () -> mController.updateState(new Preference(mContext)));
    }

    @Test
    public void testOnPreferenceChange_autoTimeZoneSet_shouldSendIntent() {
        mPreference.setChecked(true);
        mController.onPreferenceChange(mPreference, true);

        List<Intent> intentsFired = mApplication.getBroadcastIntents();
        assertThat(intentsFired.size()).isEqualTo(1);
        Intent intentFired = intentsFired.get(0);
        assertThat(intentFired.getAction()).isEqualTo(Intent.ACTION_TIME_CHANGED);
    }

    @Test
    public void testOnPreferenceChange_autoTimeZoneUnset_shouldSendIntent() {
        mPreference.setChecked(false);
        mController.onPreferenceChange(mPreference, false);

        List<Intent> intentsFired = mApplication.getBroadcastIntents();
        assertThat(intentsFired.size()).isEqualTo(1);
        Intent intentFired = intentsFired.get(0);
        assertThat(intentFired.getAction()).isEqualTo(Intent.ACTION_TIME_CHANGED);
    }

    @Test
    public void testOnPreferenceChange_wrongPreferenceType() {
        assertThrows(IllegalArgumentException.class,
                () -> mController.onPreferenceChange(new Preference(mContext), true));
    }
}

