package com.example.commutedocmaker.ui.uiUtils

import android.view.ContextThemeWrapper
import android.widget.CalendarView
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.commutedocmaker.R
import com.example.commutedocmaker.R.string.day_hours_option_selected_string_representation
import com.example.commutedocmaker.R.string.night_hours_option_selected_string_representation
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import com.example.commutedocmaker.R.style.CalenderViewCustom
import com.example.commutedocmaker.ui.convertToStringTime
import com.example.commutedocmaker.ui.theme.Typography
import com.example.cupcake.ui.theme.*
import java.text.SimpleDateFormat
import java.time.YearMonth

fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("EEEE MMMM dd yyyy", Locale.forLanguageTag("pl"))
    return formatter.format(date)
}

fun getSelectedRangeString(selectedDates: SnapshotStateList<Date>): String {
        return if (selectedDates.elementAtOrNull(1) != null) {
            val firstDate = formatDate(selectedDates[0])
            val lastDate = formatDate(selectedDates[selectedDates.lastIndex])
            "from: $firstDate\nto: $lastDate"
        } else if (selectedDates.elementAtOrNull(0) != null) {
            formatDate(selectedDates[0])
        } else {
            ""
        }
    }

@Composable
fun DatePicker(
    onDateSelected: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit,
    selectedDates: SnapshotStateList<Date> = remember { mutableStateListOf() }
) {
    val selDate = remember { mutableStateOf(LocalDate.now()) }


    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties()
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(size = 16.dp)
                )
        ) {
            Column(
                Modifier
                    .defaultMinSize(minHeight = 72.dp)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select dates".uppercase(Locale.ENGLISH),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.size(24.dp))

                Text(
//                    text = selDate.value.format(DateTimeFormatter.ofPattern("MMM d, YYYY")),
                    text = getSelectedRangeString(selectedDates),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.size(16.dp))
            }

//            CustomCalendarView(
//                onDateSelected = {
//                    selDate.value = it
//                },
//                selectedDates = selectedDates
//            )
            MultiDatePicker()

            Spacer(modifier = Modifier.size(8.dp))

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 16.dp, end = 16.dp)
            ) {
                TextButton(
                    onClick = onDismissRequest
                ) {
                    //TODO - hardcode string
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black
                    )
                }

                TextButton(
                    onClick = {
                        onDateSelected(selDate.value)
                        selectedDates.add(Date())
//                        onDismissRequest()
                    }
                ) {
                    //TODO - hardcode string
                    Text(
                        text = "OK",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black
                    )
                }

            }
        }
    }
}

@Composable
fun CustomCalendarView(
    onDateSelected: (LocalDate) -> Unit,
    selectedDates: SnapshotStateList<Date> = remember { mutableStateListOf() }
) {
    // Adds view to Compose
    AndroidView(
        modifier = Modifier.wrapContentSize(),
        factory = { context ->
            CalendarView(ContextThemeWrapper(context, CalenderViewCustom))
        },
        update = { view ->
            view.minDate = 1000 // contraints
            view.maxDate = (1000.0 * 60 * 60 * 24 * 360 * 2000).toLong()  // contraints

            view.setOnDateChangeListener { _, year, month, dayOfMonth ->
                onDateSelected(
                    LocalDate
                        .now()
                        .withMonth(month + 1)
                        .withYear(year)
                        .withDayOfMonth(dayOfMonth)
                )
            }
        }
    )
}

@Composable
fun CustomToggleButton(
    text: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = if (isSelected) md_theme_light_primary else Color.Gray
    ),
    elevation: ButtonElevation = ButtonDefaults.buttonElevation(
            defaultElevation = 12.dp,
            pressedElevation = 6.dp,
            disabledElevation = 0.dp
    ),
    shape: Shape = RoundedCornerShape(16.dp)
) {

    Button(
        onClick = { onSelect() },
        colors = colors,
        shape = shape,
        elevation = elevation
    ) {
        Text(
            text = text,
            color = Color.White
        )
    }
}


