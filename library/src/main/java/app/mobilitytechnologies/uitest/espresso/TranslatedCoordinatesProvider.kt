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
 * This file is a modified version of androidx.test.espresso.action.TranslatedCoordinatesProvider
 * included in androidx.test.espresso:espresso-core:3.3.0
 *
 * Original copyright notice is as follows:
 *
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.mobilitytechnologies.uitest.espresso

import android.view.View
import androidx.test.espresso.action.CoordinatesProvider

/**
 * [androidx.test.espresso.action.TranslatedCoordinatesProvider]の実装をKotlinに移植し、publicにしたものです。
 *
 * [androidx.test.espresso.action.GeneralClickAction]を呼び出す独自View Actionを作る場合に必要になります。
 */
class TranslatedCoordinatesProvider(val coordinatesProvider: CoordinatesProvider, val dx: Float, val dy: Float) : CoordinatesProvider {
    override fun calculateCoordinates(view: View): FloatArray {
        val xy = coordinatesProvider.calculateCoordinates(view)
        xy[0] += dx * view.width
        xy[1] += dy * view.height
        return xy
    }
}
