package de.komath.units

interface BaseUnitFormatter : UnitFormatter {
    fun power(power: Int): BaseUnitFormatter
}
