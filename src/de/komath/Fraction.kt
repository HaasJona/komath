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

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

/**
 * Represents a mathematical fraction or ratio expressed by a numerator and a denominator with a value of
 * [numerator]/[denominator]. A fraction is always expressed in a normalized form with the denominator always
 * being positive (or zero) and reduced as much as possible. Examples:
 *
 * * of(2,4) = 1/2
 * * of(2,-4) = -1/2
 * * of(3,-8) = -3/8
 *
 * Similar to a double, a fraction can have special values representing infinity and NaN. This way, all the usual
 * operations can not result in arithmetic exceptions. The special values are represented as follows:
 *
 * * 0/1 = Zero
 * * 1/0 = Positive infinity
 * * -1/0 = Negative infinity
 * * 0/0 = NaN
 *
 * Again, all parameters are automatically reduced when constructing a fraction, and thus, these are the only possible
 * "special" values. For example constructing a fraction with the value of 1234/0 will again result in 1/0 (positive infinity)
 * because the gcd of 1234 and 0 is 1234 (according to [BigInteger.gcd]).
 *
 * Unlike doubles, there is no distinction between positive zero and negative zero, because the fraction class can
 * represent arbitrarily small numbers and thus zero always represents exactly zero.
 *
 * This is a data based, immutable and threadsafe class.
 */
class Fraction private constructor(val numerator: BigInteger, val denominator: BigInteger) : Comparable<Fraction>, Number() {

    companion object {

        /**
         * Represents the value 0/0 (NaN)
         */
        val NaN = Fraction(BigInteger.ZERO, BigInteger.ZERO)

        /**
         * Represents the value 0/1 (Zero)
         */
        val ZERO = Fraction(BigInteger.ZERO, BigInteger.ONE)

        /**
         * Represents the value 1/1 (One)
         */
        val ONE = Fraction(BigInteger.ONE, BigInteger.ONE)

        /**
         * Represents the value -1/1 (Negative One)
         */
        val NEGATIVE_ONE = -ONE

        /**
         * Represents the value 1/0 (Positive Infinity)
         */
        val POSITIVE_INFINITY = Fraction(BigInteger.ONE, BigInteger.ZERO)

        /**
         * Represents the value -1/0 (Negative Infinity)
         */
        val NEGATIVE_INFINITY = -POSITIVE_INFINITY

        /**
         * Returns a Fraction with the specified [numerator] and [denominator]. The given values may be normalized and reduced.
         */
        fun of(numerator: BigInteger = BigInteger.ONE, denominator: BigInteger = BigInteger.ONE): Fraction {
            var gcd = numerator.gcd(denominator)
            if (gcd == BigInteger.ZERO) {
                return NaN
            }
            if (denominator < BigInteger.ZERO) gcd = -gcd
            return Fraction(numerator / gcd, denominator / gcd)
        }

        /**
         * Returns a Fraction with the specified [numerator] and [denominator]. The given values may be normalized and reduced.
         */
        fun of(numerator: Long = 1L, denominator: Long = 1L): Fraction = of(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator))

        /**
         * Returns a Fraction with the specified [numerator] and [denominator]. The given values may be normalized and reduced.
         */
        fun of(numerator: Int = 1, denominator: Int = 1): Fraction = of(numerator.toLong(), denominator.toLong())

        /**
         * Returns a Fraction with the specified integer value. The [denominator] will always be 1.
         */
        fun of(value: BigInteger): Fraction = of(value, BigInteger.ONE)

        /**
         * Returns a Fraction with the specified integer value. The [denominator] will always be 1.
         */
        fun of(value: Long): Fraction = of(BigInteger.valueOf(value), BigInteger.ONE)

        /**
         * Returns a Fraction with the specified integer value. The [denominator] will always be 1.
         */
        fun of(value: Int): Fraction = of(value.toLong())

