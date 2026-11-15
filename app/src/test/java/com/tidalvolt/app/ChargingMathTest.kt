package com.tidalvolt.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ChargingMathTest {
    @Test fun includesFivePercentChargingLoss() {
        val estimate = ChargingMath.toTarget(45.1, 50.0, 100.0, .55)
        assertEquals(22.55, estimate.batteryKwh, .001)
        assertEquals(23.7368, estimate.gridKwh, .001)
        assertEquals(13.0553, estimate.cost, .001)
    }
    @Test fun evAndPetrolUseSameDistance() {
        assertEquals(11.5789, ChargingMath.evCost(100.0, 20.0, .55), .001)
        assertEquals(45.6667, ChargingMath.petrolCost(100.0, 15.0, 6.85), .001)
    }
}
