package de.komath.units

fun appendUnit(stringBuilder: StringBuilder, unit: String, power: Int, powerMultiplier: Int, positivePrefixes: Boolean, negativePrefixes: Boolean) {
    appendUnit(powerMultiplier, stringBuilder, power, positivePrefixes, negativePrefixes)
    stringBuilder.append(unit)
    appendPower(stringBuilder, power)
}


private fun appendUnit(powerMultiplier: Int, stringBuilder: StringBuilder, power: Int,  positivePrefixes: Boolean, negativePrefixes: Boolean) {
    if (powerMultiplier == 0) return
    if (positivePrefixes) {
        when (powerMultiplier) {
            8 -> {
                stringBuilder.append('Y')
                return
            }
            7 -> {
                stringBuilder.append('Z')
                return
            }
            6 -> {
                stringBuilder.append('E')
                return
            }
            5 -> {
                stringBuilder.append('P')
                return
            }
            4 -> {
                stringBuilder.append('T')
                return
            }
            3 -> {
                stringBuilder.append('G')
                return
            }
            2 -> {
                stringBuilder.append('M')
                return
            }
            1 -> {
                stringBuilder.append('k')
                return
            }
        }
    }
    if (negativePrefixes) {
        when (powerMultiplier) {
            -1 -> {
                stringBuilder.append('m')
                return
            }
            -2 -> {
                stringBuilder.append('µ')
                return
            }
            -3 -> {
                stringBuilder.append('n')
                return
            }
            -4 -> {
                stringBuilder.append('p')
                return
            }
            -5 -> {
                stringBuilder.append('f')
                return
            }
            -6 -> {
                stringBuilder.append('a')
                return
            }
            -7 -> {
                stringBuilder.append('z')
                return
            }
            -8 -> {
                stringBuilder.append('y')
                return
            }
        }
    }
    stringBuilder.append("×\u202f10").append(toSuperScript(powerMultiplier * power * 3)).append("\u202f")
}

fun appendPower(stringBuilder: StringBuilder, power: Int) {
    if (power != 1) {
        stringBuilder.append(toSuperScript(power))
    }
}

fun toSuperScript(exponent: Int): CharArray {
    val chars = exponent.toString().toCharArray()
    for (i in chars.indices) {
        when (chars[i]) {
            '0' -> chars[i] = '⁰'
            '1' -> chars[i] = '¹'
            '2' -> chars[i] = '²'
            '3' -> chars[i] = '³'
            '4' -> chars[i] = '⁴'
            '5' -> chars[i] = '⁵'
            '6' -> chars[i] = '⁶'
            '7' -> chars[i] = '⁷'
            '8' -> chars[i] = '⁸'
            '9' -> chars[i] = '⁹'
            '-' -> chars[i] = '⁻'
        }
    }
    return chars
}

interface UnitFormatter {
    fun format(value: Double) : String
}