@Composable
fun ToggleGroup(
    options: List<Pair<String, () -> Unit>> = emptyList(),
    onToggle: (String, MutableState<List<String>>) -> Unit = { option, selectedOptions ->
        selectedOptions.value = listOf(option)
    },
    defaultOption: String = "",
) {
    val selectedOptions = remember { mutableStateOf(listOf(defaultOption)) }
    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        var index = 0
        options.forEach { (option, onSelectThis) ->
            CustomToggleButton(
                text = option,
                isSelected = option in selectedOptions.value,
                onSelect = {
                    if (option !in selectedOptions.value) {
                        onSelectThis()
                    }
                    onToggle(option, selectedOptions)
                },
                shape =
                when (index) {
                    0 -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    options.size - 1 -> RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                    else -> RoundedCornerShape(0.dp)
                }

            )
            index += 1
        }
    }
}

@Composable
inline fun stringResource(@StringRes id: Int): String {
    return LocalContext.current.getString(id)
}

@Composable
fun CommuteClockTimeRangeSliderWrapper(
    modifier: Modifier = Modifier,
    onUpdateTimeRange: (startTime: String, endTime: String) -> Unit,
) {
    var dayHoursSelected by remember { mutableStateOf(true) }

    Column {
        CommuteClockTimeRangeSlider(
            modifier = modifier,
            onUpdateTimeRange = onUpdateTimeRange,
            onUpdateRangeConstraints = { valueRange, sliderPosition, steps ->
                val newRange = if (dayHoursSelected) 20f..96f else 56f..152f
                if (valueRange.value != newRange) {
                    valueRange.value = newRange
                    steps.intValue = (valueRange.value.endInclusive - valueRange.value.start).toInt()
                    sliderPosition.value = valueRange.value
                }
            }
        )

        ToggleGroup(
            options = listOf(
                Pair(stringResource(id = day_hours_option_selected_string_representation))
                { dayHoursSelected = true },
                Pair(stringResource(id = night_hours_option_selected_string_representation)
                ) { dayHoursSelected = false }
            ),
            defaultOption = stringResource(id = day_hours_option_selected_string_representation)
        )
    }
}


@Composable
fun CommuteClockTimeRangeSlider(
    modifier: Modifier = Modifier,
    onUpdateTimeRange: (startTime: String, endTime: String) -> Unit,
    onUpdateRangeConstraints: (
        valueRange: MutableState<ClosedFloatingPointRange<Float>>,
        sliderPosition: MutableState<ClosedFloatingPointRange<Float>>,
        steps: MutableIntState
    ) -> Unit,
    color: Color = md_theme_light_primary
) {

    val valueRange = remember { mutableStateOf(0f..152f) }
    val sliderPosition = remember { mutableStateOf(valueRange.value) }
    val steps = remember { mutableIntStateOf((valueRange.value.endInclusive - valueRange.value.start).toInt()) }
    val sliderViewHeight by remember { mutableStateOf(80.dp) }

    onUpdateRangeConstraints(valueRange, sliderPosition, steps)

    Box (
        modifier = Modifier
            .padding(0.dp)
            .fillMaxWidth()
            .height(sliderViewHeight)
//            .border(width = 2.dp, color = Color.Red),
    ) {
        RangeSlider(
            modifier = Modifier.align(Alignment.Center),
            value = sliderPosition.value,
            steps = steps.intValue,
            valueRange = valueRange.value,
            onValueChange = { range -> sliderPosition.value = range },
            onValueChangeFinished = {
                onUpdateTimeRange(
                    /*startTime=*/  convertToStringTime(sliderPosition.value.start),
                    /*endTime=*/    convertToStringTime(sliderPosition.value.endInclusive)
                )
            }
        )
        Box(
            modifier = modifier
                .wrapContentSize()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val xOffset =
                        (sliderPosition.value.start - valueRange.value.start) / steps.intValue * (constraints.maxWidth - placeable.width)
                    layout(constraints.maxWidth, placeable.height) {
                        placeable.place(xOffset.toInt(), sliderViewHeight.value.toInt() / 10)
                    }
                }
        ) {
            Surface(
                modifier = Modifier
                    .width(60.dp)
                    .height(25.dp),
                shape = RoundedCornerShape(16.dp),
                color = color,
                shadowElevation = 15.dp
            ) {
                Text(
                    text = convertToStringTime(sliderPosition.value.start),
                    color = Color.White,
                    style = Typography.headlineSmall,
                    modifier = Modifier
                        .wrapContentSize()
                        .offset(x = 0.dp, y = (-1).dp)
                )
            }
        }
        Box(
            modifier = modifier
                .wrapContentSize()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val xOffset =
                        (sliderPosition.value.endInclusive - valueRange.value.start) / steps.intValue * (constraints.maxWidth - placeable.width)
                    layout(constraints.maxWidth, placeable.height) {
                        placeable.placeRelative(xOffset.toInt(), sliderViewHeight.value.toInt() * 19 / 10)
                    }
                }
        ) {
            Surface(
                modifier = Modifier
                    .width(60.dp)
                    .height(25.dp),
                shape = RoundedCornerShape(16.dp),
                color = color,
                shadowElevation = 15.dp
            ) {
                Text(
                    text = convertToStringTime(sliderPosition.value.endInclusive),
                    color = Color.White,
                    style = Typography.headlineSmall,
                    modifier = Modifier
                        .wrapContentSize()
                        .offset(x = 0.dp, y = (-1).dp)
                )
            }
        }
    }
}

