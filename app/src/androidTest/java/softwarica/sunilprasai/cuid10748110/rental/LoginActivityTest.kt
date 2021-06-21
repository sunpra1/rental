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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import softwarica.sunilprasai.cuid10748110.rental.utils.UserToken

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {
    @get:Rule
    var rule: ActivityScenarioRule<LoginActivity> =
        ActivityScenarioRule(LoginActivity::class.java)
    private lateinit var scenario: ActivityScenario<LoginActivity>
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = getInstrumentation().targetContext
        UserToken.getInstance(context).deleteToken()
        scenario = rule.scenario
    }

    @Test
    fun testLoginFormValidation() {
        onView(withId(R.id.loginBtn)).perform(scrollTo())
        onView(withId(R.id.loginBtn)).perform(click())
        getInstrumentation().waitForIdleSync()
        scenario.onActivity {
            assertTrue(it.findViewById<TextInputLayout>(R.id.phoneEmailTil).isErrorEnabled)
            assertTrue(it.findViewById<TextInputLayout>(R.id.passwordTil).isErrorEnabled)
        }
    }

    @Test
    fun testLoginFunctionality() {
        onView(withId(R.id.phoneEmailEt)).perform(scrollTo())
        onView(withId(R.id.phoneEmailEt)).perform(typeText("9849147995"))

        onView(withId(R.id.passwordEt)).perform(scrollTo())
        onView(withId(R.id.passwordEt)).perform(typeText("sunpra12"))

        closeSoftKeyboard()

        onView(withId(R.id.loginBtn)).perform(scrollTo())
        onView(withId(R.id.loginBtn)).perform(click())

        getInstrumentation().waitForIdleSync()
        assertNotNull(UserToken.getInstance(context).token)
    }
}