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

package app.mobilitytechnologies.uitest.extension

import android.Manifest
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.idling.concurrent.IdlingThreadPoolExecutor
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import app.mobilitytechnologies.uitest.espresso.DataBindingIdlingResource
import app.mobilitytechnologies.uitest.page.Page
import java.util.UUID
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * UI Test用のJUnit 5 Extensionです。
 *
 * 内部で、以下のIdlingResourceを有効にします。
 * - [TaskExecutorWithIdlingResourceExtension]
 * - [DataBindingIdlingResource]
 * - [CountingIdlingResource]
 *
 * また、コルーチンの待ち合わせを実現する[withIdlingCoroutineContext]も提供します。
 *
 * テストがスタートするタイミングで、 [DataBindingIdlingResource]と、[CountingIdlingResource]を初期化します。
 * コンストラクタには[Page]インターフェイスを実装したPageクラスのファクトリーを指定してください。
 */
class UiTestExtension<P : Page<P>>(initializer: (thisRef: UiTestExtension<*>) -> P) : BeforeEachCallback, AfterEachCallback {

    // 一度生成されたpageへの参照は消えないが、JUnitは1つのテストごとにExtensionオブジェクトを生成し直すので問題ない。
    // 同一オブジェクトのbeforeEach()メソッドが2回呼ばれることもない。
    /** コンストラクタ引数 `initializer` によって生成された[Page]オブジェクトへの参照です。 */
    val page: P by lazy { initializer(this) }

    /** [UiDevice]のインスタンスです。*/
    val uiDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    lateinit var countingIdlingResource: CountingIdlingResource
    lateinit var dataBindingIdlingResource: DataBindingIdlingResource
    lateinit var idlingThreadPoolExecutor: ThreadPoolExecutor

    /** IdlingResourceを実装したCoroutineDispatcherです。*/
    val idlingCoroutineDispatcher: CoroutineDispatcher by lazy { idlingThreadPoolExecutor.asCoroutineDispatcher() }

    private val dependentExtensions: List<Any> = listOf(
            TaskExecutorWithIdlingResourceExtension(),
            GrantPermissionExtension.grant(Manifest.permission.DUMP))

    override fun beforeEach(context: ExtensionContext?) {

        dependentExtensions.filterIsInstance<BeforeEachCallback>().forEach {
            it.beforeEach(context)
        }

        IdlingPolicies.setIdlingResourceTimeout(1, TimeUnit.MINUTES)
        countingIdlingResource = CountingIdlingResource("CountingIdlingResource", true)
        dataBindingIdlingResource = DataBindingIdlingResource()
        idlingThreadPoolExecutor = IdlingThreadPoolExecutor("idling thread pool executor id=${UUID.randomUUID()}",
                2, 10, 0, TimeUnit.MILLISECONDS, LinkedBlockingDeque()) { Thread(it) }
        IdlingRegistry.getInstance().register(countingIdlingResource, dataBindingIdlingResource)

        page.starting()
    }

    override fun afterEach(context: ExtensionContext?) {

        page.finished()
        // idlingThreadPoolExecutorはshutdown()を呼び出すことでunregisterされる。
        idlingThreadPoolExecutor.shutdown()
        IdlingRegistry.getInstance().unregister(countingIdlingResource, dataBindingIdlingResource)

        dependentExtensions.filterIsInstance<AfterEachCallback>().forEach {
            it.afterEach(context)
        }
    }

    /**
     * [page]の[Page.setUp]を呼び出します。引数[block]は、[page]がレシーバーの状態で実行されます。
     */
    fun setUp(block: (P.() -> Unit)? = null) = page.setUp(block)

    /**
     * [page]の[Page.tearDown]を呼び出します。引数[block]は、[page]がレシーバーの状態で実行されます。
     */
    fun tearDown(block: (P.() -> Unit)? = null) = page.tearDown(block)

    /**
     * [block]の処理が完了するまでEspressoの処理を待ち合わせるコルーチンビルダーです。
     */
    suspend inline fun <T> withIdlingCoroutineContext(noinline block: suspend CoroutineScope.() -> T) = withContext(idlingCoroutineDispatcher, block)
}