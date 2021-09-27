package app.mobilitytechnologies.uitest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import app.mobilitytechnologies.uitest.extension.UiTestExtension
import app.mobilitytechnologies.uitest.page.SimpleFragmentPage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SimpleFragmentTest {

    @JvmField
    @RegisterExtension
    val uiTestExtension = UiTestExtension { SimpleFragmentPage(it, TestFragment::class) }


    @Test
    fun captureTestFragment() {
        uiTestExtension.page.launchFragmentSimply()
        uiTestExtension.page.captureDisplay("TestFragment")
    }

    class TestFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val textView = TextView(requireContext())
            textView.text = "TestFragment"
            return textView
        }
    }
}