package ru.hse.fandomatch.domain.usecase.chat

import ru.hse.fandomatch.domain.repos.GlobalRepository

class SendMessageUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(
        userId: String,
        content: String,
        images: List<ByteArray>,
        timestamp: Long
    ): Result<Unit> {
        return runCatching {
            globalRepository.sendMessage(
                receiverId = userId,
                content = content,
                images = images,
                timestamp = timestamp,
            )
        }
    }
}
