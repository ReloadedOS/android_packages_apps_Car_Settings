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
import static com.android.car.settings.common.BasePreferenceController.DISABLED_FOR_USER;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.when;

import android.car.userlib.CarUserManagerHelper;
import android.provider.Settings;

import com.android.car.settings.CarSettingsRobolectricTestRunner;
import com.android.car.settings.testutils.ShadowCarUserManagerHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/** Unit test for {@link MasterClearEntryPreferenceController}. */
@RunWith(CarSettingsRobolectricTestRunner.class)
@Config(shadows = {ShadowCarUserManagerHelper.class})
public class MasterClearEntryPreferenceControllerTest {

    private static final String PREFERENCE_KEY = "preference_key";

    @Mock
    private CarUserManagerHelper mCarUserManagerHelper;
    private MasterClearEntryPreferenceController mController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ShadowCarUserManagerHelper.setMockInstance(mCarUserManagerHelper);

        mController = new MasterClearEntryPreferenceController(RuntimeEnvironment.application,
                PREFERENCE_KEY);
    }

    @After
    public void tearDown() {
        Settings.Global.putInt(RuntimeEnvironment.application.getContentResolver(),
                Settings.Global.DEVICE_DEMO_MODE, 0);
    }

    @Test
    public void getAvailabilityStatus_nonAdminUser_disabledForUser() {
        when(mCarUserManagerHelper.isCurrentProcessAdminUser()).thenReturn(false);

        assertThat(mController.getAvailabilityStatus()).isEqualTo(DISABLED_FOR_USER);
    }

    @Test
    public void getAvailabilityStatus_adminUser_available() {
        when(mCarUserManagerHelper.isCurrentProcessAdminUser()).thenReturn(true);

        assertThat(mController.getAvailabilityStatus()).isEqualTo(AVAILABLE);
    }

    @Test
    public void getAvailabilityStatus_demoMode_demoUser_available() {
        when(mCarUserManagerHelper.isCurrentProcessAdminUser()).thenReturn(false);
        when(mCarUserManagerHelper.isCurrentProcessDemoUser()).thenReturn(true);
        Settings.Global.putInt(RuntimeEnvironment.application.getContentResolver(),
                Settings.Global.DEVICE_DEMO_MODE, 1);

        assertThat(mController.getAvailabilityStatus()).isEqualTo(AVAILABLE);
    }
}
