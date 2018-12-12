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

package com.android.car.settings.system;

import static com.android.car.settings.common.BasePreferenceController.AVAILABLE;
import static com.android.car.settings.common.BasePreferenceController.CONDITIONALLY_UNAVAILABLE;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.car.userlib.CarUserManagerHelper;
import android.content.Context;
import android.content.pm.UserInfo;
import android.os.UserManager;
import android.provider.Settings;

import androidx.preference.Preference;

import com.android.car.settings.CarSettingsRobolectricTestRunner;
import com.android.car.settings.common.FragmentController;
import com.android.car.settings.testutils.ShadowCarUserManagerHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(CarSettingsRobolectricTestRunner.class)
@Config(shadows = {ShadowCarUserManagerHelper.class})
public class DeveloperOptionsEntryPreferenceControllerTest {

    private static final String PREFERENCE_KEY = "preference_key";

    private Context mContext;
    private DeveloperOptionsEntryPreferenceController mController;
    private Preference mPreference;
    private UserInfo mUserInfo;
    @Mock
    private CarUserManagerHelper mShadowCarUserManagerHelper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ShadowCarUserManagerHelper.setMockInstance(mShadowCarUserManagerHelper);
        mContext = RuntimeEnvironment.application;
        mController = new DeveloperOptionsEntryPreferenceController(mContext, PREFERENCE_KEY,
                mock(FragmentController.class));
        mPreference = new Preference(mContext);
        mPreference.setKey(PREFERENCE_KEY);

        // Setup admin user who is able to enable developer settings.
        mUserInfo = new UserInfo();
        when(mShadowCarUserManagerHelper.isCurrentProcessAdminUser()).thenReturn(true);
        when(mShadowCarUserManagerHelper.isCurrentProcessDemoUser()).thenReturn(false);
        when(mShadowCarUserManagerHelper.getCurrentProcessUserInfo()).thenReturn(mUserInfo);
        new CarUserManagerHelper(mContext).setUserRestriction(mUserInfo,
                UserManager.DISALLOW_DEBUGGING_FEATURES, false);
    }

    @After
    public void tearDown() {
        ShadowCarUserManagerHelper.reset();
    }

    @Test
    public void testGetAvailabilityStatus_devOptionsEnabled_isAvailable() {
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
        assertThat(mController.getAvailabilityStatus()).isEqualTo(AVAILABLE);
    }

    @Test
    public void testGetAvailabilityStatus_devOptionsDisabled_isUnavailable() {
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
        assertThat(mController.getAvailabilityStatus()).isEqualTo(CONDITIONALLY_UNAVAILABLE);
    }

    @Test
    public void testGetAvailabilityStatus_devOptionsEnabled_hasUserRestriction_isUnavailable() {
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
        new CarUserManagerHelper(mContext).setUserRestriction(mUserInfo,
                UserManager.DISALLOW_DEBUGGING_FEATURES, true);
        assertThat(mController.getAvailabilityStatus()).isEqualTo(CONDITIONALLY_UNAVAILABLE);
    }
}
