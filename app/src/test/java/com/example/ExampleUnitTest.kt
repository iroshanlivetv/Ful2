package com.example

import com.example.util.VehicleValidator
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testValidSriLankanVehicleNumbers() {
        val validPlates = listOf(
            "ABC-1234",
            "123-2222",
            "52-2236",
            "AB2-8952",
            "WP-AB-9012",
            "WP ABC-1234",
            "WP 52-2236",
            "6-1234",
            "CAB-5678",
            "SP-AB2-8952"
        )
        for (plate in validPlates) {
            assertTrue("Expected '$plate' to be valid", VehicleValidator.isValidVehicleNumber(plate))
        }
    }

    @Test
    fun testRejectNamesAndInvalidFormats() {
        val invalidInputs = listOf(
            "Kamal",
            "Kasun",
            "Ruwan",
            "Toyota",
            "John Doe",
            "My Car",
            "Car",
            "12345",
            "Vehicle",
            "Prius-2020",
            "Honda-5678"
        )
        for (input in invalidInputs) {
            assertFalse("Expected '$input' to be rejected", VehicleValidator.isValidVehicleNumber(input))
        }
    }

    @Test
    fun testDuplicateVehicleNumberDetection() {
        val existing = listOf("CAS-1234", "52-2236", "WP-AB-9012")

        // Exact match
        assertTrue(VehicleValidator.isDuplicateVehicleNumber("CAS-1234", existing))
        // Case-insensitive
        assertTrue(VehicleValidator.isDuplicateVehicleNumber("cas-1234", existing))
        // Space instead of hyphen
        assertTrue(VehicleValidator.isDuplicateVehicleNumber("CAS 1234", existing))
        // With province prefix
        assertTrue(VehicleValidator.isDuplicateVehicleNumber("WP-CAS-1234", existing))
        // Numeric vintage plate
        assertTrue(VehicleValidator.isDuplicateVehicleNumber("52-2236", existing))
        // Modern plate with province stripped
        assertTrue(VehicleValidator.isDuplicateVehicleNumber("AB-9012", existing))

        // Non duplicates
        assertFalse(VehicleValidator.isDuplicateVehicleNumber("CAB-1234", existing))
        assertFalse(VehicleValidator.isDuplicateVehicleNumber("53-2236", existing))
        assertFalse(VehicleValidator.isDuplicateVehicleNumber("WP-XY-1234", existing))
    }
}
