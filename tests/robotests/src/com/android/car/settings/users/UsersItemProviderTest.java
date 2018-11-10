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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.robolectric.RuntimeEnvironment.application;

import android.car.userlib.CarUserManagerHelper;
import android.content.pm.UserInfo;
import android.view.View;

import com.android.car.settings.CarSettingsRobolectricTestRunner;
import com.android.car.settings.R;
import com.android.car.settings.testutils.ShadowTextListItem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for UsersItemProviderTest.
 */
@RunWith(CarSettingsRobolectricTestRunner.class)
@Config(shadows = { ShadowTextListItem.class })
public class UsersItemProviderTest {
    @Mock
    private CarUserManagerHelper mCarUserManagerHelper;
    @Mock
    private UsersItemProvider.UserClickListener mUserClickListener;

    @Before
    public void setUpMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFirstUserIsCurrentUser() {
        UserInfo currentUser = new UserInfo();
        String testName = "test_user_name";
        currentUser.name = testName;
        doReturn(currentUser).when(mCarUserManagerHelper).getCurrentProcessUserInfo();
        doReturn(true).when(mCarUserManagerHelper).isCurrentProcessUser(currentUser);

        UsersItemProvider usersItemProvider = createProvider();

        ShadowTextListItem textListItem = Shadow.extract(usersItemProvider.get(0));
        assertThat(textListItem.getTitle())
                .isEqualTo(application.getString(R.string.current_user_name, testName));
    }

    @Test
    public void testGuestUsersNotShown() {
        UserInfo user10 = new UserInfo(/* id= */ 10, "User 10", /* flags= */ 0);
        UserInfo user11 = new UserInfo(/* id= */ 11, "User 11", UserInfo.FLAG_GUEST);
        UserInfo user12 = new UserInfo(/* id= */ 12, "User 12", UserInfo.FLAG_GUEST);
        UserInfo user13 = new UserInfo(/* id= */ 13, "User 13", /* flags= */ 0);
        UserInfo user14 = new UserInfo(/* id= */ 14, "User 14", /* flags= */ 0);
        List<UserInfo> users = Arrays.asList(user10, user11, user12, user13);

        doReturn(user14).when(mCarUserManagerHelper).getCurrentProcessUserInfo();
        doReturn(true).when(mCarUserManagerHelper).isCurrentProcessUser(user14);
        doReturn(users).when(mCarUserManagerHelper).getAllSwitchableUsers();

        UsersItemProvider provider = createProvider();

        assertThat(provider.size()).isEqualTo(4); // 3 real users + guest item.
        assertThat(getItem(provider, 0).getTitle())
                .isEqualTo(application.getString(R.string.current_user_name, user14.name));
        assertThat(getItem(provider, 1).getTitle()).isEqualTo(user10.name);
        assertThat(getItem(provider, 2).getTitle()).isEqualTo(user13.name);
    }

    @Test
    public void testGuestShownAsSeparateItem() {
        UserInfo user10 = new UserInfo(/* id= */ 10, "User 10", /* flags= */ 0);
        UserInfo user11 = new UserInfo(/* id= */ 11, "User 11", /* flags= */ UserInfo.FLAG_GUEST);
        UserInfo user12 = new UserInfo(/* id= */ 12, "User 12", /* flags= */ UserInfo.FLAG_GUEST);
        UserInfo user13 = new UserInfo(/* id= */ 13, "User 13", /* flags= */ 0);
        List<UserInfo> users = Arrays.asList(user10, user11, user12, user13);

        doReturn(new UserInfo(/* id= */ 14, "User 14", /* flags= */ 0))
                .when(mCarUserManagerHelper).getCurrentProcessUserInfo();
        doReturn(users).when(mCarUserManagerHelper).getAllSwitchableUsers();

        UsersItemProvider provider = createProvider();

        assertThat(getItem(provider, 3).getTitle())
                .isEqualTo(application.getString(R.string.user_guest));
    }

    @Test
    public void testClickOnUsersInvokesOnUserClicked() {
        UserInfo currentUser = new UserInfo(/* id= */ 11, "User 11", /* flags= */ 0);
        List<UserInfo> otherUsers = Arrays.asList(
                new UserInfo(/* id= */ 10, "User 10", /* flags= */ 0));

        doReturn(currentUser).when(mCarUserManagerHelper).getCurrentProcessUserInfo();
        doReturn(otherUsers).when(mCarUserManagerHelper).getAllSwitchableUsers();

        UsersItemProvider provider = createProvider();

        // Clicking on current user invokes OnUserClicked.
        ShadowTextListItem textListItem = getItem(provider, 0);
        textListItem.getOnClickListener().onClick(new View(application.getApplicationContext()));
        verify(mUserClickListener).onUserClicked(currentUser);

        // Clicking on another user invokes OnUserClicked.
        textListItem = getItem(provider, 1);
        textListItem.getOnClickListener().onClick(new View(application.getApplicationContext()));
        verify(mUserClickListener).onUserClicked(otherUsers.get(0));
    }

    private UsersItemProvider createProvider() {
        return new UsersItemProvider.Builder(RuntimeEnvironment.application.getApplicationContext(),
                mCarUserManagerHelper)
                .setOnUserClickListener(mUserClickListener)
                .setIncludeSupplementalIcon(true)
                .create();
    }

    private ShadowTextListItem getItem(UsersItemProvider provider, int index) {
        return Shadow.extract(provider.get(index));
    }
}