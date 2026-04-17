package ru.hse.fandomatch.domain.usecase.fandoms

import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.repos.GlobalRepository

class GetFandomsByQueryUseCase(
    private val globalRepository: GlobalRepository
) {
    suspend fun execute(query: String): Result<List<Fandom>> {
        return runCatching {
            globalRepository.getFandomsByQuery(query)
        }
    }
}