package ru.hse.fandomatch.ui.composables

import android.media.MediaDataSource
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.MediaItem as DomainMediaItem

@Composable
fun MediaItemView(
    mediaItem: DomainMediaItem,
    isFullScreen: Boolean = false,
    contentScale: ContentScale,
    modifier: Modifier = Modifier,
    placeholderIcon: ImageVector? = null,
    background: Color = MaterialTheme.colorScheme.background,
) {
    when (mediaItem.mediaType) {
        MediaType.IMAGE -> ImageOrPlaceholder(
            url = mediaItem.url,
            modifier = modifier,
            placeholderIcon = placeholderIcon,
            contentScale = contentScale,
            background = background,
        )

        MediaType.VIDEO -> if (isFullScreen) {
            VideoPlayer(
                videoUrl = mediaItem.url,
                modifier = modifier,
                showControls = true,
                autoPlay = true,
                background = background,
            )
        } else {
            VideoThumbnail(
                videoUrl = mediaItem.url,
                modifier = modifier,
                placeholderIcon = placeholderIcon,
                contentScale = contentScale,
                background = background,
            )
        }
    }
}

@Composable
fun ImageOrPlaceholder(
    url: String?,
    placeholderIcon: ImageVector? = null,
    contentScale: ContentScale = ContentScale.Crop,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.background,
) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = null,
        modifier = modifier
            .background(background),
        contentScale = contentScale
    ) {
        when (val state = painter.state) {
            is AsyncImagePainter.State.Loading -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    placeholderIcon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                        )
                    } ?: CircularProgressIndicator()
                }
            }
            is AsyncImagePainter.State.Error -> {
                Log.e("ImageOrPlaceholder", "Error ${state.result.throwable} loading image: $url")
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    placeholderIcon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                        )
                    } ?: Icon(
                        modifier = Modifier,
                        imageVector = Icons.Default.Error,
                        contentDescription = null
                    )
                }
            }
            else -> SubcomposeAsyncImageContent(modifier = Modifier)
        }
    }
}

@Composable
fun VideoThumbnail(
    videoUrl: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderIcon: ImageVector? = null,
    background: Color = MaterialTheme.colorScheme.background,
) {
    var thumbnailBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<Exception?>(null) }

    LaunchedEffect(videoUrl) {
        try {
            val bitmap = withContext(Dispatchers.IO) {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(videoUrl, emptyMap())
                    retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST)
                        ?.asImageBitmap()
                } catch (e: Exception) {
                    Log.e("VideoThumbnail", "Error extracting thumbnail", e)
                    error = e
                    null
                } finally {
                    retriever.release()
                }
            }
            thumbnailBitmap = bitmap
            isLoading = false
        } catch (e: Exception) {
            Log.e("VideoThumbnail", "Error loading video thumbnail", e)
            error = e
            isLoading = false
        }
    }

    Box(
        modifier = modifier
        .background(background),
    ) {
        when {
            error != null -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = placeholderIcon ?: Icons.Default.OndemandVideo,
                        contentDescription = "Video thumbnail failed to load",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            thumbnailBitmap != null -> {
                Image(
                    bitmap = thumbnailBitmap!!,
                    contentDescription = "Video thumbnail",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Icon(
            imageVector = Icons.Filled.PlayCircle,
            contentDescription = "Play video",
            tint = Color.White,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center)
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = CircleShape
                )
                .padding(8.dp)
        )
    }
}

private class ByteArrayMediaDataSource(
    private val bytes: ByteArray,
) : MediaDataSource() {
    override fun getSize(): Long = bytes.size.toLong()

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        if (position < 0 || position >= bytes.size) return -1
        val count = minOf(size.toLong(), bytes.size - position).toInt()
        System.arraycopy(bytes, position.toInt(), buffer, offset, count)
        return count
    }

    override fun close() = Unit
}

@Composable
fun VideoThumbnailFromBytes(
    videoBytes: ByteArray,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderIcon: ImageVector? = null,
    background: Color = MaterialTheme.colorScheme.background,
) {
    var thumbnailBitmap by remember(videoBytes) {
        mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
    }
    var isLoading by remember(videoBytes) { mutableStateOf(true) }
    var error by remember(videoBytes) { mutableStateOf<Exception?>(null) }

    LaunchedEffect(videoBytes) {
        try {
            val bitmap = withContext(Dispatchers.IO) {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(ByteArrayMediaDataSource(videoBytes))
                    retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        ?.asImageBitmap()
                } catch (e: Exception) {
                    Log.e("VideoThumbnailFromBytes", "Error extracting thumbnail", e)
                    error = e
                    null
                } finally {
                    retriever.release()
                }
            }
            thumbnailBitmap = bitmap
            isLoading = false
        } catch (e: Exception) {
            Log.e("VideoThumbnailFromBytes", "Error loading video thumbnail", e)
            error = e
            isLoading = false
        }
    }

    Box(
        modifier = modifier.background(background),
        contentAlignment = Alignment.Center,
    ) {
        if (error != null) {
            Icon(
                imageVector = placeholderIcon ?: Icons.Default.OndemandVideo,
                contentDescription = "Video thumbnail failed to load",
                modifier = Modifier.fillMaxSize(),
            )
        } else if (thumbnailBitmap != null) {
            Image(
                bitmap = thumbnailBitmap!!,
                contentDescription = "Video thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
            )
        } else {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        Icon(
            imageVector = Icons.Filled.PlayCircle,
            contentDescription = "Play video",
            tint = Color.White,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = CircleShape
                )
                .padding(8.dp)
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    showControls: Boolean = true,
    autoPlay: Boolean = false,
    onError: ((Exception) -> Unit)? = null,
    background: Color = MaterialTheme.colorScheme.background,
) {
    val context = LocalContext.current
    var playerError by remember { mutableStateOf<Exception?>(null) }

    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(videoUrl))
                playWhenReady = autoPlay
                prepare()

                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        playerError = Exception(error.message, error.cause)
                        onError?.invoke(playerError!!)
                        Log.e("VideoPlayer", "Playback error: ${error.message}", error)
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_BUFFERING -> Log.d("VideoPlayer", "Buffering...")
                            Player.STATE_READY -> Log.d("VideoPlayer", "Ready to play")
                            Player.STATE_ENDED -> Log.d("VideoPlayer", "Playback ended")
                            else -> {}
                        }
                    }
                })
            }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
            Log.d("VideoPlayer", "Player released")
        }
    }

    if (playerError != null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(background),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.OndemandVideo,
                contentDescription = "Video error",
                modifier = Modifier.size(48.dp)
            )
        }
    } else {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = showControls
                    controllerShowTimeoutMs = 5000
                }
            },
            modifier = modifier
                .fillMaxSize()
                .background(background)
        )
    }
}
