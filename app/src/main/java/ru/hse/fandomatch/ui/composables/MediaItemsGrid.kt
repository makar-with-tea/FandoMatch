package ru.hse.fandomatch.ui.composables

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ru.hse.fandomatch.utils.BitmapHelper
import ru.hse.fandomatch.R
import ru.hse.fandomatch.domain.model.MediaItem
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.usecase.media.DownloadMediaToGalleryUseCase
import ru.hse.fandomatch.navigation.EndIconState
import ru.hse.fandomatch.navigation.TopBarState

@Composable
fun MediaItemsGrid(
    mediaItems: List<MediaItem>,
    modifier: Modifier = Modifier,
    onItemClicked: (List<MediaItem>, Int) -> Unit,
    maxHeight: Dp? = null,
    minHeight: Dp? = null,
) {
    val maxItems = mediaItems.take(5)

    val modifierWithHeight = when {
        minHeight != null && maxHeight != null -> modifier.heightIn(min = minHeight, max = maxHeight)
        minHeight != null -> modifier.heightIn(min = minHeight)
        maxHeight != null -> modifier.heightIn(max = maxHeight)
        else -> modifier
    }
    val itemModifier = Modifier
        .padding(2.dp)
        .clip(RoundedCornerShape(8.dp))
    val smallImageModifier = Modifier
        .clip(RoundedCornerShape(8.dp))

    var firstImageHeight by remember { mutableStateOf<Dp?>(null) }
    val density = LocalDensity.current

    when (maxItems.size) {
        1 -> {
            Box(modifier = modifierWithHeight) {
                MediaItemView(
                    mediaItem = maxItems[0],
                    contentScale = ContentScale.FillWidth,
                    modifier = itemModifier
                        .fillMaxWidth()
                        .clickable {
                            onItemClicked(mediaItems, 0)
                        }
                )
            }
        }

        2 -> {
            Row(
                modifier = modifierWithHeight
            ) {
                MediaItemView(
                    mediaItem = maxItems[0],
                    contentScale = ContentScale.Crop,
                    modifier = itemModifier
                        .fillMaxHeight()
                        .onGloballyPositioned { coordinates ->
                            firstImageHeight = with(density) { coordinates.size.height.toDp() }
                        }
                        .weight(1f)
                        .clickable {
                            onItemClicked(mediaItems, 0)
                        }
                )

                MediaItemView(
                    mediaItem = maxItems[1],
                    contentScale = ContentScale.Crop,
                    modifier = itemModifier
                        .weight(1f)
                        .then(
                            if (firstImageHeight != null) Modifier.height(firstImageHeight!!) else Modifier
                        )
                        .clickable {
                            onItemClicked(mediaItems, 1)
                        }
                )
            }
        }

        3 -> {
            Row(
                modifier = modifierWithHeight
            ) {
                MediaItemView(
                    mediaItem = maxItems[0],
                    contentScale = ContentScale.Crop,
                    modifier = itemModifier
                        .fillMaxHeight()
                        .onGloballyPositioned { coordinates ->
                            firstImageHeight = with(density) { coordinates.size.height.toDp() }
                        }
                        .weight(1f)
                        .clickable {
                            onItemClicked(mediaItems, 0)
                        }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp, end = 2.dp)
                        .then(
                            if (firstImageHeight != null) Modifier.height(firstImageHeight!!) else Modifier.fillMaxHeight()
                        ),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    MediaItemView(
                        mediaItem = maxItems[1],
                        contentScale = ContentScale.Crop,
                        modifier = smallImageModifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable {
                                onItemClicked(mediaItems, 1)
                            }
                    )

                    MediaItemView(
                        mediaItem = maxItems[2],
                        contentScale = ContentScale.Crop,
                        modifier = smallImageModifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable {
                                onItemClicked(mediaItems, 2)
                            }
                    )
                }
            }
        }

        4 -> {
            Row(
                modifier = modifierWithHeight
            ) {
                MediaItemView(
                    mediaItem = maxItems[0],
                    contentScale = ContentScale.Crop,
                    modifier = itemModifier
                        .fillMaxHeight()
                        .onGloballyPositioned { coordinates ->
                            firstImageHeight = with(density) { coordinates.size.height.toDp() }
                        }
                        .weight(1f)
                        .clickable {
                            onItemClicked(mediaItems, 0)
                        }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp, end = 2.dp)
                        .then(
                            if (firstImageHeight != null) Modifier.height(firstImageHeight!!) else Modifier.fillMaxHeight()
                        ),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    MediaItemView(
                        mediaItem = maxItems[1],
                        contentScale = ContentScale.Crop,
                        modifier = smallImageModifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable {
                                onItemClicked(mediaItems, 1)
                            }
                    )

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        MediaItemView(
                            mediaItem = maxItems[2],
                            contentScale = ContentScale.Crop,
                            modifier = smallImageModifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable {
                                    onItemClicked(mediaItems, 2)
                                }
                        )

                        MediaItemView(
                            mediaItem = maxItems[3],
                            contentScale = ContentScale.Crop,
                            modifier = smallImageModifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable {
                                    onItemClicked(mediaItems, 3)
                                }
                        )
                    }
                }
            }
        }

        5 -> {
            Row(
                modifier = modifierWithHeight
            ) {
                MediaItemView(
                    mediaItem = maxItems[0],
                    contentScale = ContentScale.Crop,
                    modifier = itemModifier
                        .fillMaxHeight()
                        .onGloballyPositioned { coordinates ->
                            firstImageHeight = with(density) { coordinates.size.height.toDp() }
                        }
                        .weight(1f)
                        .clickable {
                            onItemClicked(mediaItems, 0)
                        }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp, end = 2.dp)
                        .then(
                            if (firstImageHeight != null) Modifier.height(firstImageHeight!!) else Modifier.fillMaxHeight()
                        ),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        MediaItemView(
                            mediaItem = maxItems[1],
                            contentScale = ContentScale.Crop,
                            modifier = smallImageModifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable {
                                    onItemClicked(mediaItems, 1)
                                }
                        )

                        MediaItemView(
                            mediaItem = maxItems[2],
                            contentScale = ContentScale.Crop,
                            modifier = smallImageModifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable {
                                    onItemClicked(mediaItems, 2)
                                }
                        )
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        MediaItemView(
                            mediaItem = maxItems[3],
                            contentScale = ContentScale.Crop,
                            modifier = smallImageModifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable {
                                    onItemClicked(mediaItems, 3)
                                }
                        )

                        MediaItemView(
                            mediaItem = maxItems[4],
                            contentScale = ContentScale.Crop,
                            modifier = smallImageModifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable {
                                    onItemClicked(mediaItems, 4)
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
    items: List<MediaItem>,
    initialPage: Int = 0,
    titleContent: @Composable () -> Unit,
    onDownloadItem: (MediaItem) -> Unit,
    setTopBarState: (TopBarState?) -> Unit,
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { items.size }
    )
    var pendingDownloadItem by remember { mutableStateOf<MediaItem?>(null) }

    val requestStoragePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val item = pendingDownloadItem
        pendingDownloadItem = null
        if (granted && item != null) {
            onDownloadItem(item)
        }
    }

    val currentItem = items.getOrNull(pagerState.currentPage)

    setTopBarState(
        TopBarState(
            titleContent = titleContent,
            endIcons = listOf(
                EndIconState(
                    iconId = R.drawable.ic_download,
                    onClick = {
                        val item = currentItem ?: return@EndIconState
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            pendingDownloadItem = item
                            requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        } else {
                            onDownloadItem(item)
                        }
                    },
                    descriptionId = R.string.download_media_button_description,
                )
            )
        )
    )


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
            MediaItemView(
                mediaItem = items[page],
                contentScale = ContentScale.Fit,
                isFullScreen = true,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = "${pagerState.currentPage + 1} / ${items.size}",
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

@Composable
fun AttachmentsRow(
    attachedFilesWithTypes: List<Pair<ByteArray, MediaType>>,
    onAttachmentsChanged: (List<Pair<ByteArray, MediaType>>) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
    ) {
        itemsIndexed(attachedFilesWithTypes) { index, (byteArray, type) ->
            Box(
                contentAlignment = Alignment.TopEnd,
            ) {
                when (type) {
                    MediaType.IMAGE -> {
                        val bitmap = BitmapHelper.byteArrayToBitmap(byteArray)?.asImageBitmap()
                        bitmap?.let {
                            Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }

                    MediaType.VIDEO -> {
                        VideoThumbnailFromBytes(
                            videoBytes = byteArray,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }

                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_close),
                    contentDescription = stringResource(R.string.detach_file_button_description),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            MaterialTheme.colorScheme.background,
                            shape = CircleShape
                        )
                        .padding(2.dp)
                        .clip(CircleShape)
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .clickable {
                            onAttachmentsChanged(
                                attachedFilesWithTypes.toMutableList().also {
                                    it.removeAt(index)
                                }
                            )
                        }
                )
            }

            Spacer(modifier = Modifier.size(4.dp))
        }
    }
}