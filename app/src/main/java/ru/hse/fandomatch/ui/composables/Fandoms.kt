package ru.hse.fandomatch.ui.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.hse.fandomatch.R
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.ui.navigation.EndIconState
import ru.hse.fandomatch.ui.utils.getColor


@Composable
fun FandomPlate(
    modifier: Modifier = Modifier,
    name: String,
    category: FandomCategory,
    maxWidth: Dp = 150.dp,
    endIcon: EndIconState? = null,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(category.getColor())
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .widthIn(max = maxWidth),
                    text = name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 14.sp,
            style = MaterialTheme.typography.bodyMedium
        )
            endIcon?.let {
                IconButton(
                    modifier = Modifier.size(18.dp),
                    onClick = endIcon.onClick
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = endIcon.iconId),
                        contentDescription = stringResource(endIcon.descriptionId),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FandomGrid(
    fandoms: List<Fandom>,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    endIcon: EndIconState? = null,
    endIconAction: ((Fandom) -> Unit)? = null,
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth(),
        maxLines = maxLines,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        fandoms.forEach {
            val endIcon = endIconAction?.let { action ->
                endIcon?.copy(
                    onClick = { action(it) }
                )
            }
            FandomPlate(
                name = it.name,
                category = it.category,
                endIcon = endIcon,
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

@Composable
fun FandomInput(
    foundFandoms: List<Fandom>,
    selectedFandoms: List<Fandom>,
    onFandomAdded: (Fandom) -> Unit,
    onFandomRemoved: (Fandom) -> Unit,
    onSearch: (String?) -> Unit,
    areFandomsLoading: Boolean,
) {
    var fandomInput by remember { mutableStateOf("") }
    var showDropdown by remember { mutableStateOf(foundFandoms.isNotEmpty()) }

    fun clear() {
        fandomInput = ""
        showDropdown = false
        onSearch(null)
    }

    LaunchedEffect(foundFandoms, fandomInput) {
        if (foundFandoms.isNotEmpty() && fandomInput.isNotBlank()) {
            showDropdown = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top
    ) {
        TextField(
            value = fandomInput,
            onValueChange = { input -> fandomInput = input },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    showDropdown = true
                    onSearch(fandomInput)
                }
            ),
            placeholder = { Text(stringResource(R.string.fandom_filter_input_fandom_name)) },
            trailingIcon = {
                if (showDropdown || fandomInput.isNotBlank())
                    IconButton(
                        onClick = { clear() }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.clear_search_icon_description),
                            modifier = Modifier.size(24.dp)
                        )
                    }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (showDropdown) {
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 200.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(0.5f))
            ) {
                items(foundFandoms) { fandom ->
                    Text(
                        text = fandom.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onFandomAdded(fandom)
                                clear()
                            }
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (foundFandoms.isEmpty()) {
                    item {
                        if (areFandomsLoading) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.fandom_filter_no_fandoms_found),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    }
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

        FandomGrid(
            fandoms = selectedFandoms,
            modifier = Modifier.fillMaxWidth(),
            endIcon = EndIconState(
                iconId = R.drawable.ic_close,
                onClick = {},
                descriptionId = R.string.fandom_filter_delete_fandom_icon_description
            ),
            endIconAction = { fandom -> onFandomRemoved(fandom) }
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewFandomCarouselWithDropdown() {
    val sample = List(12) { i -> Fandom(name = "Fandom $i", category = FandomCategory.OTHER, id = i) }
    FandomCarouselWithDropdown(fandoms = sample, modifier = Modifier.padding(16.dp))
}
