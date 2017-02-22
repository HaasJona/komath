package de.komath

import org.junit.Assert

/**
 * TBD Dokumentation
 */
class FractionTest {

    @org.junit.Test
    fun testOf() {
        Assert.assertEquals(Fraction.of(10, 1), Fraction.of(10))
    }
}