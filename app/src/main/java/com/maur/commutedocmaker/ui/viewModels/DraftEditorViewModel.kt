package com.maur.commutedocmaker.ui.viewModels

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maur.commutedocmaker.dataSource.draftEntry.DraftDataPatch
import com.maur.commutedocmaker.dataSource.draftEntry.DraftEntry
import com.maur.commutedocmaker.ui.viewModels.DraftEditorEvent.*
import com.maur.commutedocmaker.xlsx.sanitizeFileName
import com.maur.commutedocmaker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    data class Submit(val resourceContext: Context?) : DraftEditorEvent()
}



class DraftEditorViewModel(
    injectTitle: String = "empty title",
    injectScope: CoroutineScope? = null,
    injectDataPatch: MutableStateFlow<List<DraftDataPatch>> = MutableStateFlow(listOf(DraftDataPatch())),
    ): ViewModel() {
        private val scope = injectScope ?: viewModelScope
        private val _draftDataPatches: MutableStateFlow<List<DraftDataPatch>> = injectDataPatch
        val draftDataPatches: StateFlow<List<DraftDataPatch>> = _draftDataPatches.asStateFlow()
        private val _title: MutableState<String> = mutableStateOf(injectTitle)
        val title by derivedStateOf { _title.value }
        private fun getRes(resourceContext: Context?, resId: Int): String = resourceContext?.resources?.getString(resId) ?: ""

        private fun updateDraftDataPatchAt(patchIndex: Int, patch: DraftDataPatch) {
            _draftDataPatches.update {
                _draftDataPatches.value
                    .toMutableList()
                    .also { it[patchIndex] = patch }
                    .toList()
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
                        contentDescription = createDraftEntryDescription(_draftDataPatches.value, event?.resourceContext),
                        draftDataPatches = _draftDataPatches.value
                    )
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
                    val distance = event.distance
                        .replace(",", ".")
                        .toFloatOrNull()
                        ?: 0f
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

        private fun createDraftEntryDescription(draft: List<DraftDataPatch>, resourceContext: Context?): String {
            val dates = mutableListOf<LocalDate>()
            var payableAmount = 0f
            draft.forEach { patch ->
                patch.dates.forEach{ date ->
                    dates.add(date)
                    payableAmount += patch.cashPerKilometer * patch.distanceTravelled
                }
            }
            val payableAmountFormatted = String.format("%.2f", payableAmount)
            val firstDate = dates.minOrNull() ?: return getRes(resourceContext, R.string.empty_draft_content)
            val lastDate = dates.maxOrNull() ?: return getRes(resourceContext, R.string.empty_draft_content)

            val firstDateFormatted = firstDate.format(DateTimeFormatter.ofPattern("EEEE dd LLLL").withZone(ZoneId.systemDefault()))
            val lastDateFormatted = lastDate.format(DateTimeFormatter.ofPattern("EEEE dd LLLL").withZone(ZoneId.systemDefault()))

            val description = "${getRes(resourceContext, R.string.period)}:" +
                    "\n$firstDateFormatted - $lastDateFormatted" +
                    "\n${getRes(resourceContext, R.string.draft_description_cash)}: $payableAmountFormatted€"
            return description
        }
}

@Suppress("UNCHECKED_CAST")
class DraftEditorViewModelFactory(
    private val title: String,
    private val scope: CoroutineScope?,
    private val existingDraftEntry: DraftEntry? = null,
//    private val draftIndex: Int = -1
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DraftEditorViewModel::class.java)) {
            if (existingDraftEntry != null) {
                try {
                    val dataPatch = existingDraftEntry.draftDataPatches
                    return DraftEditorViewModel(
                        injectTitle = existingDraftEntry.title,
                        injectScope = scope,
                        injectDataPatch = MutableStateFlow(dataPatch),
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