@Composable
fun MultiDatePicker(
    selectedDates: SnapshotStateList<LocalDate> = remember { mutableStateListOf<LocalDate>() }
) {
    val today = remember { LocalDate.now() }
    var currentMonth by remember { mutableStateOf(today.month) }
    Column(
        modifier = Modifier
            .wrapContentSize()
            .padding(16.dp)
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 72.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                modifier = Modifier.wrapContentSize(),
                onClick = {currentMonth = currentMonth.minus(1)},
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back_24),
                    contentDescription = "Arrow",
                    tint = Color.Black
                )
            }

            Surface (
                color = md_theme_dark_primaryContainer,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(35)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = today.month.toString(),
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = today.year.toString(),
                        fontSize = 20.sp,
                        color = Color.White,
                    )
                }
            }
            Button(
                modifier = Modifier.wrapContentSize(),
                onClick = {currentMonth = currentMonth.plus(1)},
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_forward_24),
                    contentDescription = "Arrow",
                    tint = Color.Black
                )
            }
        }
        GridWithSelectableDatesLayout(YearMonth.of(today.year, currentMonth), selectedDates)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Selected Dates: ${selectedDates.joinToString { it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }}",
            fontSize = 18.sp,
            color = Color.Black
        )
    }
}

@Composable
fun GridWithSelectableDatesLayout(currentYearMonth: YearMonth, selectedDates: SnapshotStateList<LocalDate>) {
    val firstDayOfMonth = currentYearMonth.atDay(1)
    val firstDayOfFirstWeek = firstDayOfMonth.dayOfWeek.value - 1

    var daysOfMonth: List<LocalDate> = (1..currentYearMonth.month.maxLength()).map { LocalDate.of(currentYearMonth.year, currentYearMonth.month, it) }

    Column {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            repeat(firstDayOfFirstWeek) {
                Box(
                    modifier = Modifier.size(40.dp),
                )
            }
            repeat(7 - firstDayOfFirstWeek) {
                DateBox(daysOfMonth[it], selectedDates)
            }
        }
        daysOfMonth = daysOfMonth.drop(7 - firstDayOfFirstWeek)

        daysOfMonth.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                week.forEach { date ->
                    DateBox(date, selectedDates)
                }
            }
        }
    }
}

@Composable
fun DateBox(date: LocalDate, selectedDates: SnapshotStateList<LocalDate>) {
    val isSelected = selectedDates.contains(date)

    Surface (
        shape = CircleShape,
        shadowElevation = 4.dp,
        modifier = Modifier.size(40.dp)
            .padding(4.dp),

        color = if (isSelected) Color.Blue else Color.Gray,
        contentColor = Color.White,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    if (isSelected) {
                        selectedDates.remove(date)
                    } else {
                        selectedDates.add(date)
                    }
                },
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = date.dayOfMonth.toString(),
            )
        }
    }
}

