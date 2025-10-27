package com.example.myapplication4

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class AuthFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testSignUpAndLoginFlow() {
        // Generate a unique email for each test run to ensure no conflicts
        val uniqueEmail = "testuser_${UUID.randomUUID()}@example.com"
        val password = "password123"

        // --- Test Sign Up Flow ---
        // 1. Start on the AuthScreen and switch to Sign Up
        composeTestRule.onNodeWithText("Need an account? Sign Up").performClick()

        // 2. Enter credentials for the new user
        composeTestRule.onNodeWithText("Email").performTextInput(uniqueEmail)
        composeTestRule.onNodeWithText("Password").performTextInput(password)

        // 3. Click the Sign Up button
        composeTestRule.onNodeWithText("Sign Up").performClick()

        // 4. Wait for the user to be logged in and the profile to load from Firestore.
        // We verify by checking for the role text, which appears after login and profile fetch.
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithText("Role: student", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Role: student").assertIsDisplayed()

        // 5. Sign the user out to prepare for the login test
        composeTestRule.onNodeWithText("Sign Out").performClick()

        // --- Test Login Flow ---
        // 6. Wait for the AuthScreen to reappear after logout
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Login").fetchSemanticsNodes().isNotEmpty()
        }

        // 7. Enter the credentials of the user we just created
        composeTestRule.onNodeWithText("Email").performTextInput(uniqueEmail)
        composeTestRule.onNodeWithText("Password").performTextInput(password)

        // 8. Click the Login button
        composeTestRule.onNodeWithText("Login").performClick()

        // 9. Verify the user is logged in again successfully
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithText("Role: student", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Role: student").assertIsDisplayed()
    }
}
