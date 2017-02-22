package de.komath.experimental

import de.komath.Fraction
import java.math.BigInteger

/**
 * TBD Dokumentation
 */
class Complex private constructor(val real : BigFloat, val imaginary : BigFloat){

    companion object {
        /**
        * Represents the value 0/0 (NaN)
        */
        val NaN = Complex(BigFloat.NaN, BigFloat.NaN)

        /**
         * Represents the value 0/1 (Zero)
         */
        val ZERO = Complex(BigFloat.ZERO, BigFloat.ZERO)

        /**
         * Represents the value 1/1 (One)
         */
        val ONE = Complex(BigFloat.ONE, BigFloat.ZERO)

        /**
         * Represents the value -1/1 (Negative One)
         */
        val NEGATIVE_ONE = -ONE

        /**
         * Represents the value 1/0 (Complex Infinity)
         */
        val COMPLEX_INFINITY = Complex(BigFloat.POSITIVE_INFINITY, BigFloat.ZERO);

        fun of(real: BigFloat, imaginary: BigFloat) : Complex {
            if(real == BigFloat.NaN || imaginary == BigFloat.NaN){
                return NaN;
            }
            if(real.denominator == BigInteger.ZERO
                    || imaginary.denominator == BigInteger.ZERO){
                return COMPLEX_INFINITY;
            }
            return Complex(real, imaginary);
        }
    }

    fun reciprocal(): Complex {
        val scale = (real * real) + (imaginary * imaginary)
        return Complex(real / scale, -imaginary / scale)
    }

    fun absSqr(): BigFloat = real * real + imaginary * imaginary

    operator fun unaryMinus(): Complex = Complex(-real, -imaginary)
    operator fun plus(other: BigFloat): Complex = Complex(real + other, imaginary)
    operator fun minus(other: BigFloat): Complex = Complex(real - other, imaginary)
    operator fun times(other: BigFloat): Complex = Complex(real * other, imaginary * other)
    operator fun div(other: BigFloat): Complex = Complex(real / other, imaginary / other)

    operator fun plus(other: Complex): Complex =
            Complex(real + other.real, imaginary + other.imaginary)

    operator fun minus(other: Complex): Complex =
            Complex(real - other.real, imaginary - other.imaginary)

    operator fun times(other: Complex): Complex =
            Complex(
                    (real * other.real) - (imaginary * other.imaginary),
                    (real * other.imaginary) + (imaginary * other.real))

    operator fun div(other: Complex): Complex = this * other.reciprocal()

    override fun toString(): String {
        return "${real} ${imaginary}i"
    }
}

fun main(args: Array<String>){
    fun mandelbrot(c: Complex, maxIterations: Int): Int? {
        tailrec fun iterate(z: Complex, iterations: Int): Int? =
                when {
                    iterations == maxIterations -> null
                    (z.absSqr() > 4.0)          -> iterations
                    else                        -> iterate((z * z) + c, iterations + 1)
                }
        //println(c)
        return iterate(Complex.ZERO, 0)
    }

    val chars = "▁▂▃▅██████████████████████████████████████████████████████████████████████████████"
    val rc = BigFloat.of(Fraction.of("-.743643887037151"))
    val ic = BigFloat.of(Fraction.of(".131825904205330"))
// prints an ASCII Mandelbrot set
    for (i in -40..40) {
        for (r in -80..40) {
            val value = 599790000.0
            print(mandelbrot(Complex.of(BigFloat.of(r)/ BigFloat.ofExact(40.0)/ BigFloat.ofExact(value) + rc, BigFloat.of(i)/ BigFloat.ofExact(40.0)/ BigFloat.ofExact(value) +ic) , 200)
                    ?.let { val n = it / (200) - 6
                        if (n >= 0) '▁' + n else ' '
                    }
                    ?: ' '
            )
        }
        println()
    }
}