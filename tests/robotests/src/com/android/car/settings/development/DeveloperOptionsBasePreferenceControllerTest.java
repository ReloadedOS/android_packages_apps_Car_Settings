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

package com.android.car.settings.development;

import static com.android.car.settings.development.DevelopmentSettingsUtil.DEVELOPMENT_SETTINGS_CHANGED_ACTION;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.car.userlib.CarUserManagerHelper;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.UserManager;
import android.provider.Settings;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.car.settings.CarSettingsRobolectricTestRunner;
import com.android.car.settings.common.FragmentController;
import com.android.car.settings.testutils.ShadowCarUserManagerHelper;
import com.android.car.settings.testutils.ShadowLocalBroadcastManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(CarSettingsRobolectricTestRunner.class)
@Config(shadows = {ShadowCarUserManagerHelper.class, ShadowLocalBroadcastManager.class})
public class DeveloperOptionsBasePreferenceControllerTest {

    private static class TestDeveloperOptionsPreferenceController extends
            DeveloperOptionsBasePreferenceController {

        private boolean mOnEnabledCalled;
        private boolean mOnDisabledCalled;

        TestDeveloperOptionsPreferenceController(Context context,
                String preferenceKey,
                FragmentController fragmentController) {
            super(context, preferenceKey, fragmentController);
            mOnEnabledCalled = false;
            mOnDisabledCalled = false;
        }

        @Override
        protected void onDeveloperOptionsEnabled() {
            mOnEnabledCalled = true;
        }

        @Override
        protected void onDeveloperOptionsDisabled() {
            mOnDisabledCalled = true;
        }

        public boolean isOnEnabledCalled() {
            return mOnEnabledCalled;
        }

        public boolean isOnDisabledCalled() {
            return mOnDisabledCalled;
        }
    }

    private static final String PREFERENCE_KEY = "preference_key";

    private Context mContext;
    private TestDeveloperOptionsPreferenceController mController;
    @Mock
    private CarUserManagerHelper mCarUserManagerHelper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ShadowCarUserManagerHelper.setMockInstance(mCarUserManagerHelper);
        mContext = RuntimeEnvironment.application;
        mController = new TestDeveloperOptionsPreferenceController(mContext, PREFERENCE_KEY,
                mock(FragmentController.class));

        // Setup admin user who is able to enable developer settings.
        UserInfo userInfo = new UserInfo();
        when(mCarUserManagerHelper.isCurrentProcessAdminUser()).thenReturn(true);
        when(mCarUserManagerHelper.isCurrentProcessDemoUser()).thenReturn(false);
        when(mCarUserManagerHelper.getCurrentProcessUserInfo()).thenReturn(userInfo);
        new CarUserManagerHelper(mContext).setUserRestriction(userInfo,
                UserManager.DISALLOW_DEBUGGING_FEATURES, false);
    }

    @After
    public void tearDown() {
        ShadowCarUserManagerHelper.reset();
        ShadowLocalBroadcastManager.reset();
    }

    @Test
    public void testOnCreate_receiverRegistered() {
        assertThat(ShadowLocalBroadcastManager.getRegisteredBroadcastReceivers()).isEmpty();
        mController.onCreate();
        assertThat(ShadowLocalBroadcastManager.getRegisteredBroadcastReceivers()).isNotEmpty();
    }

    @Test
    public void testOnDestroy_receiverUnregistered() {
        mController.onCreate();
        mController.onDestroy();
        assertThat(ShadowLocalBroadcastManager.getRegisteredBroadcastReceivers()).isEmpty();
    }

    @Test
    public void testReceiveBroadcast_receiverRegistered_onEnabledCalled() {
        mController.onCreate();
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(
                new Intent(DEVELOPMENT_SETTINGS_CHANGED_ACTION));
        assertThat(mController.isOnEnabledCalled()).isTrue();
    }

    @Test
    public void testReceiveBroadcast_receiverRegistered_onDisabledCalled() {
        mController.onCreate();
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(
                new Intent(DEVELOPMENT_SETTINGS_CHANGED_ACTION));
        assertThat(mController.isOnDisabledCalled()).isTrue();
    }

    @Test
    public void testReceiveBroadcast_receiverUnregistered_onEnabledNotCalled() {
        mController.onCreate();
        mController.onDestroy();
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(
                new Intent(DEVELOPMENT_SETTINGS_CHANGED_ACTION));
        assertThat(mController.isOnEnabledCalled()).isFalse();
    }

    @Test
    public void testReceiveBroadcast_receiverUnregistered_onDisabledNotCalled() {
        mController.onCreate();
        mController.onDestroy();
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(
                new Intent(DEVELOPMENT_SETTINGS_CHANGED_ACTION));
        assertThat(mController.isOnDisabledCalled()).isFalse();
    }
}
