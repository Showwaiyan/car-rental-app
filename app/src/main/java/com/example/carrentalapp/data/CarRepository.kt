package com.example.carrentalapp.data

import com.example.carrentalapp.R
import com.example.carrentalapp.model.Car

object CarRepository {
    private val _availableCars = mutableListOf(
        Car(1, "EcoDrive 500", "Luxury Electric Sedan", 2024, 4.9f, 15000, 89, R.drawable.ic_launcher_foreground),
        Car(2, "Roadster V8", "Convertible Sport", 2023, 4.8f, 22000, 125, R.drawable.ic_launcher_foreground),
        Car(3, "Atlas 4x4", "Rugged Off-Road SUV", 2024, 4.7f, 18000, 110, R.drawable.ic_launcher_foreground),
        Car(4, "Prime Executive", "Full-size Luxury Sedan", 2023, 5.0f, 12000, 150, R.drawable.ic_launcher_foreground),
        Car(5, "Urban Mini E", "Compact City Electric", 2024, 4.6f, 8000, 65, R.drawable.ic_launcher_foreground),
    )

    val availableCars: List<Car> get() = _availableCars.toList()

    private val _favouriteIds = mutableSetOf<Int>()
    val favouriteCars: List<Car> get() = _availableCars.filter { it.id in _favouriteIds }

    var creditBalance = 500
        private set

    fun rentCar(carId: Int, days: Int): Boolean {
        val car = _availableCars.find { it.id == carId } ?: return false
        val totalCost = car.dailyCost * days
        if (totalCost > 400 || totalCost > creditBalance) return false
        creditBalance -= totalCost
        _availableCars.removeAll { it.id == carId }
        _favouriteIds.remove(carId)
        return true
    }

    fun isFavourite(carId: Int): Boolean = carId in _favouriteIds

    fun toggleFavourite(carId: Int) {
        if (!_favouriteIds.remove(carId)) _favouriteIds.add(carId)
    }

    fun reset() {
        _availableCars.clear()
        _availableCars.addAll(
            listOf(
                Car(1, "EcoDrive 500", "Luxury Electric Sedan", 2024, 4.9f, 15000, 89, R.drawable.ic_launcher_foreground),
                Car(2, "Roadster V8", "Convertible Sport", 2023, 4.8f, 22000, 125, R.drawable.ic_launcher_foreground),
                Car(3, "Atlas 4x4", "Rugged Off-Road SUV", 2024, 4.7f, 18000, 110, R.drawable.ic_launcher_foreground),
                Car(4, "Prime Executive", "Full-size Luxury Sedan", 2023, 5.0f, 12000, 150, R.drawable.ic_launcher_foreground),
                Car(5, "Urban Mini E", "Compact City Electric", 2024, 4.6f, 8000, 65, R.drawable.ic_launcher_foreground),
            )
        )
        _favouriteIds.clear()
        creditBalance = 500
    }
}
