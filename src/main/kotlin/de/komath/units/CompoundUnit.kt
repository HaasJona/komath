package de.komath.units

class CompoundUnit(override val baseComponents: Map<BaseUnit, Int>) : AbstractUnit() {
    override fun toString(): String {
        val builder = StringBuilder()
        for ((unit, power) in baseComponents) {
            builder.append(unit.toString())
            appendPower(builder, power)
        }
        return builder.toString()
    }

    override fun normalize(): Amount<*> {
        var tmp : Amount<*> = One
        for ((unit, power) in baseComponents) {
            tmp *= unit.normalize().power(power)
        }
        return tmp
    }

    override val symbol: String = getSymbol(baseComponents)

    override val formatter: UnitFormatter = CompoundUnitFormatter(this)

}

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


fun getSymbol(baseComponents: Map<BaseUnit, Int>): String {
    val stringBuilder = StringBuilder()
    val iterator = baseComponents.iterator()
    while (iterator.hasNext()) {
        val next = iterator.next()
        val unit = next.key
        val power = next.value
        stringBuilder.append(unit.symbol)
        appendPower(stringBuilder, power)
        if(iterator.hasNext()) {
            stringBuilder.append(" ")
        }
    }
    return stringBuilder.toString();
}
