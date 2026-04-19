package ru.hse.fandomatch.data.mock

import kotlinx.coroutines.flow.MutableStateFlow
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Filters
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.domain.model.AuthInfo
import ru.hse.fandomatch.domain.model.Chat
import ru.hse.fandomatch.domain.model.ChatPreview
import ru.hse.fandomatch.domain.model.Comment
import ru.hse.fandomatch.domain.model.MediaItem
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.model.UserPreferences
import java.time.LocalDateTime
import java.time.ZoneOffset

internal val mockFandoms = listOf(
    Fandom(
        id = "1",
        name = "One Piece",
        category = FandomCategory.ANIME_MANGA,
    ),
    Fandom(
        id = "2",
        name = "Но-Энор",
        category = FandomCategory.OTHER,
    ),
    Fandom(
        id = "3",
        name = "My Chemical Romance",
        category = FandomCategory.MUSIC,
    ),
    Fandom(
        id = "4",
        name = "Ведьмак",
        category = FandomCategory.BOOKS,
    ),
    Fandom(
        id = "5",
        name = "Утиные истории",
        category = FandomCategory.CARTOONS,
    ),
    Fandom(
        id = "6",
        name = "Бэтмен",
        category = FandomCategory.COMICS,
    ),
    Fandom(
        id = "7",
        name = "Star Wars",
        category = FandomCategory.TV_SERIES,
    ),
    Fandom(
        id = "8",
        name = "Dungeons and Dragons",
        category = FandomCategory.TABLETOP_GAMES,
    ),
    Fandom(
        id = "9",
        name = "The Beatles",
        category = FandomCategory.MUSIC,
    ),
    Fandom(
        id = "10",
        name = "Стража! Стража!",
        category = FandomCategory.BOOKS,
    ),
    Fandom(
        id = "11",
        name = "Легенды Олимпа",
        category = FandomCategory.MYTHOLOGY,
    ),
    Fandom(
        id = "12",
        name = "Волкодав",
        category = FandomCategory.BOOKS,
    )
)

internal fun String.getMediaByName() : MediaItem {
    return when (this) {
        "noenor_edit" -> MediaItem(
            id = this,
            url = "https://github.com/makar-with-tea/FandoMatch/blob/develop/app/src/main/res/raw/noenor_edit.mp4?raw=true",
            mediaType = MediaType.VIDEO,
        )

        "video" -> MediaItem(
            id = this,
            url = "https://media.geeksforgeeks.org/wp-content/uploads/20201217163353/Screenrecorder-2020-12-17-16-32-03-350.mp4",
            mediaType = MediaType.VIDEO,
        )

        "luffy", "lidros" -> MediaItem(
            id = this,
            url = "https://github.com/makar-with-tea/FandoMatch/blob/main/app/src/main/res/raw/$this.png?raw=true",
            mediaType = MediaType.IMAGE,
        )

        else -> MediaItem(
            id = this,
            url = "https://github.com/makar-with-tea/FandoMatch/blob/main/app/src/main/res/raw/$this.jpg?raw=true",
            mediaType = MediaType.IMAGE,
        )
    }
}

