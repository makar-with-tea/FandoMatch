package ru.hse.fandomatch.domain.exception

class AuthRefreshRequiredException: RuntimeException("Authentication refresh is required") {
    override val message: String
        get() = "Authentication refresh is required"
}
