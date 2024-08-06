package com.example.commutedocmaker.ui.viewModels

import androidx.lifecycle.ViewModel
import com.example.commutedocmaker.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

sealed class DraftEditorEvent() {
    data class BaseAddressChanged(val address: String) : DraftEditorEvent()
    data class DestinationAddressChanged(val address: String) : DraftEditorEvent()
    data class DistanceTravelledChanged(val distance: String) : DraftEditorEvent()
    data class ShiftStartTimeChanged(val time: String) : DraftEditorEvent()
    data class ShiftEndTimeChanged(val time: String) : DraftEditorEvent()
    data class ForthRouteIncludedChanged(val included: Boolean) : DraftEditorEvent()
    data class BackRouteIncludedChanged(val included: Boolean) : DraftEditorEvent()
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

data class UICollectedData(
    val baseAddress: String = "",
    val destinationAddress: String = "",
    val distanceTravelled: String = "",
    val shiftStartTime: String = "",
    val shiftEndTime: String = "",
    val forthRouteIncluded: Boolean = true,
    val backRouteIncluded: Boolean = true
)

class CreateNewDraftViewModel: ViewModel() {

        private val _uiCollectedData: MutableStateFlow<UICollectedData> = MutableStateFlow(UICollectedData())
        val uiCollectedData: StateFlow<UICollectedData> = _uiCollectedData.asStateFlow()

        fun onEvent(event: DraftEditorEvent) {
            when (event) {
                is DraftEditorEvent.Submit -> submitDraft(_uiCollectedData.value)
                is DraftEditorEvent.BaseAddressChanged -> {
                    _uiCollectedData.value.copy(baseAddress = event.address).also { _uiCollectedData.value = it }
                }
                is DraftEditorEvent.DestinationAddressChanged -> {
                    _uiCollectedData.value.copy(destinationAddress = event.address).also { _uiCollectedData.value = it }
                }
                is DraftEditorEvent.DistanceTravelledChanged -> {
                    _uiCollectedData.value.copy(distanceTravelled = event.distance).also { _uiCollectedData.value = it }
                }
                is DraftEditorEvent.ShiftStartTimeChanged -> {
                    _uiCollectedData.value.copy(shiftStartTime = event.time).also { _uiCollectedData.value = it }
                }
                is DraftEditorEvent.ShiftEndTimeChanged -> {
                    _uiCollectedData.value.copy(shiftEndTime = event.time).also { _uiCollectedData.value = it }
                }
                is DraftEditorEvent.ForthRouteIncludedChanged -> {
                    _uiCollectedData.value.copy(forthRouteIncluded = event.included).also { _uiCollectedData.value = it }
                }
                is DraftEditorEvent.BackRouteIncludedChanged -> {
                    _uiCollectedData.value.copy(backRouteIncluded = event.included).also { _uiCollectedData.value = it }
                }
            }
        }

        fun submitDraft(uiCollectedData: UICollectedData): DraftEditorEvent {
            // submit draft
            return DraftEditorEvent.Submit
        }


}