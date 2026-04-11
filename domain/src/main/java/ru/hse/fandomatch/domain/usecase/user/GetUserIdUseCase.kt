package ru.hse.fandomatch.domain.usecase.user

import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class GetUserIdUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository
) {
    suspend fun execute(): String? {
        val savedId = sharedPrefRepository.getUserId()
        savedId?.let {
            return it
        }
        val login = sharedPrefRepository.getUser() ?: return null
        val id = globalRepository.getUserInfo(login)?.id
        id?.let {
            sharedPrefRepository.saveUserId(it)
        }
        return id
    }
}
