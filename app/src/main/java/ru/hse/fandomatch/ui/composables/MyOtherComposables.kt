package ru.hse.fandomatch.ui.composables

import android.content.Context
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.hse.fandomatch.R
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
    @DrawableRes placeholderId: Int,
    context: Context,
    modifier: Modifier = Modifier
) {
    if (url != null) {
        val rawResId = rawResId(url, LocalContext.current)
        val imageBitmap by remember(rawResId) {
            mutableStateOf(
                BitmapFactory.decodeStream(context.resources.openRawResource(rawResId))
                    ?.asImageBitmap()
            )
        }
        imageBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
    } else {
        Image(
            painter = painterResource(id = placeholderId),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun AvatarWithBackground(
    backgroundUrl: String?,
    avatarUrl: String?,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
) {
    Column {
        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            RawImageOrPlaceholder(
                url = backgroundUrl,
                context = LocalContext.current,
                placeholderId = R.drawable.ic_account_placeholder, // todo replace
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f),
            )

            RawImageOrPlaceholder(
                url = avatarUrl,
                context = LocalContext.current,
                placeholderId = R.drawable.ic_account_placeholder,
                modifier = Modifier
                    .size(120.dp)
                    .aspectRatio(1f)
                    .offset(y = 30.dp)
                    .clip(CircleShape)
                    .border(4.dp, backgroundColor, CircleShape),
            )
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
