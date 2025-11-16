package ru.hse.fandomatch.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.theme.FandoMatchTheme

@Composable
fun MyTitle(
    text: String
) {
    Text(
        modifier = Modifier.padding(8.dp),
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    )
}

@Composable
fun MyTextField(
    value: String,
    label: String,
    isError: Boolean,
    enabled: Boolean = true,
    errorText: String? = null,
    onValueChange: (String) -> Unit
) {
    TextField(
        modifier = Modifier.padding(8.dp),
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
    value: String,
    label: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    onIconClick: () -> Unit,
    passwordVisibility: Boolean,
    errorText: String? = null
) {
    TextField(
        modifier = Modifier.padding(8.dp),
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
