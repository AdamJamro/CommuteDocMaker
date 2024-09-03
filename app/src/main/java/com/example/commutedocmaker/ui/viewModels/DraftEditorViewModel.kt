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
    private val _draftDataPatch: MutableStateFlow<List<DraftDataPatch>> = injectDataPatch
    val draftDataPatch: StateFlow<List<DraftDataPatch>> = _draftDataPatch.asStateFlow()
    private val _title: MutableState<String> = mutableStateOf(initTitle)
    val title by derivedStateOf { _title.value }

    private fun updateDraftDataPatchAt(patchIndex: Int, patch: DraftDataPatch) {
        _draftDataPatch.update {
            _draftDataPatch.value.toMutableList().apply {
                this[patchIndex] = patch
            }
        }
    }

    fun addDraftDataPatch() {
        _draftDataPatch.update {
            _draftDataPatch.value + DraftDataPatch()
        }
    }

    fun addDraftDataPatch(patch: DraftDataPatch) {
        _draftDataPatch.update {
            _draftDataPatch.value + patch
        }
    }

    fun removeDraftDataPatchAt(patchIndex: Int) {
        _draftDataPatch.update {
            _draftDataPatch.value.toMutableList().apply {
                removeAt(patchIndex)
            }
        }
    }

    fun removeDraftDataPatch(patch: DraftDataPatch) {
        _draftDataPatch.update {
            _draftDataPatch.value - patch
        }
    }

    fun onUiEvent(event: DraftEditorEvent): DraftEntry? {
        when (event) {
            is Submit ->
                return DraftEntry(
                    title = _title.value,
                    contentDescription = "Draft content",
                    draftDataPatches = _draftDataPatch.value
                )
            is Restore ->
                TODO("delete Restore")
            is SelectedDatesChanged -> {
                _draftDataPatch.value[event.patchIndex]
                    .copy(dates = event.dates.toList())
                    .also { updateDraftDataPatchAt(event.patchIndex, it) }
            }
            is BaseAddressChanged -> {
                _draftDataPatch.value[event.patchIndex]
                    .copy(baseAddress = event.address)
                    .also { updateDraftDataPatchAt(event.patchIndex, it) }
            }
            is DestinationAddressChanged -> {
                _draftDataPatch.value[event.patchIndex]
                    .copy(destinationAddress = event.address)
                    .also { updateDraftDataPatchAt(event.patchIndex, it) }
            }
            is DistanceTravelledChanged -> {
                _draftDataPatch.value[event.patchIndex]
                    .copy(distanceTravelled = event.distance)
                    .also { updateDraftDataPatchAt(event.patchIndex, it) }
            }
            is ShiftStartTimeChanged -> {
                _draftDataPatch.value[event.patchIndex]
                    .copy(shiftStartTime = event.time)
                    .also { updateDraftDataPatchAt(event.patchIndex, it) }
            }
            is ShiftEndTimeChanged -> {
                _draftDataPatch.value[event.patchIndex]
                    .copy(shiftEndTime = event.time)
                    .also { updateDraftDataPatchAt(event.patchIndex, it) }
            }
            is ForthRouteIncludedChanged -> {
                _draftDataPatch.value[event.patchIndex]
                    .copy(forthRouteIncluded = event.included)
                    .also { updateDraftDataPatchAt(event.patchIndex, it) }
            }
            is BackRouteIncludedChanged -> {
                _draftDataPatch.value[event.patchIndex]
                    .copy(backRouteIncluded = event.included)
                    .also { updateDraftDataPatchAt(event.patchIndex, it) }
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