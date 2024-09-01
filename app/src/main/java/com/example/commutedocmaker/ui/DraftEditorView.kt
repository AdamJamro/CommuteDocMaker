package com.example.commutedocmaker.ui

import android.app.Activity.RESULT_OK
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.commutedocmaker.R
import com.example.commutedocmaker.dataSource.DraftEntry
import com.example.commutedocmaker.ui.viewModels.DraftEditorViewModel
import kotlin.math.floor
import com.example.commutedocmaker.ui.uiUtils.DatePickerDialog
import com.example.commutedocmaker.ui.uiUtils.CommuteClockTimeRangeSliderWrapper
import com.example.commutedocmaker.ui.viewModels.DraftEditorEvent.*
import java.time.LocalDate
import com.example.commutedocmaker.ui.theme.Typography

@Composable
fun DraftEditorView (
    modifier: Modifier = Modifier,
    viewModel: DraftEditorViewModel,
    onFinishActivity: (
        resultCode: Int,
        draft: DraftEntry?
            ) -> Unit
) {
    val uiState = viewModel.draftDataPatch.collectAsState()
    var showNameChangeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp)
                .clickable(onClick = { showNameChangeDialog = true }),
            text = viewModel.getDraftTitle(),
            style = Typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        OutlinedTextField(
            value = uiState.value.baseAddress,
            onValueChange = { viewModel.onEvent(BaseAddressChanged(it)) },
            label = { Text(stringResource(id = R.string.base_address_string_representation)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.value.destinationAddress,
            onValueChange = { viewModel.onEvent(DestinationAddressChanged(it)) },
            label = { Text(stringResource(id = R.string.destination_address_string_representation)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.value.distanceTravelled,
            onValueChange = { viewModel.onEvent(DistanceTravelledChanged(it)) },
            label = { Text(stringResource(id = R.string.distance_travelled_string_representation)) },
            modifier = Modifier.fillMaxWidth()
        )

        Row {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = stringResource(id = R.string.forth_commute_included_string_representation)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Switch(
                checked = uiState.value.forthRouteIncluded,
                onCheckedChange = { viewModel.onEvent(ForthRouteIncludedChanged(it)) },
            )
        }

        Row {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = stringResource(id = R.string.back_commute_included_string_representation)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Switch(
                checked = uiState.value.backRouteIncluded,
                onCheckedChange = { viewModel.onEvent(BackRouteIncludedChanged(it)) },
            )
        }

        CommuteClockTimeRangeSliderWrapper(
            initSliderPosition =
            if (uiState.value.shiftStartTime != "")
                convertStringToFloatRange(start = uiState.value.shiftStartTime, end = uiState.value.shiftEndTime)
            else
                null,
            onUpdateTimeRange = { startTime: String, endTime: String ->
                viewModel.onEvent(ShiftStartTimeChanged(startTime))
                viewModel.onEvent(ShiftEndTimeChanged(endTime))
                Log.e("DEBUG", "update: startTime:$startTime, endTime:$endTime")
            }
        )

        var showDatePickerDialog by remember { mutableStateOf(false) }
        val selectedDates = remember { mutableStateListOf<LocalDate>() }

        Button(
            onClick = { showDatePickerDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select date")
        }
        if (showDatePickerDialog) {
            DatePickerDialog(
                onDatesSelected = { selectedDates.clear(); selectedDates.addAll(it) },
                onDismissRequest = { showDatePickerDialog = false },
                currentSelectedDates = selectedDates
            )
        }


        Spacer(modifier = Modifier.weight(1f))
        Button(
            modifier = Modifier.align(Alignment.End),
            onClick = {
                val draft : DraftEntry? = viewModel.onEvent(Submit)
                if (draft == null) {
                    Toast.makeText(null, "Invalid data!", Toast.LENGTH_SHORT).show()
                } else {
                    onFinishActivity(RESULT_OK, draft)
                }
            }
        ) {
            Text("Submit draft")
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

fun convertStringTimeToQuarters(time : String): Int {
    // format HH:MM rounded to earliest smallest quarter
    val timeList = time.split(":")
    if (timeList.size != 2)
        throw IllegalArgumentException()
    val hours = timeList[0].toInt()
    val minutes = timeList[1].toInt()
    val quarters = minutes / 15
    val result = hours * 4 + quarters
    Log.e("DEBUG", "time:$time, result:$result")
    return result
}

fun convertStringToFloatRange(start: String, end: String): ClosedFloatingPointRange<Float> {
    val minEnd = convertStringTimeToQuarters(start)
    val maxEnd = convertStringTimeToQuarters(end)
    val resultRange = if (minEnd < maxEnd) {
        minEnd.toFloat()..maxEnd.toFloat()
    } else {
        minEnd.toFloat()..(maxEnd + 24 * 4).toFloat()
    }
    Log.e("DEBUG", "RESULT RANGE $resultRange")
    return resultRange
}