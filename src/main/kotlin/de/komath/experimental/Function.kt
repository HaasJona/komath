package de.komath.experimental

    abstract class PartialFunction<in A, B> : (A) -> B {
        abstract fun isDefinedAt(x: A): Boolean

        fun <A1 : A, B1 : B> orElse(that: PartialFunction<A1, B1>): PartialFunction<A1, B> =
                object : PartialFunction<A1, B>() {
                    override fun isDefinedAt(x: A1): Boolean =
                            this@PartialFunction.isDefinedAt(x) || that.isDefinedAt(x)

                    override fun invoke(x: A1): B =
                            if (this@PartialFunction.isDefinedAt(x)) {
                                this@PartialFunction(x)
                            } else {
                                that(x)
                            }
                }

        fun <C> andThen(k: (B) -> C): PartialFunction<A, C> = object : PartialFunction<A, C>() {
            override fun isDefinedAt(x: A): Boolean = this@PartialFunction.isDefinedAt(x)
            override fun invoke(x: A): C = k(this@PartialFunction(x))
        }
    }