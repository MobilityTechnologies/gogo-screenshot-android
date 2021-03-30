/*
 * Copyright 2021 Mobility Technologies Co., Ltd.
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
 *
 * ========
 * This file is a modified version of androidx.test.rule.GrantPermissionRule
 * included in androidx.test:rules:1.3.0
 *
 * Original copyright notice is as follows:
 *
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Lice`nse is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.mobilitytechnologies.uitest.extension

import android.Manifest.permission
import androidx.annotation.VisibleForTesting
import androidx.test.internal.platform.ServiceLoaderWrapper
import androidx.test.internal.platform.content.PermissionGranter
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.permission.PermissionRequester
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * [GrantPermissionRule](https://developer.android.com/reference/androidx/test/rule/GrantPermissionRule)
 * をJUnit 5のExtensionに移植したものです。
 */
class GrantPermissionExtension : BeforeEachCallback {

    private val permissionGranter: PermissionGranter = ServiceLoaderWrapper.loadSingleService(PermissionGranter::class.java) { PermissionRequester() }

    companion object {
        /**
         * Static factory method that grants the requested permissions.
         *
         *
         * Permissions will be granted before any methods annotated with `&#64;Before` but before
         * any test method execution.
         *
         * @param permissions a variable list of Android permissions
         * @return [GrantPermissionRule]
         * @see android.Manifest.permission
         */
        fun grant(vararg permissions: String): GrantPermissionExtension {
            val extension = GrantPermissionExtension()
            extension.grantPermissions(*permissions)
            return extension
        }
    }

    private fun grantPermissions(vararg permissions: String) {
        val permissionSet = satisfyPermissionDependencies(*permissions)
        permissionGranter.addPermissions(*permissionSet.toTypedArray())
    }

    @VisibleForTesting
    fun satisfyPermissionDependencies(vararg permissions: String): List<String> {
        val permissionList: MutableList<String> = mutableListOf(*permissions)
        // Explicitly grant READ_EXTERNAL_STORAGE permission when WRITE_EXTERNAL_STORAGE was requested.
        if (permissionList.contains(permission.WRITE_EXTERNAL_STORAGE)) {
            permissionList.add(permission.READ_EXTERNAL_STORAGE)
        }
        return permissionList
    }

    override fun beforeEach(context: ExtensionContext?) {
        permissionGranter.requestPermissions()
    }
}