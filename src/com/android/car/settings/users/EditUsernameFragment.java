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
package com.android.car.settings.users;

import android.car.user.CarUserManagerHelper;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;

import com.android.car.settings.R;
import com.android.car.settings.common.BaseFragment;
import com.android.car.settingslib.util.SettingsConstants;

import com.google.android.material.textfield.TextInputEditText;

/**
 * Enables user to edit their username.
 */
public class EditUsernameFragment extends BaseFragment {
    public static final String EXTRA_USER_INFO = "extra_user_info";
    private UserInfo mUserInfo;

    private TextInputEditText mUserNameEditText;
    private Button mOkButton;
    private Button mCancelButton;
    private CarUserManagerHelper mCarUserManagerHelper;

    /**
     * Creates instance of EditUsernameFragment.
     */
    public static EditUsernameFragment newInstance(UserInfo userInfo) {
        EditUsernameFragment
                userSettingsFragment = new EditUsernameFragment();
        Bundle bundle = BaseFragment.getBundle();
        bundle.putInt(EXTRA_ACTION_BAR_LAYOUT, R.layout.action_bar_with_button);
        bundle.putInt(EXTRA_TITLE_ID, R.string.edit_user_name_title);
        bundle.putParcelable(EXTRA_USER_INFO, userInfo);
        bundle.putInt(EXTRA_LAYOUT, R.layout.edit_username_fragment);
        userSettingsFragment.setArguments(bundle);
        return userSettingsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserInfo = getArguments().getParcelable(EXTRA_USER_INFO);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mUserNameEditText = (TextInputEditText) view.findViewById(R.id.user_name_text_edit);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCarUserManagerHelper = new CarUserManagerHelper(getContext());

        showOkButton();
        showCancelButton();
        configureUsernameEditing();
    }

    private void configureUsernameEditing() {
        // Set the User's name.
        mUserNameEditText.setText(mUserInfo.name);
        mUserNameEditText.setEnabled(true);
        mUserNameEditText.setSelectAllOnFocus(true);
        mUserNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    mOkButton.setEnabled(false);
                    mUserNameEditText.setError(getString(R.string.name_input_blank_error));
                } else if (!TextUtils.isGraphic(s)) {
                    mOkButton.setEnabled(false);
                    mUserNameEditText.setError(getString(R.string.name_input_invalid_error));
                } else {
                    mOkButton.setEnabled(true);
                    mUserNameEditText.setError(null);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void showOkButton() {
        // Configure OK button.
        mOkButton = (Button) getActivity().findViewById(R.id.action_button2);
        mOkButton.setVisibility(View.VISIBLE);
        mOkButton.setText(android.R.string.ok);
        mOkButton.setOnClickListener(view -> {
            // Save new user's name.
            mCarUserManagerHelper.setUserName(mUserInfo, mUserNameEditText.getText().toString());
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    SettingsConstants.USER_NAME_SET, 1);
            getActivity().onBackPressed();
        });
    }

    private void showCancelButton() {
        // Configure Cancel button.
        mCancelButton = (Button) getActivity().findViewById(R.id.action_button1);
        mCancelButton.setVisibility(View.VISIBLE);
        mCancelButton.setText(android.R.string.cancel);
        mCancelButton.setOnClickListener(view -> {
            getActivity().onBackPressed();
        });
    }
}
