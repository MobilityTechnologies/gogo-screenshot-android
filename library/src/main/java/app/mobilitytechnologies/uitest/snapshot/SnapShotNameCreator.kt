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
package app.mobilitytechnologies.uitest.snapshot

/**
 * スクリーンショットのファイル名(拡張子除く)の命名規則を指定します。
 * スクリーンショットが撮影される度に[createFileName]メソッドが呼び出されます。
 * その結果返される文字列が、当該スクリーンショットのファイル名になります(拡張子除く)。
 */
fun interface SnapShotNameCreator {

    /**
     * スクリーンショットのファイル名を生成します。
     *
     * @param pageName スクリーンショット対象の画面名
     * @param conditionName スクリーンショット撮影時の条件名
     * @param counter 同じテストメソッドで複数枚スクリーンショットを撮影した場合の通番
     * @param optionalDescription さらに詳細な説明(nullの場合もある)
     */
    fun createFileName(pageName: String, conditionName: String, counter: Int, optionalDescription: String?): String
}