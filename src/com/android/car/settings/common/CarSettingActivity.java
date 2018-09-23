/*
 * Copyright (C) 2017 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.car.settings.common;

import android.annotation.Nullable;
import android.car.drivingstate.CarUxRestrictions;
import android.car.drivingstate.CarUxRestrictionsManager.OnUxRestrictionsChangedListener;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager.OnBackStackChangedListener;

import com.android.car.settings.R;
import com.android.car.settings.quicksettings.QuickSettingFragment;
import com.android.car.settings.wifi.WifiSettingsFragment;

/**
 * Base activity class for car settings, provides a action bar with a back button that goes to
 * previous activity.
 */
public class CarSettingActivity extends FragmentActivity implements FragmentController,
        OnUxRestrictionsChangedListener, UxRestrictionsProvider, OnBackStackChangedListener {

    private CarUxRestrictionsHelper mUxRestrictionsHelper;
    private View mRestrictedMessage;
    // Default to minimum restriction.
    private CarUxRestrictions mCarUxRestrictions = new CarUxRestrictions.Builder(
            /* reqOpt= */ true,
            CarUxRestrictions.UX_RESTRICTIONS_BASELINE,
            /* timestamp= */ 0
    ).build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_setting_activity);
        if (mUxRestrictionsHelper == null) {
            mUxRestrictionsHelper = new CarUxRestrictionsHelper(/* context= */ this, /* listener= */
                    this);
        }
        mUxRestrictionsHelper.start();
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        mRestrictedMessage = findViewById(R.id.restricted_message);
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (Settings.ACTION_WIFI_SETTINGS.equals(action)
                    || WifiManager.ACTION_PICK_WIFI_NETWORK.equals(action)) {
                launchFragment(new WifiSettingsFragment());
                return;
            }
        }

        if (getCurrentFragment() == null) {
            launchFragment(new QuickSettingFragment());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUxRestrictionsHelper.stop();
        mUxRestrictionsHelper = null;
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideKeyboard();
        // if the backstack is empty, finish the activity.
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            finish();
        }
    }

    @Override
    public void launchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.trans_right_in,
                        R.animator.trans_left_out,
                        R.animator.trans_left_in,
                        R.animator.trans_right_out)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void goBack() {
        onBackPressed();
    }

    @Override
    public void showBlockingMessage() {
        Toast.makeText(
                this, R.string.restricted_while_driving, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUxRestrictionsChanged(CarUxRestrictions restrictionInfo) {
        mCarUxRestrictions = restrictionInfo;
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof OnUxRestrictionsChangedListener) {
            ((OnUxRestrictionsChangedListener) currentFragment)
                    .onUxRestrictionsChanged(restrictionInfo);
        }
        updateBlockingView(currentFragment);
    }

    @Override
    public CarUxRestrictions getCarUxRestrictions() {
        return mCarUxRestrictions;
    }

    @Override
    public void onBackStackChanged() {
        updateBlockingView(getCurrentFragment());
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    private void updateBlockingView(@Nullable Fragment currentFragment) {
        if (currentFragment instanceof BaseFragment) {
            boolean canBeShown = ((BaseFragment) currentFragment).canBeShown(mCarUxRestrictions);
            mRestrictedMessage.setVisibility(canBeShown ? View.GONE : View.VISIBLE);
        }
    }
}
