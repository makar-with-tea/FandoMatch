package ru.hse.fandomatch.domain.usecase.matches

import ru.hse.fandomatch.domain.model.Filters
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.usecase.auth.RefreshAuthUseCase

class LoadInitialFiltersUseCase(
    private val globalRepository: GlobalRepository,
    private val refreshAuthUseCase: RefreshAuthUseCase,
) {
    suspend fun execute(): Result<Filters> {
        return runCatching {
            refreshAuthUseCase.execute {
                globalRepository.getCurrentFilters()
            }
        }
    }
}
