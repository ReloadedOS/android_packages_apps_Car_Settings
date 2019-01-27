/*
 * Copyright (C) 2019 The Android Open Source Project
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

import android.annotation.Nullable;
import android.bluetooth.BluetoothDevice;
import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.drivingstate.CarUxRestrictions;
import android.car.trust.CarTrustAgentEnrollmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.UserHandle;
import android.preference.PreferenceManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;

import com.android.car.settings.R;
import com.android.car.settings.common.FragmentController;
import com.android.car.settings.common.Logger;
import com.android.car.settings.common.PreferenceController;

import java.util.ArrayList;
import java.util.List;

/**
 * Business logic of trusted device list page
 */
public class TrustedDeviceListPreferenceController extends
        PreferenceController<PreferenceGroup> {
    private static final Logger LOG = new Logger(TrustedDeviceListPreferenceController.class);
    private final SharedPreferences mPrefs;
    private final Car mCar;
    @Nullable
    private CarTrustAgentEnrollmentManager mCarTrustAgentEnrollmentManager;
    private final CarTrustAgentEnrollmentManager.CarTrustAgentEnrollmentCallback
            mCarTrustAgentEnrollmentCallback =
            new CarTrustAgentEnrollmentManager.CarTrustAgentEnrollmentCallback() {

                @Override
                public void onEnrollmentHandshakeFailure(BluetoothDevice device, int errorCode) {
                }

                @Override
                public void onAuthStringAvailable(BluetoothDevice device, String authString) {
                }

                @Override
                public void onEscrowTokenAdded(long handle) {
                }

                @Override
                public void onTrustRevoked(long handle, boolean success) {
                    if (success) {
                        refreshUi();
                    }
                }

                @Override
                public void onEscrowTokenActiveStateChanged(long handle, boolean active) {
                    if (active) {
                        refreshUi();
                    }
                }
            };

    public TrustedDeviceListPreferenceController(Context context, String preferenceKey,
            FragmentController fragmentController, CarUxRestrictions uxRestrictions) {
        super(context, preferenceKey, fragmentController, uxRestrictions);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mCar = Car.createCar(context);
        try {
            mCarTrustAgentEnrollmentManager = (CarTrustAgentEnrollmentManager) mCar
                    .getCarManager(
                            Car.CAR_TRUST_AGENT_ENROLLMENT_SERVICE);
            mCarTrustAgentEnrollmentManager.setEnrollmentCallback(mCarTrustAgentEnrollmentCallback);
        } catch (CarNotConnectedException e) {
            LOG.e(e.getMessage(), e);
        }
    }

    @Override
    protected Class<PreferenceGroup> getPreferenceType() {
        return PreferenceGroup.class;
    }


    @Override
    protected void updateState(PreferenceGroup preferenceGroup) {
        List<Preference> updatedList = createTrustDevicePreferenceList();
        if (!isEqual(preferenceGroup, updatedList)) {
            preferenceGroup.removeAll();
            for (Preference trustedDevice : updatedList) {
                preferenceGroup.addPreference(trustedDevice);
            }
        }
        preferenceGroup.setVisible(preferenceGroup.getPreferenceCount() > 0);
    }

    /**
     * Method to compare two lists of preferences, used only by updateState method.
     *
     * @param preferenceGroup   current preference group
     * @param trustedDeviceList updated preference list
     * @return {@code true} when two lists are the same
     */
    private boolean isEqual(PreferenceGroup preferenceGroup,
            List<Preference> trustedDeviceList) {
        if (preferenceGroup.getPreferenceCount() != trustedDeviceList.size()) {
            return false;
        }
        for (Preference p : trustedDeviceList) {
            if (preferenceGroup.findPreference(p.getKey()) == null) {
                return false;
            }
        }
        return true;
    }

    private List<Preference> createTrustDevicePreferenceList() {
        List<Preference> trustedDevicesList = new ArrayList<>();
        List<Integer> handles = new ArrayList<>();
        try {
            handles = mCarTrustAgentEnrollmentManager.getEnrollmentHandlesForUser(
                    UserHandle.myUserId());
        } catch (CarNotConnectedException e) {
            LOG.e(e.getMessage(), e);
        }
        for (Integer handle : handles) {
            String res = mPrefs.getString(String.valueOf(handle), null);
            if (res != null) {
                trustedDevicesList.add(
                        createTrustedDevicePreference(res, String.valueOf(handle)));
            } else {
                LOG.e("Can not find device name for handle: " + handle);
            }
        }
        return trustedDevicesList;
    }

    private Preference createTrustedDevicePreference(String deviceName, String deviceId) {
        Preference preference = new Preference(getContext());
        preference.setIcon(R.drawable.ic_settings_bluetooth);
        preference.setTitle(deviceName);
        preference.setKey(deviceId);
        return preference;
    }
}
