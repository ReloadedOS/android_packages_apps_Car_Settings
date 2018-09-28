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

package com.android.car.settings.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.MainThread;
import androidx.lifecycle.LiveData;

import com.android.car.settings.common.Logger;

/**
 * {@link LiveData} which reflects the current device discovery state of the
 * {@link BluetoothAdapter}. If the device does not support Bluetooth, no events will propagate
 * to observers.
 *
 * @see BluetoothAdapter#ACTION_DISCOVERY_STARTED
 * @see BluetoothAdapter#ACTION_DISCOVERY_FINISHED
 */
class BluetoothDiscoveryStateLiveData extends LiveData<Boolean> {

    private static final Logger LOG = new Logger(BluetoothDiscoveryStateLiveData.class);

    private final Context mContext;
    private final BluetoothAdapter mBluetoothAdapter;
    private final IntentFilter mFilter = new IntentFilter();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setCurrentState();
        }
    };

    @MainThread
    BluetoothDiscoveryStateLiveData(Context context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            LOG.w("Bluetooth is not supported on this device");
            mContext = null;
            return;
        }
        mContext = context;
        mFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        mFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        setCurrentState();
    }

    @Override
    protected void onActive() {
        super.onActive();
        if (mBluetoothAdapter == null) {
            return;
        }
        setCurrentState();
        mContext.registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        if (mBluetoothAdapter == null) {
            return;
        }
        mContext.unregisterReceiver(mReceiver);
    }

    private void setCurrentState() {
        setValue(mBluetoothAdapter.isDiscovering());
    }
}
