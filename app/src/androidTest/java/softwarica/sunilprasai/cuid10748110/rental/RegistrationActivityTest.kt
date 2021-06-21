package softwarica.sunilprasai.cuid10748110.rental

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.google.android.material.textfield.TextInputLayout
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import softwarica.sunilprasai.cuid10748110.rental.utils.UserToken

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class RegistrationActivityTest {
    @get:Rule
    var rule: ActivityScenarioRule<RegisterActivity> =
        ActivityScenarioRule(RegisterActivity::class.java)
    private lateinit var scenario: ActivityScenario<RegisterActivity>
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = getInstrumentation().targetContext
        UserToken.getInstance(context).deleteToken()
        scenario = rule.scenario
    }

    @Test
    fun testARegistrationFormValidation() {
        onView(withId(R.id.registerBtn)).perform(scrollTo())
        onView(withId(R.id.registerBtn)).perform(click())
        getInstrumentation().waitForIdleSync()
        scenario.onActivity {
            assertTrue(it.findViewById<TextInputLayout>(R.id.nameTil).isErrorEnabled)
            assertTrue(it.findViewById<TextInputLayout>(R.id.phoneTil).isErrorEnabled)
            assertTrue(it.findViewById<TextInputLayout>(R.id.passwordTil).isErrorEnabled)
            assertTrue(it.findViewById<TextInputLayout>(R.id.cPasswordTil).isErrorEnabled)
        }
    }

    @Test
    fun testBRegistrationEmailValidation() {
        onView(withId(R.id.phoneEt)).perform(scrollTo())
        onView(withId(R.id.phoneEt)).perform(typeText("9849147995"))

        onView(withId(R.id.passwordEt)).perform(scrollTo())
        onView(withId(R.id.passwordEt)).perform(click())

        getInstrumentation().waitForIdleSync()
        scenario.onActivity {
            assertTrue(it.findViewById<TextInputLayout>(R.id.phoneTil).isErrorEnabled)
        }
    }

    @Test
    fun testCRegistrationFunctionality() {
        onView(withId(R.id.nameEt)).perform(scrollTo())
        onView(withId(R.id.nameEt)).perform(typeText("Sunil Prasai"))

        onView(withId(R.id.phoneEt)).perform(scrollTo())
        onView(withId(R.id.phoneEt)).perform(typeText("9849147996"))

        onView(withId(R.id.passwordEt)).perform(scrollTo())
        onView(withId(R.id.passwordEt)).perform(typeText("sunpra12"))

        onView(withId(R.id.cPasswordEt)).perform(scrollTo())
        onView(withId(R.id.cPasswordEt)).perform(typeText("sunpra12"))

        closeSoftKeyboard()

        onView(withId(R.id.registerBtn)).perform(scrollTo())
        onView(withId(R.id.registerBtn)).perform(click())

        getInstrumentation().waitForIdleSync()
        assertNotNull(UserToken.getInstance(context).token)
    }
}