package ru.hse.fandomatch.data.mock

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.hse.fandomatch.domain.exception.InvalidCredentialsException
import ru.hse.fandomatch.domain.model.Chat
import ru.hse.fandomatch.domain.model.ChatPreview
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.model.Token
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.repos.GlobalRepository
import java.time.LocalDateTime
import java.time.ZoneOffset

class GlobalRepositoryMock: GlobalRepository {
    var mockChat = Chat(
        chatId = 1L,
        participantId = mockProfileCards[0].id,
        participantName = mockProfileCards[0].name,
        participantAvatarUrl = mockProfileCards[0].avatarUrl,
        messages = listOf(
            Message(
                messageId = 1L,
                isFromThisUser = false,
                content = "Привет! Как дела?",
                timestamp = LocalDateTime.now().minusDays(2).toEpochSecond(ZoneOffset.UTC) * 1000,
            ),
            Message(
                messageId = 2L,
                isFromThisUser = true,
                content = "Привет! Все хорошо, спасибо. А у тебя?",
                timestamp = LocalDateTime.now().minusDays(2).plusMinutes(5)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
            ),
            Message(
                messageId = 3L,
                isFromThisUser = false,
                content = "Тоже отлично! Какие фандомы ты любишь?",
                timestamp = LocalDateTime.now().minusDays(2).plusMinutes(10)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
            ),
            Message(
                messageId = 4L,
                isFromThisUser = true,
                content = "Я обожаю аниме, особенно One Piece! А ты?",
                timestamp = LocalDateTime.now().minusDays(2).plusMinutes(15)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
            ),
            Message(
                messageId = 5L,
                isFromThisUser = false,
                content = "One Piece тоже мой любимый! А до какого момента ты досмотрел?",
                timestamp = LocalDateTime.now().minusDays(2).plusMinutes(20)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
            ),
            Message(
                messageId = 6L,
                isFromThisUser = true,
                content = "Я сейчас на Whole Cake Island. Бедный Санжи :(",
                timestamp = LocalDateTime.now().minusDays(2).plusMinutes(25)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
            ),
            Message(
                messageId = 7L,
                isFromThisUser = false,
                content = "Ооо, понимаю... Удачи тебе там))",
                timestamp = LocalDateTime.now().minusDays(2).plusMinutes(30)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
            ),
            Message(
                messageId = 8L,
                isFromThisUser = false,
                content = "А я аниме уже досмотрела, сейчас читаю мангу",
                timestamp = LocalDateTime.now().minusDays(2).plusMinutes(30)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
            ),
            Message(
                messageId = 9L,
                isFromThisUser = false,
                content = "Но хочу пересмотреть когда-нибудь заново)",
                timestamp = LocalDateTime.now().minusDays(2).plusMinutes(30)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
                imageUrls = listOf("dzimbei", "luffy")
            ),
            Message(
                messageId = 10L,
                isFromThisUser = false,
                content = "Там же столько деталей, которые можно упустить при первом просмотре",
                timestamp = LocalDateTime.now().minusDays(2).plusMinutes(35)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
            ),
            Message(
                messageId = 11L,
                isFromThisUser = false,
                content = "Та же предыстория Санджи многое меняет в восприятии первых серий, где он появляется",
                timestamp = LocalDateTime.now().minusDays(2).plusMinutes(35)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
            ),
            Message(
                messageId = 12L,
                isFromThisUser = false,
                content = "Ну и я просто хочу посмотреть на East Blue Луффи, он такой хаотичный котенок там))",
                timestamp = LocalDateTime.now().minusDays(2).plusMinutes(35)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
            ),
            Message(
                messageId = 13L,
                isFromThisUser = true,
                content = "А может, как-нибудь посмотрим ван пис с начала вместе?",
                timestamp = LocalDateTime.now().minusDays(2).plusMinutes(35)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
            )
        ),
    )

    var mockChatPreviews = MutableStateFlow(
        listOf(
            ChatPreview(
                chatId = 5L,
                participantName = mockProfileCards[4].name,
                participantAvatarUrl = mockProfileCards[4].avatarUrl,
                lastMessage = "Как вы допустили смерть Лидроса на зимке?..",
                isLastMessageFromThisUser = false,
                lastMessageTimestamp = LocalDateTime.now().minusMinutes(25)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
                newMessagesCount = 101,
            ),
            ChatPreview(
                chatId = 3L,
                participantName = mockProfileCards[2].name,
                participantAvatarUrl = mockProfileCards[2].avatarUrl,
                lastMessage = "Привет! Как дела?",
                isLastMessageFromThisUser = false,
                lastMessageTimestamp = LocalDateTime.now().minusMinutes(30)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
                newMessagesCount = 0,
            ),
            ChatPreview(
                chatId = 2L,
                participantName = mockProfileCards[1].name,
                participantAvatarUrl = mockProfileCards[1].avatarUrl,
                lastMessage = "Я тоже люблю аниме!",
                isLastMessageFromThisUser = false,
                lastMessageTimestamp = LocalDateTime.now().minusHours(5)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
                newMessagesCount = 2,
            ),
            ChatPreview(
                chatId = 1L,
                participantName = mockProfileCards[0].name,
                participantAvatarUrl = mockProfileCards[0].avatarUrl,
                lastMessage = mockChat.messages[mockChat.messages.size - 1].content,
                isLastMessageFromThisUser = mockChat.messages[mockChat.messages.size - 1].isFromThisUser,
                lastMessageTimestamp = mockChat.messages[mockChat.messages.size - 1].timestamp,
                newMessagesCount = 0,
            ),
            ChatPreview(
                chatId = 4L,
                participantName = mockProfileCards[3].name,
                participantAvatarUrl = mockProfileCards[3].avatarUrl,
                lastMessage = "Посмотрела новый эпизод Токийского Гуля, очень понравилось!",
                isLastMessageFromThisUser = true,
                lastMessageTimestamp = LocalDateTime.now().minusDays(10)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
                newMessagesCount = 0,
            ),
        )
    )

