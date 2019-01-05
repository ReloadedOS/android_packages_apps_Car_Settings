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

package com.android.car.settings.security;

import android.car.userlib.CarUserManagerHelper;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.car.settings.R;
import com.android.car.settings.common.FragmentController;
import com.android.car.settings.common.NoSetupPreferenceController;

/**
 * Business Logic for security lock preferences. It can be extended to change which fragment should
 * be opened when clicked.
 */
public abstract class LockTypeBasePreferenceController extends NoSetupPreferenceController {

    private final CarUserManagerHelper mCarUserManagerHelper;
    private String mCurrentPassword;
    private int mCurrentPasswordQuality;
    private Preference mPreference;

    public LockTypeBasePreferenceController(Context context, String preferenceKey,
            FragmentController fragmentController) {
        super(context, preferenceKey, fragmentController);
        mCarUserManagerHelper = new CarUserManagerHelper(context);
    }

    /**
     * Fragment specified here will be opened when the Preference is clicked. Return null to prevent
     * navigation.
     */
    protected abstract Fragment fragmentToOpen();

    /**
     * If the current password quality is one of the values returned by this function, the
     * controller will identify as having the current lock.
     */
    protected abstract int[] allowedPasswordQualities();


    /** Sets the quality of the current password. */
    public void setCurrentPasswordQuality(int currentPasswordQuality) {
        mCurrentPasswordQuality = currentPasswordQuality;
    }

    /** Gets whether the preference related to this controller is the current lock type. */
    protected boolean isCurrentLock() {
        for (int allowedQuality : allowedPasswordQualities()) {
            if (mCurrentPasswordQuality == allowedQuality) {
                return true;
            }
        }
        return false;
    }

    /** Sets the current password so it can be provided in the bundle in the fragment. */
    public void setCurrentPassword(String currentPassword) {
        mCurrentPassword = currentPassword;
    }

    /** Gets the current password. */
    protected String getCurrentPassword() {
        return mCurrentPassword;
    }

    /** Gets the preference controlled by this controller. */
    protected Preference getCurrentPreference() {
        return mPreference;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
    }

    @Override
    public CharSequence getSummary() {
        return isCurrentLock() ? mContext.getString(R.string.current_screen_lock) : "";
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (mPreference != preference) {
            return false;
        }

        Fragment fragment = fragmentToOpen();
        if (fragment != null) {
            if (!TextUtils.isEmpty(mCurrentPassword)) {
                Bundle args = fragment.getArguments();
                if (args == null) {
                    args = new Bundle();
                }
                args.putString(PasswordHelper.EXTRA_CURRENT_SCREEN_LOCK, mCurrentPassword);
                fragment.setArguments(args);
            }
            getFragmentController().launchFragment(fragment);
            return true;
        }
        return false;
    }

    @Override
    public int getAvailabilityStatus() {
        return mCarUserManagerHelper.isCurrentProcessGuestUser() ? DISABLED_FOR_USER : AVAILABLE;
    }
}
