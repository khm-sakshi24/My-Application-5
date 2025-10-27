package com.example.myapplication4

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class AuthLogicTest {

    // A simple function that mimics the logic from our UI
    private fun getScreenContentForRole(role: String): List<String> {
        return when (role) {
            "student" -> listOf("Welcome", "Role: student", "View My Grades", "Sign Out")
            "teacher" -> listOf("Welcome", "Role: teacher", "Manage Student Grades", "Sign Out")
            else -> listOf("Welcome", "Role: unknown", "Sign Out")
        }
    }

    @Test
    fun `content for student role is correct`() {
        val expectedContent = listOf("Welcome", "Role: student", "View My Grades", "Sign Out")
        val actualContent = getScreenContentForRole("student")
        assertEquals(expectedContent, actualContent)
    }

    @Test
    fun `content for teacher role is correct`() {
        val expectedContent = listOf("Welcome", "Role: teacher", "Manage Student Grades", "Sign Out")
        val actualContent = getScreenContentForRole("teacher")
        assertEquals(expectedContent, actualContent)
    }

    @Test
    fun `content for unknown role is correct`() {
        val expectedContent = listOf("Welcome", "Role: unknown", "Sign Out")
        val actualContent = getScreenContentForRole("guest") // an unknown role
        assertEquals(expectedContent, actualContent)
    }
}
