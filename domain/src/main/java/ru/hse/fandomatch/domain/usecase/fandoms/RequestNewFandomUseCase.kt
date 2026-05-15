package ru.hse.fandomatch.domain.usecase.fandoms

import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository
import ru.hse.fandomatch.domain.usecase.auth.RefreshAuthUseCase

class RequestNewFandomUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository,
    private val refreshAuthUseCase: RefreshAuthUseCase,
) {
    suspend fun execute(
        name: String,
        category: FandomCategory,
        description: String,
    ): Result<Unit> {
        return runCatching {
            val userId = sharedPrefRepository.getUserId()
                ?: throw RuntimeException("User ID not found in shared preferences")
            refreshAuthUseCase.execute {
                globalRepository.requestNewFandom(
                    userId = userId,
                    name = name,
                    category = category,
                    description = description
                )
            }
        }
    }
}
