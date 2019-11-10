package se.allco.githubbrowser.common.utils

/**
 * Utility class for sending nullables through the Rx emitters (onNext takes only non-null)
 */
sealed class Optional<out T> {
    data class Some<out T>(val element: T) : Optional<T>()
    object None : Optional<Nothing>() {
        override fun toString(): String = "Optional.None " + super.toString()
    }

    companion object {
        fun <T> of(value: T): Optional<T> = Some(value)
        fun <T> none(): Optional<T> = None
    }

    fun asNullable(): T? =
        when (this) {
            is Some<T> -> element
            else -> null
        }

    fun elementOrThrow(message: () -> String): T =
        when (this) {
            is Some<T> -> element
            else -> throw IllegalStateException(message())
        }
}

fun <T> T?.asOptional(): Optional<T> = when {
    this != null -> Optional.Some(this)
    else -> Optional.None
}
