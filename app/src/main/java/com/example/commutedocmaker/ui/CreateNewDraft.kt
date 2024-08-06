package com.example.commutedocmaker.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.commutedocmaker.R
import com.example.commutedocmaker.dataSource.SpreadsheetPartialData
import com.example.commutedocmaker.ui.viewModels.CreateNewDraftViewModel
import com.example.commutedocmaker.ui.viewModels.DraftEditorEvent
import kotlin.math.floor
import com.example.commutedocmaker.ui.uiUtils.DatePicker
import com.example.commutedocmaker.ui.viewModels.DraftEditorEvent.ShiftEndTimeChanged
import com.example.commutedocmaker.ui.viewModels.DraftEditorEvent.ShiftStartTimeChanged
import java.time.LocalDate
import com.example.commutedocmaker.ui.uiUtils.CommuteClockTimeRangeSlider
import com.example.commutedocmaker.ui.uiUtils.CommuteClockTimeRangeSliderWrapper
import java.time.ZoneId
import java.util.*

@Composable
fun CreateNewDraft (
    modifier: Modifier = Modifier,
    viewModel: CreateNewDraftViewModel = remember { CreateNewDraftViewModel() },
    onFinishActivity: (filepath: String?) -> Unit
) {
    val context = LocalContext.current
    val uiState = viewModel.uiCollectedData.collectAsState()
    val forthSwitchChecked = remember { mutableStateOf(true) }
    val backSwitchChecked = remember { mutableStateOf(true) }
    var distanceTravelledAsString by rememberSaveable { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.value.baseAddress,
            onValueChange = { viewModel.onEvent(DraftEditorEvent.BaseAddressChanged(it)) },
            label = { Text(stringResource(id = R.string.base_address_string_representation)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.value.destinationAddress,
            onValueChange = { viewModel.onEvent(DraftEditorEvent.DestinationAddressChanged(it)) },
            label = { Text(stringResource(id = R.string.destination_address_string_representation)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.value.distanceTravelled,
            onValueChange = { viewModel.onEvent(DraftEditorEvent.DistanceTravelledChanged(it)) },
            label = { Text(stringResource(id = R.string.distance_travelled_string_representation)) },
            modifier = Modifier.fillMaxWidth()
        )

        Row {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = stringResource(id = R.string.forth_commute_included_string_representation)
            )
            Switch(
                checked = forthSwitchChecked.value,
                onCheckedChange = { forthSwitchChecked.value = it },
            )
        }

        Row {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = stringResource(id = R.string.back_commute_included_string_representation)
            )
            Switch(
                checked = backSwitchChecked.value,
                onCheckedChange = { backSwitchChecked.value = it },
            )
        }

        CommuteClockTimeRangeSliderWrapper(
            onUpdateTimeRange = { startTime, endTime ->
                viewModel.onEvent(ShiftStartTimeChanged(startTime))
                viewModel.onEvent(ShiftEndTimeChanged(endTime))
            }
        )

        var showDatePickerDialog by remember { mutableStateOf(false) }
        var selectedDate by remember { mutableStateOf<Date?>(null) }
        var selectedDates = remember { mutableStateListOf<Date>() }


        Button(
            onClick = { showDatePickerDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select date")
        }
        if (showDatePickerDialog) {
            DatePicker(
                onDateSelected = { selectedDate = Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant()) },
                onDismissRequest = { showDatePickerDialog = false },
                selectedDates = selectedDates
            )
        }


        Spacer(modifier = Modifier.weight(1f))
        Button(
            modifier = Modifier.align(Alignment.End),
            onClick = {
                try {
                    viewModel.submitDraft(uiState.value)
                } catch (e: NumberFormatException) {
                    Toast.makeText(null, "Invalid data!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val spreadsheetPartialData = SpreadsheetPartialData(
                    title = "Title",
                    content = "Content",
                    uiCollectedData = uiState.value
                )
                val filepath = spreadsheetPartialData.filepath
                onFinishActivity(filepath)
            },
        ) {
            Text("Submit")
        }
    }
}

fun convertToStringTime(time: Float): String {
    var hours = floor(time / 4).toInt()
    if (hours > 23) {
        hours -= 24
    }
    val minutes = 15 * (time % 4).toInt()
    val hoursAsString: String = hours.toString().padStart(2, '0')
    val minutesAsString = minutes.toString().padStart(2, '0')
    val resultTime = "$hoursAsString:$minutesAsString"
    return resultTime
}
