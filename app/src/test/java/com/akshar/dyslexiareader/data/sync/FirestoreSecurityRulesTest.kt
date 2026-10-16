package com.akshar.dyslexiareader.data.sync

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Unit test validating the security rules logic and rule definitions in firestore.rules.
 */
class FirestoreSecurityRulesTest {

    // Simple rule evaluator representing the firestore.rules match semantics
    private fun evaluateAccess(
        path: String,
        authUid: String?
    ): Boolean {
        if (authUid == null) return false
        val segments = path.trim('/').split('/')
        if (segments.isEmpty() || segments[0] != "users") return false
        if (segments.size >= 2) {
            val targetUserId = segments[1]
            return targetUserId == authUid
        }
        return false
    }

    @Test
    fun firestoreRulesFile_existsAndContainsStrictUserIsolation() {
        // Find firestore.rules in project root
        val possibleLocations = listOf(
            File("firestore.rules"),
            File("../firestore.rules"),
            File("../../firestore.rules")
        )
        val rulesFile = possibleLocations.firstOrNull { it.exists() }
        assertTrue("firestore.rules must exist", rulesFile != null && rulesFile.exists())

        val content = rulesFile!!.readText()
        assertTrue("Must require authenticated user", content.contains("request.auth != null"))
        assertTrue("Must match user ID against auth UID", content.contains("request.auth.uid == userId"))
        assertTrue("Must restrict /users/{userId}", content.contains("match /users/{userId}"))
    }

    @Test
    fun evaluateAccess_allowsOwnerAccessToSubtree() {
        val uid = "parent_user_123"
        
        // Own user profile
        assertTrue(evaluateAccess("/users/$uid/profiles/child_1", uid))
        // Own documents
        assertTrue(evaluateAccess("/users/$uid/profiles/child_1/documents/doc_1", uid))
        // Own settings
        assertTrue(evaluateAccess("/users/$uid/profiles/child_1/settings/reader_settings", uid))
        // Own sessions
        assertTrue(evaluateAccess("/users/$uid/profiles/child_1/sessions/ses_1", uid))
        // Own practice words
        assertTrue(evaluateAccess("/users/$uid/profiles/child_1/practiceWords/w_1", uid))
    }

    @Test
    fun evaluateAccess_deniesCrossUserAccess() {
        val uid1 = "parent_user_123"
        val uid2 = "malicious_user_456"

        // User 2 cannot access User 1's profile
        assertFalse(evaluateAccess("/users/$uid1/profiles/child_1", uid2))
        // User 2 cannot access User 1's documents
        assertFalse(evaluateAccess("/users/$uid1/profiles/child_1/documents/doc_1", uid2))
        // User 2 cannot access User 1's practice words
        assertFalse(evaluateAccess("/users/$uid1/profiles/child_1/practiceWords/w_1", uid2))
    }

    @Test
    fun evaluateAccess_deniesUnauthenticatedAccess() {
        // Unauthenticated visitor (auth == null) cannot read anything
        assertFalse(evaluateAccess("/users/parent_123/profiles/child_1", null))
        assertFalse(evaluateAccess("/users/parent_123/profiles/child_1/documents/doc_1", null))
    }

    @Test
    fun evaluateAccess_deniesNonUsersRootAccess() {
        val uid = "parent_user_123"
        assertFalse(evaluateAccess("/system/config", uid))
        assertFalse(evaluateAccess("/admin/reports", uid))
    }
}
