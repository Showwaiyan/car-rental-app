package com.example.carrentalapp.data

import com.example.carrentalapp.R
import com.example.carrentalapp.model.Car
import com.example.carrentalapp.model.Rental

object CarRepository {
    private val _availableCars = mutableListOf(
        Car(1, "Tesla Model S", "Luxury Electric Sedan", 2024, 4.9f, 15000, 89, R.drawable.car_tesla_model_s),
        Car(2, "Mazda MX-5", "Convertible Sport", 2023, 4.8f, 22000, 125, R.drawable.car_mazda_mx5),
        Car(3, "Toyota Land Cruiser", "Rugged Off-Road SUV", 2024, 4.7f, 18000, 110, R.drawable.car_toyota_land_cruiser),
        Car(4, "Mercedes-Benz S-Class", "Full-size Luxury Sedan", 2023, 5.0f, 12000, 150, R.drawable.car_mercedes_s_class),
        Car(5, "Mini Cooper SE", "Compact City Electric", 2024, 4.6f, 8000, 65, R.drawable.car_mini_cooper_se),
    )

    val availableCars: List<Car> get() = _availableCars.toList()

    private val _favouriteIds = mutableSetOf<Int>()
    val favouriteCars: List<Car> get() = _availableCars.filter { it.id in _favouriteIds }

    private val _rentals = mutableListOf<Rental>()
    val rentedCars: List<Rental> get() = _rentals.toList()

    var creditBalance = 500
        private set

    fun rentCar(carId: Int, days: Int): Boolean {
        val car = _availableCars.find { it.id == carId } ?: return false
        val totalCost = car.dailyCost * days
        if (totalCost > 400 || totalCost > creditBalance) return false
        creditBalance -= totalCost
        _availableCars.removeAll { it.id == carId }
        _favouriteIds.remove(carId)
        _rentals.add(Rental(car, days))
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
                Car(1, "Tesla Model S", "Luxury Electric Sedan", 2024, 4.9f, 15000, 89, R.drawable.car_tesla_model_s),
                Car(2, "Mazda MX-5", "Convertible Sport", 2023, 4.8f, 22000, 125, R.drawable.car_mazda_mx5),
                Car(3, "Toyota Land Cruiser", "Rugged Off-Road SUV", 2024, 4.7f, 18000, 110, R.drawable.car_toyota_land_cruiser),
                Car(4, "Mercedes-Benz S-Class", "Full-size Luxury Sedan", 2023, 5.0f, 12000, 150, R.drawable.car_mercedes_s_class),
                Car(5, "Mini Cooper SE", "Compact City Electric", 2024, 4.6f, 8000, 65, R.drawable.car_mini_cooper_se),
            )
        )
        _favouriteIds.clear()
        _rentals.clear()
        creditBalance = 500
    }
}
