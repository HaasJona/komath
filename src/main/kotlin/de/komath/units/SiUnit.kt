package de.komath.units

import com.google.common.collect.ImmutableMap
import java.lang.Math.abs

object Meter:SiUnit("m", "Meter", 6, SiFormatter("m", "Meter", 1.0, true, true, 1))
object Kilogram :SiUnit("kg", "Kilogram", 5, SiFormatter("g", "Gram", 1000.0, true, true, 1))
object Second :SiUnit("s", "Second", 7, SiFormatter("s", "Second", 1.0, false, true, 1))
object Ampere:SiUnit("A", "Ampere", 1, SiFormatter("A", "Ampere", 1.0, true, true, 1))
object Kelvin:SiUnit("K", "Kelvin", 2, SiFormatter("K", "Kelvin", 1.0, false, false, 1))
object Mole:SiUnit("mol", "Mole", 3, SiFormatter("mol", "Mole", 1.0, true, true, 1))
object Candela:SiUnit("cd", "Candela", 4, SiFormatter("cd", "Candela", 1.0, true, true, 1))


sealed class SiUnit(override val symbol: String, val fullname: String, override val sortHint: Int, override val formatter: SiFormatter) : AbstractUnit(), BaseUnit {
    override fun factorSymbol(factor: Double): String {
        return formatter.factorSymbol(factor)
    }

    override fun factorName(factor: Double): String {
        return formatter.factorName(factor)
    }

    override val baseComponents: Map<BaseUnit, Int> = ImmutableMap.of(this, 1)

    override fun normalize(): Amount<*> {
        return Amount(1.0, this)
    }

    override fun toString(): String {
        return fullname;
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int = sortHint
}

class SiFormatter(val symbol: String, val name: String, val multiplication: Double, val positivePrefixes: Boolean, val negativePrefixes: Boolean, private val pow: Int) : BaseUnitFormatter {

    override fun power(power: Int) : SiFormatter {
        return SiFormatter(symbol, name, Math.pow(multiplication, power.toDouble()), positivePrefixes, negativePrefixes, this.pow * power)
    }    
    
    override fun format(value: Double): String {
        var adjustedValue = value * multiplication

        if (!adjustedValue.isFinite()) return value.toString() + '\u202f' + symbol
        val powFactor = Math.pow(1000.0, Math.abs(pow.toDouble()))
        var powMultiplier = 0
        
        while (Math.abs(adjustedValue) >= powFactor) {
            powMultiplier++
            adjustedValue /= powFactor
        }
        while (adjustedValue != 0.0 && abs(adjustedValue) * powFactor < 100) {
            powMultiplier--
            adjustedValue *= powFactor
        }
        val stringBuilder = StringBuilder()
        stringBuilder.append(FORMAT.get().format(adjustedValue))
        stringBuilder.append('\u202f')
        appendUnit(stringBuilder, symbol, pow, powMultiplier, positivePrefixes, negativePrefixes)
        return stringBuilder.toString()
        
    }

    fun factorSymbol(factor: Double): String {
        var adjustedValue = factor * multiplication

        if (!adjustedValue.isFinite()) return adjustedValue.toString() + '\u202f' + symbol
        val powFactor = Math.pow(1000.0, Math.abs(pow.toDouble()))
        var powMultiplier = 0

        while (Math.abs(adjustedValue) >= powFactor) {
            powMultiplier++
            adjustedValue /= powFactor
        }
        while (adjustedValue != 0.0 && abs(adjustedValue) * powFactor < 100) {
            powMultiplier--
            adjustedValue *= powFactor
        }
        val stringBuilder = StringBuilder()
        if(adjustedValue != 1.0) {
            stringBuilder.append(FORMAT.get().format(adjustedValue))
            stringBuilder.append('\u202f')
        }
        appendUnit(stringBuilder, symbol, pow, powMultiplier, positivePrefixes, negativePrefixes)
        return stringBuilder.toString()  
    }

    fun factorName(factor: Double): String {
        return name
    }
}

