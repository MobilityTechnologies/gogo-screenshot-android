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
 */

package app.mobilitytechnologies.uitest.page

import app.mobilitytechnologies.uitest.extension.UiTestExtension

/**
 * 1つの画面を表します。
 * @param IMPL 実装クラスの型
 */
interface Page<IMPL> {

    /** [UiTestExtension]への参照です。 */
    val uiTestExtension: UiTestExtension<*>

    /**
     * JUnit 5 Extensionによって、テスト開始時に呼び出されます。
     */
    fun starting()

    /**
     * JUnit 5 Extensionによって、テスト終了時に呼び出されます。
     */
    fun finished()

    /**
     * プログラマが、[UiTestExtension.setUp]を呼び出したときに、呼び出されます。
     */
    fun setUp(block: (IMPL.() -> Unit)? = null)

    /**
     * プログラマが、 [UiTestExtension.tearDown]を呼び出したときに、呼び出されます。
     */
    fun tearDown(block: (IMPL.() -> Unit)? = null)
}