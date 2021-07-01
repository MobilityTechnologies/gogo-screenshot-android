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

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SnapShot {
    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private val format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG
    private val quality = 90

    fun captureDisplay(name: String) {
        uiDevice.waitForIdle()

        val captureFile = captureFile(name)
        uiDevice.takeScreenshot(captureFile)
        Log.i(TAG, "screenshot saved: ${captureFile.absolutePath}")
    }

    fun capture(activity: Activity, name: String) {
        capture(activity, activity.window.decorView, name)
    }

    fun capture(fragment: Fragment, name: String) {
        capture(fragment.requireActivity(), fragment.requireView(), name)
    }

    fun capture(dialogFragment: DialogFragment, name: String) {
        capture(dialogFragment, dialogFragment.requireDialog().window!!.decorView.rootView, name)
    }

    fun capture(fragment: Fragment, view: View, name: String) {
        capture(fragment.requireActivity(), view, name)
    }

    fun capture(dialogFragment: DialogFragment, view: View, name: String) {
        captureView(view, dialogFragment.requireDialog().window) {
            saveBitmap(it, name)
        }
    }

    fun capture(activity: Activity, view: View, name: String) {
        captureView(view, activity) {
            saveBitmap(it, name)
        }
    }

    private fun captureView(view: View, activity: Activity, callback: (Bitmap) -> Unit) = captureView(view, activity.window, callback)

    private fun captureView(view: View, window: Window?, callback: (Bitmap) -> Unit) {

        window?.let { _window ->
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

            val viewLocation = IntArray(2)
            view.getLocationInWindow(viewLocation)

            PixelCopy.request(
                    _window,
                    Rect(
                            viewLocation[0],
                            viewLocation[1],
                            viewLocation[0] + view.width,
                            viewLocation[1] + view.height
                    ),
                    bitmap,
                    { copyResult ->

                        if (copyResult == PixelCopy.SUCCESS) {
                            callback(bitmap)
                        } else {
                            throw IllegalStateException("Pixel Copy Failure: $copyResult")
                        }
                    },
                    Handler()
            )
        }
    }

    fun saveBitmap(bitmap: Bitmap, name: String) {

        val imageFile = captureFile(name)
        var out: BufferedOutputStream? = null
        try {
            out = BufferedOutputStream(FileOutputStream(imageFile))
            bitmap.compress(format, quality, out)
            out.flush()
            Log.i(TAG, "screenshot saved: ${imageFile.absolutePath}")
        } finally {
            try {
                out?.close()
            } catch (e: Throwable) {
                Log.w(TAG, e)
            }
        }
    }

    private fun captureFile(name: String): File {
        val file = File(baseDir, "$name.${format.name}")
        val dir = checkNotNull(file.parentFile) {
            "$file doesn't have parent."
        }
        dir.mkdirs()
        if (!dir.isDirectory && !dir.canWrite()) {
            throw IOException("The directory $dir does not exist and could not be created or is not writable.")
        }
        return file
    }

    companion object {
        const val TAG = "SnapShot"
        const val PARENT_DIR_NAME = "screenshots"

        val baseDir = File(SnapShotOptions.currentSettings.rootDirectory, PARENT_DIR_NAME)

        fun zipAll() {
            val zipFile = File(SnapShotOptions.currentSettings.rootDirectory, "$PARENT_DIR_NAME.zip")
            ZipOutputStream(zipFile.outputStream()).use { zipOut ->
                requireNotNull(baseDir.listFiles()).forEach { file ->
                    file.inputStream().use { fis ->
                        val zipEntry = ZipEntry(file.name)
                        zipOut.putNextEntry(zipEntry)
                        fis.copyTo(zipOut)
                        zipOut.closeEntry()
                    }
                }
            }
            baseDir.deleteRecursively()
        }
    }
}