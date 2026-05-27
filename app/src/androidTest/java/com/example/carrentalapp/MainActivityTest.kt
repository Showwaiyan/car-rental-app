package com.example.carrentalapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.carrentalapp.data.CarRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        CarRepository.reset()
    }

    @Test
    fun appLaunches_showsCarName() {
        onView(withId(R.id.carName)).check(matches(isDisplayed()))
    }

    @Test
    fun appLaunches_showsBalance() {
        onView(withId(R.id.balanceText)).check(matches(withText("500 Credits")))
    }

    @Test
    fun clickingNext_changesCarName() {
        val carNames = CarRepository.availableCars.map { it.name }
        onView(withId(R.id.carName)).check(matches(withText(carNames[0])))
        onView(withId(R.id.nextBtn)).perform(click())
        onView(withId(R.id.carName)).check(matches(withText(carNames[1])))
    }

    @Test
    fun clickingRent_opensRentalDetails() {
        onView(withId(R.id.rentBtn)).perform(click())
        onView(withId(R.id.rentalCarName)).check(matches(isDisplayed()))
    }
}
