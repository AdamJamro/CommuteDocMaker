package com.example.commutedocmaker.ui.viewModels

import android.location.Address
import androidx.lifecycle.ViewModel
import com.example.commutedocmaker.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

sealed class DraftEditorEvent {

    object Submit : DraftEditorEvent()
}

enum class DraftFormField(
    val fieldAsString: String = "",
) {
    BASE_ADDRESS(fieldAsString = R.string.base_address_string_representation.toString()),
    DESTINATION_ADDRESS(fieldAsString = R.string.destination_address_string_representation.toString()),
    DISTANCE_TRAVELLED(fieldAsString = R.string.distance_travelled_string_representation.toString()),
    SHIFT_TIME_LENGTH(fieldAsString = R.string.shift_time_length_string_representation.toString()),
    SHIFT_START_FULL_DATE(fieldAsString = R.string.shift_start_full_date_string_representation.toString())
}

data class UIState(
    val baseAddress: String = "",
    val destinationAddress: String = "",
    val distanceTravelled: Float = 0f,
    val shiftTimeLength: Float = 0f,
    val shiftStartFullDate: Date = Date(),
)

class CreateNewDraftViewModel: ViewModel() {

        private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState())
        val uiState: StateFlow<UIState> = _uiState.asStateFlow()

        fun setBaseAddress(address: String) {
            _uiState.value.copy(baseAddress = address).also { _uiState.value = it }
        }

        fun setDestinationAddress(address: String) {
            _uiState.value.copy(destinationAddress = address).also { _uiState.value = it }
        }

        fun setDistanceTravelled(distance: Float) {
            _uiState.value.copy(distanceTravelled = distance).also { _uiState.value = it }
        }

        fun setShiftTimeLength(time: Float) {
            _uiState.value.copy(shiftTimeLength = time).also { _uiState.value = it }
        }

        fun setShiftStartFullDate(date: Date) {
            _uiState.value.copy(shiftStartFullDate = date).also { _uiState.value = it }
        }

        fun submitDraft(uiState: UIState) {

        }


}