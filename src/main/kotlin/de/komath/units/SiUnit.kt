package de.komath.units

import com.google.common.collect.ImmutableMap

object Meter:SiUnit("m", "Meter", 6, StandardFormatter("m", "Meter", 1.0, true, true, 1))
object Kilogram :SiUnit("kg", "Kilogram", 5, StandardFormatter("g", "Gram", 1000.0, true, true, 1))
object Second :SiUnit("s", "Second", 7, StandardFormatter("s", "Second", 1.0, false, true, 1))
object Ampere:SiUnit("A", "Ampere", 1, StandardFormatter("A", "Ampere", 1.0, true, true, 1))
object Kelvin:SiUnit("K", "Kelvin", 2, StandardFormatter("K", "Kelvin", 1.0, false, false, 1))
object Mole:SiUnit("mol", "Mole", 3, StandardFormatter("mol", "Mole", 1.0, true, true, 1))
object Candela:SiUnit("cd", "Candela", 4, StandardFormatter("cd", "Candela", 1.0, true, true, 1))


sealed class SiUnit(override val symbol: String, val fullname: String, override val sortHint: Int, override val formatter: StandardFormatter) : AbstractUnit(), BaseUnit {
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

