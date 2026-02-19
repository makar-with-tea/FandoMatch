package ru.hse.fandomatch.domain.usecase.feed

import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.repos.GlobalRepository

class GetFeedUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(): List<Post> {
        return globalRepository.getFeedPosts(
            beforeTimestamp = null,
            size = 100500, // todo
        )
    }
}
