package com.example.carrentalapp

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.SearchView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
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
import com.example.carrentalapp.ui.RentedCarAdapter
import com.example.carrentalapp.ui.SearchResultAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private var currentCarIndex = 0
    private var displayedCars: List<Car> = CarRepository.availableCars
    private lateinit var favouriteAdapter: FavouriteAdapter
    private lateinit var rentedCarAdapter: RentedCarAdapter
    private lateinit var searchResultAdapter: SearchResultAdapter
    private var isSearchActive = false


    private val rentResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val carId = result.data?.getIntExtra(EXTRA_CAR_ID, -1) ?: -1
        val days = result.data?.getIntExtra(EXTRA_RENTAL_DAYS, 0) ?: 0
        val confirmed = result.resultCode == RESULT_OK

        if (carId > 0 && confirmed && days > 0) {
            val success = CarRepository.rentCar(carId, days)
            if (success) {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.booking_confirmed), Snackbar.LENGTH_LONG).show()
                updateCurrentCar()
                updateFavourites()
                updateBalance()
                updateRentals()
            }
        } else if (carId > 0 && !confirmed) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.booking_cancelled), Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        AppCompatDelegate.setDefaultNightMode(prefs.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))
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

    override fun onRestart() {
        super.onRestart()
        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        val savedMode = prefs.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        if (savedMode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            delegate.setLocalNightMode(savedMode)
        }
    }

    private fun setupViews() {
        findViewById<MaterialButton>(R.id.nextBtn).setOnClickListener { showNextCar() }
        findViewById<MaterialButton>(R.id.rentBtn).setOnClickListener { openRentalScreen() }
        findViewById<ImageButton>(R.id.favBtn).setOnClickListener { toggleFavourite() }
        findViewById<ImageView>(R.id.carImage).setOnLongClickListener {
            toggleFavourite()
            val msg = if (CarRepository.isFavourite(displayedCars[currentCarIndex].id))
                getString(R.string.added_to_favourites) else getString(R.string.removed_from_favourites)
            Snackbar.make(it, msg, Snackbar.LENGTH_SHORT).show()
            true
        }
        findViewById<SwitchMaterial>(R.id.darkModeToggle).setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            getSharedPreferences("theme", MODE_PRIVATE).edit().putInt("night_mode", mode).apply()
            AppCompatDelegate.setDefaultNightMode(mode)
        }
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

        findViewById<SearchView>(R.id.searchInput).setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchCars(query ?: "")
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                searchCars(newText ?: "")
                return true
            }
        })

        val searchResultsList = findViewById<RecyclerView>(R.id.searchResultsList)
        searchResultsList.layoutManager = LinearLayoutManager(this@MainActivity)
        searchResultAdapter = SearchResultAdapter(emptyList()) { car ->
            val idx = displayedCars.indexOfFirst { it.id == car.id }
            if (idx >= 0) {
                currentCarIndex = idx
                updateDisplayedCar()
            }
            findViewById<SearchView>(R.id.searchInput).setQuery("", false)
            searchResultsList.visibility = android.view.View.GONE
            isSearchActive = false
        }
        searchResultsList.adapter = searchResultAdapter

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

        findViewById<RecyclerView>(R.id.rentalsRecycler).apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            rentedCarAdapter = RentedCarAdapter(CarRepository.rentedCars)
            adapter = rentedCarAdapter
        }
        updateRentals()
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
            findViewById<TextView>(R.id.carName).text = getString(R.string.no_cars_available)
            findViewById<MaterialButton>(R.id.rentBtn).isEnabled = false
            findViewById<MaterialButton>(R.id.nextBtn).isEnabled = false
            return
        }
        val car = displayedCars[currentCarIndex]
        findViewById<TextView>(R.id.carName).text = car.name
        findViewById<TextView>(R.id.carSubtitle).text = getString(R.string.car_subtitle, car.model, car.year.toString())
        findViewById<TextView>(R.id.carPrice).text = getString(R.string.car_price, car.dailyCost.toString())
        findViewById<TextView>(R.id.kilometresText).text = getString(R.string.car_kilometres, car.kilometres.toString())
        findViewById<RatingBar>(R.id.ratingBar).apply {
            rating = car.rating
            (progressDrawable as? android.graphics.drawable.LayerDrawable)
                ?.findDrawableByLayerId(android.R.id.background)
                ?.setTint(android.graphics.Color.WHITE)
        }
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
        findViewById<TextView>(R.id.balanceText).text = getString(R.string.balance_credits, CarRepository.creditBalance.toString())
    }

    private fun updateRentals() {
        rentedCarAdapter.update(CarRepository.rentedCars)
    }

    private fun isNightMode(): Boolean {
        val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun updateDarkModeIcon() {
        val toggle = findViewById<SwitchMaterial>(R.id.darkModeToggle)
        toggle.setOnCheckedChangeListener(null)
        toggle.isChecked = isNightMode()
        toggle.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            getSharedPreferences("theme", MODE_PRIVATE).edit().putInt("night_mode", mode).apply()
            delegate.setLocalNightMode(mode)
        }
        findViewById<ImageView>(R.id.darkModeIcon).setImageResource(
            if (isNightMode()) R.drawable.ic_dark_mode else R.drawable.ic_light_mode
        )
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
        if (query.isBlank()) {
            findViewById<RecyclerView>(R.id.searchResultsList).visibility = android.view.View.GONE
            isSearchActive = false
            return
        }
        val matches = CarRepository.availableCars.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.model.contains(query, ignoreCase = true)
        }
        findViewById<RecyclerView>(R.id.searchResultsList).visibility =
            if (matches.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
        searchResultAdapter.update(matches)
        isSearchActive = true
    }

    companion object {
        const val EXTRA_CAR_DATA = "car_data"
        const val EXTRA_CAR_ID = "car_id"
        const val EXTRA_RENTAL_DAYS = "rental_days"
    }
}
