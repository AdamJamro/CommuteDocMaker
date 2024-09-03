package com.example.commutedocmaker.ui

import android.app.Activity.RESULT_OK
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.commutedocmaker.DraftPickNameDialog
import com.example.commutedocmaker.R
import com.example.commutedocmaker.dataSource.draftEntry.DraftDataPatch
import com.example.commutedocmaker.dataSource.draftEntry.DraftEntry
import com.example.commutedocmaker.ui.viewModels.DraftEditorViewModel
import kotlin.math.floor
import com.example.commutedocmaker.ui.uiUtils.DatePickerDialog
import com.example.commutedocmaker.ui.uiUtils.CommuteClockTimeRangeSliderWrapper
import com.example.commutedocmaker.ui.viewModels.DraftEditorEvent.*
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
    val scope = rememberCoroutineScope()

    DraftPickNameDialog(
        shouldShowDialog = showNameChangeDialog,
        onDismissRequest = { showNameChangeDialog = false },
        onSubmitted = { newName: String ->
            viewModel.onUiEvent(DraftNameChanged(newName))
            showNameChangeDialog = false
        },
        previousName = viewModel.title
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 8.dp)
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(vertical = 15.dp)
                    .clickable(onClick = { showNameChangeDialog = true })
                    .width(200.dp),
                text = viewModel.title,
                style = Typography.titleLarge,
                textAlign = TextAlign.Center,
                softWrap = true
            )
            Button(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = {
                    val draft : DraftEntry? = viewModel.onUiEvent(Submit)
                    if (draft == null) {
                        Toast.makeText(null, "Invalid data!", Toast.LENGTH_SHORT).show()
                    } else {
                        onFinishActivity(RESULT_OK, draft)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Submit draft"
                )
            }
        }


        DataPatchesEditor(
            Modifier
                .wrapContentSize()
                .fillMaxWidth(),
            uiState.value,
            viewModel,
            onFinishActivity
        )
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


@Composable
fun DataPatchesEditor(
    modifier: Modifier = Modifier,
    dataPatchesState: List<DraftDataPatch>,
    viewModel: DraftEditorViewModel,
    onFinishEditor: (Int, DraftEntry?) -> Unit = { _, _ -> }
) {
    LazyColumn {
        itemsIndexed(dataPatchesState) { patchIndex, dataPatchState ->
            var showDatePickerDialog by remember { mutableStateOf(false) }
            Card(
                modifier = modifier
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            ) {
                Column(modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = dataPatchState.baseAddress,
                        onValueChange = { viewModel.onUiEvent(BaseAddressChanged(it, patchIndex)) },
                        label = { Text(stringResource(id = R.string.base_address_string_representation)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dataPatchState.destinationAddress,
                        onValueChange = { viewModel.onUiEvent(DestinationAddressChanged(it, patchIndex)) },
                        label = { Text(stringResource(id = R.string.destination_address_string_representation)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dataPatchState.distanceTravelled,
                        onValueChange = { viewModel.onUiEvent(DistanceTravelledChanged(it, patchIndex)) },
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
                            checked = dataPatchState.forthRouteIncluded,
                            onCheckedChange = { viewModel.onUiEvent(ForthRouteIncludedChanged(it, patchIndex)) },
                        )
                    }

                    Row {
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = stringResource(id = R.string.back_commute_included_string_representation)
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Switch(
                            checked = dataPatchState.backRouteIncluded,
                            onCheckedChange = { viewModel.onUiEvent(BackRouteIncludedChanged(it, patchIndex)) },
                        )
                    }

                    CommuteClockTimeRangeSliderWrapper(
                        initSliderPosition =
                        if (dataPatchState.shiftStartTime != "")
                            convertStringToFloatRange(
                                start = dataPatchState.shiftStartTime,
                                end = dataPatchState.shiftEndTime
                            )
                        else
                            null,
                        onUpdateTimeRange = { startTime: String, endTime: String ->
                            viewModel.onUiEvent(ShiftStartTimeChanged(startTime, patchIndex))
                            viewModel.onUiEvent(ShiftEndTimeChanged(endTime, patchIndex))
                            Log.e("DEBUG", "update: startTime:$startTime, endTime:$endTime")
                        }
                    )

//        val selectedDates = remember { mutableStateListOf<LocalDate>() }

                    Button(
                        onClick = { showDatePickerDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Select date")
                    }
                }
            }

            Spacer(modifier = Modifier.size(12.dp))

            if (showDatePickerDialog) {
                DatePickerDialog(
                    onDatesSelected = {
//                    selectedDates.clear(); selectedDates.addAll(it)
                        viewModel.onUiEvent(SelectedDatesChanged(it, patchIndex))
                    },
                    onDismissRequest = { showDatePickerDialog = false },
                    currentSelectedDates = dataPatchState.dates
                )
            }
        }

        item {
            Column(Modifier.wrapContentSize().fillParentMaxWidth()){
                Button(
                    modifier = Modifier.align(Alignment.Start),
                    onClick = {
                        viewModel.addDraftDataPatch()
                    }
                ) {
                    Text(stringResource(R.string.create_new_data_patach_label))
                }

                Spacer(modifier = Modifier.weight(1f))
                Button(
                    modifier = Modifier.align(Alignment.End),
                    onClick = {
                        val draft : DraftEntry? = viewModel.onUiEvent(Submit)
                        if (draft == null) {
                            Toast.makeText(null, "Invalid data!", Toast.LENGTH_SHORT).show()
                        } else {
                            onFinishEditor(RESULT_OK, draft)
                        }
                    }
                ) {
                    Text("Submit draft")
                }
            }
        }
    }

}