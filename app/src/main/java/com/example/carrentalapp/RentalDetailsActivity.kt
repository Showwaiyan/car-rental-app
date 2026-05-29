package com.example.carrentalapp

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Paint
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.carrentalapp.data.CarRepository
import com.example.carrentalapp.model.Car
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar

class RentalDetailsActivity : AppCompatActivity() {

    private lateinit var car: Car
    private var selectedDays = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rental_details)

        car = intent.getParcelableExtra(MainActivity.EXTRA_CAR_DATA) ?: run {
            Toast.makeText(this, "Car data missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
    }

    private fun setupViews() {
        findViewById<TextView>(R.id.rentalCarName).text = car.name
        findViewById<TextView>(R.id.rentalCarSubtitle).text = "${car.model} \u2022 ${car.year}"
        findViewById<TextView>(R.id.rentalDailyCost).text = "$${car.dailyCost}/day"
        findViewById<TextView>(R.id.rentalRatingText).text = car.rating.toString()
        findViewById<ImageView>(R.id.rentalCarImage).setImageResource(car.imageResId)
        findViewById<TextView>(R.id.dailyRentalAmount).text = "$${car.dailyCost}.00"
        findViewById<TextView>(R.id.pickupDateText).text = "Today"

        val slider = findViewById<Slider>(R.id.daySlider)
        slider.addOnChangeListener { _, value, _ ->
            selectedDays = value.toInt()
            val label = "$selectedDays day${if (selectedDays > 1) "s" else ""}"
            findViewById<TextView>(R.id.selectedDaysText).text = label
            findViewById<TextView>(R.id.dailyRentalLabel).text = "Daily rental ($label)"
            updateTotalCost()
        }

        findViewById<ImageButton>(R.id.backBtn).setOnClickListener {
            val intent = Intent().apply {
                putExtra(MainActivity.EXTRA_CAR_ID, car.id)
                putExtra(MainActivity.EXTRA_RENTAL_DAYS, selectedDays)
            }
            setResult(RESULT_CANCELED, intent)
            Snackbar.make(findViewById(android.R.id.content), "Booking cancelled.", Snackbar.LENGTH_LONG).show()
            finish()
        }

        findViewById<MaterialButton>(R.id.saveBtn).setOnClickListener {
            val totalCost = car.dailyCost * selectedDays
            if (totalCost > 400) {
                showError("Booking cannot exceed 400 credits")
                return@setOnClickListener
            }
            if (totalCost > CarRepository.creditBalance) {
                showError("Insufficient credits. You have ${CarRepository.creditBalance} credits.")
                return@setOnClickListener
            }
            val intent = Intent().apply {
                putExtra(MainActivity.EXTRA_CAR_ID, car.id)
                putExtra(MainActivity.EXTRA_RENTAL_DAYS, selectedDays)
            }
            setResult(RESULT_OK, intent)
            Snackbar.make(findViewById(android.R.id.content), "Booking confirmed!", Snackbar.LENGTH_LONG).show()
            finish()
        }

        findViewById<TextView>(R.id.termsLink).apply {
            paint.flags = paint.flags or Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener {
                Snackbar.make(findViewById(android.R.id.content), "Terms & Conditions available on our website.", Snackbar.LENGTH_LONG).show()
            }
        }

        findViewById<ImageButton>(R.id.darkModeToggle).setOnClickListener { toggleDarkMode() }
        updateDarkModeIcon()

        updateTotalCost()
    }

    private fun isNightMode(): Boolean {
        val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun toggleDarkMode() {
        AppCompatDelegate.setDefaultNightMode(
            if (isNightMode()) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
        )
    }

    private fun updateDarkModeIcon() {
        val toggle = findViewById<ImageButton>(R.id.darkModeToggle)
        toggle.setImageResource(if (isNightMode()) R.drawable.ic_light_mode else R.drawable.ic_dark_mode)
        val color = com.google.android.material.color.MaterialColors.getColor(
            toggle, com.google.android.material.R.attr.colorOnSurfaceVariant
        )
        toggle.imageTintList = ColorStateList.valueOf(color)
    }

    private fun updateTotalCost() {
        val totalCost = car.dailyCost * selectedDays
        val costText = "$$totalCost"
        findViewById<TextView>(R.id.totalCostText).text = costText
        findViewById<TextView>(R.id.btnTotalPrice).text = costText
        findViewById<TextView>(R.id.errorText).visibility = android.view.View.GONE
    }

    private fun showError(message: String) {
        findViewById<TextView>(R.id.errorText).text = message
        findViewById<TextView>(R.id.errorText).visibility = android.view.View.VISIBLE
    }
}
