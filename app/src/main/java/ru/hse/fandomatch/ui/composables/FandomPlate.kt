package ru.hse.fandomatch.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.ui.theme.CustomColors

@Composable
fun FandomPlate(
    modifier: Modifier = Modifier,
    name: String,
    category: FandomCategory,
    maxWidth: Dp = 150.dp,
) {
    Box(
        modifier = modifier
            .widthIn(max = maxWidth)
            .clip(RoundedCornerShape(16.dp))
            .background(getColorFromCategory(category))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 14.sp,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun getColorFromCategory(category: FandomCategory): Color {
    return when (category) {
        FandomCategory.ANIME_MANGA -> CustomColors.animeMangaBackground
        FandomCategory.BOOKS -> CustomColors.booksBackground
        FandomCategory.CARTOONS -> CustomColors.cartoonsBackground
        FandomCategory.FILMS -> CustomColors.filmsBackground
        FandomCategory.TV_SERIES -> CustomColors.tvSeriesBackground
        FandomCategory.GAMES -> CustomColors.gamesBackground
        FandomCategory.TABLETOP_GAMES -> CustomColors.tabletopGamesBackground
        FandomCategory.MUSIC -> CustomColors.musicBackground
        FandomCategory.COMICS -> CustomColors.comicsBackground
        FandomCategory.THEATER_MUSICALS -> CustomColors.theaterMusicalsBackground
        FandomCategory.PODCASTS -> CustomColors.podcastsBackground
        FandomCategory.CONTENT_CREATORS -> CustomColors.contentCreatorsBackground
        FandomCategory.CELEBRITIES -> CustomColors.celebritiesBackground
        FandomCategory.SPORTS -> CustomColors.sportsBackground
        FandomCategory.HISTORY -> CustomColors.historyBackground
        FandomCategory.MYTHOLOGY -> CustomColors.mythologyBackground
        FandomCategory.OTHER -> CustomColors.otherBackground
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FandomGrid(
    fandoms: List<Pair<String, FandomCategory>>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth(),
        maxLines = 2,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        fandoms.forEach { (name, category) ->
            FandomPlate(
                name = name,
                category = category,
            )
        }
    }
}
