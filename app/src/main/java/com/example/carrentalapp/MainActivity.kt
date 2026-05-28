package com.example.carrentalapp

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carrentalapp.data.CarRepository
import com.example.carrentalapp.model.Car
import com.example.carrentalapp.ui.FavouriteAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private var currentCarIndex = 0
    private var displayedCars: List<Car> = CarRepository.availableCars
    private lateinit var favouriteAdapter: FavouriteAdapter

    private val rentResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val carId = result.data?.getIntExtra(EXTRA_CAR_ID, -1) ?: -1
        val days = result.data?.getIntExtra(EXTRA_RENTAL_DAYS, 0) ?: 0
        val confirmed = result.resultCode == RESULT_OK

        if (carId > 0 && confirmed && days > 0) {
            val success = CarRepository.rentCar(carId, days)
            if (success) {
                Snackbar.make(findViewById(android.R.id.content), "Booking confirmed!", Snackbar.LENGTH_LONG).show()
                updateCurrentCar()
                updateFavourites()
                updateBalance()
            }
        } else if (carId > 0 && !confirmed) {
            Snackbar.make(findViewById(android.R.id.content), "Booking cancelled.", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupViews()
        updateCurrentCar()
        updateFavourites()
        updateBalance()
    }

    private fun setupViews() {
        findViewById<MaterialButton>(R.id.nextBtn).setOnClickListener { showNextCar() }
        findViewById<MaterialButton>(R.id.rentBtn).setOnClickListener { openRentalScreen() }
        findViewById<ImageButton>(R.id.favBtn).setOnClickListener { toggleFavourite() }
        findViewById<ImageButton>(R.id.darkModeToggle).setOnClickListener { toggleDarkMode() }
        updateDarkModeIcon()

        findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.sortToggleGroup)
            .addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (!isChecked) return@addOnButtonCheckedListener
                when (checkedId) {
                    R.id.sortRatingBtn -> sortByRating()
                    R.id.sortYearBtn -> sortByYear()
                    R.id.sortCostBtn -> sortByCost()
                }
            }

        val searchInput = findViewById<EditText>(R.id.searchInput)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchCars(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val recycler = findViewById<RecyclerView>(R.id.favouritesRecycler)
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        favouriteAdapter = FavouriteAdapter(emptyList()) { car ->
            val idx = displayedCars.indexOfFirst { it.id == car.id }
            if (idx >= 0) {
                currentCarIndex = idx
                updateDisplayedCar()
            }
        }
        recycler.adapter = favouriteAdapter
    }

    private fun showNextCar() {
        if (displayedCars.isEmpty()) return
        currentCarIndex = (currentCarIndex + 1) % displayedCars.size
        updateDisplayedCar()
    }

    private fun updateCurrentCar() {
        displayedCars = CarRepository.availableCars
        currentCarIndex = 0
        if (displayedCars.isNotEmpty()) updateDisplayedCar()
    }

    private fun updateDisplayedCar() {
        if (displayedCars.isEmpty()) {
            findViewById<TextView>(R.id.carName).text = "No cars available"
            findViewById<MaterialButton>(R.id.rentBtn).isEnabled = false
            findViewById<MaterialButton>(R.id.nextBtn).isEnabled = false
            return
        }
        val car = displayedCars[currentCarIndex]
        findViewById<TextView>(R.id.carName).text = car.name
        findViewById<TextView>(R.id.carSubtitle).text = "${car.model} \u2022 ${car.year}"
        findViewById<TextView>(R.id.carPrice).text = "$${car.dailyCost} / day"
        findViewById<TextView>(R.id.kilometresText).text = "${car.kilometres} km"
        findViewById<TextView>(R.id.ratingText).text = car.rating.toString()
        findViewById<TextView>(R.id.typeText).text = car.model.substringBefore(" ").uppercase()
        findViewById<ImageView>(R.id.carImage).setImageResource(car.imageResId)
        findViewById<MaterialButton>(R.id.rentBtn).isEnabled = true
        findViewById<MaterialButton>(R.id.nextBtn).isEnabled = true
        updateFavIcon(CarRepository.isFavourite(car.id))
    }

    private fun updateFavIcon(isFav: Boolean) {
        findViewById<ImageButton>(R.id.favBtn).setImageResource(
            if (isFav) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )
    }

    private fun openRentalScreen() {
        if (displayedCars.isEmpty()) return
        val car = displayedCars[currentCarIndex]
        val intent = Intent(this, RentalDetailsActivity::class.java).apply {
            putExtra(EXTRA_CAR_DATA, car)
        }
        rentResultLauncher.launch(intent)
    }

    private fun toggleFavourite() {
        if (displayedCars.isEmpty()) return
        val car = displayedCars[currentCarIndex]
        CarRepository.toggleFavourite(car.id)
        updateFavIcon(CarRepository.isFavourite(car.id))
        updateFavourites()
    }

    private fun updateFavourites() {
        favouriteAdapter.update(CarRepository.favouriteCars)
    }

    private fun updateBalance() {
        findViewById<TextView>(R.id.balanceText).text = "${CarRepository.creditBalance} Credits"
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

    private fun sortByRating() {
        displayedCars = displayedCars.sortedByDescending { it.rating }
        currentCarIndex = 0
        updateDisplayedCar()
    }

    private fun sortByYear() {
        displayedCars = displayedCars.sortedByDescending { it.year }
        currentCarIndex = 0
        updateDisplayedCar()
    }

    private fun sortByCost() {
        displayedCars = displayedCars.sortedBy { it.dailyCost }
        currentCarIndex = 0
        updateDisplayedCar()
    }

    private fun searchCars(query: String) {
        displayedCars = if (query.isBlank()) {
            CarRepository.availableCars
        } else {
            CarRepository.availableCars.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.model.contains(query, ignoreCase = true)
            }
        }
        currentCarIndex = 0
        if (displayedCars.isNotEmpty()) updateDisplayedCar()
        else {
            findViewById<TextView>(R.id.carName).text = "No cars found"
        }
    }

    companion object {
        const val EXTRA_CAR_DATA = "car_data"
        const val EXTRA_CAR_ID = "car_id"
        const val EXTRA_RENTAL_DAYS = "rental_days"
    }
}
