package ru.hse.fandomatch.domain.usecase.fandoms

import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class RequestNewFandomUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository,
) {
    suspend fun execute(
        name: String,
        category: FandomCategory,
        description: String,
    ): Result<Unit> {
        return runCatching {
            throw RuntimeException("wiwiwi")
            val userId = sharedPrefRepository.getUserId() ?: throw RuntimeException("User ID not found in shared preferences")
            globalRepository.requestNewFandom(
                userId = userId,
                name = name,
                category = category,
                description = description
            )
        }
    }
}
