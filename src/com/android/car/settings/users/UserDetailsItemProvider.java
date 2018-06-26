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

import android.car.user.CarUserManagerHelper;
import android.content.Context;
import android.content.pm.UserInfo;

import androidx.car.widget.ListItem;
import androidx.car.widget.ListItemProvider;

import com.android.car.settings.R;

/**
 * Implementation of {@link ListItemProvider} for {@link UserDetailsFragment}.
 * Creates a single item that represents the passed in user.
 */
class UserDetailsItemProvider extends ListItemProvider {
    private final Context mContext;
    private final CarUserManagerHelper mCarUserManagerHelper;
    private final EditUserListener mEditUserListener;
    private UserListItem mItem;

    UserDetailsItemProvider(UserInfo userInfo, Context context, EditUserListener editUserListener,
            CarUserManagerHelper userManagerHelper) {
        mContext = context;
        mCarUserManagerHelper = userManagerHelper;
        mEditUserListener = editUserListener;
        refreshItem(userInfo);
    }

    @Override
    public ListItem get(int position) {
        return mItem;
    }

    @Override
    public int size() {
        // Single item being provided.
        return 1;
    }

    /**
     * Re-creates the user item.
     */
    public void refreshItem(UserInfo userInfo) {
        mItem = new UserListItem(userInfo, mContext, mCarUserManagerHelper);
        if (mCarUserManagerHelper.isCurrentProcessUser(userInfo)) {
            // Current user should be able to edit their own username.
            mItem.setSupplementalIcon(R.drawable.ic_mode_edit, /* showDivider= */ false,
                    v -> mEditUserListener.onEditUserClicked(userInfo));
        }
    }

    /**
     * Interface for registering clicks on edit.
     */
    interface EditUserListener {
        /**
         * Invoked when edit button is clicked.
         *
         * @param userInfo User for which the click is registered.
         */
        void onEditUserClicked(UserInfo userInfo);
    }
}
