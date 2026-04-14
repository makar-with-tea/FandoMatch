package ru.hse.fandomatch.data.mock

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.hse.fandomatch.domain.exception.InvalidCredentialsException
import ru.hse.fandomatch.domain.model.Chat
import ru.hse.fandomatch.domain.model.ChatPreview
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.domain.model.AuthInfo
import ru.hse.fandomatch.domain.model.OtherProfileItem
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.repos.GlobalRepository
import java.time.LocalDateTime
import java.time.ZoneOffset

class GlobalRepositoryMock: GlobalRepository {
    var mockChat = Chat(
        chatId = "1",
        participantId = mockProfileCards[0].id,
        participantName = mockProfileCards[0].name,
        participantAvatarUrl = mockProfileCards[0].avatarUrl
    )
    val mockMessages: MutableStateFlow<List<Message>> = MutableStateFlow(listOf(
        Message(
            messageId = "1",
            isFromThisUser = false,
            content = "Привет! Как дела?",
            timestamp = LocalDateTime.now().minusDays(2).toEpochSecond(ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = "2",
            isFromThisUser = true,
            content = "Привет! Все хорошо, спасибо. А у тебя?",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(5)
                .toEpochSecond(ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = "3",
            isFromThisUser = false,
            content = "Тоже отлично! Какие фандомы ты любишь?",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(10)
                .toEpochSecond(ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = "4",
            isFromThisUser = true,
            content = "Я обожаю аниме, особенно One Piece! А ты?",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(15)
                .toEpochSecond(ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = "5",
            isFromThisUser = false,
            content = "One Piece тоже мой любимый! А до какого момента ты досмотрел?",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(20)
                .toEpochSecond(ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = "6",
            isFromThisUser = true,
            content = "Я сейчас на Whole Cake Island. Бедный Санжи :(",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(25)
                .toEpochSecond(ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = "7",
            isFromThisUser = false,
            content = "Ооо, понимаю... Удачи тебе там))",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(30)
                .toEpochSecond(ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = "8",
            isFromThisUser = false,
            content = "А я аниме уже досмотрела, сейчас читаю мангу",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(30)
                .toEpochSecond(ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = "9",
            isFromThisUser = false,
            content = "Но хочу пересмотреть когда-нибудь заново)",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(30)
                .toEpochSecond(ZoneOffset.UTC) * 1000,
            imageUrls = listOf("dzimbei", "luffy")
        ),
        Message(
            messageId = "10",
            isFromThisUser = false,
            content = "Там же столько деталей, которые можно упустить при первом просмотре",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(35)
                .toEpochSecond(ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = "11",
            isFromThisUser = false,
            content = "Та же предыстория Санджи многое меняет в восприятии первых серий, где он появляется",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(35)
                .toEpochSecond(ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = "12",
            isFromThisUser = false,
            content = "Ну и я просто хочу посмотреть на East Blue Луффи, он такой хаотичный котенок там))",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(35)
                .toEpochSecond(ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = "13",
            isFromThisUser = true,
            content = "А может, как-нибудь посмотрим ван пис с начала вместе?",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(35)
                .toEpochSecond(ZoneOffset.UTC) * 1000,
        )
    )
    )

    var mockChatPreviews = MutableStateFlow(
        listOf(
            ChatPreview(
                chatId = "5",
                participantName = mockProfileCards[4].name,
                participantAvatarUrl = mockProfileCards[4].avatarUrl,
                lastMessage = "Как вы допустили смерть Лидроса на зимке?..",
                isLastMessageFromThisUser = false,
                lastMessageTimestamp = LocalDateTime.now().minusMinutes(25)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
                newMessagesCount = 101,
            ),
            ChatPreview(
                chatId = "3",
                participantName = mockProfileCards[2].name,
                participantAvatarUrl = mockProfileCards[2].avatarUrl,
                lastMessage = "Привет! Как дела?",
                isLastMessageFromThisUser = false,
                lastMessageTimestamp = LocalDateTime.now().minusMinutes(30)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
                newMessagesCount = 0,
            ),
            ChatPreview(
                chatId = "2",
                participantName = mockProfileCards[1].name,
                participantAvatarUrl = mockProfileCards[1].avatarUrl,
                lastMessage = "Я тоже люблю аниме!",
                isLastMessageFromThisUser = false,
                lastMessageTimestamp = LocalDateTime.now().minusHours(5)
                    .toEpochSecond(ZoneOffset.UTC) * 1000,
                newMessagesCount = 2,
            ),
            ChatPreview(
                chatId = "1",
                participantName = mockProfileCards[0].name,
                participantAvatarUrl = mockProfileCards[0].avatarUrl,
                lastMessage = mockMessages.value[mockMessages.value.size - 1].content,
                isLastMessageFromThisUser = mockMessages.value[mockMessages.value.size - 1].isFromThisUser,
                lastMessageTimestamp = mockMessages.value[mockMessages.value.size - 1].timestamp,
                newMessagesCount = 0,
            ),
            ChatPreview(
                chatId = "4",
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

    override suspend fun getUser(profileId: String): User? {
        val result = (mockUsers + mockUser).find { it.id == profileId } // todo
        return result.also {
            Log.d("GlobalRepositoryMock", "getUser: $it")
        }
    }

    override suspend fun login(
        login: String,
        password: String
    ): AuthInfo {
        if (login == (mockUser.profileType as ProfileType.Own).login && password == mockPassword)
            return mockAuthInfo.also {
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
    ): AuthInfo {
        mockUser = mockUser.copy(
            name = name,
            age = ((System.currentTimeMillis() - dateOfBirthMillis) / (1000L * 60 * 60 * 24 * 365)).toInt(),
            gender = gender,
            profileType = ProfileType.Own(
                email = email,
                login = login,
            )
        )
        return mockAuthInfo.also {
            Log.d("GlobalRepositoryMock", "register: successful for user $login")
        }
    }

    override suspend fun updateUser(
        name: String,
        bio: String?,
        gender: Gender,
        city: City,
        avatarUrl: String?,
        backgroundUrl: String?
    ) {
        Log.d("GlobalRepositoryMock", "updateUser: successful for user $name")
    }

    override suspend fun deleteUser(login: String) {
        Log.d("GlobalRepositoryMock", "deleteUser: successful for user $login")
    }

    override suspend fun checkPassword(
        login: String,
        password: String
    ): Boolean {
        return (login == (mockUser.profileType as ProfileType.Own).login && password == mockPassword).also {
            Log.d(
                "GlobalRepositoryMock",
                "checkPassword: ${if (it) "valid" else "invalid"} for user $login"
            )
        }
    }

    override suspend fun getFriends(): List<OtherProfileItem> {
        return mockUsers
            .filter { it.profileType is ProfileType.Friend }
            .map {
                OtherProfileItem(
                    id = it.id,
                    name = it.name,
                    login = (it.profileType as ProfileType.Friend).login,
                    avatarUrl = it.avatarUrl,
                )
            }
            .also {
                Log.d(
                    "GlobalRepositoryMock",
                    "getFriends: returned ${it.size} friends"
                )
            }
    }

    override suspend fun getFriendRequests(): List<OtherProfileItem> {
        return mockUsers
            .filter { it.profileType is ProfileType.Stranger }
            .map {
                OtherProfileItem(
                    id = it.id,
                    name = it.name,
                    login = null,
                    avatarUrl = it.avatarUrl,
                )
            }
            .also {
                Log.d(
                    "GlobalRepositoryMock",
                    "getFriendRequests: returned ${it.size} friend requests"
                )
            }
    }

    override suspend fun getVerificationCode(email: String) {
        Log.d("GlobalRepositoryMock", "getVerificationCode: sent code to email $email")
    }

    override suspend fun checkVerificationCode(code: String): Boolean {
        return (code == mockVerificationCode).also {
            Log.d(
                "GlobalRepositoryMock",
                "checkVerificationCode: ${if (it) "valid" else "invalid"} code $code"
            )
        }
    }

    override suspend fun resetPassword(code: String, newPassword: String) {
        if (code == mockVerificationCode) {
            mockPassword = newPassword
            Log.d("GlobalRepositoryMock", "resetPassword: password reset successful with code $code")
        } else {
            Log.d("GlobalRepositoryMock", "resetPassword: invalid code $code")
            throw IllegalArgumentException("Invalid verification code")
        }
    }

    override suspend fun getSuggestedProfiles(size: Int): List<ProfileCard> {
        return mockProfileCards.shuffled().take(size).also {
            Log.d(
                "GlobalRepositoryMock",
                "getSuggestedProfiles: returned ${it.size} profiles"
            )
        }
    }

    override suspend fun likeOrDislikeProfile(userId: String, isLike: Boolean) {
        Log.d(
            "GlobalRepositoryMock",
            "likeProfile: ${if (isLike) "" else "dis"}liked profile $userId"
        )
    }

    override suspend fun setFilters(
        userId: String,
        genders: List<Gender>,
        minAge: Int?,
        maxAge: Int?,
        categories: List<FandomCategory>,
        fandoms: List<Fandom>,
        onlyInUserCity: Boolean
    ) {
        mockFilters = mockFilters.copy(
            genders = genders,
            minAge = minAge ?: mockFilters.minAge,
            maxAge = maxAge ?: mockFilters.maxAge,
            categories = categories,
            fandoms = fandoms,
            onlyInUserCity = onlyInUserCity,
        )
    }

    override suspend fun subscribeToChatPreviews(
        beforeTimestamp: Long?,
        size: Int
    ): StateFlow<List<ChatPreview>> {
        return mockChatPreviews.also {
            Log.d("GlobalRepositoryMock", "subscribeToChatPreviews: subscribed with size $size")
        }
    }

    override suspend fun subscribeToChatMessages(
        userId: String,
        beforeTimestamp: Long?,
        size: Int
    ): StateFlow<List<Message>> {
//        val messages = if (beforeTimestamp == null) {
//            mockMessages.value
//        } else {
//            mockMessages.value.filter { it.timestamp < beforeTimestamp }
//        }
//        val result = messages
//            .sortedByDescending { it.timestamp }
//            .take(size)
//            .also {
//                Log.d(
//                    "GlobalRepositoryMock",
//                    "loadChatMessages: returned <= ${it.size} messages for userId $userId before $beforeTimestamp"
//                )
//            }
//        mockMessages.value = result todo пагинация
        return mockMessages
    }

    override suspend fun loadChatInfo(userId: String): Chat {
        Log.d("GlobalRepositoryMock", "loadChatInfo: returned chat info for userId $userId")
        return mockChat
    }

    override suspend fun sendMessage(
        receiverId: String,
        content: String,
        images: List<ByteArray>,
        timestamp: Long
    ) {
        mockMessages.value += Message(
                    messageId = (mockMessages.value.size + 1).toString(),
                    isFromThisUser = true,
                    content = content,
                    imageUrls = images.map { "luffy" }, // todo upload images and get urls
                    timestamp = timestamp,
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

    override suspend fun getUserPosts(
        userId: String,
        beforeTimestamp: Long?,
        size: Int
    ): List<Post> {
        val user = (mockUsers + mockUser).find { it.id == userId } ?: return emptyList()
        val type = user.profileType
        val userPosts = (mockPosts + mockUserPosts).filter { it.authorId == userId }
        var posts = if (beforeTimestamp == null) {
            userPosts
        } else {
            userPosts.filter { it.timestamp < beforeTimestamp }
        }
        if (type is ProfileType.Stranger) {
            posts = posts.map { it.copy(authorLogin = null) }
        }
        return posts
    }

    override suspend fun getFandomCategories(): List<FandomCategory> {
        return FandomCategory.entries
    }

    override suspend fun getFandomsByQuery(query: String): List<Fandom> {
        return mockFandoms.filter { it.name.contains(query, ignoreCase = true) }
    }
}
