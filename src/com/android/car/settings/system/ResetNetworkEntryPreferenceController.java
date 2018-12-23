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

import com.android.car.settings.common.FragmentController;
import com.android.car.settings.common.PreferenceController;
import com.android.car.settings.common.RestrictedPreference;

/** Controller which determines if network reset should be displayed based on user status. */
public class ResetNetworkEntryPreferenceController extends
        PreferenceController<RestrictedPreference> {

    private final CarUserManagerHelper mCarUserManagerHelper;

    public ResetNetworkEntryPreferenceController(Context context, String preferenceKey,
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
        return mCarUserManagerHelper.isCurrentProcessAdminUser() ? AVAILABLE : DISABLED_FOR_USER;
    }
}
