package ru.hse.fandomatch.ui.editprofile

import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom

sealed class EditProfileState {
    enum class EditProfileError {
        NAME_LENGTH,
        NAME_CONTENT,
        DESCRIPTION_LENGTH,
        PHOTO_UPLOAD,
        CITY_NOT_FOUND,
        NETWORK,
        IDLE,
    }
    
    object Idle : EditProfileState()
    object Loading : EditProfileState()
    data class Main(
        val id: String,
        val name: String,
        val nameError: EditProfileError = EditProfileError.IDLE,
        val login: String,
        val description: String?,
        val descriptionError: EditProfileError = EditProfileError.IDLE,
        val avatarUrl: String?,
        val avatarError: EditProfileError = EditProfileError.IDLE,
        val backgroundUrl: String?,
        val backgroundError: EditProfileError = EditProfileError.IDLE,
        val fandoms: List<Fandom>,
        val foundFandoms: List<Fandom>,
        val areFandomsLoading: Boolean,
        val city: City?,
        val cityError: EditProfileError = EditProfileError.IDLE,
    ) : EditProfileState()
    object Error : EditProfileState()
}

sealed class EditProfileEvent {
    data class NameChanged(val name: String) : EditProfileEvent()
    data class DescriptionChanged(val description: String) : EditProfileEvent()
    data class AvatarChanged(val avatar: ByteArray?) : EditProfileEvent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AvatarChanged

            return avatar.contentEquals(other.avatar)
        }

        override fun hashCode(): Int {
            return avatar?.contentHashCode() ?: 0
        }
    }

    data class BackgroundChanged(val background: ByteArray?) : EditProfileEvent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as BackgroundChanged

            if (!background.contentEquals(other.background)) return false

            return true
        }

        override fun hashCode(): Int {
            return background?.contentHashCode() ?: 0
        }
    }

    data class FandomAdded(val fandom: Fandom) : EditProfileEvent()
    data class FandomRemoved(val fandom: Fandom) : EditProfileEvent()
    data class FandomSearched(val query: String?) : EditProfileEvent()
    data object AddFandomButtonClicked : EditProfileEvent()
    data class CityChanged(val cityName: String) : EditProfileEvent()
    data object SaveButtonClicked : EditProfileEvent()
    data object LoadProfileData : EditProfileEvent()
    data object Clear : EditProfileEvent()
}

sealed class EditProfileAction {
    object NavigateToAddFandom : EditProfileAction()
    object NavigateToMyProfile : EditProfileAction()
}
