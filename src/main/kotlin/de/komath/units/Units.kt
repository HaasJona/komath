@file:Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")

package de.komath.units

import com.google.common.collect.ImmutableMap
import java.text.NumberFormat


// Derived Units
object Radian : AbstractUnit(), BaseUnit by Unity.rename("rad", "Radian", false, false)
object Steradian : AbstractUnit(), BaseUnit by Unity.rename("sr", "Steradian", false, false)
object Hertz : AbstractUnit(), BaseUnit by Second.pow(-1).rename("Hz", "Hertz", true, true)
object Newton : AbstractUnit(), BaseUnit by Meter.times(Kilogram).times(Second.pow(-2)).rename("N", "Newton", true, true)
object Pascal : AbstractUnit(), BaseUnit by (Newton / Meter / Meter).rename("Pa", "Pascal", true, true)
object Joule : AbstractUnit(), BaseUnit by Newton.times(Meter).rename("J", "Joule", true, true)
object Watt : AbstractUnit(), BaseUnit by Joule.times(Second.pow(-1)).rename("W", "Watt", true, true)
object Coulomb : AbstractUnit(), BaseUnit by Second.times(Ampere).rename("C", "Coulomb", true, true)
object Volt : AbstractUnit(), BaseUnit by Watt.times(Ampere.pow(-1)).rename("V", "Volt", true, true)
object Farad : AbstractUnit(), BaseUnit by Coulomb.times(Volt.pow(-1)).rename("F", "Farad", true, true)
object Ohm : AbstractUnit(), BaseUnit by Volt.times(Ampere.pow(-1)).rename("Ω", "Ohm", true, true)
object Siemens : AbstractUnit(), BaseUnit by Ohm.pow(-1).rename("S", "Siemens", true, true)
object Weber : AbstractUnit(), BaseUnit by Joule.times(Ampere.pow(-1)).rename("Wb", "Weber", true, true)
object Tesla : AbstractUnit(), BaseUnit by Weber.times(Meter.pow(-2)).rename("T", "Tesla", true, true)
object Henry : AbstractUnit(), BaseUnit by Weber.times(Ampere.pow(-1)).rename("H", "Henry", true, true)
object Lumen : AbstractUnit(), BaseUnit by (Candela * Steradian).rename("lm", "Lumen", true, true)
object Lux : AbstractUnit(), BaseUnit by (Lumen / Meter / Meter).rename("lx", "Lux", true, true)
object Becquerel : AbstractUnit(), BaseUnit by Hertz.rename("Bq", "Becquerel", true, true)
object Gray : AbstractUnit(), BaseUnit by (Joule / Kilogram).rename("Gy", "Gray", true, true)
object Sievert : AbstractUnit(), BaseUnit by (Joule / Kilogram).rename("Sv", "Sievert", true, true)
object Katal : AbstractUnit(), BaseUnit by (Mole / Second).rename("kat", "Katal", true, true)

// Common units
object Kilometer : AbstractUnit(), BaseUnit by Meter * 1000.0
object Centimeter : AbstractUnit(), BaseUnit by Meter * 0.01
object Millimeter : AbstractUnit(), BaseUnit by Meter * 0.001
object Micrometer : AbstractUnit(), BaseUnit by Meter * 0.000001
object Nanometer : AbstractUnit(), BaseUnit by Meter * 0.000000001
object Tonne : AbstractUnit(), BaseUnit by Kilogram.times(1000.0, "t", "Tonne", true, false)
object Kilotonne : AbstractUnit(), BaseUnit by Tonne * 1000.0
object Minute : AbstractUnit(), BaseUnit by Second.times(60.0, "min", "Minute", false, false)
object Hour : AbstractUnit(), BaseUnit by Minute.times(60.0, "h", "Hour", false, false)
object Day : AbstractUnit(), BaseUnit by Minute.times(24.0, "d", "Day", false, false)
object Millisecond : AbstractUnit(), BaseUnit by Second * 0.001
object Microsecond : AbstractUnit(), BaseUnit by Second * 0.000001
object Nanosecond : AbstractUnit(), BaseUnit by Second * 0.000000001
object Gram : AbstractUnit(), BaseUnit by Kilogram * 0.001
object Milligram : AbstractUnit(), BaseUnit by Kilogram * 0.000001
object Microgram : AbstractUnit(), BaseUnit by Kilogram * 0.000000001
object Nanogram : AbstractUnit(), BaseUnit by Kilogram * 0.000000000001
object Dalton : AbstractUnit(), BaseUnit by Kilogram.times(1.660_539_040_20e-27, "Da", "Dalton", false, false)
object UnifiedAtomicMassUnit : AbstractUnit(), BaseUnit by Kilogram.times(1.660_539_040_20e-27, "u", "Unified Atomic Mass Unit", false, false)
object SquareMeter : AbstractUnit(), DerivedUnit by Meter.pow(2)
object CubicMeter : AbstractUnit(), DerivedUnit by Meter.pow(3)
object Liter : AbstractUnit(), BaseUnit by CubicMeter.times(0.001, "l", "Liter", false, true)
object Hectare : AbstractUnit(), BaseUnit by SquareMeter.times(10000.0, "ha", "Hectare", false, false)


