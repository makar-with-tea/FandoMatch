package ru.hse.fandomatch.data.mock

import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Filters
import ru.hse.fandomatch.domain.model.Gender
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
