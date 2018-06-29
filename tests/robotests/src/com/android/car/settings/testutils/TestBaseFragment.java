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

package com.android.car.settings.testutils;

import android.os.Bundle;

import com.android.car.settings.R;
import com.android.car.settings.common.BaseFragment;

/**
 * Simple implementation of BaseFragment to be used in tests that require a fragment but don't care
 * which fragment is being used.
 */
public class TestBaseFragment extends BaseFragment {
    public static TestBaseFragment newInstance() {
        TestBaseFragment testFragment = new TestBaseFragment();
        Bundle bundle = BaseFragment.getBundle();
        bundle.putInt(EXTRA_TITLE_ID, R.string.users_list_title);
        bundle.putInt(EXTRA_LAYOUT, R.layout.list_fragment); // random layout.
        bundle.putInt(EXTRA_ACTION_BAR_LAYOUT, R.layout.action_bar_with_button); //random layout.
        testFragment.setArguments(bundle);
        return testFragment;
    }
}
