package ru.hse.fandomatch.ui.addfandom

import ru.hse.fandomatch.domain.model.FandomCategory

sealed class AddFandomState {
    enum class AddFandomError {
        NAME_LENGTH,
        DESCRIPTION_LENGTH,
        NETWORK,
        IDLE
    }
    data class Main(
        val name: String = "",
        val category: FandomCategory = FandomCategory.OTHER,
        val description: String = "",
        val nameError: AddFandomError = AddFandomError.IDLE,
        val descriptionError: AddFandomError = AddFandomError.IDLE,
        val isButtonAvailable: Boolean = true,
        val isLoading: Boolean = false
    ) : AddFandomState()
}

sealed class AddFandomEvent {
    data class NameChanged(val name: String): AddFandomEvent()
    data class CategoryChanged(val category: FandomCategory): AddFandomEvent()
    data class DescriptionChanged(val description: String): AddFandomEvent()
    data object SendButtonClicked: AddFandomEvent()
    data object Clear: AddFandomEvent()
}

sealed class AddFandomAction {
    data object ShowSuccessToastAndGoBack : AddFandomAction()
    data object ShowNetworkErrorToast : AddFandomAction()
}
