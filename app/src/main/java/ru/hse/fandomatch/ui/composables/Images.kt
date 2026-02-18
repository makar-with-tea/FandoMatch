package ru.hse.fandomatch.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.navigation.EndIconState
import ru.hse.fandomatch.ui.navigation.TopBarState

@Composable
fun ImagesGrid(
    imageUrls: List<String>,
    modifier: Modifier = Modifier,
    onImageClicked: (List<String>, Int) -> Unit,
    maxHeight: Dp = 300.dp,
    minHeight: Dp = 0.dp,
) {
    val maxImages = imageUrls.take(5)
    val imageModifier = Modifier
        .padding(2.dp)
        .clip(RoundedCornerShape(8.dp))

    val context = LocalContext.current

    when (maxImages.size) {
        1 -> {
            Box(modifier = modifier) {
                RawImageOrPlaceholder(
                    url = maxImages[0],
                    placeholderId = R.drawable.ic_account_placeholder, // todo
                    context = context,
                    contentScale = ContentScale.FillWidth,
                    modifier = imageModifier
                        .heightIn(minHeight, maxHeight)
                        .fillMaxWidth()
                        .clickable {
                            onImageClicked(imageUrls, 0)
                        }
                )
            }
        }

        2 -> {
            Row(
                modifier = Modifier
                    .heightIn(minHeight, maxHeight)
            ) {
                RawImageOrPlaceholder(
                    url = maxImages[0],
                    placeholderId = R.drawable.ic_account_placeholder, // todo
                    context = context,
                    contentScale = ContentScale.FillWidth,
                    modifier = imageModifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clickable {
                            onImageClicked(imageUrls, 0)
                        }
                )

                RawImageOrPlaceholder(
                    url = maxImages[1],
                    placeholderId = R.drawable.ic_account_placeholder, // todo
                    context = context,
                    contentScale = ContentScale.FillWidth,
                    modifier = imageModifier
                        .weight(1f)
                        .clickable {
                            onImageClicked(imageUrls, 1)
                        }
                )
            }
        }

        3 -> {
            Row(
                modifier = Modifier
                    .heightIn(minHeight, maxHeight)
            ) {
                RawImageOrPlaceholder(
                    url = maxImages[0],
                    placeholderId = R.drawable.ic_account_placeholder, // todo
                    context = context,
                    contentScale = ContentScale.FillWidth,
                    modifier = imageModifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            onImageClicked(imageUrls, 0)
                        }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    RawImageOrPlaceholder(
                        url = maxImages[1],
                        placeholderId = R.drawable.ic_account_placeholder, // todo
                        context = context,
                        contentScale = ContentScale.FillWidth,
                        modifier = imageModifier
                            .weight(1f)
                            .clickable {
                                onImageClicked(imageUrls, 1)
                            }
                    )

                    RawImageOrPlaceholder(
                        url = maxImages[2],
                        placeholderId = R.drawable.ic_account_placeholder, // todo
                        context = context,
                        contentScale = ContentScale.FillWidth,
                        modifier = imageModifier
                            .weight(1f)
                            .clickable {
                                onImageClicked(imageUrls, 2)
                            }
                    )
                }
            }
        }

        4 -> {
            Row(
                modifier = Modifier
                    .heightIn(minHeight, maxHeight)
            ) {
                RawImageOrPlaceholder(
                    url = maxImages[0],
                    placeholderId = R.drawable.ic_account_placeholder, // todo
                    context = context,
                    contentScale = ContentScale.FillWidth,
                    modifier = imageModifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            onImageClicked(imageUrls, 0)
                        }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    RawImageOrPlaceholder(
                        url = maxImages[1],
                        placeholderId = R.drawable.ic_account_placeholder, // todo
                        context = context,
                        contentScale = ContentScale.FillWidth,
                        modifier = imageModifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clickable {
                                onImageClicked(imageUrls, 1)
                            }
                    )

                    Row(
                        modifier = Modifier.weight(1f)
                    ) {
                        RawImageOrPlaceholder(
                            url = maxImages[2],
                            placeholderId = R.drawable.ic_account_placeholder, // todo
                            context = context,
                            contentScale = ContentScale.FillWidth,
                            modifier = imageModifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clickable {
                                    onImageClicked(imageUrls, 2)
                                }
                        )

                        RawImageOrPlaceholder(
                            url = maxImages[3],
                            placeholderId = R.drawable.ic_account_placeholder, // todo
                            context = context,
                            contentScale = ContentScale.FillWidth,
                            modifier = imageModifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clickable {
                                    onImageClicked(imageUrls, 3)
                                }
                        )
                    }
                }
            }
        }

        5 -> {
            Row(
                modifier = Modifier
                    .heightIn(minHeight, maxHeight)
            ) {
                RawImageOrPlaceholder(
                    url = maxImages[0],
                    placeholderId = R.drawable.ic_account_placeholder, // todo
                    context = context,
                    contentScale = ContentScale.FillWidth,
                    modifier = imageModifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            onImageClicked(imageUrls, 0)
                        }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Row(
                        modifier = Modifier.weight(1f)
                    ) {
                        RawImageOrPlaceholder(
                            url = maxImages[1],
                            placeholderId = R.drawable.ic_account_placeholder, // todo
                            context = context,
                            contentScale = ContentScale.FillWidth,
                            modifier = imageModifier
                                .weight(1f)
                                .clickable {
                                    onImageClicked(imageUrls, 1)
                                }
                        )

                        RawImageOrPlaceholder(
                            url = maxImages[2],
                            placeholderId = R.drawable.ic_account_placeholder, // todo
                            context = context,
                            contentScale = ContentScale.FillWidth,
                            modifier = imageModifier
                                .weight(1f)
                                .clickable {
                                    onImageClicked(imageUrls, 2)
                                }
                        )
                    }
                    Row(
                        modifier = Modifier.weight(1f)
                    ) {
                        RawImageOrPlaceholder(
                            url = maxImages[3],
                            placeholderId = R.drawable.ic_account_placeholder, // todo
                            context = context,
                            contentScale = ContentScale.FillWidth,
                            modifier = imageModifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clickable {
                                    onImageClicked(imageUrls, 3)
                                }
                        )

                        RawImageOrPlaceholder(
                            url = maxImages[4],
                            placeholderId = R.drawable.ic_account_placeholder, // todo
                            context = context,
                            contentScale = ContentScale.FillWidth,
                            modifier = imageModifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clickable {
                                    onImageClicked(imageUrls, 4)
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImagesScreen(
    urls: List<String>,
    initialPage: Int = 0,
    titleContent: @Composable () -> Unit,
    setTopBarState: (TopBarState?) -> Unit,
) {
    setTopBarState(
        TopBarState(
            titleContent = titleContent,
            endIcons = listOf(
                EndIconState(
                    iconId = R.drawable.ic_download,
                    onClick = { /* TODO download current image */ },
                    descriptionId = R.string.download_media_button_description,
                )
            )
        )
    )

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { urls.size }
    )
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.BottomCenter
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
        ) { page ->
            RawImageOrPlaceholder(
                url = urls[page],
                placeholderId = R.drawable.ic_account_placeholder, // todo
                contentScale = ContentScale.Fit,
                context = context,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = "${pagerState.currentPage + 1} / ${urls.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(8.dp)
                .clip(CircleShape)
                .align(Alignment.BottomCenter)
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                .padding(8.dp)
        )
    }
}