        /**
         * Returns a fraction that represents the exact value of the specified IEEE 754 double. Special values
         * like NaN and Infinity are translated to the special fractions appropriately.
         *
         * Because of the binary exponent representation of a double value, the [denominator] of the created fraction
         * will always be a power of 2 (or 0).
         */
        fun ofExact(value: Double): Fraction {
            if (value == 0.0) return ZERO
            if (value.isNaN()) return NaN
            if (value.isInfinite()) {
                return if (value > 0) POSITIVE_INFINITY else NEGATIVE_INFINITY
            }
            return ofExactNoSpecialValue(value)
        }

        /**
         * Returns the simplest fraction, whose [double value][toDouble] is equal to the specified double (within double accuracy).
         */
        fun of(value: Double): Fraction {
            if (value == 0.0) return ZERO
            if (value.isNaN()) return NaN
            if (value.isInfinite()) {
                return if (value > 0) POSITIVE_INFINITY else NEGATIVE_INFINITY
            }
            var tmp = ofExactNoSpecialValue(value).continuedFraction()
            return tmp.toFraction({ fraction -> value == fraction.toDouble() })
        }

        private val DOUBLE_DENOMINATOR = BigInteger.ZERO.setBit(1075)

        /**
         * Bit fiddling to extract exact double value, special doubles like 0.0 or NaN must be handled separately.
         */
        private fun ofExactNoSpecialValue(value: Double): Fraction {
            val bits = java.lang.Double.doubleToLongBits(value)
            val sign = bits < 0
            val exp = (bits and 0x7ff0000000000000L ushr 52).toInt()
            var significand = (bits and 0x000fffffffffffffL) or 0x0010000000000000L
            if (sign) significand = -significand
            return of(BigInteger.valueOf(significand) * BigInteger.ZERO.setBit(exp), DOUBLE_DENOMINATOR)
        }

        /**
         * Returns a fraction that represents the exact value of the specified IEEE 754 single (float). Special values
         * like NaN and Infinity are translated to the special fractions appropriately.
         *
         * Because of the binary exponent representation of a float value, the [denominator] of the created fraction
         * will always be a power of 2 (or 0).
         */
        fun ofExact(value: Float): Fraction {
            if (value == 0.0f) return ZERO
            if (value.isNaN()) return NaN
            if (value.isInfinite()) {
                return if (value > 0) POSITIVE_INFINITY else NEGATIVE_INFINITY
            }
            return ofExactNoSpecialValue(value)
        }

        /**
         * Returns the simplest fraction, whose [float value][toFloat] is equal to the specified float (within float accuracy).
         */
        fun of(value: Float): Fraction {
            if (value == 0.0f) return ZERO
            if (value.isNaN()) return NaN
            if (value.isInfinite()) {
                return if (value > 0) POSITIVE_INFINITY else NEGATIVE_INFINITY
            }
            var tmp = ofExactNoSpecialValue(value).continuedFraction()
            return tmp.toFraction({ fraction -> value == fraction.toFloat() })
        }

        private val FLOAT_DENOMINATOR = BigInteger.ZERO.setBit(150)

