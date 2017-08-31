package de.komath.units

class CompoundUnitFormatter(val compoundUnit: CompoundUnit) : UnitFormatter {
    override fun format(value: Double): String {
        if (!value.isFinite()) return value.toString()
        
        var adjustedValue = value;
        
        val powerFactor = 1000.0
        var powerMultiplier = 0

        while (Math.abs(adjustedValue) > 10 * powerFactor) {
            powerMultiplier++
            adjustedValue /= powerFactor
        }
        while (adjustedValue != 0.0 && Math.abs(adjustedValue) * powerFactor < 100) {
            powerMultiplier--
            adjustedValue *= powerFactor
        }
        val stringBuilder = StringBuilder()
        stringBuilder.append(FORMAT.get().format(adjustedValue))
        stringBuilder.append('\u202f')
        appendUnit(stringBuilder, compoundUnit.symbol, 1, powerMultiplier, false, false)
        return stringBuilder.toString()

    }

}