//Factors

object Deca {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * 10.0
    }
}

object Hecto {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * 100.0
    }
}

object Kilo {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * 1_000.0
    }
}

object Mega {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * 1_000_000.0
    }
}

object Giga {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * 1_000_000_000.0
    }
}

object Tera {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * 1_000_000_000_000.0
    }
}

object Peta {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * 1_000_000_000_000_000.0
    }
}

object Exa {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * 1_000_000_000_000_000_000.0
    }
}

object Zetta {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * 1_000_000_000_000_000_000_000.0
    }
}

object Yotta {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * 1_000_000_000_000_000_000_000_000.0
    }
}

object Deci {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * .1
    }
}

object Centi {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * .01
    }
}

object Milli {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * .001
    }
}

object Micro {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * .000_001
    }
}

object Nano {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * .000_000_001
    }
}

object Pico {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * .000_000_000_001
    }
}

object Femto {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * .000_000_000_000_001
    }
}

object Atto {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * .000_000_000_000_000_001
    }
}

object Zepto {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * .000_000_000_000_000_000_001
    }
}

object Yocto {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * .000_000_000_000_000_000_000_001
    }
}

interface DerivedUnit {
    val symbol : String   
    val formatter : UnitFormatter
    val baseComponents: Map<BaseUnit, Int>
    fun normalize(): Amount<*>
    override fun toString() : String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

    infix fun pow(pow: Int) : DerivedUnit {
        if(pow == 1) return this
        return getUnit(baseComponents.mapValues { it.value * pow })
    }
}

interface BaseUnit : DerivedUnit {
    override val formatter : BaseUnitFormatter
    fun factorSymbol(factor: Double): String
    fun factorName(factor: Double): String
    val sortHint: Int

    override infix fun pow(pow: Int) : DerivedUnit {
        if(pow == 1) return this
        return BaseUnitPower(this, pow)
    }
}

abstract class AbstractUnit : DerivedUnit {

    override fun equals(other: Any?): Boolean {
        val unit = other as? AbstractUnit ?: return false
        val normalized = normalize()
        val otherNormalized = unit.normalize()
        return normalized.value == otherNormalized.value
        && normalized.unit.baseComponents == otherNormalized.unit.baseComponents
    }

    override fun hashCode(): Int {
        val normalize = normalize()
        return normalize.value.hashCode() + normalize.unit.baseComponents.hashCode()
    }
}

operator fun DerivedUnit.times(unit: DerivedUnit) : DerivedUnit {
    val map = baseComponents.toMutableMap()
    val other = unit.baseComponents
    for ((k, v) in other) {
        if(map.containsKey(k)){
            map.put(k, map[k]!! + v)
        }
        else {
            map.put(k, v);
        }
    }
    return getUnit(map)
}

fun DerivedUnit.times(factor: Double, symbol: String, name : String, positivePrefixes: Boolean, negativePrefixes: Boolean) : BaseUnit {
    return FactorUnit(this, factor, symbol, name, positivePrefixes, negativePrefixes)
}

operator fun BaseUnit.times(factor : Double) : BaseUnit {
    return BaseFactorUnit(this, factor)
}

operator fun DerivedUnit.div(unit: DerivedUnit) : DerivedUnit {
    val map = baseComponents.toMutableMap()
    val other = unit.baseComponents
    for ((k, v) in other) {
        if(map.containsKey(k)){
            map.put(k, map[k]!! - v)
        }
        else {
            map.put(k, -v);
        }
    }
    return getUnit(map)
}

fun getUnit(unitPowers: Map<BaseUnit, Int>): DerivedUnit {
    val filtered = ImmutableMap.copyOf(unitPowers.filter { it.value != 0 }.entries.sortedWith(UnitSorter))
    if(filtered.isEmpty()) {
        return Unity
    }
    if(filtered.size == 1){
        val (unit, power) = filtered.entries.single()
        if(power == 1) return unit
        return BaseUnitPower(unit, power) 
    }
    return CompoundUnit(filtered)
}

