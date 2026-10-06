package com.akshar.dyslexiareader.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinSecurityUtilTest {

    @Test
    fun generateSalt_returnsNonEmptyUniqueString() {
        val salt1 = PinSecurityUtil.generateSalt()
        val salt2 = PinSecurityUtil.generateSalt()

        assertTrue(salt1.isNotEmpty())
        assertTrue(salt2.isNotEmpty())
        assertNotEquals(salt1, salt2) // Must be cryptographically random per profile
    }

    @Test
    fun hashPin_producesDeterministicHashForSameSalt() {
        val pin = "1234"
        val salt = "sampleRandomSalt123"

        val hash1 = PinSecurityUtil.hashPin(pin, salt)
        val hash2 = PinSecurityUtil.hashPin(pin, salt)

        assertEquals(hash1, hash2)
        assertNotEquals(pin, hash1) // Never store plain text
    }

    @Test
    fun verifyPin_validatesCorrectPinAndRejectsWrongPin() {
        val correctPin = "9876"
        val wrongPin = "1111"
        val salt = PinSecurityUtil.generateSalt()
        val storedHash = PinSecurityUtil.hashPin(correctPin, salt)

        assertTrue(PinSecurityUtil.verifyPin(correctPin, storedHash, salt))
        assertFalse(PinSecurityUtil.verifyPin(wrongPin, storedHash, salt))
        assertFalse(PinSecurityUtil.verifyPin("", storedHash, salt))
    }

    @Test
    fun picturePin_encodesAndVerifiesCorrectSequence() {
        val selectedIcons = listOf("dog", "apple", "star")
        val encodedPin = PinSecurityUtil.encodePicturePin(selectedIcons)
        assertEquals("dog_apple_star", encodedPin)

        val salt = PinSecurityUtil.generateSalt()
        val storedHash = PinSecurityUtil.hashPin(encodedPin, salt)

        // Same sequence verifies
        val enteredIcons = listOf("dog", "apple", "star")
        assertTrue(PinSecurityUtil.verifyPin(PinSecurityUtil.encodePicturePin(enteredIcons), storedHash, salt))

        // Different sequence fails (order matters for PIN unlock)
        val wrongSequence = listOf("star", "dog", "apple")
        assertFalse(PinSecurityUtil.verifyPin(PinSecurityUtil.encodePicturePin(wrongSequence), storedHash, salt))
    }
}