        /**
         * Bit fiddling to extract exact float value, special floats like 0.0 or NaN must be handled separately.
         */
        private fun ofExactNoSpecialValue(value: Float): Fraction {
            val bits = java.lang.Float.floatToIntBits(value)
            val sign = bits < 0
            val exp = (bits and 0x78000000 ushr 23).toInt()
            var significand = (bits and 0x007fffff) or 0x00800000
            if (sign) significand = -significand
            return of(BigInteger.valueOf(significand.toLong()) * BigInteger.ZERO.setBit(exp), FLOAT_DENOMINATOR)
        }

    }

    /**
     * Returns the string representation of this fraction, which is equal to [numerator] + `/` + [denominator], for example `-13/5`.
     */
    override fun toString(): String {
        return "$numerator/$denominator"
    }

    /**
     * Returns the mixed number representation of this fraction, which is formatted as `[-]a b/c`, for example
     *
     *   * `1 3/5` for the fraction `8/5`
     *   * `-2 3/5` for the fraction `-13/5`
     *
     * If the integer part (`a`) or the [denominator] would be zero, this returns the same String as [toString]
     */
    fun toMixedString(): String {
        if(denominator == BigInteger.ZERO) return toString()
        val divideAndRemainder = numerator.divideAndRemainder(denominator)
        if(divideAndRemainder[0] == BigInteger.ZERO) return toString()
        return divideAndRemainder[0].toString() + " " + divideAndRemainder[1].abs().toString() + "/" + denominator.toString()
    }

    /**
     * Converts this fraction to the nearest double value.
     */
    override fun toDouble(): Double {
        // Inefficient, but more accurate than numerator.toDouble() / denominator.toDouble()
        return toString(16).toDouble()
    }

    /**
     * Converts this fraction to the nearest float value.
     */
    override fun toFloat(): Float {
        // Inefficient, but more accurate than numerator.toFloat() / denominator.toFloat()
        return toString(9).toFloat()
    }

    /**
     * Returns a string representation of this fraction with the specified number of maximum decimal places and the specified radix (default: 10).
     *
     * Special values like Infinity or NaN are returned in the same way as [Double.toString]
     */
    fun toString(n: Int, radix: Int = 10): String {
        if(radix < 2) throw IllegalArgumentException("radix")
        if(denominator == BigInteger.ZERO){
            if(numerator > BigInteger.ZERO){
                return Double.POSITIVE_INFINITY.toString()
            } else if(numerator < BigInteger.ZERO){
                return Double.NEGATIVE_INFINITY.toString()
            } else {
                return Double.NaN.toString()
            }
        }
        val radixBigInt = BigInteger.valueOf(radix.toLong())
        val s = StringBuilder()
        var divideAndRemainder = numerator.divideAndRemainder(denominator)
        if(n > 0) s.append(divideAndRemainder[0].toString(radix)).append('.')
        var remainder = divideAndRemainder[1].abs()
        for(i in 1..n){
            remainder *= radixBigInt
            divideAndRemainder = remainder.divideAndRemainder(denominator)
            remainder = divideAndRemainder[1]
            s.append(divideAndRemainder[0].toString(radix))
            if(remainder == BigInteger.ZERO) break
        }
        return s.toString()
    }

    /**
     * Returns the BigInteger value (rounded towards zero) of this fraction. If the denominator is zero, returns zero.
     */
    fun toBigInteger() = if(denominator == BigInteger.ZERO) BigInteger.ZERO else numerator / denominator

    /**
     * Returns this as a BigDecimal
     * @param  scale scale of the {@code BigDecimal} quotient to be returned.
     * @param  roundingMode rounding mode to apply.
     * @throws ArithmeticException if denominator is zero
     */
    fun toBigDecimal(scale: Int, roundingMode: RoundingMode) = BigDecimal(numerator).divide(BigDecimal(denominator), scale, roundingMode)

    /**
     * Returns this as a BigDecimal
     * @param  mathContext the [MathContext] to use for rounding and accuracy
     * @throws ArithmeticException if denominator is zero
     */
    fun toBigDecimal(mathContext: MathContext) = BigDecimal(numerator).divide(BigDecimal(denominator), mathContext)

    /**
     * Returns `toBigInteger().toLong()`. If the denominator is zero, returns zero.
     */
    override fun toLong() = toBigInteger().toLong()
    /**
     * Returns `toBigInteger().toInt()`. If the denominator is zero, returns zero.
     */
    override fun toInt() = toBigInteger().toInt()
    /**
     * Returns `toBigInteger().toShort()`. If the denominator is zero, returns zero.
     */
    override fun toShort() = toBigInteger().toShort()
    /**
     * Returns `toBigInteger().toChar()`. If the denominator is zero, returns zero.
     */
    override fun toChar() = toBigInteger().toChar()
    /**
     * Returns `toBigInteger().toByte()`. If the denominator is zero, returns zero.
     */
    override fun toByte() = toBigInteger().toByte()

    operator fun plus(other: Fraction) = of(numerator * other.denominator + other.numerator * denominator, denominator * other.denominator)

    operator fun minus(other: Fraction) = of(numerator * other.denominator - other.numerator * denominator, denominator * other.denominator)

    operator fun times(other: Fraction) = of(numerator * other.numerator, denominator * other.denominator)

    operator fun div(other: Fraction) = of(numerator * other.denominator, denominator * other.numerator)

    operator fun mod(other: Fraction) = other * (this / other).frac()

    operator fun unaryMinus(): Fraction = Fraction(-numerator, denominator)

    operator fun unaryPlus(): Fraction = this

    override infix operator fun compareTo(other: Fraction): Int {
        return (numerator * other.denominator).compareTo(other.numerator * denominator)
    }

    infix operator fun compareTo(other: BigInteger): Int {
        return compareTo(of(other))
    }

    infix operator fun compareTo(other: Long): Int {
        return compareTo(of(other))
    }

    infix operator fun compareTo(other: Int): Int {
        return compareTo(of(other))
    }

    infix operator fun compareTo(other: Double): Int {
        return compareTo(ofExact(other))
    }

    infix operator fun compareTo(other: Float): Int {
        return compareTo(ofExact(other.toDouble()))
    }

    /**
     * Returns the fractional part of this fraction, which is this fraction with any integer part removed. For example
     *
     * `frac(13/10) = frac(43/10) = 3/10`
     *
     * negative fractions will return a negative fractional part, but otherwise work the same. For example
     *
     * `frac(-13/10) = frac(-43/10) = -3/10`
     *
     * The returned fraction is always between (inclusive) -1/1 and 1/1. If `this` is infinity or NaN, `this` is returned.
     */
    fun frac(): Fraction {
        if (denominator == BigInteger.ZERO) return this
        return Fraction(numerator % denominator, denominator)
    }

    /**
     * Converts this fraction into a continued fraction representation.
     *
     * @throws ArithmeticException if the denominator is zero (`this` is Infinity or NaN)
     */
    fun continuedFraction(): ContinuedFraction {
        if(denominator == BigInteger.ZERO) throw ArithmeticException(toString(0));
        return ContinuedFraction(
                Iterable {
                    ContinuedFractionIterator(this)
                }
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Fraction

        if (numerator != other.numerator) return false
        if (denominator != other.denominator) return false

        return true
    }

    override fun hashCode() = numerator.hashCode() + denominator.hashCode() * 2047

    operator fun component1() = numerator

    operator fun component2() = denominator
}