    override suspend fun getUserInfo(login: String): User? {
        val result = (mockUsers + mockUser).find { it.login == login }
        return result.also {
            Log.d("GlobalRepositoryMock", "getUserInfo: $it")
        }
    }

    override suspend fun login(
        login: String,
        password: String
    ): Token {
        if (login == mockUser.login && password == mockPassword) return mockToken.also {
            Log.d("GlobalRepositoryMock", "login: successful for user $login")
        }
        else throw InvalidCredentialsException().also {
            Log.d("GlobalRepositoryMock", "login: failed for user $login")
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        login: String,
        dateOfBirthMillis: Long,
        gender: Gender,
        avatarByteArray: ByteArray?,
        password: String
    ): Token {
        mockUser = mockUser.copy(
            name = name,
            email = email,
            login = login,
            birthDate = LocalDateTime.ofEpochSecond(
                dateOfBirthMillis / 1000,
                0,
                ZoneOffset.UTC
            ).toLocalDate(),
            gender = gender,
        )
        return mockToken.also {
            Log.d("GlobalRepositoryMock", "register: successful for user $login")
        }
    }

    override suspend fun updateUser(
        name: String?,
        surname: String?,
        email: String?,
        login: String,
        password: String?
    ) {
        Log.d("GlobalRepositoryMock", "updateUser: successful for user $login")
    }

    override suspend fun deleteUser(login: String) {
        Log.d("GlobalRepositoryMock", "deleteUser: successful for user $login")
    }

    override suspend fun checkPassword(
        login: String,
        password: String
    ): Boolean {
        return (login == mockUser.login && password == mockPassword).also {
            Log.d(
                "GlobalRepositoryMock",
                "checkPassword: ${if (it) "valid" else "invalid"} for user $login"
            )
        }
    }

    override suspend fun getSuggestedProfiles(userId: Long, size: Int): List<ProfileCard> {
        return mockProfileCards.shuffled().take(size).also {
            Log.d(
                "GlobalRepositoryMock",
                "getSuggestedProfiles: returned ${it.size} profiles for userId $userId"
            )
        }
    }

    override suspend fun likeOrDislikeProfile(userId: Long, profileId: Long, isLike: Boolean) {
        Log.d("GlobalRepositoryMock", "likeProfile: user $userId ${if (isLike) "" else "dis"}liked profile $profileId")
    }

    override suspend fun subscribeToChatPreviews(
        beforeTimestamp: Long?,
        size: Int
    ): StateFlow<List<ChatPreview>> {
        return mockChatPreviews.also {
            Log.d("GlobalRepositoryMock", "subscribeToChatPreviews: subscribed with size $size")
        }
    }

    override suspend fun loadChatMessages(
        userId: Long,
        beforeTimestamp: Long?,
        size: Int
    ): List<Message> {
        val messages = if (beforeTimestamp == null) {
            mockChat.messages
        } else {
            mockChat.messages.filter { it.timestamp < beforeTimestamp }
        }
        val result = messages
            .sortedByDescending { it.timestamp }
            .take(size)
            .also {
                Log.d(
                    "GlobalRepositoryMock",
                    "loadChatMessages: returned <= ${it.size} messages for userId $userId before $beforeTimestamp"
                )
            }
        return result
    }

    override suspend fun loadChatInfo(userId: Long): Chat {
        Log.d("GlobalRepositoryMock", "loadChatInfo: returned chat info for userId $userId")
        return mockChat
    }

    override suspend fun sendMessage(
        receiverId: Long,
        content: String,
        images: List<ByteArray>,
        timestamp: Long
    ) {
        mockChat = mockChat.copy(
            messages = mockChat.messages + Message(
                messageId = mockChat.messages.size.toLong() + 1,
                isFromThisUser = true,
                content = content,
                imageUrls = images.map { "luffy" }, // todo upload images and get urls
                timestamp = timestamp,
            )
        )

        mockChatPreviews.value = mockChatPreviews.value.map {
            if (it.participantName == mockChat.participantName) {
                it.copy(
                    lastMessage = content,
                    isLastMessageFromThisUser = true,
                    lastMessageTimestamp = timestamp,
                )
            } else it
        }
            .sortedBy {
                -it.lastMessageTimestamp
            }
    }

    override suspend fun getFeedPosts(beforeTimestamp: Long?, size: Int): List<Post> {
        val posts = if (beforeTimestamp == null) {
            mockPosts
        } else {
            mockPosts.filter { it.timestamp < beforeTimestamp }
        }
        val result = posts
            .sortedByDescending { it.timestamp }
            .take(size)
            .also {
                Log.d(
                    "GlobalRepositoryMock",
                    "loadPosts: returned <= ${it.size} posts before $beforeTimestamp"
                )
            }
        return result
    }
}
