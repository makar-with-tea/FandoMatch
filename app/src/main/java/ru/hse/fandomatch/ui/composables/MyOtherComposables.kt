package ru.hse.fandomatch.ui.composables

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.navigation.EndIconState
import ru.hse.fandomatch.ui.utils.rawResId

@Composable
fun MyTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier.padding(8.dp),
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    )
}

@Composable
fun MyTextField(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    isError: Boolean,
    enabled: Boolean = true,
    errorText: String? = null,
    onValueChange: (String) -> Unit,
) {
    TextField(
        modifier = modifier.padding(8.dp),
        value = value,
        label = { Text(label) },
        isError = isError,
        enabled = enabled,
        onValueChange = onValueChange,
        supportingText = { errorText?.let { Text(it) } }
    )
}

@Composable
fun MyPasswordField(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    onIconClick: () -> Unit,
    passwordVisibility: Boolean,
    errorText: String? = null
) {
    TextField(
        modifier = modifier.padding(8.dp),
        value = value,
        label = { Text(label) },
        isError = isError,
        supportingText = { errorText?.let { Text(it) } },
        onValueChange = onValueChange,
        trailingIcon = {
            IconButton(onClick = onIconClick) {
                Icon(
                    painterResource(
                        id =
                            if (passwordVisibility) R.drawable.ic_visibility
                            else R.drawable.ic_visibility_off
                    ),
                    contentDescription = if (passwordVisibility)
                        stringResource(R.string.turn_visibility_off_description)
                    else stringResource(R.string.turn_visibility_on_description),
                )
            }
        },
        visualTransformation =
            if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation()
    )
}

@Composable
fun LoadingBlock() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun RawImageOrPlaceholder(
    url: String?,
    @DrawableRes placeholderId: Int = R.drawable.ic_account_placeholder,
    contentScale: ContentScale = ContentScale.Crop,
    context: Context,
    modifier: Modifier = Modifier
) {
    if (url != null) {
        val rawResId = rawResId(url, LocalContext.current)
        val imageBitmap by remember(rawResId) {
            mutableStateOf(
                try {
                    BitmapFactory.decodeStream(context.resources.openRawResource(rawResId))
                        ?.asImageBitmap()
                } catch (e: Exception) {
                    Log.i("RawImageOrPlaceholder", "Error loading image from raw resource: $url", e)
                    null
                }
            )
        }
        imageBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                modifier = modifier,
                contentScale = contentScale,
            )
        }
    } else {
        Image(
            painter = painterResource(id = placeholderId),
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale,
        )
    }
}

@Composable
fun AvatarWithBackground(
    backgroundUrl: String?,
    avatarUrl: String?,
    onEditAvatar: (() -> Unit)? = null,
    onEditBackground: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
) {
    Column {
        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            Box {
                RawImageOrPlaceholder(
                    url = backgroundUrl,
                    context = LocalContext.current,
                    placeholderId = R.drawable.ic_account_placeholder, // todo replace
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f)
                        .background(backgroundColor),
                )
                if (onEditBackground != null) {
                    IconButton(
                        onClick = onEditBackground,
                        modifier = Modifier
                            .padding(20.dp)
                            .size(24.dp)
                            .align(Alignment.BottomEnd)
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_edit),
                            contentDescription = stringResource(R.string.edit_background_icon_description),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Box {
                RawImageOrPlaceholder(
                    url = avatarUrl,
                    context = LocalContext.current,
                    placeholderId = R.drawable.ic_account_placeholder,
                    modifier = Modifier
                        .size(120.dp)
                        .aspectRatio(1f)
                        .offset(y = 30.dp)
                        .clip(CircleShape)
                        .background(backgroundColor)
                        .border(4.dp, backgroundColor, CircleShape),
                )
                if (onEditAvatar != null) {
                    IconButton(
                        onClick = onEditAvatar,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = 30.dp)
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_edit),
                            contentDescription = stringResource(R.string.edit_avatar_icon_description),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        Spacer(
            modifier = Modifier.height(30.dp)
        )
    }
}

@Composable
fun NewMessagesIndicator(
    modifier: Modifier = Modifier,
    count: Int,
) {
    val shortenedCount = if (count > 99) "99+" else count.toString()
    if (count > 0) {
        Box(
            modifier = modifier
                .height(28.dp)
                .defaultMinSize(minWidth = 28.dp)
                .background(MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(28.dp))
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = shortenedCount,
                color = MaterialTheme.colorScheme.onTertiary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EndIcon(endIcon: EndIconState) {
    IconButton(onClick = endIcon.onClick) {
        Icon(
            imageVector = ImageVector.vectorResource(id = endIcon.iconId),
            contentDescription = stringResource(endIcon.descriptionId),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun MyCheckBox(
    modifier: Modifier = Modifier,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clickable { onCheckedChange(!isChecked) }
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null,
        )
        Text(label)
    }
}

@Composable
fun AvatarAndNameBlock(
    avatarUrl: String?,
    name: String,
    login: String?,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    avatarSize: Dp = 44.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RawImageOrPlaceholder(
            modifier = Modifier
                .padding(start = 4.dp, top = 2.dp, bottom = 2.dp, end = 8.dp)
                .size(avatarSize)
                .clip(CircleShape),
            url = avatarUrl,
            placeholderId = R.drawable.ic_account_placeholder,
            context = LocalContext.current,
        )

        Column {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            login?.let {
                Text(
                    text = it,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
