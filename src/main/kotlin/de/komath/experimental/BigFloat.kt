package de.komath.experimental

import de.komath.FpHelper
import de.komath.Fraction
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

/**
 * Work in Progress
 */
class BigFloat private constructor(val numerator: BigInteger, val denominator : BigInteger, val exp: BigInteger) : Comparable<BigFloat>, Number(){

    companion object {

        /**
         * Represents the value 0/0 (NaN)
         */
        val NaN = BigFloat(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO)

        /**
         * Represents the value 0/1 (Zero)
         */
        val ZERO = BigFloat(BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO)

        /**
         * Represents the value 1/1 (One)
         */
        val ONE = BigFloat(BigInteger.ONE, BigInteger.ONE, BigInteger.ZERO)

        /**
         * Represents the value -1/1 (Negative One)
         */
        val NEGATIVE_ONE = -ONE

        /**
         * Represents the value 1/0 (Positive Infinity)
         */
        val POSITIVE_INFINITY = BigFloat(BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO)

        /**
         * Represents the value -1/0 (Negative Infinity)
         */
        val NEGATIVE_INFINITY = -POSITIVE_INFINITY

        fun of(numerator: BigInteger, denominator: BigInteger, exp: BigInteger): BigFloat {
            if(denominator == BigInteger.ZERO){
                val signum = numerator.signum()
                if(signum > 0) return POSITIVE_INFINITY
                if(signum < 0) return NEGATIVE_INFINITY
                return NaN
            }
            if(numerator == BigInteger.ZERO) return ZERO

            var numeratorShift = numerator.lowestSetBit
            var denominatorShift = denominator.lowestSetBit
            if(numerator.bitLength() > 1024) {
                numeratorShift += numerator.bitLength() - 1024;
            }
            if(denominator.bitLength() > 1024) {
                denominatorShift += denominator.bitLength() - 1024;
            }
            return BigFloat(numerator.shiftRight(numeratorShift), denominator.shiftRight(denominatorShift), exp + BigInteger.valueOf(numeratorShift.toLong()) - BigInteger.valueOf(denominatorShift.toLong()))
        }

        fun of(value: Fraction): BigFloat = of(value.numerator, value.denominator, BigInteger.ZERO)

        fun of(value: BigInteger): BigFloat = of(Fraction.of(value))

        fun of(value: Long): BigFloat = of(BigInteger.valueOf(value))

        fun of(value: Int): BigFloat = of(value.toLong())

        fun ofExact(value: Double): BigFloat {
            if (value.isNaN()) return NaN
            if (value.isInfinite()) {
                return if (value > 0) POSITIVE_INFINITY else NEGATIVE_INFINITY
            }
            return ofExactNoSpecialValue(value)
        }

        /**
         * Bit fiddling to extract exact double value, special doubles like 0.0 or NaN must be handled separately.
         */
        private fun ofExactNoSpecialValue(value: Double): BigFloat {
            val helper = FpHelper.ofDouble(value)
            return of(Fraction.of(helper.significand).numerator, Fraction.of(helper.significand).denominator, helper.exp)
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
        fun ofExact(value: Float): BigFloat {
            if (value.isNaN()) return NaN
            if (value.isInfinite()) {
                return if (value > 0) POSITIVE_INFINITY else NEGATIVE_INFINITY
            }
            return ofExactNoSpecialValue(value)
        }

        /**
         * Bit fiddling to extract exact float value, special floats like 0.0 or NaN must be handled separately.
         */
        private fun ofExactNoSpecialValue(value: Float): BigFloat {
            val helper = FpHelper.ofFloat(value)
            return of(Fraction.of(helper.significand).numerator, Fraction.of(helper.significand).denominator, helper.exp)
        }

        /**
         * Returns a fraction that represents the exact value of the specified BigDecimal.
         *
         * For example `Fraction.of(BigDecimal.valueOf(0.3)) == 3/10`.
         */
        fun of(value: BigDecimal) : BigFloat {
            if(value.scale() > 0)
                return of(Fraction.of(value.unscaledValue(), BigInteger.TEN.pow(value.scale())))
            else
                return of(value.toBigIntegerExact())
        }
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
        return FpHelper(significand, exp-negExp).toDouble()
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
        return FpHelper(significand, exp-negExp).toFloat()
    }
//
//    /**
//     * Returns a string representation of this fraction with the specified number of maximum decimal places and the specified radix (default: 10).
//     *
//     * Special values like Infinity or NaN are returned in the same way as [Double.toString]
//     */
//    fun toString(n: Int, radix: Int = 10): String {
//        if(radix < 2) throw IllegalArgumentException("radix")
//        if(denominator == BigInteger.ZERO){
//            if(numerator > BigInteger.ZERO){
//                return Double.POSITIVE_INFINITY.toString()
//            } else if(numerator < BigInteger.ZERO){
//                return Double.NEGATIVE_INFINITY.toString()
//            } else {
//                return Double.NaN.toString()
//            }
//        }
//        val radixBigInt = BigInteger.valueOf(radix.toLong())
//        val s = StringBuilder()
//        var divideAndRemainder = numerator.divideAndRemainder(denominator)
//        if(n > 0) s.append(divideAndRemainder[0].toString(radix)).append('.')
//        var remainder = divideAndRemainder[1].abs()
//        for(i in 1..n){
//            remainder *= radixBigInt
//            divideAndRemainder = remainder.divideAndRemainder(denominator)
//            remainder = divideAndRemainder[1]
//            s.append(divideAndRemainder[0].toString(radix))
//            if(remainder == BigInteger.ZERO) break
//        }
//        return s.toString()
//    }

    /**
     * Returns the BigInteger value (rounded towards zero) of this  If the denominator is zero, returns zero.
     */
    fun toBigInteger(): BigInteger {
        if(exp.signum() >= 0) {
            return numerator.shiftLeft(exp.intValueExact()).divide(denominator)
        }
        else {
            return numerator.divide(denominator.shiftRight(exp.intValueExact()))
        }
    }

    /**
     * Returns this as a BigDecimal
     * @param  scale scale of the {@code BigDecimal} quotient to be returned.
     * @param  roundingMode rounding mode to apply.
     * @throws ArithmeticException if denominator is zero
     */
    fun toBigDecimal(scale: Int, roundingMode: RoundingMode): BigDecimal {
        if(exp.signum() >= 0) {
            return BigDecimal(numerator.shiftLeft(exp.intValueExact())).divide(BigDecimal(denominator), scale, roundingMode)
        }
        else {
            return BigDecimal(numerator).divide(BigDecimal(denominator.shiftRight(exp.intValueExact())), scale, roundingMode)
        }
    }

    /**
     * Returns this as a BigDecimal
     * @param  mathContext the [MathContext] to use for rounding and accuracy
     * @throws ArithmeticException if denominator is zero
     */
    fun toBigDecimal(mathContext: MathContext): BigDecimal {
        if(exp.signum() >= 0) {
            return BigDecimal(numerator.shiftLeft(exp.intValueExact())).divide(BigDecimal(denominator), mathContext)
        }
        else {
            return BigDecimal(numerator).divide(BigDecimal(denominator.shiftRight(exp.intValueExact())), mathContext)
        }
    }

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

    operator fun plus(other: BigFloat) : BigFloat {
        var myNum = numerator;
        var myDen = denominator;
        val e : BigInteger;
        if(exp == other.exp){
            e = exp;
        }
        else if(exp > other.exp){
            myNum = myNum.shiftLeft((exp-other.exp).intValueExact())
            e = other.exp;
        }
        else {
            myDen = myDen.shiftRight((exp-other.exp).intValueExact())
            e = exp
        }
        return of(myNum * other.denominator + other.numerator * myDen, denominator * other.denominator, e)
    }

    operator fun minus(other: BigFloat)  : BigFloat {
        var myNum = numerator;
        var myDen = denominator;
        val e : BigInteger;
        if(exp == other.exp){
            e = exp;
        }
        else if(exp > other.exp){
            myNum = myNum.shiftLeft((exp-other.exp).intValueExact())
            e = other.exp;
        }
        else {
            myDen = myDen.shiftRight((exp-other.exp).intValueExact())
            e = exp
        }
        return of(myNum * other.denominator - other.numerator * myDen, denominator * other.denominator, e)
    }

    operator fun times(other: BigFloat) = of(numerator * other.numerator, denominator * other.denominator, exp + other.exp)

    operator fun div(other: BigFloat) = of(numerator * other.denominator, denominator * other.numerator, exp - other.exp)

    operator fun mod(other: BigFloat) : BigFloat {
        return of(toFraction() % other.toFraction())
    }

    fun toFraction(): Fraction {
        return Fraction.of(numerator, denominator).shiftLeft(exp.intValueExact());
    }

    operator fun unaryMinus(): BigFloat = BigFloat(-numerator, denominator, exp)

    operator fun unaryPlus(): BigFloat = this

    override infix operator fun compareTo(other: BigFloat): Int {
        return toFraction().compareTo(other.toFraction())
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
        return toDouble().compareTo(other)
    }

    infix operator fun compareTo(other: Float): Int {
        return compareTo(ofExact(other.toDouble()))
    }

    override fun toString(): String {
        return toFraction().toBigDecimal(32, RoundingMode.DOWN).toString()
    }
}

fun main(args: Array<String>) {
    val ofExact = BigFloat.ofExact(Math.PI).times(BigFloat.of(64).div(BigFloat.ofExact(Math.PI)))
    println("ofExact = ${ofExact}")
    println("ofExact.toDouble() = ${ofExact.toDouble()}")
}