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
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.preference.Preference;

import com.android.car.settings.R;
import com.android.car.settings.common.FragmentController;
import com.android.car.settings.common.PreferenceController;

import java.util.StringJoiner;

/** Controller to determine which items appear as resetable within the reset network description. */
public class ResetNetworkItemsPreferenceController extends PreferenceController<Preference> {

    public ResetNetworkItemsPreferenceController(Context context, String preferenceKey,
            FragmentController fragmentController, CarUxRestrictions uxRestrictions) {
        super(context, preferenceKey, fragmentController, uxRestrictions);
    }

    @Override
    protected Class<Preference> getPreferenceType() {
        return Preference.class;
    }

    @Override
    protected void updateState(Preference preference) {
        preference.setSummary(getSummary());
    }

    private CharSequence getSummary() {
        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        if (hasFeature(PackageManager.FEATURE_WIFI)) {
            joiner.add(getContext().getString(R.string.reset_network_item_wifi));
        }
        if (hasFeature(PackageManager.FEATURE_TELEPHONY)) {
            joiner.add(getContext().getString(R.string.reset_network_item_mobile));
        }
        if (hasFeature(PackageManager.FEATURE_BLUETOOTH)) {
            joiner.add(getContext().getString(R.string.reset_network_item_bluetooth));
        }
        return joiner.toString();
    }

    private boolean hasFeature(String feature) {
        return getContext().getPackageManager().hasSystemFeature(feature);
    }
}