/**
 * A representation of a simple continued fraction. This class currently expects a finite continued fraction, feeding it
 * infinitely many BigIntegers may lead to some algorithms not terminating correctly for now.
 */
class ContinuedFraction(private val arg: Iterable<BigInteger>) : Comparable<ContinuedFraction>, Number(), Iterable<BigInteger> {

    companion object {
        /**
         * Creates a continued fraction from a collection of BigIntegers [b0; b1, b2, ...]
         */
        fun of(value: Collection<BigInteger>): ContinuedFraction {
            return when {
                value.isEmpty() -> throw IllegalArgumentException("Empty collection")
                else -> ContinuedFraction(value.toList())
            }
        }

        /**
         * Converts an existing fraction into a continued fraction representation
         */
        fun of(value: Fraction) : ContinuedFraction = value.continuedFraction()
    }

    override fun iterator(): Iterator<BigInteger> {
        return arg.iterator()
    }

    override fun toString(): String {
        val builder = StringBuilder("[")
        var n = 0;
        for (bigInteger in this) {
            builder.append(bigInteger)
            builder.append(if (n == 0) "; " else ", ")
            n++;
            if (n > 10) {
                builder.append("...  ")
                break
            }
        }
        builder.setLength(builder.length - 2)
        builder.append("]")
        return builder.toString();
    }