object UnitSorter : Comparator<Map.Entry<BaseUnit, Int>> {
    override fun compare(o1: Map.Entry<BaseUnit, Int>, o2: Map.Entry<BaseUnit, Int>): Int {
        if(o1.value != o2.value) {
            if(o1.value == 1) return -1
            if(o2.value == 1) return 1
            if(o1.value > 0 && o2.value < 0) {
                return -1
            }
            if(o1.value < 0 && o2.value > 0) {
                return 1
            }
        }
        return Integer.compare(o1.key.sortHint, o2.key.sortHint)
    }

}

object Unity : AbstractUnit() {
    override fun toString() = "Unity"

    override fun normalize(): Amount<*> {
        return One
    }

    override val symbol: String get() = ""
    
    override val formatter: UnitFormatter get() = UnityFormatter
    
    override val baseComponents: Map<BaseUnit, Int> get() = ImmutableMap.of<BaseUnit, Int>()
}

val One: Amount<Unity> = Amount(1.0, Unity)

private object UnityFormatter : UnitFormatter {
    override fun format(value: Double) : String {
        if (!value.isFinite()) return value.toString()
        return FORMAT.get().format(value)
    }
}

class FactorUnit(val unit: DerivedUnit, val factor: Double, override val symbol: String, val name : String, positivePrefixes: Boolean, negativePrefixes: Boolean) : AbstractUnit(), BaseUnit {
    override val sortHint: Int
        get() = Int.MIN_VALUE 

    override fun normalize(): Amount<*> {
        return unit.normalize() * factor
    }

    override fun factorSymbol(factor: Double): String {
        return formatter.factorSymbol(factor)
    }

    override fun factorName(factor: Double): String {
        return formatter.factorName(factor)
    }

    override fun toString(): String {
        return name
    }

    override val formatter = SiFormatter(symbol, name,1.0, positivePrefixes, negativePrefixes, 1)
    
    override val baseComponents: Map<BaseUnit, Int> = ImmutableMap.of(this, 1)
}

class BaseFactorUnit(val unit: BaseUnit, val factor: Double) : AbstractUnit(), BaseUnit {
    override val sortHint: Int
        get() = unit.sortHint

    override fun normalize(): Amount<*> {
        return unit.normalize() * factor
    }

    override val formatter = BaseFactorFormatter(unit, factor)
    
    override val baseComponents: Map<BaseUnit, Int> = ImmutableMap.of(this, 1)

    override val symbol: String = unit.factorSymbol(factor)

    override fun factorSymbol(factor: Double): String {
        return unit.factorSymbol(this.factor * factor)
    }

    override fun toString() = unit.factorName(this.factor)

    override fun factorName(factor: Double): String {
        return unit.factorName(this.factor * factor)
    }
}

class BaseFactorFormatter(val unit: DerivedUnit, val factor: Double) : BaseUnitFormatter {
    
    override fun format(value: Double): String {
        return unit.formatter.format(value * factor)
    }

    override fun power(power: Int): BaseUnitFormatter {
        return BaseFactorFormatter(unit.pow(power), Math.pow(factor, power.toDouble()))
    }

}

fun DerivedUnit.rename(symbol: String, name : String, positivePrefixes: Boolean, negativePrefixes: Boolean): BaseUnit {
    return RenamedUnit(this, symbol, name, positivePrefixes, negativePrefixes)
}

class RenamedUnit(val base: DerivedUnit, override val symbol: String, val name: String,  positivePrefixes: Boolean, negativePrefixes: Boolean) : AbstractUnit(), BaseUnit {
    override val sortHint: Int
        get() = Int.MIN_VALUE

    override fun factorSymbol(factor: Double): String {
        return formatter.factorSymbol(factor)
    }

    override fun factorName(factor: Double): String {
        return formatter.factorName(factor)
    }

    override fun normalize(): Amount<*> {
        return base.normalize()
    }

    override val formatter = SiFormatter(symbol, name,1.0, positivePrefixes, negativePrefixes, 1)
    
    override val baseComponents: Map<BaseUnit, Int> = ImmutableMap.of(this, 1)

    override fun toString(): String = name
}

val FORMAT = ThreadLocal.withInitial<NumberFormat> { NumberFormat.getNumberInstance() }

class BaseUnitPower(val unit: BaseUnit, val pow: Int) : AbstractUnit(), DerivedUnit {
    override fun toString(): String {
        return unit.toString() + String(toSuperScript(pow))
    }

    override fun normalize(): Amount<*> {
        return unit.normalize().power(pow)
    }

    override val formatter = unit.formatter.power(pow)
    
    override val baseComponents: Map<BaseUnit, Int> = ImmutableMap.of(unit, pow)

    override val symbol: String = getSymbol(baseComponents)
}
