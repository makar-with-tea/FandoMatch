package ru.hse.fandomatch.domain.exception

class EmailAlreadyInUseException: RuntimeException("Email is already in use") {
    override val message: String
        get() = "The provided email is already in use. Please choose a different email."
}
