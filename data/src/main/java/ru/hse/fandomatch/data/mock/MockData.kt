package ru.hse.fandomatch.data.mock

import ru.hse.fandomatch.domain.model.Chat
import ru.hse.fandomatch.domain.model.ChatPreview
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Filters
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.model.Token
import ru.hse.fandomatch.domain.model.User
import java.time.LocalDate
import java.time.LocalDateTime

val mockFandoms = listOf(
    Fandom(
        id = 1,
        name = "One Piece",
        category = FandomCategory.ANIME_MANGA,
    ),
    Fandom(
        id = 2,
        name = "Но-Энор",
        category = FandomCategory.OTHER,
    ),
    Fandom(
        id = 3,
        name = "My Chemical Romance",
        category = FandomCategory.MUSIC,
    ),
    Fandom(
        id = 4,
        name = "Ведьмак",
        category = FandomCategory.BOOKS,
    ),
    Fandom(
        id = 5,
        name = "Утиные истории",
        category = FandomCategory.CARTOONS,
    ),
    Fandom(
        id = 6,
        name = "Бэтмен",
        category = FandomCategory.COMICS,
    ),
    Fandom(
        id = 7,
        name = "Star Wars",
        category = FandomCategory.TV_SERIES,
    ),
    Fandom(
        id = 8,
        name = "Dungeons and Dragons",
        category = FandomCategory.TABLETOP_GAMES,
    ),
    Fandom(
        id = 9,
        name = "The Beatles",
        category = FandomCategory.MUSIC,
    ),
    Fandom(
        id = 10,
        name = "Стража! Стража!",
        category = FandomCategory.BOOKS,
    ),
    Fandom(
        id = 11,
        name = "Легенды Олимпа",
        category = FandomCategory.MYTHOLOGY,
    ),
    Fandom(
        id = 12,
        name = "Волкодав",
        category = FandomCategory.BOOKS,
    )
)
val mockUser = User(
    id = 1,
    nickname = "Johny",
    login = "johndoe",
    email = "johndoe@email.com",
    phone = "88005553535",
    fandoms = mockFandoms,
    description = "Just some guy. But this guy has a long bio! Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Or whatever. But I don't want it to be too long, though. I need some restrictions hm.",
    name = "John",
    gender = Gender.MALE,
    passwordHash = "password1",
    birthDate = LocalDate.of(1990, 5, 20),
    avatarUrl = "dzimbei",
    backgroundUrl = "peace_was_never_an_option",
    city = City.MOSCOW,
)

val mockToken = Token(
    accessToken = "accessToken",
    refreshToken = "refreshToken",
)

val mockProfileCards = listOf(
    ProfileCard(
        id = 12345,
        fandoms = mockFandoms,
        description = "Я люблю аниме и музыку :3",
        name = "Алиса",
        gender = Gender.FEMALE,
        avatarUrl = "luffy",
        age = 21,
        compatibilityPercentage = 95,
    ),
    ProfileCard(
        id = 67890,
        fandoms = listOf(mockFandoms[0]),
        description = "Пират в поисках своего личного приключения.",
        name = "Пользователь_123",
        gender = Gender.MALE,
        avatarUrl = "peace_was_never_an_option",
        age = null,
        compatibilityPercentage = 80,
    ),
    ProfileCard(
        id = 11223,
        fandoms = listOf(mockFandoms[2]),
        description = "Обожаю рок-музыку и долгие прогулки по ночному городу.",
        name = "Лесное нечто",
        gender = Gender.NOT_SPECIFIED,
        avatarUrl = "pet_the_forbidden_dog",
        age = 25,
        compatibilityPercentage = 70,
    ),
    ProfileCard(
        id = 44556,
        fandoms = listOf(),
        description = "Просто загадочный человек.",
        name = "Дана",
        gender = Gender.FEMALE,
        avatarUrl = null,
        age = 30,
        compatibilityPercentage = 60,
    ),
    ProfileCard(
        id = 44556,
        fandoms = listOf(mockFandoms[1]),
        description = "Пожалуйста, давайте поговорим о Лидросе",
        name = "Но-энор мой но-энор",
        gender = Gender.FEMALE,
        avatarUrl = "lidros",
        age = 30,
        compatibilityPercentage = 60,
    ),
)

