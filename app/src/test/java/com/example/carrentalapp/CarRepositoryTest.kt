package com.example.carrentalapp

import com.example.carrentalapp.data.CarRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CarRepositoryTest {
    @Before
    fun setUp() {
        CarRepository.reset()
    }

    @Test
    fun initialCarsCount_isFive() {
        assertEquals(5, CarRepository.availableCars.size)
    }

    @Test
    fun initialBalance_is500() {
        assertEquals(500, CarRepository.creditBalance)
    }

    @Test
    fun rentCar_deductsBalance() {
        val car = CarRepository.availableCars.first()
        CarRepository.rentCar(car.id, 1)
        assertEquals(500 - car.dailyCost, CarRepository.creditBalance)
    }

    @Test
    fun rentCar_removesCarFromAvailable() {
        val car = CarRepository.availableCars.first()
        CarRepository.rentCar(car.id, 1)
        assertFalse(CarRepository.availableCars.any { it.id == car.id })
    }

    @Test
    fun rentCar_exceeding400_fails() {
        val car = CarRepository.availableCars.maxBy { it.dailyCost }
        assertFalse(CarRepository.rentCar(car.id, 7))
    }

    @Test
    fun rentCar_exceedingBalance_fails() {
        val cars = CarRepository.availableCars.sortedByDescending { it.dailyCost }
        if (cars.isNotEmpty()) {
            CarRepository.rentCar(cars[0].id, 1)
        }
        val remaining = CarRepository.availableCars.firstOrNull()
        if (remaining != null && remaining.dailyCost > CarRepository.creditBalance) {
            assertFalse(CarRepository.rentCar(remaining.id, 1))
        }
    }

    @Test
    fun toggleFavourite_addsAndRemoves() {
        val car = CarRepository.availableCars.first()
        assertFalse(CarRepository.isFavourite(car.id))
        CarRepository.toggleFavourite(car.id)
        assertTrue(CarRepository.isFavourite(car.id))
        CarRepository.toggleFavourite(car.id)
        assertFalse(CarRepository.isFavourite(car.id))
    }

    @Test
    fun rentCar_failsForInvalidId() {
        assertFalse(CarRepository.rentCar(999, 1))
    }
}
