package ru.hse.fandomatch.data.mock

import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
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
    avatarUrl = "luffy",
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
        description = "Just a pirate in search of adventure.",
        name = "Bob",
        gender = Gender.MALE,
        avatarUrl = "peace_was_never_an_option",
        age = null,
        compatibilityPercentage = 80,
    ),
    ProfileCard(
        id = 11223,
        fandoms = listOf(mockFandoms[2]),
        description = "Music is life.",
        name = "Charlie",
        gender = Gender.NOT_SPECIFIED,
        avatarUrl = "pet_the_forbidden_dog",
        age = 25,
        compatibilityPercentage = 70,
    ),
    ProfileCard(
        id = 44556,
        fandoms = listOf(),
        description = "Just a mysterious person.",
        name = "Dana",
        gender = Gender.FEMALE,
        avatarUrl = null,
        age = 30,
        compatibilityPercentage = 60,
    ),
)