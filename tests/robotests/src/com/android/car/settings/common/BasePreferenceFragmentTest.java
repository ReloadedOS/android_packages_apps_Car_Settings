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

package com.android.car.settings.common;

import static com.android.car.settings.common.BasePreferenceController.CONDITIONALLY_UNAVAILABLE;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.car.drivingstate.CarUxRestrictions;
import android.content.Context;

import androidx.preference.Preference;

import com.android.car.settings.CarSettingsRobolectricTestRunner;
import com.android.car.settings.R;
import com.android.car.settings.testutils.BaseTestActivity;
import com.android.car.settings.testutils.FragmentController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

/** Unit test for {@link BasePreferenceFragment}. */
@RunWith(CarSettingsRobolectricTestRunner.class)
public class BasePreferenceFragmentTest {

    private static final String SPY_CONTROLLER_KEY = "spy_controller_key";
    private static final String LIFECYCLE_CONTROLLER_KEY = "lifecycle_controller_key";
    private static final String XML_CONTROLLER_KEY = "xml_controller_key";

    private FragmentController<TestBasePreferenceFragment> mFragmentController;
    private TestBasePreferenceFragment mFragment;

    @Before
    public void setUp() {
        mFragmentController = FragmentController.of(new TestBasePreferenceFragment());
        mFragment = mFragmentController.get();
    }

    @Test
    public void use_returnsController() {
        mFragmentController.setup();

        assertThat(mFragment.use(FakePreferenceController.class, XML_CONTROLLER_KEY)).isNotNull();
    }

    @Test
    public void onAttach_registersLifecycleObservers() {
        mFragmentController.create();
        LifecycleFakePreferenceController controller = mFragment.use(
                LifecycleFakePreferenceController.class,
                LIFECYCLE_CONTROLLER_KEY);

        assertThat(controller.mOnCreateCalled).isTrue();

        mFragmentController.destroy();

        assertThat(controller.mOnDestroyCalled).isTrue();
    }

    @Test
    public void onStart_callsDisplayPreference() {
        mFragmentController.create().start();

        verify(mFragment.mSpyPreferenceController).displayPreference(
                mFragment.getPreferenceScreen());
    }

    @Test
    public void onStart_initializesUxRestrictions() {
        mFragmentController.create().start();

        CarUxRestrictions initialUxRestrictions =
                ((UxRestrictionsProvider) mFragment.requireActivity()).getCarUxRestrictions();
        verify(mFragment.mSpyPreferenceController).onUxRestrictionsChanged(
                argThat(restrictionInfo -> restrictionInfo.isSameRestrictions(
                        initialUxRestrictions)));
    }

    @Test
    public void onStart_availableController_callsUpdateState() {
        mFragmentController.create().start();

        verify(mFragment.mSpyPreferenceController).updateState(
                argThat(pref -> pref.getKey().equals(SPY_CONTROLLER_KEY)));
    }

    @Test
    public void onStart_unavailableController_doesNotCallUpdateState() {
        mFragmentController.create();
        mFragment.mSpyPreferenceController.setAvailabilityStatus(CONDITIONALLY_UNAVAILABLE);

        mFragmentController.start();

        verify(mFragment.mSpyPreferenceController, never()).updateState(any());
    }

    @Test
    public void onStart_preferenceNotFound_doesNotCallUpdateState() {
        mFragmentController.create();
        when(mFragment.mSpyPreferenceController.getPreferenceKey()).thenReturn("not_found_key");

        mFragmentController.start();

        verify(mFragment.mSpyPreferenceController, never()).updateState(any());
    }

    @Test
    public void onPreferenceTreeClick_callsHandlePreferenceTreeClick() {
        mFragmentController.setup();
        Preference firstPreference = mFragment.getPreferenceScreen().getPreference(0);

        mFragment.onPreferenceTreeClick(firstPreference);

        verify(mFragment.mSpyPreferenceController).handlePreferenceTreeClick(firstPreference);
    }

    @Test
    public void onUxRestrictionsChanged_callsOnUxRestrictionChanged() {
        CarUxRestrictions restrictionInfo = new CarUxRestrictions.Builder(/* reqOpt= */ true,
                CarUxRestrictions.UX_RESTRICTIONS_NO_KEYBOARD, /* timestamp= */ 0).build();
        mFragmentController.setup();
        ((BaseTestActivity) mFragment.requireActivity()).setCarUxRestrictions(restrictionInfo);

        mFragment.onUxRestrictionsChanged(restrictionInfo);

        verify(mFragment.mSpyPreferenceController).onUxRestrictionsChanged(restrictionInfo);
    }

    @Test
    public void onUxRestrictionsChanged_noValueChange_doesNotCallsOnUxRestrictionChanged() {
        mFragmentController.setup();
        CarUxRestrictions initialUxRestrictions =
                ((UxRestrictionsProvider) mFragment.requireActivity()).getCarUxRestrictions();

        mFragment.onUxRestrictionsChanged(initialUxRestrictions);

        // Only called once in setup. No additional call.
        verify(mFragment.mSpyPreferenceController).onUxRestrictionsChanged(
                argThat(restrictionInfo -> restrictionInfo.isSameRestrictions(
                        initialUxRestrictions)));
    }

    @Test
    public void onUxRestrictionsChanged_callsDisplayPreference() {
        CarUxRestrictions restrictionInfo = new CarUxRestrictions.Builder(/* reqOpt= */ true,
                CarUxRestrictions.UX_RESTRICTIONS_NO_KEYBOARD, /* timestamp= */ 0).build();
        mFragmentController.setup();
        ((BaseTestActivity) mFragment.requireActivity()).setCarUxRestrictions(restrictionInfo);

        mFragment.onUxRestrictionsChanged(restrictionInfo);

        // Times 2: onStart from setup, onUxRestrictionsChanged.
        verify(mFragment.mSpyPreferenceController, times(2)).displayPreference(
                mFragment.getPreferenceScreen());
    }

    @Test
    public void onUxRestrictionsChanged_callsUpdateState() {
        CarUxRestrictions restrictionInfo = new CarUxRestrictions.Builder(/* reqOpt= */ true,
                CarUxRestrictions.UX_RESTRICTIONS_NO_KEYBOARD, /* timestamp= */ 0).build();
        mFragmentController.setup();
        ((BaseTestActivity) mFragment.requireActivity()).setCarUxRestrictions(restrictionInfo);

        mFragment.onUxRestrictionsChanged(restrictionInfo);

        // Times 2: onStart from setup, onUxRestrictionsChanged.
        verify(mFragment.mSpyPreferenceController, times(2)).updateState(
                argThat(pref -> pref.getKey().equals(SPY_CONTROLLER_KEY)));
    }

    /** Concrete {@link BasePreferenceFragment} for exercising base methods. */
    public static class TestBasePreferenceFragment extends BasePreferenceFragment {

        FakePreferenceController mSpyPreferenceController;

        @Override
        protected int getPreferenceScreenResId() {
            return R.xml.base_preference_fragment;
        }

        @Override
        protected List<BasePreferenceController> createPreferenceControllers(Context context) {
            mSpyPreferenceController = spy(
                    new FakePreferenceController(context, SPY_CONTROLLER_KEY));
            return Collections.singletonList(mSpyPreferenceController);
        }
    }
}
