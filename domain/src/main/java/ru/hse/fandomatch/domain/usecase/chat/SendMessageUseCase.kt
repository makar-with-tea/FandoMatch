package ru.hse.fandomatch.domain.usecase.chat

import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.repos.GlobalRepository

class SendMessageUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(
        userId: String,
        content: String,
        mediaIdsWithTypes: List<Pair<String, MediaType>>,
        timestamp: Long
    ): Result<Unit> {
        return runCatching {
            globalRepository.sendMessage(
                receiverId = userId,
                content = content,
                mediaIdsWithTypes = mediaIdsWithTypes,
                timestamp = timestamp,
            )
        }
    }
}
