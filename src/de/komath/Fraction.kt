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
import java.math.RoundingMode
import java.util.*
import kotlin.collections.MutableList

class Fraction private constructor(val numerator: BigInteger, val denominator: BigInteger) : Comparable<Fraction>, Number() {

    companion object {

        val NaN = Fraction(BigInteger.ZERO, BigInteger.ZERO)
        val ZERO = Fraction(BigInteger.ZERO, BigInteger.ONE)
        val ONE = Fraction(BigInteger.ONE, BigInteger.ONE)
        val NEGATIVE_ONE = -ONE
        val POSITIVE_INFINITY = Fraction(BigInteger.ONE, BigInteger.ZERO)
        val NEGATIVE_INFINITY = -POSITIVE_INFINITY

        fun of(numerator: BigInteger = BigInteger.ONE, denominator: BigInteger = BigInteger.ONE): Fraction {
            var gcd = numerator.gcd(denominator)
            if (gcd == BigInteger.ZERO) {
                return NaN
            }
            if (denominator < BigInteger.ZERO) gcd = -gcd
            return Fraction(numerator / gcd, denominator / gcd)
        }

        fun of(numerator: Long = 1L, denominator: Long = 1L): Fraction = of(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator))
        fun of(numerator: Int = 1, denominator: Int = 1): Fraction = of(numerator.toLong(), denominator.toLong())
        fun of(value: BigInteger): Fraction = of(value, BigInteger.ONE)
        fun of(value: Long): Fraction = of(BigInteger.valueOf(value), BigInteger.ONE)
        fun of(value: Int): Fraction = of(value.toLong())

        private val DOUBLE_DENOMINATOR = BigInteger.ZERO.setBit(1075)

        fun of(value: Double): Fraction {
            if(value.isNaN()) return NaN
            if(value.isInfinite()){
                return if (value > 0) POSITIVE_INFINITY else NEGATIVE_INFINITY
            }
            val bits = java.lang.Double.doubleToLongBits(value)
            val sign = bits < 0
            val exp = (bits and 0x7ff0000000000000L ushr 52).toInt()
            var significand = (bits and 0x000fffffffffffffL) or 0x0010000000000000L
            if (sign) significand = -significand
            return of(BigInteger.valueOf(significand) * BigInteger.ZERO.setBit(exp), DOUBLE_DENOMINATOR)
        }

        fun of(value: ClosedRange<Double>): Fraction{
            var tmp = of((value.start + value.endInclusive) / 2.0).continuedFraction()
            return tmp.toFractionInRange(value);
        }

    }

    override fun toString(): String {
        return "$numerator/$denominator"
    }

    fun toMixedString(): String {
        val divideAndRemainder = numerator.divideAndRemainder(denominator)
        return divideAndRemainder[0].toString() + " " + divideAndRemainder[1].toString() + "/" + denominator.toString();
    }

    override fun toDouble() = numerator.toDouble() / denominator.toDouble()

    override fun toFloat() = toDouble().toFloat()

    fun toBigInteger() = numerator / denominator

    fun toBigDecimal(scale: Int, roundingMode: RoundingMode) = BigDecimal(numerator).divide(BigDecimal(denominator), scale, roundingMode)

    override fun toLong() = (numerator / denominator).toLong()

    override fun toInt() = (numerator / denominator).toInt()

    override fun toShort() = (numerator / denominator).toShort()

    override fun toChar() = (numerator / denominator).toChar()

    override fun toByte() = (numerator / denominator).toByte()

    operator fun plus(other: Fraction) = of(numerator * other.denominator + other.numerator * denominator, denominator * other.denominator)

    operator fun minus(other: Fraction) = of(numerator * other.denominator - other.numerator * denominator, denominator * other.denominator)

    operator fun times(other: Fraction) = of(numerator * other.numerator, denominator * other.denominator)

    operator fun div(other: Fraction) = of(numerator * other.denominator, denominator * other.numerator)

    operator fun mod(other: Fraction) = other * (this / other).frac()

    operator fun unaryMinus(): Fraction = Fraction(-numerator, denominator)

    operator fun unaryPlus(): Fraction = this

    override fun compareTo(other: Fraction): Int {
        return (numerator * other.denominator).compareTo(other.numerator * denominator)
    }

    fun compareTo(other: BigInteger): Int {
        return compareTo(of(other))
    }

    fun compareTo(other: Long): Int {
        return compareTo(of(other))
    }

    fun compareTo(other: Int): Int {
        return compareTo(of(other))
    }

    fun compareTo(other: Double): Int {
        return compareTo(of(other))
    }

    fun compareTo(other: Float): Int {
        return compareTo(of(other.toDouble()))
    }

    fun frac(): Fraction {
        if (denominator == BigInteger.ZERO) return this
        return Fraction(numerator % denominator, denominator)
    }

    fun continuedFraction() : ContinuedFraction {
        return ContinuedFraction(
                Iterable {
                    ContinuedFractionIterator(this)
                }
        )
    }

    override fun equals(other: Any?): Boolean{
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Fraction

        if (numerator != other.numerator) return false
        if (denominator != other.denominator) return false

        return true
    }

    override fun hashCode(): Int{
        var result = numerator.hashCode()
        result += 31 * result + denominator.hashCode()
        return result
    }

    operator fun component1() = numerator

    operator fun component2() = denominator
}

class ContinuedFraction(private val arg: Iterable<BigInteger>) : Iterable<BigInteger> {
    override fun iterator(): Iterator<BigInteger> {
        return arg.iterator()
    }

    override fun toString(): String {
        val builder = StringBuilder("[")
        var n = 0;
        for (bigInteger in this) {
            builder.append(bigInteger)
            builder.append(", ")
            n++;
            if(n > 10){
                builder.append("...  ")
                break
            }
        }
        builder.setLength(builder.length-2)
        builder.append("]")
        return builder.toString();
    }

    fun toFraction(n: Int) : Fraction {
        var a = Fraction.ZERO
        var b = Fraction.POSITIVE_INFINITY
        var i = 0
        for (bigInteger in this) {
            val c = Fraction.of(
                    bigInteger * b.numerator + a.numerator,
                    bigInteger * b.denominator + a.denominator
            )
            a = b
            b = c
            if(++i == n) break;
        }
        return b
    }

    fun toFractionInRange(value: ClosedRange<Double>): Fraction {
        var a = Fraction.ZERO
        var b = Fraction.POSITIVE_INFINITY
        var i = 0
        for (bigInteger in this) {
            var c = Fraction.of(
                    bigInteger * b.numerator + a.numerator,
                    bigInteger * b.denominator + a.denominator
            )

            if(value.contains(c.toDouble())) {
                var bTmp = bigInteger;
                while (true){
                    bTmp -= BigInteger.ONE;
                    val c2 = Fraction.of(
                            bTmp * b.numerator + a.numerator,
                            bTmp * b.denominator + a.denominator
                    )
                    if(value.contains(c2.toDouble())){
                        c = c2
                    }
                    else {
                        return c
                    }
                }
            }

            a = b
            b = c
        }
        return b
    }
}

class ContinuedFractionIterator(fraction: Fraction) : Iterator<BigInteger> {

    var a :BigInteger
    var b :BigInteger

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
