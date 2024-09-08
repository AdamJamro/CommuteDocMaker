package com.example.commutedocmaker.ui.viewModels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.commutedocmaker.dataSource.draftEntry.DraftDataPatch
import com.example.commutedocmaker.dataSource.draftEntry.DraftEntry
import com.example.commutedocmaker.ui.viewModels.DraftEditorEvent.*
import com.example.commutedocmaker.xlsx.sanitizeFileName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class DraftEditorEvent {
    data class BaseAddressChanged(val address: String, val patchIndex: Int) : DraftEditorEvent()
    data class DestinationAddressChanged(val address: String, val patchIndex: Int) : DraftEditorEvent()
    data class DistanceTravelledChanged(val distance: String, val patchIndex: Int) : DraftEditorEvent()
    data class ShiftStartTimeChanged(val time: String, val patchIndex: Int) : DraftEditorEvent()
    data class ShiftEndTimeChanged(val time: String, val patchIndex: Int) : DraftEditorEvent()
    data class ForthRouteIncludedChanged(val included: Boolean, val patchIndex: Int) : DraftEditorEvent()
    data class BackRouteIncludedChanged(val included: Boolean, val patchIndex: Int) : DraftEditorEvent()
    data class SelectedDatesChanged(val dates: Collection<LocalDate>, val patchIndex: Int) : DraftEditorEvent()
    data class CashPerKilometerChanged(val cashPerKilometer: Float, val patchIndex: Int) : DraftEditorEvent()
    data class DraftNameChanged(val title: String) : DraftEditorEvent()
    data object Submit : DraftEditorEvent()

    //TODO("delete Restore")
    data class Restore(val draftSavedRawData: DraftEntry) : DraftEditorEvent()
}



class DraftEditorViewModel(
    initTitle: String,
    private val scope: CoroutineScope,
    injectDataPatch: MutableStateFlow<List<DraftDataPatch>> = MutableStateFlow(listOf(DraftDataPatch()))
//    private val draftEntryIndex: Int = -1 // -1 for no overwriting
): ViewModel() {
    private val _draftDataPatches: MutableStateFlow<List<DraftDataPatch>> = injectDataPatch
    val draftDataPatches: StateFlow<List<DraftDataPatch>> = _draftDataPatches.asStateFlow()
    private val _title: MutableState<String> = mutableStateOf(initTitle)
    val title by derivedStateOf { _title.value }

    private fun updateDraftDataPatchAt(patchIndex: Int, patch: DraftDataPatch) {
        _draftDataPatches.update {
            _draftDataPatches.value.toMutableList().apply {
                this[patchIndex] = patch
            }
        }
    }

    fun addDraftDataPatch() {
        _draftDataPatches.update {
            _draftDataPatches.value + DraftDataPatch()
        }
    }

    fun addDraftDataPatch(patch: DraftDataPatch) {
        _draftDataPatches.update {
            _draftDataPatches.value + patch
        }
    }

    fun removeDraftDataPatchAt(patchIndex: Int) {
        _draftDataPatches.update {
            _draftDataPatches.value.toMutableList().apply {
                removeAt(patchIndex)
            }.toList()
        }
    }

    fun removeDraftDataPatch(patch: DraftDataPatch) {
        _draftDataPatches.update {
            _draftDataPatches.value - patch
        }
    }

    fun onUiEvent(event: DraftEditorEvent): DraftEntry? {
        when (event) {
            is Submit ->
                return DraftEntry(
                    title = _title.value,
                    contentDescription = "Draft content",
                    draftDataPatches = _draftDataPatches.value
                )
            is Restore ->
                TODO("delete Restore")
            is SelectedDatesChanged -> {
                _draftDataPatches.value[event.patchIndex]
                    .copy(dates = event.dates.toList())
                    .also { updateDraftDataPatchAt(event.patchIndex, it) }
            }
            is BaseAddressChanged -> {
                _draftDataPatches.value[event.patchIndex]
                    .copy(baseAddress = event.address)
                    .also { updateDraftDataPatchAt(event.patchIndex, it) }
            }
            is DestinationAddressChanged -> {
                _draftDataPatches.value[event.patchIndex]
                    .copy(destinationAddress = event.address)
                    .also { updateDraftDataPatchAt(event.patchIndex, it) }
            }
            is DistanceTravelledChanged -> {
                val distance = event.distance.toFloatOrNull() ?: 0f
                _draftDataPatches.value[event.patchIndex]
                    .copy(distanceTravelled = distance)
                    .also { updateDraftDataPatchAt(event.patchIndex, it) }
            }
            is ShiftStartTimeChanged -> {
                _draftDataPatches.value[event.patchIndex]
                    .copy(shiftStartTime = event.time)
                    .also { updateDraftDataPatchAt(event.patchIndex, it) }
            }
            is ShiftEndTimeChanged -> {
                _draftDataPatches.value[event.patchIndex]
                    .copy(shiftEndTime = event.time)
                    .also { updateDraftDataPatchAt(event.patchIndex, it) }
            }
            is ForthRouteIncludedChanged -> {
                _draftDataPatches.value[event.patchIndex]
                    .copy(forthRouteIncluded = event.included)
                    .also { updateDraftDataPatchAt(event.patchIndex, it) }
            }
            is BackRouteIncludedChanged -> {
                _draftDataPatches.value[event.patchIndex]
                    .copy(backRouteIncluded = event.included)
                    .also { updateDraftDataPatchAt(event.patchIndex, it) }
            }
            is CashPerKilometerChanged -> {
                _draftDataPatches.value[event.patchIndex]
                    .copy(cashPerKilometer = event.cashPerKilometer)
                    .also { updateDraftDataPatchAt(event.patchIndex, it) }
            }
            is DraftNameChanged -> {
                scope.launch {
                    _title.value = sanitizeFileName(event.title)
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
                    val dataPatch = existingDraftEntry.draftDataPatches
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