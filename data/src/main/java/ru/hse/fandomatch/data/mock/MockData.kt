package ru.hse.fandomatch.data.mock

import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Filters
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.domain.model.AuthInfo
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.model.UserPreferences

val mockFandoms = listOf(
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
var mockUser = User(
    id = "1",
    fandoms = mockFandoms,
    description = "Just some guy. But this guy has a long bio! Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Or whatever. But I don't want it to be too long, though. I need some restrictions hm.",
    name = "John",
    gender = Gender.MALE,
    age = 25,
    avatarUrl = "dzimbei",
    backgroundUrl = "peace_was_never_an_option",
    city = City(
        nameRussian = "Москва",
        nameEnglish = "Moscow",
    ),
    profileType = ProfileType.Own(
        login = "johndoe",
        email = "johndoe@email.com",
    )
)

var mockPassword = "qwerty123!"
var mockVerificationCode = "123456"

val mockAuthInfo = AuthInfo(
    accessToken = "accessToken",
    refreshToken = "refreshToken",
    userId = mockUser.id,
)

val mockProfileCards = listOf(
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
        avatarUrl = "luffy",
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
        avatarUrl = "peace_was_never_an_option",
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
        avatarUrl = "pet_the_forbidden_dog",
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
        avatarUrl = "ne_poluchaetsya",
        age = 30,
        compatibilityPercentage = 60,
        city = null,
    ),
    ProfileCard(
        id = "44557",
        fandoms = listOf(mockFandoms[1]),
        description = "Пожалуйста, давайте поговорим о Лидросе",
        name = "Но-энор мой но-энор",
        gender = Gender.FEMALE,
        avatarUrl = "lidros",
        age = 21,
        compatibilityPercentage = 60,
        city = City(
            nameRussian = "Санкт-Петербург",
            nameEnglish = "Saint Petersburg",
        ),
    ),
)

val mockUsers = mockProfileCards.map {
    User(
        id = it.id,
        fandoms = it.fandoms,
        description = it.description,
        name = it.name,
        gender = it.gender,
        age = it.age,
        avatarUrl = it.avatarUrl,
        backgroundUrl = "what_is_written_here",
        city = it.city,
        profileType = when (it.id) {
            "12345" -> ProfileType.Friend("alice")
            "44556" -> ProfileType.Friend("dana")
            else -> ProfileType.Stranger
        },
    )
}

var mockFilters = Filters(
    genders = Gender.entries,
    minAge = 18,
    maxAge = 30,
    categories = listOf(FandomCategory.ANIME_MANGA, FandomCategory.GAMES),
    fandoms = listOf(),
    userCity = City(
        nameRussian = "Москва",
        nameEnglish = "Moscow",
    ),
    onlyInUserCity = true,
)

val mockPosts = listOf(
    Post(
        id = "1",
        authorId = "12345",
        authorName = "Алиса",
        authorLogin = "alice",
        authorAvatarUrl = "luffy",
        content = "Привет! Я только что зарегистрировалась и хочу поделиться своей любовью к аниме и музыке!",
        imageUrls = listOf("luffy", "peace_was_never_an_option"),
        likeCount = 10,
        commentCount = 5,
        isLikedByCurrentUser = false,
        timestamp = System.currentTimeMillis() - 3600_000, // 1 hour ago
    ),
    Post(
        id = "2",
        authorId = "67890",
        authorName = "Пользователь",
        authorLogin = "pirate123",
        authorAvatarUrl = "peace_was_never_an_option",
        content = "Приветствую всех! Я пират в поисках своего личного приключения. Кто еще здесь любит аниме? Давайте обмениваться рекомендациями и обсуждать любимые серии! А может, даже устроим совместный просмотр? :)",
        imageUrls = listOf("peace_was_never_an_option"),
        likeCount = 20,
        commentCount = 10,
        isLikedByCurrentUser = true,
        timestamp = System.currentTimeMillis() - 7200_000, // 2 hours ago
    ),
    Post(
        id = "3",
        authorId = "11223",
        authorName = "Лесное нечто",
        authorLogin = "forestentity",
        authorAvatarUrl = "pet_the_forbidden_dog",
        content = "Хочется на концерт брингов...",
        imageUrls = listOf(),
        likeCount = 5,
        commentCount = 2,
        isLikedByCurrentUser = false,
        timestamp = System.currentTimeMillis() - 10800_000, // 3 hours ago
    ),
    Post(
        id = "4",
        authorId = "44556",
        authorName = "Дана",
        authorLogin = "dana",
        authorAvatarUrl = "ne_poluchaetsya",
        content = "Просто загадочный человек.",
        imageUrls = listOf("what_is_written_here", "what_is_written_here", "what_is_written_here", "what_is_written_here", "what_is_written_here"),
        likeCount = 0,
        commentCount = 0,
        isLikedByCurrentUser = false,
        timestamp = System.currentTimeMillis() - 14400_000, // 4 hours ago
    )
)

val mockUserPosts = listOf(
    Post(
        id = "5",
        authorId = mockUser.id,
        authorName = mockUser.name,
        authorLogin = (mockUser.profileType as ProfileType.Own).login,
        authorAvatarUrl = mockUser.avatarUrl,
        content = "Вау, я умею писать посты!",
        imageUrls = listOf("dzimbei"),
        likeCount = 100,
        commentCount = 50,
        isLikedByCurrentUser = false,
        timestamp = System.currentTimeMillis() - 3600_000, // 1 hour ago
    ),
    Post(
        id = "6",
        authorId = mockUser.id,
        authorName = mockUser.name,
        authorLogin = (mockUser.profileType as ProfileType.Own).login,
        authorAvatarUrl = mockUser.avatarUrl,
        content = "Еще один пост от меня.",
        imageUrls = listOf(),
        likeCount = 150,
        commentCount = 75,
        isLikedByCurrentUser = false,
        timestamp = System.currentTimeMillis() - 7200_000, // 2 hours ago
    ),
    Post(
        id = "7",
        authorId = mockUser.id,
        authorName = mockUser.name,
        authorLogin = (mockUser.profileType as ProfileType.Own).login,
        authorAvatarUrl = mockUser.avatarUrl,
        content = "Люблю это приложение!",
        imageUrls = listOf("what_is_written_here"),
        likeCount = 200,
        commentCount = 100,
        isLikedByCurrentUser = true,
        timestamp = System.currentTimeMillis() - 10800_000, // 3 hours ago
    )
)

val mockCities = listOf(
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
) // todo the same with categories

val mockUserPreferences = UserPreferences(
    matchesEnabled = true,
    messagesEnabled = false,
    hideMyPostsFromNonMatches = true,
)
