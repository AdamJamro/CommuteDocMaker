package com.example.commutedocmaker.ui.viewModels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.commutedocmaker.dataSource.DraftDataPatch
import com.example.commutedocmaker.dataSource.DraftEntry
import com.example.commutedocmaker.ui.viewModels.DraftEditorEvent.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class DraftEditorEvent {
    data class BaseAddressChanged(val address: String) : DraftEditorEvent()
    data class DestinationAddressChanged(val address: String) : DraftEditorEvent()
    data class DistanceTravelledChanged(val distance: String) : DraftEditorEvent()
    data class ShiftStartTimeChanged(val time: String) : DraftEditorEvent()
    data class ShiftEndTimeChanged(val time: String) : DraftEditorEvent()
    data class ForthRouteIncludedChanged(val included: Boolean) : DraftEditorEvent()
    data class BackRouteIncludedChanged(val included: Boolean) : DraftEditorEvent()
    data class DraftNameChanged(val title: String) : DraftEditorEvent()
    data class SelectedDatesChanged(val dates: List<LocalDate>) : DraftEditorEvent()
    data object Submit : DraftEditorEvent()

    data class Restore(val draftSavedRawData: DraftEntry) : DraftEditorEvent()
}



class DraftEditorViewModel(
    initTitle: String,
    private val scope: CoroutineScope,
    private val _draftDataPatch: MutableStateFlow<DraftDataPatch> = MutableStateFlow(DraftDataPatch()),
//    private val draftEntryIndex: Int = -1 // -1 for no overwriting
): ViewModel() {

    val draftDataPatch: StateFlow<DraftDataPatch> = _draftDataPatch.asStateFlow()
    private val _title: MutableState<String> = mutableStateOf(initTitle)
    val getDraftTitle = { _title.value }

    fun onEvent(event: DraftEditorEvent): DraftEntry? {
        when (event) {
            is Submit ->
                return DraftEntry(
                    title = _title.value,
                    content = "Draft content",
                    draftDataPatches = listOf(_draftDataPatch.value)
                )
            is Restore ->
                TODO("delete Restore")
            is SelectedDatesChanged -> {
                scope.launch {
                    _draftDataPatch.value.copy(dates = event.dates).also { _draftDataPatch.value = it }
                }
            }
            is BaseAddressChanged -> {
                scope.launch {
                    _draftDataPatch.value.copy(baseAddress = event.address).also { _draftDataPatch.value = it }
                }
            }
            is DestinationAddressChanged -> {
                scope.launch {
                    _draftDataPatch.value.copy(destinationAddress = event.address).also { _draftDataPatch.value = it }
                }
            }
            is DistanceTravelledChanged -> {
                scope.launch {
                    _draftDataPatch.value.copy(distanceTravelled = event.distance).also { _draftDataPatch.value = it }
                }
            }
            is ShiftStartTimeChanged -> {
                scope.launch {
                    _draftDataPatch.value.copy(shiftStartTime = event.time).also { _draftDataPatch.value = it }
                }
            }
            is ShiftEndTimeChanged -> {
                scope.launch {
                    _draftDataPatch.value.copy(shiftEndTime = event.time).also { _draftDataPatch.value = it }
                }
            }
            is ForthRouteIncludedChanged -> {
                scope.launch {
                    _draftDataPatch.value.copy(forthRouteIncluded = event.included).also { _draftDataPatch.value = it }
                }
            }
            is BackRouteIncludedChanged -> {
                scope.launch {
                    _draftDataPatch.value.copy(backRouteIncluded = event.included).also { _draftDataPatch.value = it }
                }
            }
            is DraftNameChanged -> {
                scope.launch {
                    _title.value = event.title
                }
            }
        }
        return null
    }
}

@Suppress("UNCHECKED_CAST")
class DraftEditorViewModelFactory(
    private val title: String,
    private val scope: CoroutineScope,
    private val existingDraftEntry: DraftEntry? = null,
//    private val draftIndex: Int = -1
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DraftEditorViewModel::class.java)) {
            if (existingDraftEntry != null) {
                try {
                    val dataPatch = existingDraftEntry.draftDataPatches.first()
                    return DraftEditorViewModel(
                        title,
                        scope,
                        MutableStateFlow(dataPatch),
//                        draftIndex
                    ) as T
                } catch (e: NoSuchElementException) {
                    //pass
                }
            }
            return DraftEditorViewModel(title, scope) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}