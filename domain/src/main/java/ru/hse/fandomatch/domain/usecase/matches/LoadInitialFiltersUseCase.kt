package ru.hse.fandomatch.domain.usecase.matches

import ru.hse.fandomatch.domain.model.Filters
import ru.hse.fandomatch.domain.repos.GlobalRepository

class LoadInitialFiltersUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(): Result<Filters> {
        return runCatching {
            globalRepository.getCurrentFilters()
        }
    }
}
