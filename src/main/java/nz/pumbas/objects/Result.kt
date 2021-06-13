package nz.pumbas.objects

import nz.pumbas.resources.Resource
import java.util.function.Consumer

data class Result<T>(val value: T?, val reason: Resource?)
{
    companion object {
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
    }

    fun hasReason(): Boolean {
        return null != reason
    }

    fun hasValue(): Boolean {
        return null != value
    }

    fun isResourceAbsent(): Boolean {
        return null == reason
    }

    fun isValueAbsent(): Boolean {
        return null == value
    }

    fun ifReasonPresent(consumer: Consumer<Resource>): Result<T> {
        reason?.let { consumer.accept(it) }
        return this
    }

    fun ifValuePresent(consumer: Consumer<T>): Result<T> {
        value?.let { consumer.accept(it) }
        return this
    }
}