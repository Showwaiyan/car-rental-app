package com.example.carrentalapp.model

data class Rental(
    val car: Car,
    val days: Int,
) {
    val totalCost: Int get() = car.dailyCost * days
}
