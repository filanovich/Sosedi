package by.imlab.data.network.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class User(
    val token: String? = null,
    val displayName: String? = null
)