package ru.hse.fandomatch.domain.exception

class LoginAlreadyInUseException: RuntimeException("Login is already in use") {
    override val message: String
        get() = "The provided login is already in use. Please choose a different login."
}
