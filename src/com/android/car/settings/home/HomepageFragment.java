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

package com.android.car.settings.home;

import android.content.Context;

import androidx.annotation.XmlRes;
import androidx.loader.app.LoaderManager;

import com.android.car.settings.R;
import com.android.car.settings.common.BasePreferenceFragment;
import com.android.car.settings.suggestions.SuggestionsPreferenceController;

/**
 * Homepage for settings for car.
 */
public class HomepageFragment extends BasePreferenceFragment {

    @Override
    @XmlRes
    protected int getPreferenceScreenResId() {
        return R.xml.homepage_fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        use(SuggestionsPreferenceController.class, "suggestions").setLoaderManager(
                LoaderManager.getInstance(/* owner= */ this));
    }
}
