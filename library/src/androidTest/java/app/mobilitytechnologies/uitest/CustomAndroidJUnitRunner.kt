package app.mobilitytechnologies.uitest

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import app.mobilitytechnologies.uitest.snapshot.SnapShot

class CustomAndroidJUnitRunner : AndroidJUnitRunner() {
    override fun onCreate(arguments: Bundle?) {
        val newArguments = UiTestRunListener.appendListenerArgument(arguments)
        super.onCreate(newArguments)
    }

    override fun finish(resultCode: Int, results: Bundle?) {
        SnapShot.zipAll()
        super.finish(resultCode, results)
    }
}