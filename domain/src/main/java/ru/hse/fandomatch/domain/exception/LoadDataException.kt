package ru.hse.fandomatch.domain.exception

class LoadDataException: RuntimeException("Failed to load data") {
    override val message: String
        get() = "Failed to load data. Please check your internet connection and authorization status" +
                " or try again."
}
