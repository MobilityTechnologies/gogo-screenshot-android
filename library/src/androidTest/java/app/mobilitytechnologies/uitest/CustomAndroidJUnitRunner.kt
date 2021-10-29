package app.mobilitytechnologies.uitest

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner

class CustomAndroidJUnitRunner : AndroidJUnitRunner() {
    override fun onCreate(arguments: Bundle?) {
        val newArguments = UiTestRunListener.appendListenerArgument(arguments)
        super.onCreate(newArguments)
    }
}