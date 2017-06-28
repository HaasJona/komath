package de.komath.units

import java.time.Duration

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
class Amount<U : DerivedUnit>(val value: Double, val unit : U) {

    operator fun plus(other : Amount<U>) : Amount<U> {
        if(unit != other.unit) throw IllegalArgumentException("Incompatible Units: $unit, ${other.unit}")
        return Amount(value + other.value, unit)
    } 
    
    operator fun minus(other : Amount<U>) : Amount<U> {
        if(unit != other.unit) throw IllegalArgumentException("Incompatible Units: $unit, ${other.unit}")
        return Amount(value - other.value, unit)
    } 
        
    operator fun times(other : Amount<*>) : Amount<*> {
        return Amount(value * other.value, unit * other.unit)
    }
        
    operator fun div(other : Amount<*>) : Amount<*> {
        return Amount(value / other.value, unit / other.unit)
    }
        
    operator fun times(other : DerivedUnit) : Amount<*> {
        return Amount(value, unit * other)
    }
        
    operator fun div(other : DerivedUnit) : Amount<*> {
        return Amount(value, unit / other)
    }

    operator fun times(other : Number) : Amount<*> {
        return Amount(value * other.toDouble(), unit)
    }

    operator fun div(other : Number) : Amount<*> {
        return Amount(value / other.toDouble(), unit)
    }

    operator fun unaryMinus() = Amount(-value, unit)
    
    operator fun unaryPlus() = this

    override fun toString(): String {
        return unit.formatter.format(value)
    }

    fun normalize(): Amount<*> {
        return unit.normalize() * value
    }
    
    infix fun <N : DerivedUnit> to(newUnit : N) : Amount<N> {
        val amount = unit.normalize() / newUnit.normalize() * newUnit * value
        if(amount.unit != newUnit) {
            throw IllegalArgumentException("Unit ${amount.unit.normalize().unit} can not be converted to $newUnit")
        }
        @Suppress("UNCHECKED_CAST")
        return amount as Amount<N>
    }

    fun power(pow: Int): Amount<*> {
        return Amount(Math.pow(value, pow.toDouble()), unit.pow(pow))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.let { it::class.java } != this::class.java) return false

        other as Amount<*>

        if (value != other.value) return false
        if (unit != other.unit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + unit.hashCode()
        return result
    }


}

operator fun <U : DerivedUnit> Number.times(unit : U) : Amount<U> {
    return Amount(toDouble(), unit)
}

operator fun <U : DerivedUnit> Number.div(unit : U) : Amount<DerivedUnit> {
    return Amount(toDouble(), unit.pow(-1))
}

fun Amount<Second>.toDuration() = Duration.ofSeconds(value.toLong())

fun main(args: Array<String>) {
    val a = 500 * Kilo(Meter) / (100 * Meter)
    println(a)
    val b = 500 * Kilo(Meter) / Hour
    println(b)
    println(a.normalize())
    println(b.normalize())
    println(500 * Second)
    println(500 * Kilo(Kilo(Kilogram)))

    println(500 * Newton * 200 * Ampere / (200 * Newton * Ampere))
    
    println (500 * Watt * Second to Joule)
    
    println (50 * Meter * Meter to Hectare)
    
}

object Kilo {
    operator fun invoke(unit : BaseUnit) : BaseUnit {
        return unit * 1000.0
    }
}
