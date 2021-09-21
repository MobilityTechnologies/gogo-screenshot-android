package app.mobilitytechnologies.uitest

import androidx.fragment.app.Fragment
import app.mobilitytechnologies.uitest.extension.UiTestExtension
import app.mobilitytechnologies.uitest.page.SimpleFragmentPage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SampleTest {

    @JvmField
    @RegisterExtension
    val uiTestExtension = UiTestExtension { SimpleFragmentPage(it, TestFragment::class) }


    @Test
    fun captureTestFragment() {
        uiTestExtension.page.launchFragmentSimply()
        uiTestExtension.page.captureActivityOrFragment("TestFragment")
    }

    class TestFragment() : Fragment()
}