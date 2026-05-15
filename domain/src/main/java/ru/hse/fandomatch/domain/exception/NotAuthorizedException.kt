package ru.hse.fandomatch.domain.exception

class NotAuthorizedException: RuntimeException("Not authorized") {
    override val message: String
        get() = "Not authorized"
}
