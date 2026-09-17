package com.example.util

object VehicleValidator {

    private val PROVINCE_PREFIXES = listOf("WP", "CP", "SP", "NP", "EP", "NW", "NC", "UP", "SG")

    /**
     * Normalizes a vehicle number for comparison by removing hyphens, spaces, and converting to uppercase.
     */
    fun normalize(vehicleNo: String): String {
        return vehicleNo.uppercase()
            .replace("-", "")
            .replace(" ", "")
            .trim()
    }

    /**
     * Strips province prefix if present, returning the core plate number.
     * E.g. "WPABC1234" -> "ABC1234".
     */
    fun coreNumber(vehicleNo: String): String {
        val norm = normalize(vehicleNo)
        for (prov in PROVINCE_PREFIXES) {
            if (norm.startsWith(prov) && norm.length > prov.length + 3) {
                return norm.substring(prov.length)
            }
        }
        return norm
    }

    /**
     * Checks if a vehicle number is duplicate against existing vehicles.
     */
    fun isDuplicateVehicleNumber(newNumber: String, existingNumbers: List<String>): Boolean {
        val newNorm = normalize(newNumber)
        val newCore = coreNumber(newNumber)
        if (newNorm.isBlank()) return false

        return existingNumbers.any { existing ->
            val exNorm = normalize(existing)
            val exCore = coreNumber(existing)
            exNorm == newNorm || exCore == newCore
        }
    }

    /**
     * Regex matching valid Sri Lankan vehicle registration formats:
     * - Letter series: ABC-1234, WP-ABC-1234, WP-AB-9012, CAB-1234
     * - Vintage numeric series: 123-2222, 52-2236, 6-1234, WP 52-2236
     * - Alphanumeric series: AB2-8952, 2A-1234, A2-1234
     *
     * Disallows user names, brand names, and random text (e.g. "Kamal", "Toyota", "My Car").
     */
    private val VEHICLE_NUMBER_REGEX = Regex(
        """^(?:(?:WP|CP|SP|NP|EP|NW|NC|UP|SG)[ -]?)?(?:(?:[A-Z]{1,3}|[A-Z]+\d+|\d+[A-Z]+)[ -]?\d{3,4}|\d{1,3}[ -]\d{3,4})$""",
        RegexOption.IGNORE_CASE
    )

    fun isValidVehicleNumber(input: String): Boolean {
        val trimmed = input.trim()
        if (trimmed.length < 4 || trimmed.length > 15) return false

        if (!VEHICLE_NUMBER_REGEX.matches(trimmed)) return false

        val digitCount = trimmed.count { it.isDigit() }
        if (digitCount < 3) return false

        return true
    }
}
