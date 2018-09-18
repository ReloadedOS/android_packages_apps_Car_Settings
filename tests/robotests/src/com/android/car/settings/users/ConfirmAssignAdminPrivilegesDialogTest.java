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

package com.android.car.settings.users;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.verify;

import android.content.pm.UserInfo;
import android.widget.Button;

import androidx.fragment.app.DialogFragment;

import com.android.car.settings.CarSettingsRobolectricTestRunner;
import com.android.car.settings.R;
import com.android.car.settings.testutils.BaseTestActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;

/**
 * Tests for ConfirmAssignAdminPrivilegesDialog.
 */
@RunWith(CarSettingsRobolectricTestRunner.class)
public class ConfirmAssignAdminPrivilegesDialogTest {
    private static final String CONFIRM_ASSIGN_ADMIN_DIALOG_TAG = "ConfirmAssignAdminDialog";;
    private BaseTestActivity mTestActivity;

    @Before
    public void setUpTestActivity() {
        MockitoAnnotations.initMocks(this);

        mTestActivity = Robolectric.buildActivity(BaseTestActivity.class)
                .setup()
                .get();
    }

    @Test
    public void testConfirmAssignAdminInvokesOnAssignAdminConfirmed() {
        UserInfo testUser = new UserInfo();
        ConfirmAssignAdminPrivilegesDialog dialog = new ConfirmAssignAdminPrivilegesDialog();

        ConfirmAssignAdminPrivilegesDialog.ConfirmAssignAdminListener listener =
                Mockito.mock(ConfirmAssignAdminPrivilegesDialog.ConfirmAssignAdminListener.class);
        dialog.setConfirmAssignAdminListener(listener);
        showDialog(dialog);

        // Invoke confirm assign admin.
        clickPositiveButton(dialog);

        verify(listener).onAssignAdminConfirmed();
        assertThat(isDialogShown()).isFalse(); // Dialog is dismissed.
    }

    @Test
    public void testCancelDismissesDialog() {
        ConfirmAssignAdminPrivilegesDialog dialog = new ConfirmAssignAdminPrivilegesDialog();
        showDialog(dialog);

        assertThat(isDialogShown()).isTrue(); // Dialog is shown.

        // Invoke cancel.
        clickNegativeButton(dialog);

        assertThat(isDialogShown()).isFalse(); // Dialog is dismissed.
    }

    @Test
    public void testNoClickListenerDismissesDialog() {
        ConfirmAssignAdminPrivilegesDialog dialog = new ConfirmAssignAdminPrivilegesDialog();
        showDialog(dialog);

        // Invoke confirm assign admin.
        clickPositiveButton(dialog);

        assertThat(isDialogShown()).isFalse(); // Dialog is dismissed.
    }

    private void showDialog(ConfirmAssignAdminPrivilegesDialog dialog) {
        dialog.show(mTestActivity.getSupportFragmentManager(), CONFIRM_ASSIGN_ADMIN_DIALOG_TAG);
    }

    private boolean isDialogShown() {
        return mTestActivity.getSupportFragmentManager()
                .findFragmentByTag(CONFIRM_ASSIGN_ADMIN_DIALOG_TAG) != null;
    }

    private void clickPositiveButton(DialogFragment dialogFragment) {
        Button positiveButton = (Button) dialogFragment.getDialog().getWindow()
                .findViewById(R.id.positive_button);
        positiveButton.callOnClick();
    }

    private void clickNegativeButton(DialogFragment dialogFragment) {
        Button negativeButton = (Button) dialogFragment.getDialog().getWindow()
                .findViewById(R.id.negative_button);
        negativeButton.callOnClick();
    }
}
