package by.imlab.data.model

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
//sealed class Result<out T : Any> {
sealed class Result {
    data class Success<out T : Any>(val data: T?) : Result()
    data class Error(val message: String?) : Result()

//    data class Success<out T : Any>(val data: T) : Result<T>()
//    data class Error(val message: String?) : Result<String>()

//    override fun toString(): String {
//        return when (this) {
//            is Success<*> -> "Success[data=$data]"
//            is Error -> "Error[exception=$message]"
//        }
//    }
}