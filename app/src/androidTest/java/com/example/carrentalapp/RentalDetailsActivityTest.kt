package com.example.carrentalapp

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.carrentalapp.data.CarRepository
import com.example.carrentalapp.model.Car
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RentalDetailsActivityTest {

    private val car: Car = CarRepository.availableCars.first()

    @get:Rule
    val activityRule = ActivityScenarioRule<RentalDetailsActivity>(
        Intent(
            ApplicationProvider.getApplicationContext(),
            RentalDetailsActivity::class.java
        ).apply {
            putExtra(MainActivity.EXTRA_CAR_DATA, car)
        }
    )

    @Before
    fun setUp() {
        CarRepository.reset()
    }

    @Test
    fun launches_showsCarName() {
        onView(withId(R.id.rentalCarName)).check(matches(withText(car.name)))
    }

    @Test
    fun launches_showsCarSubtitle() {
        onView(withId(R.id.rentalCarSubtitle)).check(matches(withText("${car.model} \u2022 ${car.year}")))
    }

    @Test
    fun launches_showsDailyCost() {
        onView(withId(R.id.rentalDailyCost)).check(matches(withText("$${car.dailyCost}/day")))
    }

    @Test
    fun launches_showsRatingBar() {
        onView(withId(R.id.rentalRatingBar)).check(matches(isDisplayed()))
    }

    @Test
    fun launches_showsSlider() {
        onView(withId(R.id.daySlider)).check(matches(isDisplayed()))
    }

    @Test
    fun launches_showsPickupDate() {
        onView(withId(R.id.pickupDateText)).check(matches(withText("Today")))
    }

    @Test
    fun launches_showsDailyRentalAmount() {
        onView(withId(R.id.dailyRentalAmount)).check(matches(withText("$${car.dailyCost}.00")))
    }

    @Test
    fun launches_showsCreditBalance() {
        onView(withId(R.id.balanceText)).check(matches(isDisplayed()))
    }

    @Test
    fun launches_showsTotalCost() {
        val expected = "$$car.dailyCost"
        onView(withId(R.id.totalCostText)).check(matches(withText(expected)))
    }

    @Test
    fun launches_showsBackButton() {
        onView(withId(R.id.backBtn)).check(matches(isDisplayed()))
    }

    @Test
    fun launches_showsTermsLink() {
        onView(withId(R.id.termsLink)).check(matches(isDisplayed()))
    }
}
