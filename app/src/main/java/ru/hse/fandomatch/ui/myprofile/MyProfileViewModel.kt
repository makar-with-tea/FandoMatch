package ru.hse.fandomatch.ui.myprofile

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.hse.fandomatch.data.mock.mockUser

class MyProfileViewModel(
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {

    private val _state: MutableStateFlow<MyProfileState> = MutableStateFlow(MyProfileState.Main(mockUser))
    val state: StateFlow<MyProfileState> get() = _state
    private val _action = MutableStateFlow<MyProfileAction?>(null)
    val action: StateFlow<MyProfileAction?> get() = _action

    fun obtainEvent(event: MyProfileEvent) {
        Log.i("ProfileViewModel", "Obtained event: $event")
        when (event) {
            else -> Unit // whatever
        }
    }
}
