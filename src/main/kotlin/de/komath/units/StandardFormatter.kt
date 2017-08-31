package de.komath.units

class StandardFormatter(val symbol: String, val name: String, val multiplication: Double = 1.0, val positivePrefixes: Boolean, val negativePrefixes: Boolean, private val pow: Int = 1) : BaseUnitFormatter {

    override fun power(power: Int) : StandardFormatter {
        return StandardFormatter(symbol, name, Math.pow(multiplication, power.toDouble()), positivePrefixes, negativePrefixes, this.pow * power)
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
        while (adjustedValue != 0.0 && Math.abs(adjustedValue) * powFactor < 100) {
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
        while (adjustedValue != 0.0 && Math.abs(adjustedValue) * powFactor < 100) {
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
        val tmp = factor * multiplication
        when (tmp) {
            1.0 -> return name
        }
        if (positivePrefixes) {
            when (tmp) {
                10.0 -> return "Deca$name".toLowerCase().capitalize()
                100.0 -> return "Hecto$name".toLowerCase().capitalize()
                1000.0 -> return "Kilo$name".toLowerCase().capitalize()
                1000_000.0 -> return "Mega$name".toLowerCase().capitalize()
                1000_000_000.0 -> return "Giga$name".toLowerCase().capitalize()
                1000_000_000_000.0 -> return "Tera$name".toLowerCase().capitalize()
                1000_000_000_000_000.0 -> return "Peta$name".toLowerCase().capitalize()
                1000_000_000_000_000_000.0 -> return "Exa$name".toLowerCase().capitalize()
                1000_000_000_000_000_000_000.0 -> return "Zetta$name".toLowerCase().capitalize()
                1000_000_000_000_000_000_000_000.0 -> return "Yotta$name".toLowerCase().capitalize()
            }
        }
        if (positivePrefixes) {
            when (tmp) {
                .1 -> return "Deci$name".toLowerCase().capitalize()
                .01 -> return "Centi$name".toLowerCase().capitalize()
                .001 -> return "Milli$name".toLowerCase().capitalize()
                .000_001 -> return "Micro$name".toLowerCase().capitalize()
                .000_000_001 -> return "Nano$name".toLowerCase().capitalize()
                .000_000_000_001 -> return "Pico$name".toLowerCase().capitalize()
                .000_000_000_000_001 -> return "Femto$name".toLowerCase().capitalize()
                .000_000_000_000_000_001 -> return "Atto$name".toLowerCase().capitalize()
                .000_000_000_000_000_000_001 -> return "Zepto$name".toLowerCase().capitalize()
                .000_000_000_000_000_000_000_001 -> return "Yocto$name".toLowerCase().capitalize()
            }
        }
        return "$factor·$name"
    }
}
