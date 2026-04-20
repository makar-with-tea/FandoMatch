package ru.hse.fandomatch.ui.addfandom

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.utils.getColor
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.composables.MyTextField
import ru.hse.fandomatch.utils.toStringId

@Composable
fun AddFandomScreen(
    navigateBack: () -> Unit,
    viewModel: AddFandomViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()

    Log.d("AuthorizationScreen", "State: $state")
    when (action.value) {
        AddFandomAction.ShowSuccessToastAndGoBack -> {
            Toast.makeText(
                LocalContext.current,
                R.string.add_fandom_success,
                Toast.LENGTH_SHORT
            ).show()
            navigateBack()
            viewModel.obtainEvent(AddFandomEvent.Clear)
        }

        AddFandomAction.ShowNetworkErrorToast -> {
            Toast.makeText(
                LocalContext.current,
                R.string.network_error,
                Toast.LENGTH_SHORT
            ).show()
            viewModel.obtainEvent(AddFandomEvent.Clear)
        }

        null -> {}
    }

    when (state.value) {
        is AddFandomState.Main -> {
            MainState(
                state.value as AddFandomState.Main,
                onFandomNameChanged = { viewModel.obtainEvent(AddFandomEvent.NameChanged(it)) },
                onFandomCategoryChanged = { viewModel.obtainEvent(AddFandomEvent.CategoryChanged(it)) },
                onFandomDescriptionChanged = { viewModel.obtainEvent(AddFandomEvent.DescriptionChanged(it)) },
                onSendButtonClick = { viewModel.obtainEvent(AddFandomEvent.SendButtonClicked) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainState(
    state: AddFandomState.Main,
    onFandomNameChanged: (String) -> Unit,
    onFandomCategoryChanged: (FandomCategory) -> Unit,
    onFandomDescriptionChanged: (String) -> Unit,
    onSendButtonClick: () -> Unit,
) {
    val name: MutableState<String> = remember { mutableStateOf(state.name) }
    val category: MutableState<FandomCategory> = remember { mutableStateOf(state.category) }
    val description: MutableState<String> = remember { mutableStateOf(state.description) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    stringResource(id = R.string.add_fandom_description),
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                )
            }

            item {
                MyTextField(
                    value = name.value,
                    label = stringResource(id = R.string.fandom_name_label),
                    isError = state.nameError != AddFandomState.AddFandomError.IDLE,
                    errorText = if (state.nameError != AddFandomState.AddFandomError.NETWORK)
                        state.nameError.toText() else null
                ) {
                    name.value = it
                    onFandomNameChanged(it)
                }
            }

            item {
                Text(
                    stringResource(id = R.string.fandom_category_label),
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                )
            }

            item {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .menuAnchor()
                            .width(200.dp)
                            .clip(CircleShape)
                            .background(
                                category.value.getColor()
                            )
                            .clickable { expanded = true }
                            .padding(horizontal = 2.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(id = category.value.toStringId()),
                            modifier = Modifier,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    }
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.exposedDropdownSize(true)
                    ) {
                        FandomCategory.entries.forEach { newCategory ->
                            DropdownMenuItem(
                                text = { Text(stringResource(id = newCategory.toStringId())) },
                                onClick = {
                                    category.value = newCategory
                                    onFandomCategoryChanged(newCategory)
                                    expanded = false
                                },
                                modifier = Modifier.background(newCategory.getColor()),
                            )
                        }
                    }
                }
            }

            item {
                MyTextField(
                    value = description.value,
                    label = stringResource(id = R.string.fandom_description_label),
                    isError = state.descriptionError != AddFandomState.AddFandomError.IDLE,
                    errorText = if (state.descriptionError != AddFandomState.AddFandomError.NETWORK)
                        state.descriptionError.toText() else null,
                    hideOnDone = false
                ) {
                    description.value = it
                    onFandomDescriptionChanged(it)
                }
            }

            item {
                Button(
                    onClick = { onSendButtonClick() },
                    enabled = state.descriptionError == AddFandomState.AddFandomError.IDLE &&
                            state.nameError == AddFandomState.AddFandomError.IDLE
                ) {
                    Text(stringResource(id = R.string.send_request_button))
                }
            }
        }
    }

    if (state.isLoading) {
        LoadingBlock()
    }
}

@Composable
private fun AddFandomState.AddFandomError.toText() = when (this) {
    AddFandomState.AddFandomError.NAME_LENGTH ->
        stringResource(id = R.string.fandom_name_length_error)
    AddFandomState.AddFandomError.DESCRIPTION_LENGTH ->
        stringResource(id = R.string.fandom_description_length_error)
    AddFandomState.AddFandomError.NETWORK ->
        stringResource(id = R.string.network_error)
    AddFandomState.AddFandomError.IDLE -> ""
}

@Composable
fun LoadingState() {
    LoadingBlock()
}

@Preview(showBackground = true)
@Composable
fun AddFandomScreenPreview() {
    AddFandomScreen(
            {}
    )
}
