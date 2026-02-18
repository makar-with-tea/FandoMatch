package ru.hse.fandomatch.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.hse.fandomatch.R

@Composable
fun FeedPost(
    userName: String,
    userLogin: String?,
    userAvatarUrl: String?,
    postDate: String,
    postText: String?,
    imageUrls: List<String>,
    areCommentsAvailable: Boolean,
    likeCount: Int,
    commentCount: Int,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onImageClicked: (List<String>, Int) -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(8.dp)
    ) {
        AvatarAndNameBlock(
            name = userName,
            avatarUrl = userAvatarUrl,
            login = userLogin,
            modifier = Modifier.padding(bottom = 4.dp),
            backgroundColor = backgroundColor,
        )
        if (imageUrls.isNotEmpty()) {
            ImagesGrid(
                imageUrls = imageUrls,
                modifier = Modifier.padding(bottom = 4.dp),
                onImageClicked = onImageClicked,
                maxHeight = 500.dp
            )
        }
        postText?.let {
            Text(postText, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLikeClick, enabled = areCommentsAvailable) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = stringResource(R.string.like_button_description),
                        tint = if (isLiked) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
                Text(likeCount.toString())
            }
            if (areCommentsAvailable) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCommentClick, enabled = areCommentsAvailable) {
                        Icon(
                            Icons.AutoMirrored.Filled.Comment,
                            contentDescription = stringResource(R.string.comment_button_description)
                        )
                    }
                    Text(commentCount.toString())
                }
            }

            Text(postDate, style = MaterialTheme.typography.bodySmall)
        }
    }
}
