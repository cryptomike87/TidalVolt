package com.tidalvolt.app

data class ChargingEstimate(val batteryKwh: Double, val gridKwh: Double, val cost: Double)

object ChargingMath {
    fun toTarget(capacityKwh: Double, currentSoc: Double, targetSoc: Double, rate: Double, efficiency: Double = .95): ChargingEstimate {
        require(capacityKwh > 0 && rate >= 0 && efficiency > 0 && efficiency <= 1)
        val start = currentSoc.coerceIn(0.0, 100.0)
        val end = targetSoc.coerceIn(start, 100.0)
        val battery = capacityKwh * (end - start) / 100.0
        val grid = battery / efficiency
        return ChargingEstimate(battery, grid, grid * rate)
    }
    fun evCost(miles: Double, kwhPer100Miles: Double, rate: Double, efficiency: Double = .95): Double {
        require(miles >= 0 && kwhPer100Miles > 0 && rate >= 0 && efficiency > 0 && efficiency <= 1)
        return miles * kwhPer100Miles / 100.0 / efficiency * rate
    }
    fun petrolCost(miles: Double, mpg: Double, pricePerGallon: Double): Double {
        require(miles >= 0 && mpg > 0 && pricePerGallon >= 0)
        return miles / mpg * pricePerGallon
    }
}
