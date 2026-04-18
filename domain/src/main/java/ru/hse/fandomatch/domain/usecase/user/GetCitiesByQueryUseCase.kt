package ru.hse.fandomatch.domain.usecase.user

import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.repos.GlobalRepository

class GetCitiesByQueryUseCase(
    private val globalRepository: GlobalRepository
) {
    suspend fun execute(query: String): Result<List<City>> {
        return runCatching {
            globalRepository.getCitiesByQuery(query)
        }
    }
}
