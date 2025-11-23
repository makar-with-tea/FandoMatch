package ru.hse.fandomatch.domain.usecase.user

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
        avatarUri: String?,
        password: String
    ) {
        val res = globalRepository.register(name, email, login, password)
        sharedPrefRepository.saveUser(login)
        sharedPrefRepository.saveToken(res.accessToken)
        sharedPrefRepository.saveRefreshToken(res.refreshToken)
    }
}
