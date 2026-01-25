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
        avatarByteArray: ByteArray?,
        password: String
    ) {
        // todo use all info
        val res = globalRepository.register(
            name = name,
            email = email,
            login = login,
            dateOfBirthMillis = dateOfBirthMillis,
            gender = gender,
            avatarByteArray = avatarByteArray,
            password = password
        )
        sharedPrefRepository.saveUser(login)
        sharedPrefRepository.saveToken(res.accessToken)
        sharedPrefRepository.saveRefreshToken(res.refreshToken)
    }
}
