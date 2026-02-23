package ru.hse.fandomatch.data.mock

import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Filters
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.model.Token
import ru.hse.fandomatch.domain.model.User
import java.time.LocalDate

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
var mockUser = User(
    id = 1,
    login = "johndoe",
    email = "johndoe@email.com",
    phone = "88005553535",
    fandoms = mockFandoms,
    description = "Just some guy. But this guy has a long bio! Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Or whatever. But I don't want it to be too long, though. I need some restrictions hm.",
    name = "John",
    gender = Gender.MALE,
    birthDate = LocalDate.of(1990, 5, 20),
    avatarUrl = "dzimbei",
    backgroundUrl = "peace_was_never_an_option",
    city = City.MOSCOW,
)

val mockPassword = "qwerty123!"

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
        age = 17,
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
        avatarUrl = "ne_poluchaetsya",
        age = 30,
        compatibilityPercentage = 60,
    ),
    ProfileCard(
        id = 44557,
        fandoms = listOf(mockFandoms[1]),
        description = "Пожалуйста, давайте поговорим о Лидросе",
        name = "Но-энор мой но-энор",
        gender = Gender.FEMALE,
        avatarUrl = "lidros",
        age = 21,
        compatibilityPercentage = 60,
    ),
)

val mockUsers = mockProfileCards.map {
    User(
        id = it.id,
        login = when (it.id) {
            12345L -> "alice"
            67890L -> "pirate_123"
            11223L -> "forest_entity"
            44556L -> "dana"
            44557L -> "lidros_4ever"
            else -> "user_${it.id}"
        },
        email = "${it.name.lowercase()}@example.com",
        phone = null,
        fandoms = it.fandoms,
        description = it.description,
        name = it.name,
        gender = it.gender,
        birthDate = LocalDate.now().minusYears((it.age).toLong()),
        avatarUrl = it.avatarUrl,
        backgroundUrl = "what_is_written_here",
        city = City.MOSCOW,
    )
}

val mockFilters = Filters(
    genders = Gender.entries,
    minAge = 18,
    maxAge = 30,
    categories = listOf(FandomCategory.ANIME_MANGA, FandomCategory.GAMES),
    fandoms = listOf(),
    userCity = City.MOSCOW,
    onlyInUserCity = true,
)

val mockPosts = listOf(
    Post(
        id = 1,
        authorId = 12345,
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
        id = 2,
        authorId = 67890,
        authorName = "Пользователь_123",
        authorLogin = "pirate_123",
        authorAvatarUrl = "peace_was_never_an_option",
        content = "Приветствую всех! Я пират в поисках своего личного приключения. Кто еще здесь любит аниме? Давайте обмениваться рекомендациями и обсуждать любимые серии! А может, даже устроим совместный просмотр? :)",
        imageUrls = listOf("peace_was_never_an_option"),
        likeCount = 20,
        commentCount = 10,
        isLikedByCurrentUser = true,
        timestamp = System.currentTimeMillis() - 7200_000, // 2 hours ago
    ),
    Post(
        id = 3,
        authorId = 11223,
        authorName = "Лесное нечто",
        authorLogin = "forest_entity",
        authorAvatarUrl = "pet_the_forbidden_dog",
        content = "Хочется на концерт брингов...",
        imageUrls = listOf(),
        likeCount = 5,
        commentCount = 2,
        isLikedByCurrentUser = false,
        timestamp = System.currentTimeMillis() - 10800_000, // 3 hours ago
    ),
    Post(
        id = 4,
        authorId = 44556,
        authorName = "Дана",
        authorLogin = "dana",
        authorAvatarUrl = "ne_poluchaetsya",
        content = "Просто загадочный человек.",
        imageUrls = listOf("what_is_written_here"),
        likeCount = 0,
        commentCount = 0,
        isLikedByCurrentUser = false,
        timestamp = System.currentTimeMillis() - 14400_000, // 4 hours ago
    )
)

val mockUserPosts = listOf(
    Post(
        id = 5,
        authorId = 1,
        authorName = "John",
        authorLogin = "johndoe",
        authorAvatarUrl = "dzimbei",
        content = "Вау, я умею писать посты!",
        imageUrls = listOf("dzimbei"),
        likeCount = 100,
        commentCount = 50,
        isLikedByCurrentUser = false,
        timestamp = System.currentTimeMillis() - 3600_000, // 1 hour ago
    ),
    Post(
        id = 6,
        authorId = 1,
        authorName = "John",
        authorLogin = "johndoe",
        authorAvatarUrl = "dzimbei",
        content = "Еще один пост от меня.",
        imageUrls = listOf(),
        likeCount = 150,
        commentCount = 75,
        isLikedByCurrentUser = false,
        timestamp = System.currentTimeMillis() - 7200_000, // 2 hours ago
    ),
    Post(
        id = 7,
        authorId = 1,
        authorName = "John",
        authorLogin = "johndoe",
        authorAvatarUrl = "dzimbei",
        content = "Люблю это приложение!",
        imageUrls = listOf("what_is_written_here"),
        likeCount = 200,
        commentCount = 100,
        isLikedByCurrentUser = true,
        timestamp = System.currentTimeMillis() - 10800_000, // 3 hours ago
    )
)