val mockChatPreviews = listOf(
    ChatPreview(
        chatId = 5L,
        participantName = mockProfileCards[4].name,
        participantAvatarUrl = mockProfileCards[4].avatarUrl,
        lastMessage = "Как вы допустили смерть Лидроса на зимке?..",
        isLastMessageFromThisUser = false,
        lastMessageTimestamp = LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        newMessagesCount = 101,
    ),
    ChatPreview(
        chatId = 3L,
        participantName = mockProfileCards[2].name,
        participantAvatarUrl = mockProfileCards[2].avatarUrl,
        lastMessage = "Привет! Как дела?",
        isLastMessageFromThisUser = false,
        lastMessageTimestamp = LocalDateTime.now().minusMinutes(30).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        newMessagesCount = 0,
    ),
    ChatPreview(
        chatId = 2L,
        participantName = mockProfileCards[1].name,
        participantAvatarUrl = mockProfileCards[1].avatarUrl,
        lastMessage = "Я тоже люблю аниме!",
        isLastMessageFromThisUser = false,
        lastMessageTimestamp = LocalDateTime.now().minusHours(5).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        newMessagesCount = 2,
    ),
    ChatPreview(
        chatId = 1L,
        participantName = mockProfileCards[0].name,
        participantAvatarUrl = mockProfileCards[0].avatarUrl,
        lastMessage = "А может, как-нибудь посмотрим ван пис с начала вместе?",
        isLastMessageFromThisUser = true,
        lastMessageTimestamp = LocalDateTime.now().minusDays(2).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        newMessagesCount = 0,
    ),
    ChatPreview(
        chatId = 4L,
        participantName = mockProfileCards[3].name,
        participantAvatarUrl = mockProfileCards[3].avatarUrl,
        lastMessage = "Посмотрела новый эпизод Токийского Гуля, очень понравилось!",
        isLastMessageFromThisUser = true,
        lastMessageTimestamp = LocalDateTime.now().minusDays(10).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        newMessagesCount = 0,
    ),
)

val mockChat = Chat(
    chatId = 1L,
    participantId = mockProfileCards[0].id,
    participantName = mockProfileCards[0].name,
    participantAvatarUrl = mockProfileCards[0].avatarUrl,
    messages = listOf(
        Message(
            messageId = 1L,
            isFromThisUser = false,
            content = "Привет! Как дела?",
            timestamp = LocalDateTime.now().minusDays(2).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = 2L,
            isFromThisUser = true,
            content = "Привет! Все хорошо, спасибо. А у тебя?",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(5).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = 3L,
            isFromThisUser = false,
            content = "Тоже отлично! Какие фандомы ты любишь?",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(10).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = 4L,
            isFromThisUser = true,
            content = "Я обожаю аниме, особенно One Piece! А ты?",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(15).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = 5L,
            isFromThisUser = false,
            content = "One Piece тоже мой любимый! А до какого момента ты досмотрел?",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(20).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = 6L,
            isFromThisUser = true,
            content = "Я сейчас на Whole Cake Island. Бедный Санжи :(",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(25).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = 7L,
            isFromThisUser = false,
            content = "Ооо, понимаю... Удачи тебе там))",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(30).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = 8L,
            isFromThisUser = false,
            content = "А я аниме уже досмотрела, сейчас читаю мангу",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(30).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = 9L,
            isFromThisUser = false,
            content = "Но хочу пересмотреть когда-нибудь заново)",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(30).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = 10L,
            isFromThisUser = false,
            content = "Там же столько деталей, которые можно упустить при первом просмотре",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(35).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = 11L,
            isFromThisUser = false,
            content = "Та же предыстория Санджи многое меняет в восприятии первых серий, где он появляется",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(35).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = 12L,
            isFromThisUser = false,
            content = "Ну и я просто хочу посмотреть на East Blue Луффи, он такой хаотичный котенок там))",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(35).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        ),
        Message(
            messageId = 13L,
            isFromThisUser = true,
            content = "А может, как-нибудь посмотрим ван пис с начала вместе?",
            timestamp = LocalDateTime.now().minusDays(2).plusMinutes(35).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        )
    ),
)

val mockFilters = Filters(
    genders = Gender.entries,
    minAge = 18,
    maxAge = 30,
    categories = listOf(FandomCategory.ANIME_MANGA, FandomCategory.GAMES),
    fandoms = listOf(),
    userCity = City.MOSCOW,
    onlyInUserCity = true,
)
