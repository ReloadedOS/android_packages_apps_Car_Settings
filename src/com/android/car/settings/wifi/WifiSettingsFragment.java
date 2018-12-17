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
 * limitations under the License.
 */
package com.android.car.settings.wifi;

import android.car.drivingstate.CarUxRestrictions;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import androidx.car.widget.PagedListView;

import com.android.car.settings.R;
import com.android.car.settings.common.BaseFragment;
import com.android.car.settings.common.CarUxRestrictionsHelper;

/**
 * Main page to host Wifi related preferences.
 */
public class WifiSettingsFragment extends BaseFragment implements CarWifiManager.Listener {
    private CarWifiManager mCarWifiManager;
    private AccessPointListAdapter mAdapter;
    private Switch mWifiSwitch;
    private ProgressBar mProgressBar;
    private PagedListView mListView;
    private TextView mMessageView;
    private ViewSwitcher mViewSwitcher;
    private boolean mShowSavedApOnly;

    @Override
    @LayoutRes
    protected int getActionBarLayoutId() {
        return R.layout.action_bar_with_toggle;
    }

    @Override
    @LayoutRes
    protected int getLayoutId() {
        return R.layout.wifi_list;
    }

    @Override
    @StringRes
    protected int getTitleId() {
        return R.string.wifi_settings;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCarWifiManager = new CarWifiManager(getContext(), /* listener= */ this);

        mProgressBar = requireActivity().findViewById(R.id.progress_bar);
        mListView = getView().findViewById(R.id.list);
        mMessageView = getView().findViewById(R.id.message);
        mViewSwitcher = getView().findViewById(R.id.view_switcher);
        setupWifiSwitch();
        if (mCarWifiManager.isWifiEnabled()) {
            showList();
            setProgressBarVisible(true);
        } else {
            showMessage(R.string.wifi_disabled);
        }
        mAdapter = new AccessPointListAdapter(
                getContext(),
                mCarWifiManager,
                mShowSavedApOnly
                        ? mCarWifiManager.getSavedAccessPoints()
                        : mCarWifiManager.getAllAccessPoints(),
                getFragmentController());
        mAdapter.showAddNetworkRow(!mShowSavedApOnly);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mCarWifiManager.start();
        onWifiStateChanged(mCarWifiManager.getWifiState());
    }

    @Override
    public void onStop() {
        super.onStop();
        mCarWifiManager.stop();
        setProgressBarVisible(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCarWifiManager.destroy();
    }

    @Override
    public void onAccessPointsChanged() {
        refreshData();
    }

    @Override
    public void onWifiStateChanged(int state) {
        mWifiSwitch.setChecked(mCarWifiManager.isWifiEnabled());
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                showList();
                setProgressBarVisible(true);
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                setProgressBarVisible(false);
                showMessage(R.string.wifi_disabled);
                break;
            default:
                showList();
        }
    }

    /**
     * This fragment will adapt to restriction, so can always be shown.
     */
    @Override
    public boolean canBeShown(CarUxRestrictions carUxRestrictions) {
        return true;
    }

    @Override
    public void onUxRestrictionsChanged(CarUxRestrictions restrictionInfo) {
        mShowSavedApOnly = CarUxRestrictionsHelper.isNoSetup(restrictionInfo);
        refreshData();
    }

    private void setProgressBarVisible(boolean visible) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void refreshData() {
        if (mAdapter != null) {
            mAdapter.showAddNetworkRow(!mShowSavedApOnly);
            mAdapter.updateAccessPoints(mShowSavedApOnly
                    ? mCarWifiManager.getSavedAccessPoints()
                    : mCarWifiManager.getAllAccessPoints());
            // if the list is empty, keep showing the progress bar, the list should reset
            // every couple seconds.
            // TODO: Consider show a message in the list view place.
            if (!mAdapter.isEmpty()) {
                setProgressBarVisible(false);
            }
        }
        if (mCarWifiManager != null) {
            mWifiSwitch.setChecked(mCarWifiManager.isWifiEnabled());
        }
    }

    private void showMessage(@StringRes int resId) {
        if (mViewSwitcher.getCurrentView() != mMessageView) {
            mViewSwitcher.showNext();
        }
        mMessageView.setText(getResources().getString(resId));
    }

    private void showList() {
        if (mViewSwitcher.getCurrentView() != mListView) {
            mViewSwitcher.showPrevious();
        }
    }

    private void setupWifiSwitch() {
        mWifiSwitch = (Switch) getActivity().findViewById(R.id.toggle_switch);
        mWifiSwitch.setChecked(mCarWifiManager.isWifiEnabled());
        mWifiSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mWifiSwitch.isChecked() != mCarWifiManager.isWifiEnabled()) {
                mCarWifiManager.setWifiEnabled(mWifiSwitch.isChecked());
            }
        });
    }
}
