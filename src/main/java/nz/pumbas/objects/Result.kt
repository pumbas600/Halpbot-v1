package nz.pumbas.objects

import nz.pumbas.resources.Resource

data class Result<T>(val value: T?, val reason: Resource?)
{
    companion object {
        private val Empty = of(null)

        @JvmStatic
        fun <T> of(value: T?, reason: Resource?) : Result<T> {
            return Result(value, reason)
        }

        @JvmStatic
        fun <T> of(value: T?) : Result<T> {
            return Result(value, null)
        }

        @JvmStatic
        fun <T> of(reason: Resource) : Result<T> {
            return Result(null, reason)
        }

        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <T> empty(): Result<T> {
            return this.Empty as Result<T>;
        }
    }

    fun hasReason(): Boolean {
        return null != this.reason
    }

    fun hasValue(): Boolean {
        return null != this.value
    }

    fun isResourceAbsent(): Boolean {
        return null == this.reason
    }

    fun isValueAbsent(): Boolean {
        return null == this.value
    }

    fun ifReasonPresent(consumer: (Resource) -> Unit): Result<T> {
        reason?.let { consumer(it) }
        return this
    }

    fun ifValuePresent(consumer: (T) -> Unit): Result<T> {
        value?.let { consumer(it) }
        return this
    }

    fun isEmpty(): Boolean {
        return !this.hasValue() && !this.hasReason()
    }

    /**
     * If this [Result] is empty, it returns the [Result] in the supplier, otherwise it returns itself.
     */
    fun ifEmpty(supplier: () -> Result<T>): Result<T> {
        return if (this.isEmpty()) supplier() else this
    }

    /**
     * If this [Result] is empty, it returns the alternative [Result], otherwise it returns itself.
     */
    fun ifEmpty(alternative: Result<T>): Result<T> {
        return if (this.isEmpty()) alternative else this
    }

    fun orElse(value: T): T {
        return this.value ?: value
    }

    fun <K> map(mapper: (T?) -> K?): Result<K> {
        return of(mapper(this.value), this.reason)
    }

    fun <K> mapResult(mapper: (Result<T>) -> Result<K>): Result<K> {
        return mapper(this)
    }
}