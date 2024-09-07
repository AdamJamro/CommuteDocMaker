package com.example.commutedocmaker.ui

import android.app.Activity.RESULT_OK
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.example.commutedocmaker.DraftPickNameDialog
import com.example.commutedocmaker.R
import com.example.commutedocmaker.dataSource.draftEntry.DraftDataPatch
import com.example.commutedocmaker.dataSource.draftEntry.DraftEntry
import com.example.commutedocmaker.ui.CashPerKilometer.PER_DEFAULT_ROUTE
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

enum class CashPerKilometer(val rate: Float, val description: Int) {
    PER_DEFAULT_ROUTE(0.21f, R.string.per_default_route),
    PER_TWO_PEOPLE(0.29f, R.string.per_two_people),
    PER_THREE_PEOPLE(0.35f, R.string.per_three_people),
    PER_FOUR_PEOPLE(0.45f, R.string.per_four_people),
    PER_FIVE_PEOPLE(0.55f, R.string.per_five_people),
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DataPatchesEditor(
    modifier: Modifier = Modifier,
    dataPatchesState: List<DraftDataPatch>,
    viewModel: DraftEditorViewModel,
    onFinishEditor: (Int, DraftEntry?) -> Unit = { _, _ -> }
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var dropdownMenuExpanded by remember { mutableStateOf(false) }


    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),

    ) {
//        item(Unit) {
//            AnimatedContent (
//                targetState = dataPatchState,
//                transitionSpec = {
//                    if (targetState != initialState) {
//                        slideInVertically() + fadeIn() togetherWith
//                                slideOutVertically() + fadeOut()
//                    } else {
//                        fadeIn() togetherWith fadeOut()
//                    }
//                },
//                label = "PatchEditorAnimated"
//            ) { targetState ->
//            }
//        }
        itemsIndexed(
            items = dataPatchesState,
            key = { index, _ -> index },
        ) { patchIndex, dataPatchState ->
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .animateItem(),
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

                    val options = CashPerKilometer.entries
                        .map {
                            Pair("${it.rate}€/km - ${stringResource(it.description)}", it.rate.toString())
                        }
                    var rateFieldText by remember { mutableStateOf(PER_DEFAULT_ROUTE.rate.toString()) }
                    val icon = if(dropdownMenuExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.ArrowDropDown
                    var textFieldSize by remember { mutableStateOf(Size.Zero) }

                    Box {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    textFieldSize = coordinates.size.toSize()
                                },
                            value = rateFieldText,
                            label = { Text(stringResource(R.string.cash_per_kilometer_string_representation)) },
                            onValueChange = {
                                rateFieldText = it
                            },
                            readOnly = true,
                            trailingIcon = {
                                Box(
                                    contentAlignment = Alignment.CenterEnd,
                                    modifier = Modifier
                                        .width(with(LocalDensity.current) { textFieldSize.width.toDp() * 0.8f })
                                        .padding(12.dp)
                                        .clickable(
                                            onClick = {
                                                dropdownMenuExpanded = !dropdownMenuExpanded
                                            },
                                        )
                                ) {
                                    Icon(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clickable {
                                                dropdownMenuExpanded = !dropdownMenuExpanded
                                            },
                                        imageVector = icon,
                                        contentDescription = "Dropdown"
                                    )
                                }
                            }
                        )

                        DropdownMenu(
                            expanded = dropdownMenuExpanded,
                            onDismissRequest = { dropdownMenuExpanded = false },
                            modifier = Modifier
                                .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text= { Text(text = option.first, fontSize = 16.sp) },

                                    onClick = {
                                        rateFieldText = option.second
                                        dropdownMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }


                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
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
                    Spacer(modifier = Modifier.size(12.dp))
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
            Column(
                Modifier
                    .wrapContentSize()
                    .fillParentMaxWidth()){
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