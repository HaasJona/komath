/*
 * Copyright (c) 2016.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.komath

import java.math.BigInteger

public class Fraction private constructor(val numerator: BigInteger, val denominator: BigInteger) : Comparable<Fraction> {

    companion object {
        fun of(numerator: BigInteger, denominator: BigInteger): Fraction {
            var gcd = numerator.gcd(denominator)
            if (gcd == BigInteger.ZERO) {
                return Fraction(BigInteger.ZERO, BigInteger.ZERO)
            }
            if (denominator < BigInteger.ZERO) gcd = -gcd
            return Fraction(numerator / gcd, denominator / gcd)
        }

        fun of(numerator: Long, denominator: Long): Fraction = of(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator))
        fun of(numerator: Int, denominator: Int): Fraction = of(numerator.toLong(), denominator.toLong())
        fun of(value: BigInteger): Fraction = of(value, BigInteger.ONE)
        fun of(value: Long): Fraction = of(BigInteger.valueOf(value), BigInteger.ONE)
        fun of(value: Int): Fraction = of(value.toLong())

        private val DOUBLE_DENOMINATOR = BigInteger.ZERO.setBit(1075)

        fun of(value: Double): Fraction {
            val bits = java.lang.Double.doubleToLongBits(value)
            val sign = bits < 0
            val exp = (bits and 0x7ff0000000000000L ushr 52).toInt()
            var significand = (bits and 0x000fffffffffffffL) or 0x0010000000000000L
            if (sign) significand = -significand
            return of(BigInteger.valueOf(significand) * BigInteger.ZERO.setBit(exp), DOUBLE_DENOMINATOR)
        }

    }

    override fun toString(): String {
        return "$numerator/$denominator"
    }

    operator fun plus(other: Fraction) = of(numerator * other.denominator + other.numerator * denominator, denominator * other.denominator)

    operator fun minus(other: Fraction) = of(numerator * other.denominator - other.numerator * denominator, denominator * other.denominator)

    operator fun times(other: Fraction) = of(numerator * other.numerator, denominator * other.denominator)

    operator fun div(other: Fraction) = of(numerator * other.denominator, denominator * other.numerator)

    operator fun mod(other: Fraction): Fraction {
        return other * (this / other).frac();
    }
    //
    //    operator fun rangeTo(other: Fraction) : FractionRange {
    //
    //    }

    override fun compareTo(other: Fraction): Int {
        return 0;
    }

    fun compareTo(other: BigInteger): Int {
        return compareTo(of(other));
    }

    fun compareTo(other: Long): Int {
        return compareTo(of(other));
    }

    fun compareTo(other: Int): Int {
        return compareTo(of(other));
    }

    fun compareTo(other: Double): Int {
        return compareTo(of(other));
    }

    fun compareTo(other: Float): Int {
        return compareTo(of(other.toDouble()));
    }

    fun frac(): Fraction {
        if (denominator == BigInteger.ZERO) return this;
        return Fraction(numerator % denominator, denominator);
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Fraction

        if (numerator != other.numerator) return false
        if (denominator != other.denominator) return false

        return true
    }

    override fun hashCode(): Int {
        var result = numerator.hashCode()
        result += 31 * result + denominator.hashCode()
        return result
    }


}

class FractionRange {

}
