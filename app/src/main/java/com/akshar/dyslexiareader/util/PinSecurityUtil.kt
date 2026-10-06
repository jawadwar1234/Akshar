package com.akshar.dyslexiareader.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object PinSecurityUtil {

    // 9 friendly icons for Child Picture PIN:
    // "dog", "cat", "elephant", "flower", "car", "apple", "sun", "star", "balloon"
    val PICTURE_PIN_ICONS = listOf(
        PicturePinIcon("dog", "Dog", "🐶"),
        PicturePinIcon("cat", "Cat", "🐱"),
        PicturePinIcon("elephant", "Elephant", "🐘"),
        PicturePinIcon("flower", "Flower", "🌺"),
        PicturePinIcon("car", "Car", "🚗"),
        PicturePinIcon("apple", "Apple", "🍎"),
        PicturePinIcon("sun", "Sun", "☀️"),
        PicturePinIcon("star", "Star", "⭐"),
        PicturePinIcon("balloon", "Balloon", "🎈")
    )

    data class PicturePinIcon(
        val id: String,
        val name: String,
        val symbol: String
    )

    /**
     * Generates a 16-byte cryptographically secure random salt encoded in Base64.
     */
    fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return try {
            Base64.encodeToString(saltBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            // Fallback for JVM unit tests without android.util.Base64 mock
            java.util.Base64.getEncoder().encodeToString(saltBytes)
        }
    }

    /**
     * Hashes a PIN (numeric string or joined picture tokens) using SHA-256 + Salt.
     * Never stores plain text PINs.
     */
    fun hashPin(pin: String, salt: String): String {
        val saltedInput = "$pin:$salt"
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(saltedInput.toByteArray(Charsets.UTF_8))
        return try {
            Base64.encodeToString(hashBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            java.util.Base64.getEncoder().encodeToString(hashBytes)
        }
    }

    /**
     * Verifies an entered PIN against the stored hash and profile salt.
     */
    fun verifyPin(enteredPin: String, storedHash: String, salt: String): Boolean {
        if (enteredPin.isBlank() || storedHash.isBlank() || salt.isBlank()) return false
        val computedHash = hashPin(enteredPin, salt)
        return computedHash == storedHash
    }

    /**
     * Serializes 3 selected picture icon IDs into a canonical sequence token.
     * Example: ["dog", "star", "car"] -> "dog_star_car"
     */
    fun encodePicturePin(selectedIconIds: List<String>): String {
        return selectedIconIds.joinToString("_")
    }
}
