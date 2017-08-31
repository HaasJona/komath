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