internal val mockProfileCards = listOf(
    ProfileCard(
        id = "12345",
        fandoms = mockFandoms,
        description = """
            Я люблю аниме и музыку :3
            А еще длинные описания))
            Особенно такие, многострочные, чтобы в высоту много места занимали. Но и в ширину тоже можно, я не против, конечно.
            Так вот, вы не хотите поговорить о ван писе? Там пираты, приключения и свержение коррумпированного мирового правительства, здорово, правда?
        """.trimIndent(),
        name = "Алиса",
        gender = Gender.FEMALE,
        avatar = "luffy".getMediaByName(),
        age = 21,
        compatibilityPercentage = 95,
        city = City(
            nameRussian = "Москва",
            nameEnglish = "Moscow",
        ),
    ),
    ProfileCard(
        id = "67890",
        fandoms = listOf(mockFandoms[0]),
        description = "Пират в поисках своего личного приключения.",
        name = "Пользователь",
        gender = Gender.MALE,
        avatar = "peace_was_never_an_option".getMediaByName(),
        age = 17,
        compatibilityPercentage = 80,
        city = City(
            nameRussian = "Москва",
            nameEnglish = "Moscow",
        ),
    ),
    ProfileCard(
        id = "11223",
        fandoms = listOf(mockFandoms[2]),
        description = "Обожаю рок-музыку и долгие прогулки по ночному городу.",
        name = "Лесное нечто",
        gender = Gender.NOT_SPECIFIED,
        avatar = "pet_the_forbidden_dog".getMediaByName(),
        age = 25,
        compatibilityPercentage = 70,
        city = null,
    ),
    ProfileCard(
        id = "44556",
        fandoms = listOf(),
        description = "Просто загадочный человек.",
        name = "Дана",
        gender = Gender.FEMALE,
        avatar = "ne_poluchaetsya".getMediaByName(),
        age = 30,
        compatibilityPercentage = 60,
        city = null,
    ),
    ProfileCard(
        id = "44557",
        fandoms = listOf(mockFandoms[1]),
        description = "Пожалуйста, давайте поговорим о Лидросе",
        name = "Но-энор",
        gender = Gender.FEMALE,
        avatar = "lidros".getMediaByName(),
        age = 21,
        compatibilityPercentage = 60,
        city = City(
            nameRussian = "Санкт-Петербург",
            nameEnglish = "Saint Petersburg",
        ),
    ),
)
internal var mockChat = Chat(
    chatId = "1",
    participantId = mockProfileCards[0].id,
    participantName = mockProfileCards[0].name,
    participantAvatarUrl = mockProfileCards[0].avatar?.url,
)
internal val mockMessages: MutableStateFlow<List<Message>> = MutableStateFlow(listOf(
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
        mediaItems = listOf("video", "luffy").map { it.getMediaByName() },
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

internal var mockChatPreviews = MutableStateFlow(
    listOf(
        ChatPreview(
            chatId = "5",
            participantName = mockProfileCards[4].name,
            participantAvatarUrl = mockProfileCards[4].avatar?.url,
            lastMessage = "Как вы допустили смерть Лидроса на зимке?..",
            isLastMessageFromThisUser = false,
            lastMessageTimestamp = LocalDateTime.now().minusMinutes(25)
                .toEpochSecond(ZoneOffset.UTC) * 1000,
            newMessagesCount = 101,
        ),
        ChatPreview(
            chatId = "3",
            participantName = mockProfileCards[2].name,
            participantAvatarUrl = mockProfileCards[2].avatar?.url,
            lastMessage = "Привет! Как дела?",
            isLastMessageFromThisUser = false,
            lastMessageTimestamp = LocalDateTime.now().minusMinutes(30)
                .toEpochSecond(ZoneOffset.UTC) * 1000,
            newMessagesCount = 0,
        ),
        ChatPreview(
            chatId = "2",
            participantName = mockProfileCards[1].name,
            participantAvatarUrl = mockProfileCards[1].avatar?.url,
            lastMessage = "Я тоже люблю аниме!",
            isLastMessageFromThisUser = false,
            lastMessageTimestamp = LocalDateTime.now().minusHours(5)
                .toEpochSecond(ZoneOffset.UTC) * 1000,
            newMessagesCount = 2,
        ),
        ChatPreview(
            chatId = "1",
            participantName = mockProfileCards[0].name,
            participantAvatarUrl = mockProfileCards[0].avatar?.url,
            lastMessage = mockMessages.value[mockMessages.value.size - 1].content,
            isLastMessageFromThisUser = mockMessages.value[mockMessages.value.size - 1].isFromThisUser,
            lastMessageTimestamp = mockMessages.value[mockMessages.value.size - 1].timestamp,
            newMessagesCount = 0,
        ),
        ChatPreview(
            chatId = "4",
            participantName = mockProfileCards[3].name,
            participantAvatarUrl = mockProfileCards[3].avatar?.url,
            lastMessage = "Посмотрела новый эпизод Токийского Гуля, очень понравилось!",
            isLastMessageFromThisUser = true,
            lastMessageTimestamp = LocalDateTime.now().minusDays(10)
                .toEpochSecond(ZoneOffset.UTC) * 1000,
            newMessagesCount = 0,
        ),
    )
)

internal var mockUser = User(
    id = "1",
    fandoms = mockFandoms,
    description = "Just some guy. But this guy has a long bio! Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Or whatever. But I don't want it to be too long, though. I need some restrictions hm.",
    name = "John",
    gender = Gender.MALE,
    age = 25,
    avatar = "dzimbei".getMediaByName(),
    background = "peace_was_never_an_option".getMediaByName(),
    city = City(
        nameRussian = "Москва",
        nameEnglish = "Moscow",
    ),
    profileType = ProfileType.Own(
        login = "johndoe",
        email = "johndoe@email.com",
    )
)

internal var mockUserPosts = listOf(
    Post(
        id = "5",
        authorId = mockUser.id,
        authorName = mockUser.name,
        authorLogin = (mockUser.profileType as ProfileType.Own).login,
        authorAvatar = mockUser.avatar,
        content = "Вау, я умею писать посты!",
        mediaItems = listOf("dzimbei".getMediaByName()),
        likeCount = 100,
        commentCount = 50,
        isLikedByCurrentUser = false,
        timestamp = System.currentTimeMillis() - 3600_000, // 1 hour ago
        fandoms = listOf(mockFandoms[0], mockFandoms[2], mockFandoms[4])
    ),
    Post(
        id = "6",
        authorId = mockUser.id,
        authorName = mockUser.name,
        authorLogin = (mockUser.profileType as ProfileType.Own).login,
        authorAvatar = mockUser.avatar,
        content = "Еще один пост от меня.",
        mediaItems = listOf(),
        likeCount = 150,
        commentCount = 75,
        isLikedByCurrentUser = false,
        timestamp = System.currentTimeMillis() - 7200_000, // 2 hours ago
        fandoms = listOf(mockFandoms[0], mockFandoms[1])
    ),
    Post(
        id = "7",
        authorId = mockUser.id,
        authorName = mockUser.name,
        authorLogin = (mockUser.profileType as ProfileType.Own).login,
        authorAvatar = mockUser.avatar,
        content = "Люблю это приложение!",
        mediaItems = listOf("what_is_written_here").map { it.getMediaByName() },
        likeCount = 200,
        commentCount = 100,
        isLikedByCurrentUser = true,
        timestamp = System.currentTimeMillis() - 10800_000, // 3 hours ago
        fandoms = listOf(mockFandoms[2], mockFandoms[3], mockFandoms[4])
    )
)

val mockComments = listOf(
    Comment(
        authorName = "Алиса",
        authorLogin = "alice",
        authorAvatar = "luffy".getMediaByName(),
        timestamp = System.currentTimeMillis() - 1800_000, // 30 minutes ago
        content = "Отличный пост! Я тоже люблю аниме и музыку!"
    ),
    Comment(
        authorName = "Пользователь",
        authorLogin = "pirate123",
        authorAvatar = "peace_was_never_an_option".getMediaByName(),
        timestamp = System.currentTimeMillis() - 1200_000, // 20 minutes ago
        content = "Приветствую! Рада видеть новых людей, которые любят аниме! Какие у вас любимые серии?"
    ),
    Comment(
        authorName = "Лесное нечто",
        authorLogin = "forestentity",
        authorAvatar = "pet_the_forbidden_dog".getMediaByName(),
        timestamp = System.currentTimeMillis() - 600_000, // 10 minutes ago
        content = "Привет! Я тоже обожаю аниме, особенно One Piece"
    ),
)

var mockPosts = listOf(
    Post(
        id = "1",
        authorId = "12345",
        authorName = "Алиса",
        authorLogin = "alice",
        authorAvatar = "luffy".getMediaByName(),
        content = "Привет! Я только что зарегистрировалась и хочу поделиться своей любовью к аниме и музыке!",
        mediaItems = listOf("noenor_edit", "peace_was_never_an_option").map { it.getMediaByName() },
        likeCount = 10,
        commentCount = 5,
        isLikedByCurrentUser = false,
        timestamp = System.currentTimeMillis() - 3600_000, // 1 hour ago
        fandoms = listOf(mockFandoms[0], mockFandoms[2])
    ),
    Post(
        id = "2",
        authorId = "67890",
        authorName = "Пользователь",
        authorLogin = "pirate123",
        authorAvatar = "peace_was_never_an_option".getMediaByName(),
        content = "Приветствую всех! Я пират в поисках своего личного приключения. Кто еще здесь любит аниме? Давайте обмениваться рекомендациями и обсуждать любимые серии! А может, даже устроим совместный просмотр? :)",
        mediaItems = listOf("peace_was_never_an_option").map { it.getMediaByName() },
        likeCount = 20,
        commentCount = 10,
        isLikedByCurrentUser = true,
        timestamp = System.currentTimeMillis() - 7200_000, // 2 hours ago
        fandoms = listOf(mockFandoms[0])
    ),
    Post(
        id = "3",
        authorId = "11223",
        authorName = "Лесное нечто",
        authorLogin = "forestentity",
        authorAvatar = "pet_the_forbidden_dog".getMediaByName(),
        content = "Хочется на концерт брингов...",
        mediaItems = listOf(),
        likeCount = 5,
        commentCount = 2,
        isLikedByCurrentUser = false,
        timestamp = System.currentTimeMillis() - 10800_000, // 3 hours ago
        fandoms = listOf(mockFandoms[2])
    ),
    Post(
        id = "4",
        authorId = "44556",
        authorName = "Дана",
        authorLogin = "dana",
        authorAvatar = "ne_poluchaetsya".getMediaByName(),
        content = "Просто загадочный человек.",
        mediaItems = listOf("what_is_written_here", "what_is_written_here", "what_is_written_here", "what_is_written_here", "what_is_written_here").map { it.getMediaByName() },
        likeCount = 0,
        commentCount = 0,
        isLikedByCurrentUser = false,
        timestamp = System.currentTimeMillis() - 14400_000, // 4 hours ago
        fandoms = mockFandoms,
    )
)

var mockFilters = Filters(
    genders = Gender.entries,
    minAge = 18,
    maxAge = 30,
    categories = listOf(FandomCategory.ANIME_MANGA, FandomCategory.GAMES),
    fandoms = listOf(),
    onlyInUserCity = true,
)

internal val mockUsers = mockProfileCards.map {
    User(
        id = it.id,
        fandoms = it.fandoms,
        description = it.description,
        name = it.name,
        gender = it.gender,
        age = it.age,
        avatar = it.avatar,
        background = "what_is_written_here".getMediaByName(),
        city = it.city,
        profileType = when (it.id) {
            "12345" -> ProfileType.Friend("alice")
            "44556" -> ProfileType.Friend("dana")
            "67890" -> ProfileType.Stranger(true)
            else -> ProfileType.Stranger(false)
        },
    )
}

internal var mockPassword = "qwerty123!"

internal var mockVerificationCode = "123456"

internal val mockAuthInfo = AuthInfo(
    accessToken = "accessToken",
    refreshToken = "refreshToken",
    userId = mockUser.id,
)

internal val mockCities = listOf(
    City(
        nameRussian = "Москва",
        nameEnglish = "Moscow",
    ),
    City(
        nameRussian = "Санкт-Петербург",
        nameEnglish = "Saint Petersburg",
    ),
    City(
        nameRussian = "Новосибирск",
        nameEnglish = "Novosibirsk",
    ),
    City(
        nameRussian = "Екатеринбург",
        nameEnglish = "Yekaterinburg",
    ),
    City(
        nameRussian = "Казань",
        nameEnglish = "Kazan",
    ),
)

// still external :(
val mockUserPreferences = UserPreferences(
    matchesEnabled = true,
    messagesEnabled = false,
    hideMyPostsFromNonMatches = true,
)
