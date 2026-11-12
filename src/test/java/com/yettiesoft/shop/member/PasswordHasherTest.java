package com.yettiesoft.shop.member;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    @Test
    void hashDoesNotContainPlainPassword() {
        String stored = PasswordHasher.hash("secret123");

        assertFalse(stored.contains("secret123"));
    }

    @Test
    void matchesOnlyTheOriginalPassword() {
        String stored = PasswordHasher.hash("secret123");

        assertTrue(PasswordHasher.matches("secret123", stored));
        assertFalse(PasswordHasher.matches("secret124", stored));
    }

    @Test
    void samePasswordHashesDifferentlyEachTime() {
        assertNotEquals(PasswordHasher.hash("secret123"), PasswordHasher.hash("secret123"));
    }
}
