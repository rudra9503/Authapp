package com.neuroverse.authapp.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    // ---- validateName ----

    @Test
    fun `name blank returns BLANK`() {
        assertEquals(FieldError.BLANK, Validators.validateName(""))
        assertEquals(FieldError.BLANK, Validators.validateName("   "))
    }

    @Test
    fun `name single character returns NAME_TOO_SHORT`() {
        assertEquals(FieldError.NAME_TOO_SHORT, Validators.validateName("A"))
    }

    @Test
    fun `name single character with padding is trimmed and too short`() {
        assertEquals(FieldError.NAME_TOO_SHORT, Validators.validateName(" A "))
    }

    @Test
    fun `name with exactly two characters is valid`() {
        assertNull(Validators.validateName("Jo"))
    }

    @Test
    fun `normal full name is valid`() {
        assertNull(Validators.validateName("Rudraksh Abhange"))
    }

    // ---- validateEmail ----

    @Test
    fun `email blank returns BLANK`() {
        assertEquals(FieldError.BLANK, Validators.validateEmail(""))
        assertEquals(FieldError.BLANK, Validators.validateEmail("   "))
    }

    @Test
    fun `standard email is valid`() {
        assertNull(Validators.validateEmail("user@example.com"))
    }

    @Test
    fun `email with plus tag and subdomains is valid`() {
        assertNull(Validators.validateEmail("first.last+tag@mail.example.co.in"))
    }

    @Test
    fun `email with surrounding spaces is trimmed and valid`() {
        assertNull(Validators.validateEmail("  user@example.com  "))
    }

    @Test
    fun `email without at sign is invalid`() {
        assertEquals(FieldError.INVALID_EMAIL, Validators.validateEmail("userexample.com"))
    }

    @Test
    fun `email without top-level domain is invalid`() {
        assertEquals(FieldError.INVALID_EMAIL, Validators.validateEmail("user@example"))
    }

    @Test
    fun `email without domain is invalid`() {
        assertEquals(FieldError.INVALID_EMAIL, Validators.validateEmail("user@"))
    }

    @Test
    fun `email with space inside is invalid`() {
        assertEquals(FieldError.INVALID_EMAIL, Validators.validateEmail("us er@example.com"))
    }

    // ---- validateNewPassword ----

    @Test
    fun `new password empty returns BLANK`() {
        assertEquals(FieldError.BLANK, Validators.validateNewPassword(""))
    }

    @Test
    fun `new password of seven characters is too short`() {
        assertEquals(FieldError.PASSWORD_TOO_SHORT, Validators.validateNewPassword("abcdefg"))
    }

    @Test
    fun `new password of exactly eight characters is valid`() {
        assertNull(Validators.validateNewPassword("abcdefgh"))
    }

    @Test
    fun `new password longer than minimum is valid`() {
        assertNull(Validators.validateNewPassword("a-much-longer-passphrase-1"))
    }

    // ---- validateRequired (login) ----

    @Test
    fun `required empty returns BLANK`() {
        assertEquals(FieldError.BLANK, Validators.validateRequired(""))
    }

    @Test
    fun `required non-empty is valid and does not enforce password policy`() {
        assertNull(Validators.validateRequired("x"))
    }
}
