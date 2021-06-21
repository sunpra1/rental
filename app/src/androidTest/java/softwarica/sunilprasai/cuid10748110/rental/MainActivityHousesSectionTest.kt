package softwarica.sunilprasai.cuid10748110.rental

import android.app.Activity.RESULT_OK
import android.app.Instrumentation.ActivityResult
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Assert.*
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class MainActivityHousesSectionTest {
    @get:Rule
    var rule: IntentsTestRule<MainActivity> =
        IntentsTestRule(MainActivity::class.java)
    private lateinit var context: Context
    private lateinit var mainActivity: MainActivity

    @Before
    fun setUp() {
        context = getInstrumentation().targetContext
        mainActivity = rule.activity!!
    }

    @Test
    fun testAAddHouseFormValidation() {
        onView(withId(R.id.addHouseOptionMenu)).perform(click())

        getInstrumentation().waitForIdleSync()

        onView(withId(R.id.addHouseBtn)).perform(scrollTo())
        onView(withId(R.id.addHouseBtn)).perform(click())

        getInstrumentation().waitForIdleSync()

        assertTrue(mainActivity.findViewById<TextInputLayout>(R.id.addressTil).isErrorEnabled)
        assertTrue(mainActivity.findViewById<TextInputLayout>(R.id.floorsCountTil).isErrorEnabled)
        assertTrue(mainActivity.findViewById<TextInputLayout>(R.id.locationLatitudeTil).isErrorEnabled)
        assertTrue(mainActivity.findViewById<TextInputLayout>(R.id.locationLongitudeTil).isErrorEnabled)
        assertTrue(mainActivity.findViewById<TextInputLayout>(R.id.selectImagesBtnTil).isErrorEnabled)
    }

    @Test
    fun testBAddHouseFunctionality() {
        val houseCountBeforeAdding =
            mainActivity.findViewById<RecyclerView>(R.id.housesRV).adapter!!.itemCount
        val expectedIntent: Matcher<Intent> = allOf(
            hasAction(Intent.ACTION_PICK),
            hasType("image/*")
        )
        intending(expectedIntent).respondWith(createGalleryPickedActivityResult())
        onView(withId(R.id.addHouseOptionMenu)).perform(click())
        getInstrumentation().waitForIdleSync()

        onView(withId(R.id.addressEt)).perform(scrollTo())
        onView(withId(R.id.addressEt)).perform(typeText("Nepal"))

        onView(withId(R.id.floorsCountEt)).perform(scrollTo())
        onView(withId(R.id.floorsCountEt)).perform(typeText("3"))
        closeSoftKeyboard()

        onView(withId(R.id.getCurrentLocationBtn)).perform(scrollTo())
        onView(withId(R.id.getCurrentLocationBtn)).perform(click())
        getInstrumentation().waitForIdleSync()

        onView(withId(R.id.selectImagesBtn)).perform(scrollTo())
        onView(withId(R.id.selectImagesBtn)).perform(click())
        getInstrumentation().waitForIdleSync()
        intended(expectedIntent)
        getInstrumentation().waitForIdleSync()

        onView(withId(R.id.addHouseBtn)).perform(scrollTo())
        onView(withId(R.id.addHouseBtn)).perform(click())
        getInstrumentation().waitForIdleSync()

        val houseCountAfterAdding =
            mainActivity.findViewById<RecyclerView>(R.id.housesRV).adapter!!.itemCount
        assertEquals(houseCountBeforeAdding + 1, houseCountAfterAdding)
    }

    @Test
    fun testCEditHouseFunctionality() {
        onView(withText("Nepal")).perform(swipeRight())
        getInstrumentation().waitForIdleSync()

        onView(withId(R.id.addressEt)).perform(scrollTo())
        onView(withId(R.id.addressEt)).perform(clearText())
        onView(withId(R.id.addressEt)).perform(typeText("Nepal Updated"))

        onView(withId(R.id.floorsCountEt)).perform(scrollTo())
        onView(withId(R.id.floorsCountEt)).perform(clearText())
        onView(withId(R.id.floorsCountEt)).perform(typeText("2"))

        onView(withId(R.id.updateHouseBtn)).perform(scrollTo())
        onView(withId(R.id.updateHouseBtn)).perform(click())
        getInstrumentation().waitForIdleSync()
        Thread.sleep(3000)
        assertNotNull(onView(withText("Nepal Updated")))
        assertNotNull(onView(withText("2")))
    }

    @Test
    fun testDDeleteHouseFunctionality() {
        val houseCountBeforeAdding =
            mainActivity.findViewById<RecyclerView>(R.id.housesRV).adapter!!.itemCount
        onView(withText("Nepal Updated")).perform(swipeLeft())
        getInstrumentation().waitForIdleSync()
        Thread.sleep(3000)
        val houseCountAfterAdding =
            mainActivity.findViewById<RecyclerView>(R.id.housesRV).adapter!!.itemCount
        assertEquals(houseCountBeforeAdding, houseCountAfterAdding + 1)
    }

    private fun createGalleryPickedActivityResult(): ActivityResult {
        val imageUri = context.getExternalFilesDir("house.png")!!.toUri()
        return ActivityResult(RESULT_OK, Intent().apply {
            data = imageUri
        })
    }
}