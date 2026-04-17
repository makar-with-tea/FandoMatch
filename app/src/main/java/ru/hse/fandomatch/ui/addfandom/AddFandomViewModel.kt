package ru.hse.fandomatch.ui.addfandom

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.checkDescriptionLength
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.checkFandomNameLength
import ru.hse.fandomatch.domain.usecase.fandoms.RequestNewFandomUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserIdUseCase

class AddFandomViewModel(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val requestNewFandomUseCase: RequestNewFandomUseCase,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {
    private val _state: MutableStateFlow<AddFandomState> =
        MutableStateFlow(AddFandomState.Main())
    val state: StateFlow<AddFandomState>
        get() = _state
    private val _action = MutableStateFlow<AddFandomAction?>(null)
    val action: StateFlow<AddFandomAction?>
        get() = _action

    fun obtainEvent(event: AddFandomEvent) {
        when (event) {
            is AddFandomEvent.NameChanged -> nameChanged(event.name)
            is AddFandomEvent.CategoryChanged -> categoryChanged(event.category)
            is AddFandomEvent.DescriptionChanged -> descriptionChanged(event.description)
            AddFandomEvent.SendButtonClicked -> send()
            AddFandomEvent.Clear -> clear()
        }
    }

    private fun nameChanged(name: String) {
        if (!name.checkFandomNameLength()) {
            _state.value = (_state.value as AddFandomState.Main).copy(
                name = name,
                nameError = AddFandomState.AddFandomError.NAME_LENGTH,
                isButtonAvailable = false
            )
            return
        }
        _state.value = (_state.value as AddFandomState.Main).copy(
            name = name,
            nameError = AddFandomState.AddFandomError.IDLE,
            isButtonAvailable = true
        )
    }

    private fun categoryChanged(category: FandomCategory) {
        _state.value = (_state.value as AddFandomState.Main).copy(category = category)
    }

    private fun descriptionChanged(description: String) {
        if (!description.checkDescriptionLength()) {
            _state.value = (_state.value as AddFandomState.Main).copy(
                description = description,
                nameError = AddFandomState.AddFandomError.DESCRIPTION_LENGTH,
                isButtonAvailable = false
            )
            return
        }
        _state.value = (_state.value as AddFandomState.Main).copy(description = description)
    }

    private fun send() {
        val currentState = _state.value as AddFandomState.Main
        _state.value = currentState.copy(
            isLoading = true
        )

        if (!currentState.name.checkFandomNameLength()) {
            _state.value = currentState.copy(
                nameError = AddFandomState.AddFandomError.NAME_LENGTH,
                isLoading = false,
                isButtonAvailable = false
            )
            return
        }

        if (!currentState.description.checkDescriptionLength()) {
            _state.value = currentState.copy(
                descriptionError = AddFandomState.AddFandomError.DESCRIPTION_LENGTH,
                isLoading = false,
                isButtonAvailable = false
            )
            return
        }

        viewModelScope.launch(dispatcherIO) {
            val userId = getUserIdUseCase.execute()
            userId?.let {
                val result = requestNewFandomUseCase.execute(
                    userId = userId,
                    name = currentState.name,
                    category = currentState.category,
                    description = currentState.description,
                )
                if (result.isFailure) {
                    Log.e(
                        "AddFandomViewModel",
                        "Failed to request new fandom",
                        result.exceptionOrNull()
                    )
                    withContext(dispatcherMain) {
                        _state.value = currentState.copy(
                            isLoading = false
                        )
                        _action.value = AddFandomAction.ShowNetworkErrorToast
                    }
                } else {
                    withContext(dispatcherMain) {
                        _state.value = currentState.copy(
                            isLoading = false
                        )
                        _action.value = AddFandomAction.ShowSuccessToastAndGoBack
                    }
                }
            } ?: withContext(dispatcherMain) {
                Log.e(
                    "AddFandomViewModel",
                    "Failed to get user id"
                )
                _state.value = currentState.copy(
                    isLoading = false
                )
                _action.value = AddFandomAction.ShowNetworkErrorToast
            }
        }
    }

    private fun clear() {
        _state.value = AddFandomState.Main()
        _action.value = null
    }
}
