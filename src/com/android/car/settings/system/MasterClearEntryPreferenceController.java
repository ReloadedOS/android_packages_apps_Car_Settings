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

import android.car.drivingstate.CarUxRestrictions;
import android.car.userlib.CarUserManagerHelper;
import android.content.Context;
import android.os.UserManager;

import com.android.car.settings.common.FragmentController;
import com.android.car.settings.common.PreferenceController;
import com.android.car.settings.common.RestrictedPreference;

/**
 * Controller which determines if master clear (aka "factory reset") should be displayed based on
 * user status.
 */
public class MasterClearEntryPreferenceController extends
        PreferenceController<RestrictedPreference> {

    private final CarUserManagerHelper mCarUserManagerHelper;

    public MasterClearEntryPreferenceController(Context context, String preferenceKey,
            FragmentController fragmentController, CarUxRestrictions uxRestrictions) {
        super(context, preferenceKey, fragmentController, uxRestrictions);
        mCarUserManagerHelper = new CarUserManagerHelper(context);
    }

    @Override
    protected Class<RestrictedPreference> getPreferenceType() {
        return RestrictedPreference.class;
    }

    @Override
    public int getAvailabilityStatus() {
        return (mCarUserManagerHelper.isCurrentProcessAdminUser() || isDemoUser()) ? AVAILABLE
                : DISABLED_FOR_USER;
    }

    private boolean isDemoUser() {
        return UserManager.isDeviceInDemoMode(getContext())
                && mCarUserManagerHelper.isCurrentProcessDemoUser();
    }
}
