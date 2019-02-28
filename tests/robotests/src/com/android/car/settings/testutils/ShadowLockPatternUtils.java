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

import com.android.internal.widget.LockPatternUtils;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(LockPatternUtils.class)
public class ShadowLockPatternUtils {

    private static LockPatternUtils sInstance;

    public static void setInstance(LockPatternUtils lockPatternUtils) {
        sInstance = lockPatternUtils;
    }

    @Resetter
    public static void reset() {
        sInstance = null;
    }

    @Implementation
    protected void clearLock(String savedCredential, int userHandle) {
        byte[] savedCredentialBytes = savedCredential != null
                ? savedCredential.getBytes() : null;
        sInstance.clearLock(savedCredentialBytes, userHandle);
    }

    @Implementation
    protected int getKeyguardStoredPasswordQuality(int userHandle) {
        return sInstance.getKeyguardStoredPasswordQuality(userHandle);
    }
}
