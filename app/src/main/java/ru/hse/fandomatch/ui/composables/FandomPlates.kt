package ru.hse.fandomatch.ui.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.hse.fandomatch.domain.model.Fandom
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
    fandoms: List<Fandom>,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth(),
        maxLines = maxLines,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        fandoms.forEach {
            FandomPlate(
                name = it.name,
                category = it.category,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FandomCarouselWithDropdown(
    fandoms: List<Fandom>,
    modifier: Modifier = Modifier,
    maxPlateWidth: Dp = 150.dp,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable {
                expanded = !expanded
            }
    ) {
        if (!expanded) {
            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                userScrollEnabled = false,
            ) {
                items(fandoms) { f ->
                    FandomPlate(
                        modifier = Modifier,
                        name = f.name,
                        category = f.category,
                        maxWidth = maxPlateWidth
                    )
                }
            }
        } else {
            FandomGrid(
                fandoms = fandoms,
                modifier = Modifier.weight(1f),
            )
        }

        Box(
            modifier = Modifier
                .size(24.dp)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewFandomCarouselWithDropdown() {
    val sample = List(12) { i -> Fandom(name = "Fandom $i", category = FandomCategory.OTHER, id = i) }
    FandomCarouselWithDropdown(fandoms = sample, modifier = Modifier.padding(16.dp))
}
