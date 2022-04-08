package by.imlab.data.state

sealed class DataFetchState<out T> {

    object Pending : DataFetchState<Nothing>()
    object Loading : DataFetchState<Nothing>()
    data class Success<out T>(val data: T) : DataFetchState<T>()
    data class Error(val throwable: Throwable) : DataFetchState<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Pending -> "Pending"
            is Loading -> "Loading"
            is Success<*> -> "Success[data=${data}]"
            is Error -> "Error[throwable=$throwable]"
        }
    }
}