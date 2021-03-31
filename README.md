# GOGO Screenshot Test for Android

## 概要

GOGO Screenshot Test for Android (以降GOGO Screenshot Testと表記します)は、
Androidアプリに対するスクリーンショットテストを書くときに良く使う機能をひとまとめにしたライブラリです。

GOGO Screenshot Testの特徴は以下の通りです。

- [JUnit 5のExtension](https://junit.org/junit5/docs/current/user-guide/#extensions)として提供しています
- スクリーンショットを撮るテストの書きやすさに注力しています
- 画面の表示が完了するまで待ち合わせるために必要な[Espresso idling resources](https://developer.android.com/training/testing/espresso/idling-resource)実装のうち、よく使うものを予めセットアップしています
- 公式に提供されている[FragmentScenario](https://developer.android.com/reference/kotlin/androidx/fragment/app/testing/FragmentScenario)を改善し、Fragment起動時にホストとなるActivityを差し替えられるようにした`AppCompatFragmentScenario`を提供しています


## セットアップ

[JitPack](https://jitpack.io/#MobilityTechnologies/gogo-screenshot-android)を使っています。

- トップレベルの`build.gradle`にJitPackリポジトリを登録します  
  ```groovy
  allprojects {
      repositories {
        ...
        maven { url 'https://jitpack.io' }
      }
  }
  ```
- `app/build.gradle`に依存関係を追加します。  
    ※前述の`AppCompatFragmentScenario`にて利用する`Activity`のデフォルト実装`FragmentTestingActivity`を内部に含んでいます。そのため、`androidTestImplementation`ではなく`debugImplementation`として追加してください  
    ```groovy
    dependencies {
        debugImplementation 'com.github.MobilityTechnologies:gogo-screenshot-android:0.0.1'
        ...
    }
    ```
- AndroidのInstrumented TestでJUnit5を利用可能とするために[android-junit5](https://github.com/mannodermaus/android-junit5) Gradle Pluginをセットアップします。以下のリンク先にある手順を全て実施してください
  - [Download](https://github.com/mannodermaus/android-junit5#download)
  - [Setup](https://github.com/mannodermaus/android-junit5#setup)
  - [Instrumentation Test Support](https://github.com/mannodermaus/android-junit5#instrumentation-test-support)
- スクリーンショットを保存したい場合、`AndroidJUnitRunner`のサブクラスを定義します。
  定義したクラスは`app/src/androidTest/`配下に保存してください。
  ```kotlin
  class MyAndroidJUnitRunner : AndroidJUnitRunner() {
      override fun onCreate(arguments: Bundle?) {
          // ★1
          val newArguments = UiTestRunListener.appendListenerArgument(arguments)
          super.onCreate(newArguments)
      }

      override fun onStart() {
          // ★2
          SnapShotOptions.currentSettings = SnapShotOptions.DEFAULT_SETTINGS.copy(
                  buildFlavorPathComponent = BuildConfig.FLAVOR
          )
          super.onStart()
      }
  
      override fun finish(resultCode: Int, results: Bundle?) {
          // ★3
          SnapShot.zipAll()
          super.finish(resultCode, results)
          // ★4
          UiTestDeveloperSettings.onInstrumentationFinished()
      }
  }

  ```
  - ★1 **(必須)** `onCreate()`メソッド内に処理を書きます。`UiTestRunListener.appendListenerArgument(arguments)`の戻り値を`super.onCreate()`の引数に渡してください
  - ★2: **(任意)** `onStart()`メソッド内に処理を書きます。`SnapShotOptions`を使って起動オプションを変更したい場合に、`super.onStart()`の直前に書いてください。
    指定できる内容は後述します。
  - ★3: **(任意)** `finish()`メソッド内に処理を書きます。`/sdcard` に保存したスクリーンショット画像をzipファイルにまとめたい場合に、`super.finish()`の直前に書いてください
  - ★4 **(必須)** `finish()`メソッド内に処理を書きます。`super.finish()`呼び出しよりも後に書いてください
- 定義した`AndroidJUnitRunner`のサブクラス名を、build.gradleの`android.testInstrumentationRunner`に指定します。  
  ```groovy
  android {
      ...
      testInstrumentationRunner "com.example.MyAndroidJUnitRunner"
  }
  ```

★1と★4は、 テスト実行中だけ以下の設定を有効ににするためのものです。
- ステータスバーの内容を固定化するためにシステムUIデモモードを有効化します
- Android 11以上で`/sdcard`配下にスクリーンショット画像を保存できるようにMANAGE_EXTERNAL_STORAGE権限を付与します

## テストを書く場所

AndroidのInstrumented Testとして書きます。`app/src/androidTest/`配下にテストコードを置いてください。

## テストの起動オプション

[AndroidJUnitRunnerの起動オプション](https://developer.android.com/reference/androidx/test/runner/AndroidJUnitRunner#execution-options:)を使ってスクリーンショットの撮り方や保存するファイル名をカスタマイズすることができます。
指定できるオプションは次の通りです。

| オプション名 | 取り得る値 | デフォルト値 | 意味 |
|:------------:|:----------:|:------------:|:----|
| `encodeScreenshotFileName` | `true` または `false` | `false` | スクリーンショットファイル名に非ASCII文字が含まれないようにBase64エンコードするかどうかを指定します。|
|`screenshotType`| `original` または `visual_regression` | `original` | `visual_regression`を指定した場合、同じ画面であれば、できるだけ画像差分が出ないようにスクリーンショットを撮ります。<br>現在の実装ではGoogle Mapで表示される地図の差分が出るのを抑えるため、地図部分を非表示にしてスクリーンショットを撮ります。|

AndroidJUnitRunnerの起動オプションは、`build.gradle`の`android.testInstrumentationRunnerArgument`を使って指定できます。

```groovy
android {
    ...
    testInstrumentationRunnerArgument "encodeScreenshotFileName", "false"
    testInstrumentationRunnerArgument "screenshotType", "visual_regression"
    ...
}
```

また、「セットアップ」で定義した`AndroidJUnitRunner`のサブクラス内(★1の箇所)で、`SnapShotOptions`を使って起動オプションを指定することもできます。
その場合に指定できるオプションは次の通りです。指定方法は前述の★1のコード例を参照してください。

| プロパティ名 | 型 | デフォルト値 | 意味 |
|:------------:|:----------:|:------------:|:----|
| `encodeFileName` | `Boolean` | - | AndroidJUnitRunnerの起動オプション`encodeScreenshotFileName`と同じです |
| `screenshotType`| `ScreenshotType` | - | AndroidJUnitRunnerの起動オプション`screenshotType`と同じです |
| `rootDirectory` | `File`     | `/sdcard/${applicationId}`| スクリーンショットを保存するディレクトリを指定します |
| `buildFlavorPathComponent` | `String?` | `null` | プロダクトフレーバーが定義されていて、スクリーンショット保存ディレクトリをフレーバーごとに分けたい場合は、フレーバー名を指定してください |

## スクリーンショットを撮るテストを書く

スクリーンショットを撮るテストを書くためのおおまかなステップは次の通りです。

- スクリーンショット対象画面を起動する
- 起動した画面に表示される内容を調整する
- スクリーンショットを撮る
- 取ったスクリーンショットを確認する

以降で順を追って説明します。

### スクリーンショット対象画面を起動する

起動したい画面の種類によって、`SimpleActivityPage`、`SimpleFragmentPage`、`SimpleDialogFragmentPage`のいずれかを使います。それぞれの用途は次の通りです。

- `SimpleActivityPage`
  - 特定のActivityを起動したいとき
  - Activityが直接NavHostFragmentを持っているときに、そのNavHostFragment管理下のFragmentを起動したいとき
- `SimpleFragmentPage`
  - 特定のFragmentを起動したいとき
  - FragmentがNavHostFragmentを持っているときに、そのNavHostFragment管理下のFragmentを起動したいとき
- `SimpleDialogFragmentPage`
  - 特定のDialogFragmentを起動したいとき
  
#### SimpleActivityPage

`MyActivity`を起動する場合のコード例は次の通りです。

```kotlin
@JvmField
@RegisterExtension
val uiTestExtension = UiTestExtension { SimpleActivityPage(it, MyActivity::class) }

...

@Test
fun myTest() {
    val intent = (MyActivityを起動するためのIntent)
    // MyActivityを起動する
    uiTestExtension.page.launchActivitySimply(intent)
} 
```

`MyActivity`が`NavHostFragment`を持っており、その`NavHostFragment`が管理するFragmentを起動したい場合は、
`SimpleActivityPage`コンストラクタの第3引数に`NavHostFragment`がセットされているview IDを指定してください。

```kotlin
@JvmField
@RegisterExtension
val uiTestExtension = UiTestExtension { SimpleActivityPage(it, MyActivity::class, R.id.my_nav_host) }

@Test
fun myTest() {
    val intent = (MyActivityを起動するためのIntent)

    // MyActivityを起動してからλ式で指定されたnavigateを実行し、
    // デスティネーション R.id.myFragment が表示されるまで待つ
    uiTestExtension.page.launchFragmentByNavController(R.id.myFragment, intent) { 
        // itはNavController
        it.navigate(...) // R.id.myFragmentに遷移するアクションを指定
    }
} 
```

#### SimpleFragmentPage

`MyFragment`を起動する場合のコード例は次の通りです。
`SimpleFragmentPage`コンストラクタの第3引数には、ホストするActivityに適用したいテーマを指定してください。

```kotlin
@JvmField
@RegisterExtension
val uiTestExtension = UiTestExtension { SimpleFragmentPage(it, MyFragment::class, R.style.AppTheme) }

...

@Test
fun myTest() {
    // MyFragmentを起動する
    uiTestExtension.page.launchFragmentSimply()
}
```

`MyFragment`のインスタンス化方法を指定したい場合(`MyFragment.newInstance(...)`などが提供されている場合)は、
`launchFragmentSimply()`の代わりに`launchFragmentByCreator`を使って起動してください。

```kotlin
@Test
fun myTest() {
    // MyFragmentを起動する
    uiTestExtension.page.launchFragmentByCreator {
        MyFragment.newInstance(...)
    }
}
```

`SimpleActivityPage`と同様にナビゲーションにも対応しています。
`MyFragment`が`NavHostFragment`を持っており、その`NavHostFragment`が管理するFragmentを起動したい場合は、
`SampleFragmentPage`コンストラクタの第4引数に`NavHostFragment`がセットされているview IDを指定してください。

```kotlin
@JvmField
@RegisterExtension
val uiTestExtension = UiTestExtension {
    SimpleFragmentPage(it, MyFragment::class, R.style.AppTheme, R.id.my_nav_host)
}

...

@Test
fun myTest() {
    // MyFragmentを起動してからλ式で指定されたnavigateを実行し、
    // デスティネーション R.id.myFragment2 が表示されるまで待つ
    uiTestExtension.page.launchChildFragmentByNavController(R.id.myFragment2) {
        // itはNavCotroller
        it.navigate(...) // R.id.myFragment2に遷移するアクションを指定
    }
}
```

#### SimpleDialogFragmentPage

`MyDialogFragment`を起動する場合のコード例は次の通りです。
`SimpleDialogFragmentPage`の第3引数には、通常は`DialogHostingFragment::class`を指定してください。

```kotlin
@JvmField
@RegisterExtension
val uiTestExtension = UiTestExtension {
    SimpleDialogFragmentPage(it, MyDialogFragment::class, DialogHostingFragment::class)
}

...

@Test
fun myTest() {
    // MyDialogFragmentを起動する
    uiTestExtension.page.launchDialogFragmentByCreator {
        MyDialogFragment.newInstance(....)
    }
}
```

DialogFragmentの中には、そのダイアログをホストするFragmentに特定のリスナインターフェイスの実装を要求するものがあります。
そのようなDialogFragmentを起動するには、前準備としてそのリスナインターフェイスを実装した`DialogHostingFragment`のサブクラスを定義してください。

`MyDialogFragment`が、ホスト側に`MyListener`と`MyListener2`インターフェイスの実装を要求している場合の例は次の通りです。

```kotlin
class MyDialogHostingFragment(
        myListener: MyListener,
        myListener2: MyListener2
    ) : DialogHostingFragment(), MyListener by myListener, MyListener2 by myListener2
```

ポイントは次の3つです。

- `DialogHostFragment`を継承する
- コンストラクタ引数で、実装が必要なリスナインターフェースを受け取るようにする。  
  ※ここで定義したFragmentはリフレクションを使ってインスタンス化するため、 **コンストラクタの引数でリスナインターフェイス以外のものを受け取らないようにしてください。**
- `by`を使ってリスナの処理をコンストラクタ引数のオブジェクトに移譲する

その上で、次のようにしてください。
`SimpleDialogFragmentPage`コンストラクタの第3引数が`MyDialogHostingFragment::class`になっている点が最初の例との違いです。

```kotlin
@JvmField
@RegisterExtension
val uiTestExtension = UiTestExtension {
    SimpleDialogFragmentPage(it, MyDialogFragment::class, MyDialogHostingFragment::class)
}

...

@Test
fun myTest() {
    // MyDialogFragmentを起動する
    uiTestExtension.page.launchDialogFragmentByCreator {
        MyDialogFragment.newInstance(....)
    }
}
```

### 起動した画面に表示される内容を調整する

画面に表示される内容を調整する方法はアプリの設計により様々ですが、たとえば次のような方法が考えられます。

- サーバーからのレスポンスを固定化するために、Repositoryのメソッドをスタブ化する
- 起動したActivityやFragmentにアクセスして、状態を変更する

#### Repositoryのメソッドをスタブ化する

本ライブラリは、スタブ化について支援する仕組みは用意していません。
DIライブラリやモックライブラリを使って実現してください。

それらのライブラリを使うにあたって毎回同じ前処理が必要な場合は、前述の`Simple{Activity,Fragment,DialogFragment}Page`のカスタマイズ版を定義することができます。

その場合は`Simple{Activity,Fragment,DialogFragment}Page`を別名でコピーし、`starting()`メソッドや`finished()`メソッドをオーバーライドしてください。
`starting()`はJUnit5の[BeforeEachCallback](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/BeforeEachCallback.html)のタイミングで、`finished()`は[AfterEachCallback](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/AfterEachCallback.html)のタイミングで、それぞれ実行されます。

以下にKoinを使った初期化の例を示します。
オーバーライドする場合は、必ず`super.starting()`や`super.finished()`を呼び出してください。

```kotlin
class MyActivityPage<...>(...) : ActivityScenarioPage<...>(...) {

    override fun starting() {
        super.starting()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(ApplicationProvider.getApplicationContext())
            modules(...) // Repositoryのスタブ版に差し替える
        }
    }
    
    override fun finished() {
        stopKoin()
        super.finished()
    }
}
```

#### 起動したActivityやFragmentにアクセスする

起動しているActivityやFragmentにアクセスできる`onActivity`のようなメソッドを提供していますので活用してください。
それぞれのメソッドの詳細はKDocコメントを参照してください。

- `ActivityScenarioPage` (`SimpleActivityPage`のスーパークラス)
  - `scenario.onActivity()`
- `FragmentScenarioPage` (`SimpleFragmentPage`のスーパークラス)
  - `scenario.onFragment()`
  - `onCurrentChildFragment()`
- `DialogFragmentPage` (`SimpleDialogFragmentPage`のスーパークラス)
  - `onDialogFragment()`

### スクリーンショットを撮る

スクリーンショットは次のように書くことで取得できます。

```kotlin
uiTestExtension.page.captureDisplay("画面の状態")

// 状態の組み合わせなど、補足の説明が必要な場合は第二引数に追加する(オプション)
uiTestExtension.page.captureDisplay("画面の状態", "補足の説明")
```

撮影したスクリーンショットはデフォルトで次のパスに保存されます。

補足の説明がない場合: `/sdcard/アプリケーションID/screenshots/画面のクラス名/画面の状態-スクリーンショット取得順を表す番号.PNG`

補足の説明がある場合: `/sdcard/アプリケーションID/screenshots/画面のクラス名/画面の状態-スクリーンショット取得順を表す番号-補足の説明.PNG`

パスを構成する要素の詳細は次のとおりです。

**ディレクトリを構成する要素**

|要素|デフォルト値|  |
|:------------|:----------|:------------|
| ルートディレクトリ | sdcard/アプリケーションID | スクリーンショット保存先のルートディレクトリ<br>`SnapShotOptions#rootDirectory`で変更可|
| 画像保存先ディレクトリ| screenshots | `SnapShotOptions#buildFlavorPathComponent`でscreenshots/BuildFlavorに変更可  |
| 画面のクラス名 | Pageで指定された画面のクラス名 | `UiTestExtension#page.snapShotPageName`で変更可|


**ファイル名を構成する要素**

| 要素 |用途|  
|:------------|:----------|
|画面の状態| スクリーンショットを取得した時点での画面の状態や条件を表します |
| スクリーンショット取得順を表す番号| 同一の状態・条件に対して複数枚スクリーンショットを取得するときにスクリーンショット取得順がわかるように自動で連番されます |
| 補足の説明 | 画面の状態だけでは表現が難しい場合に説明を追加します | 


#### スクリーンショット取得範囲の指定

スクリーンショットの取得範囲を指定することができます。

```kotlin
//　画面全体のスクリーンショットを取得する
// ダイアログやSurfaceViewが含まれる場合はこのメソッドを使用する
uiTestExtension.page.captureDisplay("画面の状態", "補足の説明(オプション)")

// Pageに指定したActivityもしくはFragmentのスクリーンショットを取得します
// ActivityScenarioPageを利用している場合はActivity・FragmentScenarioPageを利用している場合はFragmentのスクリーンショットを取得します
uiTestExtension.page.captureActivityOrFragment("画面の状態", "補足の説明(オプション)")

// 特定のViewのスクリーンショットを取得します
// ActivityScenarioPageを利用している場合はActivity・FragmentScenarioPageを利用している場合はFragmentのインスタンスがラムダ式の引数に渡されるので、スクリーンショットを取得したいViewのインスタンスを返します
uiTestExtension.page.captureViewFromActivityOrFragment("画面の状態", "補足の説明(オプション)") {　activityOrFragment -> 
    // スクリーンショットを取得したいViewのインスタンスを返す
}
```

#### 連続するスクリーンショットの取得

同一の状態・条件に対して複数枚のスクリーンショットを取得する場合、次のように書くことができます。

```kotlin
uiTestExtension.page.captureSequentially("画面の状態・条件") {
            
            // なにかしらのUIの変更

            captureDisplay("補足の説明-1") // captureActivityOrFragmentも利用可

            // なにかしらのUIの変更

            captureDisplay("補足の説明-2")

            // なにかしらのUIの変更

            captureDisplay() // 補足の説明は省略可能
}
```

このとき、`画面の状態・条件-01-補足の説明-1.PNG`と`画面の状態・条件-02-補足の説明-2.PNG`と`画面の状態・条件-03.PNG`の3枚のスクリーンショットが取得できます。


**スクロールをしながらスクリーンショットを取得する**

上記の仕組みを利用して、スクロール可能な画面のスクリーンショットを取得できます。

スクロール可能な画面は、一枚のスクリーンショットだとコンテンツ全体が取得できない可能性があります。
スクロールしながらスクリーンショットを取ることで、コンテンツ全体のスクリーンショットを複数枚に分けて取得することができます。

```kotlin
// R.id.scroll_view = スクロールをしたいViewのID
// R.id.bottom_view = スクロールをしたいViewの中で一番下に位置するViewのID
// スクロールの下端がbottom_viewと揃うまでスクロールとスクリーンショットの取得を繰り返す
uiTestExtension.page.captureSequentially("画面の状態") {
    captureEachScrolling(R.id.scroll_view, R.id.bottom_view)
}

```


## Tips

### 画面表示が完了するまで待ち合わせる

####  コルーチンの待ち合わせ

`UiTestExtension`は`idlingCoroutineDispatcher`というフィールドをもっており、これはUIテストで待ち合わせ可能なCoroutineのDispatherです。

UIテスト中に実行されるCoroutineのDispatcherを`idlingCoroutineDispatcher`に変更することで、Coroutineの待ち合わせをすることができます。

また、引数にFunction Typeを受け取る`withIdlingCoroutineContext`メソッドがあります。
これは、引数のFunction Typeを非同期で実行した上でテストコードで待ち合わせを行います。

このメソッドの具体的な活用例は次のとおりです。([mockk](https://mockk.io/)利用時の例)

```kotlin
// スタブ設定時にあえて値の返却を遅延させた上で待ち合わせを行いたいときにwithIdlingCoroutineContextを利用可能
// 例: 同期的に実行してしまうとUIに不整合が発生する場合等 
coEvery { repository.stubSuspendFunc()} coAnswers {
    uiTestExtension.withIdlingCoroutineContext {
        // 値を返す
    }
}
```

  
####  CountingIdlingResourceを使った待ち合わせ

`UiTestExtension`は[CountingIdlingResource](https://developer.android.com/reference/androidx/test/espresso/idling/CountingIdlingResource)をフィールドに持っています。

CountingIdlingResourceはカウンタが0のときをアイドル状態、0より大きい時をビジー状態とみなし、アイドル状態になるまで待ち合わせを行います。

非同期処理の開始と終了がフックできる場合に利用できます。

```kotlin
uiTestExtension.countingIdlingResource.increment()

asyncSomethingMethod() {
    uiTestExtension.countingIdlingResource.decrement()
}
```   

#### UI Automator (UiDevice) を使った待ち合わせ

IdlingResourceでの待ち合わせが難しい場合、特定のViewが特定の状態になるまで待つといった待ち合わせ処理を[UI Automator(UiDevice)](https://developer.android.com/training/testing/ui-automator)で書くことができます。

例: 特定のIDボタンがenabledの状態になるまで待つ

```kotlin
// waitUntilおよびtoResourceNameはutilsにヘルパー関数として定義している
uiTestExtension.uiDevice.waitUntil(By.res(toResourceName(R.id.button_next)).enabled(true))
```


## License

```
Copyright 2021 Mobility Technologies Co., Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

### Third Party Content

- `DataBindingIdlingResource.kt` is modified version of [Android Architecture Blueprints v2](https://github.com/android/architecture-components-samples/blob/1d7a759f742e8bdaf1eb4531e38ea9270301c577/GithubBrowserSample/app/src/androidTest/java/com/android/example/github/util/DataBindingIdlingResource.kt) and [GithubBrowserSample](https://github.com/android/architecture-components-samples/blob/1d7a759f742e8bdaf1eb4531e38ea9270301c577/GithubBrowserSample/app/src/androidTest/java/com/android/example/github/util/DataBindingIdlingResource.kt) licensed under the Apache License, Version 2.0.
- `TranslatedCoordinatesProvider.kt` is a modified version of [TranslatedCoordinatesProvider.java](https://github.com/android/android-test/blob/androidx-test-1.3.0/espresso/core/java/androidx/test/espresso/action/TranslatedCoordinatesProvider.java) licensed under the Apache License, Version 2.0.
- `CountingTaskExecutorExtension.kt` is a modified version of [CountingTaskExecutorRule.java](https://dl.google.com/dl/android/maven2/androidx/arch/core/core-testing/2.1.0/core-testing-2.1.0-sources.jar) licensed under the Apache License, Version 2.0.
- `GrantPermissionExtension.kt` is a modified version of [GrantPermissionRule.java](https://github.com/android/android-test/blob/androidx-test-1.3.0/runner/rules/java/androidx/test/rule/GrantPermissionRule.java) licensed under the Apache License, Version 2.0.
- `TaskExecutorWithIdlingResourceExtension.kt` is copied from [GithubBrowserSample](https://github.com/android/architecture-components-samples/blob/1d7a759f742e8bdaf1eb4531e38ea9270301c577/GithubBrowserSample/app/src/androidTest/java/com/android/example/github/util/TaskExecutorWithIdlingResourceRule.kt) licensed under the Apache License, Version 2.0.
- `AppCompatFragmentScenario.kt`, `FragmentFactoryHolderViewModel.kt` and `FragmentTestingActivity.kt` are modified versions of [FragmentScenario.java](https://dl.google.com/dl/android/maven2/androidx/fragment/fragment-testing/1.2.5/fragment-testing-1.2.5-sources.jar) licensed under the Apache License, Version 2.0.
- `isBelowBottomLine()`, `findView()` and `getTopViewGroup()` defined in `ViewAssertions.kt` are modified versions of [PositionAssertions.java](https://github.com/android/android-test/blob/androidx-test-1.3.0/espresso/core/java/androidx/test/espresso/assertion/PositionAssertions.java) licensed under the Apache License, Version 2.0.
