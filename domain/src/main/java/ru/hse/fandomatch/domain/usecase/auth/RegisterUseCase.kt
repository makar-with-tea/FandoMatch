package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class RegisterUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository
) {
    suspend fun execute(
        name: String,
        email: String,
        login: String,
        dateOfBirthMillis: Long,
        gender: Gender,
        password: String
    ): Result<Unit> = runCatching {
        val res = globalRepository.register(
            name = name,
            email = email,
            login = login,
            dateOfBirthMillis = dateOfBirthMillis,
            gender = gender,
            password = password
        )
        sharedPrefRepository.saveUserId(res.userId)
        sharedPrefRepository.saveToken(res.accessToken)
        sharedPrefRepository.saveRefreshToken(res.refreshToken)
    }
}
