package ru.hse.fandomatch.domain.usecase.matches

import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class ApplyFiltersUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository,
) {
    suspend fun execute(
        genders: List<Gender>,
        minAge: Int,
        maxAge: Int,
        categories: List<FandomCategory>,
        fandoms: List<Fandom>,
        onlyInUserCity: Boolean,
    ): Result<Unit> {
        return runCatching {
            val userId = sharedPrefRepository.getUserId()
                ?: throw RuntimeException("User ID not found in shared preferences")
            globalRepository.setFilters(
                userId = userId,
                genders = genders,
                minAge = minAge,
                maxAge = maxAge,
                categories = categories,
                fandoms = fandoms,
                onlyInUserCity = onlyInUserCity,
            )
        }
    }
}