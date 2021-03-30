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

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.test.core.app.ApplicationProvider
import app.mobilitytechnologies.uitest.R
import app.mobilitytechnologies.uitest.extension.UiTestExtension
import app.mobilitytechnologies.uitest.snapshot.ScreenshotType
import app.mobilitytechnologies.uitest.snapshot.SnapShotOptions
import app.mobilitytechnologies.uitest.utils.wait
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ある[Fragment]が直接内包している[SupportMapFragment]についてのヘルパークラスです。
 *
 * 以下の機能を提供しています。
 * - [SupportMapFragment.getMapAsync]を使って[GoogleMap]を取得する
 * - 地図が表示されるまで待つ
 */
class SupportMapFragmentHelper(
        val uiTestExtension: UiTestExtension<*>,

        /**
         * 目的の[SupportMapFragment]を直接内包している`Fragment`を取得する方法を指定します。
         *
         * `() -> Fragment`型(Fragmentを返す関数)ではなく、
         * `((Fragment) -> Unit) -> Unit` (取得した`Fragment`を引数に渡すような関数)を指定します。
         *
         * たとえば[app.mobilitytechnologies.uitest.scenario.AppCompatFragmentScenario.onFragment]などを直接指定できます。
         */
        val onFragmentFunc: ((Fragment) -> Unit) -> Unit,

        /**
         * 目的の[SupportMapFragment]に対応するfragment IDを指定します。
         * [onFragmentFunc]が複数の[SupportMapFragment]を内包しているときに役に立ちます。
         *
         * この引数が指定されている場合は、そのIDに対応する`SupportMapFragment`が対象になります。
         * そうでない場合(`null`が指定されている場合)は、最初に見付かった`SupportMapFragment`が対象になります。
         */
        @IdRes val mapFragmentId: Int? = null
) {
    companion object {
        const val TAG = "SupportMapFragmentHelper"
    }

    /**
     * Instrumentation Argumentとして指定された [ScreenshotType] です。
     */
    val screenshotType = SnapShotOptions.currentSettings.screenshotType

    /**
     * [GoogleMap]インスタンスを引数にして[block]を呼び出します。
     * この[block]は、[SupportMapFragment.getMapAsync]に渡されます。
     *
     * @param block GoogleMapインスタンスを引数に受け取るλ式
     */
    fun onGoogleMap(block: (GoogleMap) -> Unit) {
        this.onFragmentFunc {
            it.retrieveSupportMapFragment(mapFragmentId).getMapAsync(block)
        }
    }

    /**
     * Instrumentation Argumentとして指定された [screenshotType] にしたがって、地図のスタイルを変更します。
     * [ScreenshotType.VISUAL_REGRESSION] の場合は地図には何も表示されなくなります(単色で塗り潰された状態になります)。
     */
    fun changeMapStyleByScreenshotType() {
        onGoogleMap { googleMap ->
            if (screenshotType == ScreenshotType.VISUAL_REGRESSION) {
                googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(ApplicationProvider.getApplicationContext(), R.raw.empty_map))
            }
        }
    }

    /**
     * [onGoogleMap]で得られる地図のロードが完了するまで、最大[app.mobilitytechnologies.uitest.utils.DEFAULT_TIMEOUT] msec待ちます。
     *
     * @param delay GoogleMapの`onMapLoadedCallback`が呼ばれてから、さらに待つ時間(msec)を指定します(デフォルト値: 10 msec)。
     *   0以下の数値を指定した場合は、onMapLoadedCallbackが呼ばれたら即座に戻ってきます。
     */
    fun waitMapLoadCompleted(delay: Long = 10L) {
        val loadCompleted = AtomicBoolean(false)
        this.onGoogleMap {
            it.setOnMapLoadedCallback {
                if (delay > 0) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        loadCompleted.set(true)
                    }, delay)
                } else {
                    loadCompleted.set(true)
                }
            }
        }
        val completed = wait { loadCompleted.get() }
        if (!completed) {
            Log.w(TAG, "waitMapLoadCompleted timed out")
        }
    }

    /**
     * レシーバーのFragmentが直接内包しているSupportMapFragmentを取得します。
     * 複数のSupportMapFragmentを内包している場合は、引数[mapFragmentId]でSupportMapFragmentのIDを指定してください。
     * [mapFragmentId]が指定されていない場合、最初に見付かったSupportMapFragmentを返します。
     */
    private fun Fragment.retrieveSupportMapFragment(@IdRes mapFragmentId: Int? = null): SupportMapFragment {
        val fragmentManager = this.childFragmentManager
        return mapFragmentId?.let {
            fragmentManager.findFragmentById(mapFragmentId) as SupportMapFragment
        } ?: fragmentManager.fragments.filterIsInstance<SupportMapFragment>().first()
    }
}