package com.example.carrentalapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        findViewById<ImageView>(R.id.rentalCarImage).setImageResource(car.imageResId)
        findViewById<TextView>(R.id.rentalCarName).text = car.name
        findViewById<TextView>(R.id.rentalCarSubtitle).text = "${car.model} \u2022 ${car.year}"
        findViewById<TextView>(R.id.rentalDailyCost).text = "$${car.dailyCost} / day"

        val slider = findViewById<Slider>(R.id.daySlider)
        slider.addOnChangeListener { _, value, _ ->
            selectedDays = value.toInt()
            findViewById<TextView>(R.id.selectedDaysText).text = "$selectedDays day${if (selectedDays > 1) "s" else ""}"
            updateTotalCost()
        }

        findViewById<MaterialButton>(R.id.backBtn).setOnClickListener {
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

        updateTotalCost()
    }

    private fun updateTotalCost() {
        val totalCost = car.dailyCost * selectedDays
        findViewById<TextView>(R.id.totalCostText).text = "$$totalCost"
        findViewById<TextView>(R.id.errorText).visibility = android.view.View.GONE
    }

    private fun showError(message: String) {
        findViewById<TextView>(R.id.errorText).text = message
        findViewById<TextView>(R.id.errorText).visibility = android.view.View.VISIBLE
    }
}