    /**
     * Converts this continued fraction back into a normal fraction representation.
     *
     * @param n How many convergents to calculate. This can be used to simplify a fraction. Defaults to
     * Int.MAX_VALUE, which should return the original value (creating a continued fraction with more integers will likely lead to other problems)
     */
    fun toFraction(n: Int = Int.MAX_VALUE): Fraction {
        var olda = BigInteger.ZERO
        var oldb = BigInteger.ONE
        var cura = BigInteger.ONE
        var curb = BigInteger.ZERO
        var i = 0
        for (bigInteger in this) {
            val newa = cura * bigInteger + olda
            val newb = curb * bigInteger + oldb
            olda = cura
            cura = newa
            oldb = curb
            curb = newb
            if (++i == n) break
        }
        return Fraction.of(cura, curb);
    }

    /**
     * Converts this continued fraction to a regular fraction and simplifies it as much as possible as long as the
     * supplied condition is true. This method first generates more and more exact first order convergents until the
     * condition returns true and then it tries to find the best second order convergent by simplifying the fraction
     * until the condition returns false again. The last fraction for which the convergent returns true is returned.
     *
     * The condition is supposed to return true for `toFraction()` as parameter, however this is not enforced.
     * If the condition never returns true for any approximation, this method returns the exact fractional value.
     */
    fun toFraction(condition: (Fraction) -> Boolean): Fraction {
        var olda = BigInteger.ZERO
        var oldb = BigInteger.ONE
        var cura = BigInteger.ONE
        var curb = BigInteger.ZERO
        for (bigInteger in this) {
            val newa = cura * bigInteger + olda
            val newb = curb * bigInteger + oldb

            if (condition(Fraction.of(newa, newb))) {
                var bTmp = bigInteger;
                while (true) {
                    bTmp -= BigInteger.ONE;
                    val newaX = cura * bTmp + olda
                    val newbX = curb * bTmp + oldb
                    if (!condition(Fraction.of(newaX, newbX))) {
                        return Fraction.of(cura, curb);
                    }
                }
            }
            olda = cura
            cura = newa
            oldb = curb
            curb = newb
        }
        return Fraction.of(cura, curb);
    }

    override fun compareTo(other: ContinuedFraction): Int {
        return toFraction().compareTo(other.toFraction());
    }

    fun toBigInteger() = toFraction().toBigInteger()

    fun toBigDecimal(scale: Int, roundingMode: RoundingMode) = toFraction().toBigDecimal(scale, roundingMode)

    override fun toByte(): Byte {
        return toFraction().toByte()
    }

    override fun toChar(): Char {
        return toFraction().toChar()
    }

    override fun toDouble(): Double {
        return toFraction().toDouble()
    }

    override fun toFloat(): Float {
        return toFraction().toFloat()
    }

    override fun toInt(): Int {
        return toFraction().toInt()
    }

    override fun toLong(): Long {
        return toFraction().toLong()
    }

    override fun toShort(): Short {
        return toFraction().toShort()
    }
}

private class ContinuedFractionIterator(fraction: Fraction) : Iterator<BigInteger> {

    var a: BigInteger
    var b: BigInteger

    init {
        a = fraction.numerator
        b = fraction.denominator
    }

    override fun hasNext(): Boolean {
        return b != BigInteger.ZERO
    }

    override fun next(): BigInteger {
        val h = a.divideAndRemainder(b)
        a = b
        b = h[1]
        return h[0]
    }
}


// Extension functions for reverse operators
infix operator fun BigInteger.compareTo(fraction: Fraction): Int {
    return Fraction.of(this).compareTo(fraction);
}

infix operator fun Long.compareTo(fraction: Fraction): Int {
    return Fraction.of(this).compareTo(fraction);
}

infix operator fun Int.compareTo(fraction: Fraction): Int {
    return Fraction.of(this).compareTo(fraction);
}

infix operator fun Double.compareTo(fraction: Fraction): Int {
    return Fraction.ofExact(this).compareTo(fraction);
}

infix operator fun Float.compareTo(fraction: Fraction): Int {
    return Fraction.ofExact(this).compareTo(fraction);
}