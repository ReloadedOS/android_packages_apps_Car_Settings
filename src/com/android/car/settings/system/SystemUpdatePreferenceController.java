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

import static android.content.Context.CARRIER_CONFIG_SERVICE;

import android.car.userlib.CarUserManagerHelper;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.text.TextUtils;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.car.settings.R;
import com.android.car.settings.Utils;
import com.android.car.settings.common.Logger;
import com.android.car.settings.common.NoSetupPreferenceController;

/**
 * Controller which determines if the system update preference should be displayed based on
 * device and user status. When the preference is clicked, this controller broadcasts a client
 * initiated action if an intent is available in carrier-specific telephony configuration.
 *
 * @see CarrierConfigManager#KEY_CI_ACTION_ON_SYS_UPDATE_BOOL
 */
public class SystemUpdatePreferenceController extends NoSetupPreferenceController {

    private static final Logger LOG = new Logger(SystemUpdatePreferenceController.class);

    private final CarUserManagerHelper mCarUserManagerHelper;

    public SystemUpdatePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mCarUserManagerHelper = new CarUserManagerHelper(context);
    }

    @Override
    public int getAvailabilityStatus() {
        if (!mContext.getResources().getBoolean(R.bool.config_show_system_update_settings)) {
            return UNSUPPORTED_ON_DEVICE;
        }
        return mCarUserManagerHelper.isCurrentProcessAdminUser() ? AVAILABLE : DISABLED_FOR_USER;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            Utils.updatePreferenceToSpecificActivityOrRemove(mContext, screen, getPreferenceKey(),
                    Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (TextUtils.equals(getPreferenceKey(), preference.getKey())) {
            CarrierConfigManager configManager = (CarrierConfigManager) mContext.getSystemService(
                    CARRIER_CONFIG_SERVICE);
            PersistableBundle b = configManager.getConfig();
            if (b != null && b.getBoolean(CarrierConfigManager.KEY_CI_ACTION_ON_SYS_UPDATE_BOOL)) {
                ciActionOnSysUpdate(b);
            }
        }
        // Return false as to not block other handlers.
        return false;
    }

    /** Trigger client initiated action (send intent) on system update. */
    private void ciActionOnSysUpdate(PersistableBundle b) {
        String intentStr = b.getString(
                CarrierConfigManager.KEY_CI_ACTION_ON_SYS_UPDATE_INTENT_STRING);
        if (!TextUtils.isEmpty(intentStr)) {
            String extra = b.getString(
                    CarrierConfigManager.KEY_CI_ACTION_ON_SYS_UPDATE_EXTRA_STRING);
            String extraVal = b.getString(
                    CarrierConfigManager.KEY_CI_ACTION_ON_SYS_UPDATE_EXTRA_VAL_STRING);

            Intent intent = new Intent(intentStr);
            if (!TextUtils.isEmpty(extra)) {
                intent.putExtra(extra, extraVal);
            }
            LOG.d("ciActionOnSysUpdate: broadcasting intent " + intentStr + " with extra " + extra
                    + ", " + extraVal);
            mContext.getApplicationContext().sendBroadcast(intent);
        }
    }
}
