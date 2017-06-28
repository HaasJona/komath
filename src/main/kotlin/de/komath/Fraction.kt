/*
 * Copyright (c) 2016 Jonathan Haas.
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
import java.util.regex.Pattern

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
 * operations do not result in arithmetic exceptions. The special values are represented as follows:
 *
 * * 0/1 = Zero
 * * 1/0 = Positive infinity
 * * -1/0 = Negative infinity
 * * 0/0 = NaN
 *
 * Again, all parameters are automatically reduced when constructing a fraction, and thus, these are the only possible
 * "special" values. For example constructing a fraction with the value of 1234/0 will again result in 1/0 (positive infinity).
 *
 * Unlike doubles, there is no distinction between positive zero and negative zero, because the fraction class can
 * represent arbitrarily small numbers and thus zero always represents exactly zero.
 *
 * [NaN] represents an invalid value and is for example returned when dividing zero by zero or multiplying infinity with zero.
 * Unless otherwise noted, all operations of this class with a NaN value as argument also return NaN.
 *
 * This is a data based, immutable and threadsafe class.
 */
class Fraction internal constructor(val numerator: BigInteger, val denominator: BigInteger) : Number(), Comparable<Fraction> {

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
        fun of(numerator: Long, denominator: Long): Fraction = of(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator))

        /**
         * Returns a Fraction with the specified [numerator] and [denominator]. The given values may be normalized and reduced.
         */
        fun of(numerator: Int, denominator: Int): Fraction = of(numerator.toLong(), denominator.toLong())

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
         *
         * For example `Fraction.ofExact(0.3) == 5404319552844595/18014398509481984`. If you need 3/10 instead, try [Fraction.of].
         */
        fun ofExact(value: Double): Fraction {
            if (value.isNaN()) return NaN
            if (value.isInfinite()) {
                return if (value > 0) POSITIVE_INFINITY else NEGATIVE_INFINITY
            }
            return ofExactNoSpecialValue(value)
        }

        /**
         * Returns the simplest fraction, whose [double value][toDouble] is equal to the specified double (within double accuracy).
         *
         * Note that "the simplest" is computed on a best effort basis without requiring excessive computation. The exact algorithm may change in the future. 
         * The only guarantee is, that the Fraction converted back into a double will be equal to the specified double.
         * 
         * For example `Fraction.of(0.3) == 3/10`
         */
        fun of(value: Double): Fraction {
            if (value.isNaN()) return NaN
            if (value.isInfinite()) {
                return if (value > 0) POSITIVE_INFINITY else NEGATIVE_INFINITY
            }
            val tmp = ofExactNoSpecialValue(value).continuedFraction()
            return tmp.toFraction({ fraction -> value == fraction.toDouble() })
        }

        /**
         * Bit fiddling to extract exact double value, special doubles like 0.0 or NaN must be handled separately.
         */
        private fun ofExactNoSpecialValue(value: Double): Fraction {
            val helper = FpHelper.ofDouble(value)
            val exponent = helper.exp.intValueExact()
            if(exponent >= 0)
                return of(helper.significand.shiftLeft(exponent), BigInteger.ONE)
            else
                return of(helper.significand, BigInteger.ZERO.setBit(-exponent))
        }

        /**
         * Returns a fraction that represents the exact value of the specified IEEE 754 single (float). Special values
         * like NaN and Infinity are translated to the special fractions appropriately.
         *
         * Because of the binary exponent representation of a float value, the [denominator] of the created fraction
         * will always be a power of 2 (or 0).
         *
         * For example `Fraction.ofExact(0.3f) == 5033165/16777216`. If you need 3/10 instead, try [Fraction.of].
         */
        fun ofExact(value: Float): Fraction {
            if (value.isNaN()) return NaN
            if (value.isInfinite()) {
                return if (value > 0) POSITIVE_INFINITY else NEGATIVE_INFINITY
            }
            return ofExactNoSpecialValue(value)
        }

        /**
         * Returns the simplest fraction, whose [float value][toFloat] is equal to the specified float (within float accuracy).
         *
         * Note that "the simplest" is computed on a best effort basis without requiring excessive computation. The exact algorithm may change in the future.
         * The only guarantee is, that the Fraction converted back into a float will be equal to the specified float.
         * 
         * For example `Fraction.of(0.3f) == 3/10`
         */
        fun of(value: Float): Fraction {
            if (value.isNaN()) return NaN
            if (value.isInfinite()) {
                return if (value > 0) POSITIVE_INFINITY else NEGATIVE_INFINITY
            }
            val tmp = ofExactNoSpecialValue(value).continuedFraction()
            return tmp.toFraction({ fraction -> value == fraction.toFloat() })
        }

        /**
         * Bit fiddling to extract exact float value, special floats like 0.0 or NaN must be handled separately.
         */
        private fun ofExactNoSpecialValue(value: Float): Fraction {
            val helper = FpHelper.ofFloat(value)
            val exponent = helper.exp.intValueExact()
            if(exponent >= 0)
                return of(helper.significand.shiftLeft(exponent), BigInteger.ONE)
            else
                return of(helper.significand, BigInteger.ZERO.setBit(-exponent))
        }

        /**
         * Returns a fraction that represents the exact value of the specified BigDecimal.
         *
         * For example `Fraction.of(BigDecimal.valueOf(0.3)) == 3/10`.
         */
        fun of(value: BigDecimal) : Fraction {
            if(value.scale() > 0)
                return of(value.unscaledValue(), BigInteger.TEN.pow(value.scale()))
            else
                return of(value.toBigIntegerExact())
        }

        /**
         * Returns a fraction that represents the exact value of `numerator/denominator`.
         */
        fun of(numerator: BigDecimal, denominator: BigDecimal) : Fraction = of(numerator) / of(denominator)

        private val HEX_PATTERN_INLINE: Pattern = Pattern.compile("^\\s*(?:([+-]?\\p{Digit}+) +)?([+-]?(?:\\p{Digit}+(?:\\.\\p{Digit}*)?|\\.\\p{Digit})(?:[eE][+-]?\\p{Digit}+)?)(?:\\s*/\\s*([+-]?(?:\\p{Digit}+(?:\\.\\p{Digit}*)?|\\.\\p{Digit})(?:[eE][+-]?\\p{Digit}+)?))?\\s*$")

        /**
         * Parses a String and returns the fractional value of this String. The string can
         * in particular have one of the following forms:
         *
         * -    Decimal (example 3.65, anything that can be parsed by [BigDecimal])
         * -    Mixed String (example 5 3/4, as returned by [toMixedString])
         * -    Normal fraction (example 13/5, as returned by [toString])
         * -    Anything than can be parsed as a double (especially Infinity or NaN).
         *
         * Generally for any regular value, the String is expected to be
         *
         * -    A integer value followed by whitespace (optional, default = 0) followed by
         * -    A decimal value representing the numerator followed by
         * -    A slash (optionally surrounded by whitespace) and a decimal value representing the denominator (optional, default = 1)
         *
         * The resulting fraction then has a value of
         *
         *     integer + sign(integer) * numerator / denominator
         *
         * with sign(n) = -1 for n < 0, else 1
         *
         * Note that this means that "-4 3/5" will result in a fraction with the value of `-4 - 3/5` (-23/5)
         * (as expected) and that it is usually not useful to repeat the sign at the numerator or denominator when
         * creating a fraction using the mixed string representation. A [value] argument of "-4 -3/5" would actually
         * result in a [Fraction] with a value of `-4 + 3/5` (-17/5).
         *
         * Note that this also means, that you can use decimals as [numerator] and [denominator].
         * For example `Fraction.of("12.5/0.1")` would result in a [Fraction] with the value of 125/1.
         *
         */
        fun of(value: String) : Fraction {
            val matcher = HEX_PATTERN_INLINE.matcher(value)
            if(matcher.matches()){
                val int = matcher.group(1)
                val numerator = matcher.group(2)
                val denominator = matcher.group(3)
                var result = of(BigDecimal(numerator!!))
                if(denominator != null){
                    result /= of(BigDecimal(denominator))
                }
                if(int != null) {

                    if(int.startsWith('-')){
                        result = -result
                    }

                    result += of(BigInteger(int))
                }
                return result
            }
            return of(value.toDouble())
        }
        
        const val PATTERN = "([+-]?\\p{Digit}+(?:\\.\\p{Digit}*)?|\\.\\p{Digit})(?:\\((\\p{Digit}+)\\))?(?:[eE]([+-]?\\p{Digit}+))?"
        
        fun ofRepeatingString(value: String): Fraction {
            val matcher = Pattern.compile(PATTERN).matcher(value)
            if(matcher.matches()){
                val decimal : String = matcher.group(1)
                val repeating : String? = matcher.group(2)
                val exponent : String? = matcher.group(3)
                val bigDecimal = BigDecimal(decimal)
                val tmp : Fraction
                if(repeating != null) {
                    val bigDecimalRepeating = BigDecimal(decimal + repeating).movePointRight(repeating.length)
                    val foo = bigDecimalRepeating.minus(bigDecimal)
                    tmp = of(foo) / of(BigDecimal.ONE.movePointRight(repeating.length) - BigDecimal.ONE)
                }
                else {
                    tmp = of(bigDecimal)
                }
                
                if(exponent != null){
                    return tmp * of(BigDecimal.ONE.movePointRight(Integer.parseInt(exponent)))
                }
                
                return tmp
            }
            throw IllegalArgumentException()
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
    fun toMixedString(radix: Int = 10): String {
        if(denominator == BigInteger.ZERO) return toString()
        val divideAndRemainder = numerator.divideAndRemainder(denominator)
        if(divideAndRemainder[0] == BigInteger.ZERO) return toString()
        return "${divideAndRemainder[0].toString(radix)} ${divideAndRemainder[1].abs().toString(radix)}/${denominator.toString(radix)}"
    }

    /**
     * Converts this fraction to the nearest double value.
     */
    override fun toDouble(): Double {
        if(this == NaN) return Double.NaN
        if(this == POSITIVE_INFINITY) return Double.POSITIVE_INFINITY
        if(this == NEGATIVE_INFINITY) return Double.NEGATIVE_INFINITY
        val negExp = BigInteger.valueOf(denominator.bitLength().toLong()).shiftLeft(1) + BigInteger.valueOf(52)
        val significand = numerator.shiftLeft(negExp.intValueExact()) / denominator
        return FpHelper(significand, -negExp).toDouble()
    }

    /**
     * Converts this fraction to the nearest float value.
     */
    override fun toFloat(): Float {
        if(this == NaN) return Float.NaN
        if(this == POSITIVE_INFINITY) return Float.POSITIVE_INFINITY
        if(this == NEGATIVE_INFINITY) return Float.NEGATIVE_INFINITY
        val negExp = BigInteger.valueOf(denominator.bitLength().toLong()).shiftLeft(1) + BigInteger.valueOf(23)
        val significand = numerator.shiftLeft(negExp.intValueExact()) / denominator
        return FpHelper(significand, -negExp).toFloat()
    }

    /**
     * Returns a (decimal) string representation of this fraction with the specified number of maximum decimal places and the specified radix (default: 10).
     *
     * Special values like Infinity or NaN are returned in the same way as [Double].toString()
     */
    fun toString(n: Int, radix: Int = 10, roundingMode: RoundingMode = RoundingMode.DOWN): String {
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
        var divideAndRemainder = numerator.divideAndRemainder(denominator)
        if(n == 0) {
            return round(divideAndRemainder[0], divideAndRemainder[1], roundingMode).toString(radix)
        }
        val s = StringBuilder()
        s.append(divideAndRemainder[0].toString(radix))
        var remainder = divideAndRemainder[1].abs()
        if(remainder == BigInteger.ZERO) return s.toString()
        s.append('.')
        for(i in 1..n){
            remainder *= radixBigInt
            divideAndRemainder = remainder.divideAndRemainder(denominator)
            remainder = divideAndRemainder[1]
            if(i != n) {
                s.append(divideAndRemainder[0].toString(radix))
            }
            else {
                s.append(round(divideAndRemainder[0], divideAndRemainder[1], roundingMode).toString(radix))
            }
            if(remainder == BigInteger.ZERO) break
        }
        return s.toString()
    }

    private fun round(divide: BigInteger, remainder: BigInteger, roundingMode: RoundingMode): BigInteger {

        fun roundUp(divide: BigInteger) = if (divide.signum() >= 0) divide + BigInteger.ONE else divide - BigInteger.ONE

        val roundingModeInt = when (roundingMode) {
            RoundingMode.CEILING -> if(numerator.signum() < 0) RoundingMode.DOWN else RoundingMode.UP
            RoundingMode.FLOOR -> if(numerator.signum() < 0) RoundingMode.UP else RoundingMode.DOWN
            RoundingMode.HALF_EVEN -> if(divide.toInt() % 2 == 0) RoundingMode.HALF_DOWN else RoundingMode.HALF_UP
            else -> roundingMode
        }
        return when(roundingModeInt){
            RoundingMode.UP -> if(remainder != BigInteger.ZERO) roundUp(divide) else divide
            RoundingMode.DOWN -> divide
            RoundingMode.HALF_UP -> if(remainder.abs().shiftLeft(1) >= denominator) roundUp(divide) else divide
            RoundingMode.HALF_DOWN -> if(remainder.abs().shiftLeft(1) > denominator) roundUp(divide) else divide
            else -> if(remainder == BigInteger.ZERO) divide else throw ArithmeticException("rounding necessary")
        }
    }


    /**
     * Returns a repeating String representation of this fraction in the specified radix (default: 10).
     * The repeating part (if any) will be enclosed in parens, for example `of("7/12").toRepeatingString() == "0.58(3)"` or `of("22/7").toRepeatingString() == "3.(142857)"`.
     * 
     * The integer part before the dot will never be included in the repeating portion of the String, for example `of(100,3).toRepeatingString(radix) == 33.(3)`.
     *
     * For large denominators, the resulting String can become very long and the computation can become very expensive. Because of that, a limit can be specified that limits the length
     * of the fractional part to a specific count of digits. If this happens, there will be no parens in the resulting String, and it will instead end with "...". This limit can be disabled by
     * using zero or a negative number for the limit.
     * 
     * Special values like Infinity or NaN are returned in the same way as [Double].toString()
     */
    fun toRepeatingString(radix: Int = 10, limit : Int = 255): String {
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
        s.append(divideAndRemainder[0].toString(radix))
        var remainder = divideAndRemainder[1].abs()
        if(remainder == BigInteger.ZERO) return s.toString()
        s.append('.')
        val remainders = mutableMapOf<BigInteger, Int>()
        while (true){
            val old = remainders.put(remainder, s.length)
            if(old != null){
                s.insert(old, "(")
                s.append(")")
                return s.toString()
            }
            if(limit > 0 && remainders.size > limit) {
                s.append("...")
                return s.toString()
            }
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
    fun toBigInteger(): BigInteger = if(denominator == BigInteger.ZERO) BigInteger.ZERO else numerator / denominator

    /**
     * Returns the BigInteger value (rounded with the specified [roundingmode]) of this fraction. If the denominator is zero, returns zero.
     */
    fun toBigInteger(roundingMode: RoundingMode): BigInteger {
        val divideAndRemainder = numerator.divideAndRemainder(denominator)
        return round(divideAndRemainder[0], divideAndRemainder[1], roundingMode)
    }

    /**
     * Returns this as a BigDecimal
     * @param  scale scale of the {@code BigDecimal} quotient to be returned.
     * @param  roundingMode rounding mode to apply.
     * @throws ArithmeticException if denominator is zero
     */
    fun toBigDecimal(scale: Int, roundingMode: RoundingMode): BigDecimal = BigDecimal(numerator).divide(BigDecimal(denominator), scale, roundingMode)

    /**
     * Returns this as a BigDecimal
     * @param  mathContext the [MathContext] to use for rounding and accuracy
     * @throws ArithmeticException if denominator is zero
     */
    fun toBigDecimal(mathContext: MathContext): BigDecimal = BigDecimal(numerator).divide(BigDecimal(denominator), mathContext)

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

    fun toLongExact(roundingMode: RoundingMode) = toBigInteger(roundingMode).longValueExact()

    fun toIntExact(roundingMode: RoundingMode) = toBigInteger(roundingMode).intValueExact()

    fun toShortExact(roundingMode: RoundingMode) = toBigInteger(roundingMode).shortValueExact()

    fun toByteExact(roundingMode: RoundingMode) = toBigInteger(roundingMode).byteValueExact()

    operator fun plus(other: Fraction) = of(numerator * other.denominator + other.numerator * denominator, denominator * other.denominator)

    operator fun minus(other: Fraction) = of(numerator * other.denominator - other.numerator * denominator, denominator * other.denominator)

    operator fun times(other: Fraction) = of(numerator * other.numerator, denominator * other.denominator)

    operator fun div(other: Fraction) = of(numerator * other.denominator, denominator * other.numerator)

    operator fun rem(other: Fraction) = other * (this / other).frac()

    operator fun unaryMinus(): Fraction = Fraction(-numerator, denominator)

    operator fun unaryPlus(): Fraction = this
    
    override infix operator fun compareTo(other: Fraction): Int {
        return (numerator * other.denominator).compareTo(other.numerator * denominator)
    }

    /**
     * Returns the multiplicative inverse of this fraction.
     *
     * * If this is [NaN], return [NaN].
     * * If this is an infinity, returns [ZERO].
     * * If this is [ZERO], returns [POSITIVE_INFINITY]
     *
     * @return the reciprocal fraction
     */
    fun reciprocal(): Fraction {
        if(numerator.signum() < 0){
            return Fraction(-denominator, -numerator) 
        }
        return Fraction(denominator, numerator)
    }

    /**
     * Returns the fractional part of this fraction, which is this fraction with any integer part removed (`this % 1`). For example:
     *
     * `frac(13/10) == frac(43/10) == 3/10`
     *
     * Negative fractions will return a negative fractional part, but otherwise work the same. For example:
     *
     * `frac(-13/10) == frac(-43/10) == -3/10`
     *
     * The returned fraction is always between (exclusive) -1/1 and 1/1. As an exception, if `this` is infinity or NaN, `this` is returned instead.
     */
    fun frac(): Fraction {
        if (denominator == BigInteger.ZERO) return this
        return Fraction(numerator % denominator, denominator)
    }

    /**
     * Returns the signum of the fraction:
     *
     * * 1 if the fraction is positive
     * * -1 if the fraction is negative
     * * 0 if the fraction is [ZERO] or [NaN]
     */
    fun signum() = numerator.signum()

    /**
     * Converts this fraction into a continued fraction representation.
     *
     * @throws ArithmeticException if the denominator is zero (`this` is Infinity or NaN)
     */
    fun continuedFraction(): ContinuedFraction {
        if(denominator == BigInteger.ZERO) throw ArithmeticException(toString(0))
        return ContinuedFraction.of(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Fraction) return false

        if (numerator != other.numerator) return false
        if (denominator != other.denominator) return false

        return true
    }

    override fun hashCode() = numerator.hashCode() + denominator.hashCode() * 2047

    operator fun component1() = numerator

    operator fun component2() = denominator

    /**
     * Multiplies this fraction by 2 to the power of [shift] as if the number were shifted left in a binary representation and returns the resulting fraction.
     */
    fun shiftLeft(shift: Int): Fraction {
        if(denominator == BigInteger.ZERO) return this
        var nu = numerator
        var de = denominator
        if(shift > 0) {
            val lsb = de.lowestSetBit
            if(shift > lsb){
                de = de.shiftRight(lsb)
                nu = nu.shiftLeft(shift - lsb)
            }
            else {
                de = de.shiftRight(shift)
            }
        }
        else if (shift < 0) {
            val lsb = nu.lowestSetBit
            if(-shift > lsb){
                nu = nu.shiftRight(lsb)
                de = de.shiftLeft(-shift - lsb)
            }
            else {
                nu = nu.shiftRight(-shift)
            }
        }
        return Fraction(nu, de)
    }

    /**
     * Divides this fraction by 2 to the power of [shift] as if the number were shifted right in a binary representation and returns the resulting fraction.
     */
    fun shiftRight(shift: Int) = shiftLeft(-shift)
}

/**
 * A representation of a simple continued fraction. This class currently expects a finite continued fraction, feeding it
 * infinitely many BigIntegers may lead to some algorithms not terminating correctly for now.
 */
class ContinuedFraction private constructor(private val arg: Iterable<BigInteger>) : Comparable<ContinuedFraction>, Number(), Iterable<BigInteger> {

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
        fun of(value: Fraction): ContinuedFraction = ContinuedFraction(Iterable { ContinuedFractionIterator(value) })
    }

    override fun iterator(): Iterator<BigInteger> {
        return arg.iterator()
    }

    override fun toString(): String {
        val builder = StringBuilder("[")
        var n = 0
        for (bigInteger in this) {
            builder.append(bigInteger)
            builder.append(if (n == 0) "; " else ", ")
            n++
            if (n > 10) {
                builder.append("...  ")
                break
            }
        }
        builder.setLength(builder.length - 2)
        builder.append("]")
        return builder.toString()
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
        return Fraction.of(cura, curb)
    }

    /**
     * Converts this continued fraction to a regular fraction and simplifies it as much as possible as long as the
     * supplied condition is true. This method generates more and more exact first order approximations until the
     * condition returns true. Generating second order approximations could in theory result in even simpler fractions,
     * but this causes some performance difficulties that haven't been solved yet. In the future this method may be changed to
     * (optionally) generate the best second order approximation.
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
                return Fraction.of(newa, newb)
            }
            olda = cura
            cura = newa
            oldb = curb
            curb = newb
        }
        return Fraction.of(cura, curb)
    }

    override fun compareTo(other: ContinuedFraction): Int {
        return toFraction().compareTo(other.toFraction())
    }

    fun toBigInteger(): BigInteger = toFraction().toBigInteger()

    fun toBigDecimal(scale: Int, roundingMode: RoundingMode): BigDecimal = toFraction().toBigDecimal(scale, roundingMode)

    fun toBigDecimal(mathContext: MathContext): BigDecimal = toFraction().toBigDecimal(mathContext)

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

internal class FpHelper(val significand: BigInteger, val exp: BigInteger){

    companion object{
        /**
         * Bit fiddling to extract exact double value, special doubles like Infinity or NaN must be handled separately.
         */
        fun ofDouble(value : Double) : FpHelper{
            val bits = java.lang.Double.doubleToLongBits(value)
            val sign = bits < 0
            var exp = (bits and 0x7ff0000000000000L ushr 52).toInt()
            var significand = (bits and 0x000fffffffffffffL)
            if(exp > 0) significand = significand or 0x0010000000000000L else exp = 1
            if (sign) significand = -significand
            return FpHelper(BigInteger.valueOf(significand), BigInteger.valueOf((exp - 1075).toLong()))
        }

        /**
         * Bit fiddling to extract exact float value, special floats like Infinity or NaN must be handled separately.
         */
        fun ofFloat(value : Float) : FpHelper{
            val bits = java.lang.Float.floatToIntBits(value)
            val sign = bits < 0
            var exp = (bits and 0x7f800000 ushr 23).toInt()
            var significand = (bits and 0x007fffff)
            if(exp > 0) significand = significand or 0x00800000 else exp = 1
            if (sign) significand = -significand
            return FpHelper(BigInteger.valueOf(significand.toLong()), BigInteger.valueOf((exp - 150).toLong()))
        }
    }

    fun toDouble(): Double {
        var mantissa = significand.abs()
        val bitLength = mantissa.bitLength()
        var e = exp
        e += BigInteger.valueOf((bitLength - 53).toLong())
        if(bitLength > 52)
            mantissa = divide(mantissa, BigInteger.ZERO.setBit(bitLength - 53), RoundingMode.HALF_EVEN)
        else
            mantissa = mantissa.shiftRight(bitLength - 53)
        e += BigInteger.valueOf(1075)
        if (mantissa == BigInteger.ZERO) e = BigInteger.ZERO
        if (e < BigInteger.ZERO) {
            try {
                mantissa = mantissa.shiftRight(1 - (e.intValueExact()))
            } catch (e: ArithmeticException) {
                mantissa = BigInteger.ZERO
            }
            e = BigInteger.ZERO
        }
        var intExp = try {
            e.intValueExact()
        } catch (e: ArithmeticException) {
            2047
        }
        if (intExp > 2046) {
            // Inf
            intExp = 2047
            mantissa = BigInteger.ZERO
        }
        var doubleBits: Long = (mantissa.longValueExact() and 0x000fffffffffffffL) or (intExp.toLong().shl(52))
        if (significand.signum() < 0) {
            doubleBits = doubleBits or 1L.shl(63)
        }
        return java.lang.Double.longBitsToDouble(doubleBits)
    }

    fun toFloat(): Float {
        var mantissa = significand.abs()
        val bitLength = mantissa.bitLength()
        var e = exp
        e += BigInteger.valueOf((bitLength - 24).toLong())
        if(bitLength > 23)
            mantissa = divide(mantissa, BigInteger.ZERO.setBit(bitLength - 24), RoundingMode.HALF_EVEN)
        else
            mantissa = mantissa.shiftRight(bitLength - 24)
        e += BigInteger.valueOf(150)
        if (mantissa == BigInteger.ZERO) e = BigInteger.ZERO
        if (e < BigInteger.ZERO) {
            try {
                mantissa = mantissa.shiftRight(1 - (e.intValueExact()))
            } catch (e: ArithmeticException) {
                mantissa = BigInteger.ZERO
            }
            e = BigInteger.ZERO
        }
        var intExp = try {
            e.intValueExact()
        } catch (e: ArithmeticException) {
            255
        }
        if (intExp > 254) {
            // Inf
            intExp = 255
            mantissa = BigInteger.ZERO
        }
        var intBits: Int = (mantissa.intValueExact() and 0x007fffff) or (intExp.shl(23))
        if (significand.signum() < 0) {
            intBits = intBits or 1.shl(31)
        }
        return java.lang.Float.intBitsToFloat(intBits)
    }

    fun divide(p: BigInteger, q: BigInteger , mode : RoundingMode) : BigInteger {
      val pDec = BigDecimal(p)
        val qDec = BigDecimal(q)
        return pDec.divide(qDec, 0, mode).toBigIntegerExact()
    }

}
