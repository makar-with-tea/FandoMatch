package ru.hse.fandomatch.domain.usecase.fandoms

import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.repos.GlobalRepository

class RequestNewFandomUseCase(
    private val globalRepository: GlobalRepository
) {
    suspend fun execute(
        userId: String,
        name: String,
        category: FandomCategory,
        description: String,
    ): Result<Unit> {
        return runCatching {
            globalRepository.requestNewFandom(
                userId = userId,
                name = name,
                category = category,
                description = description
            )
        }
    }
}