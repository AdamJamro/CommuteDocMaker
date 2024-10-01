package com.maur.commutedocmaker.ui.uiUtils

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.maur.commutedocmaker.R.string.day_hours_option_selected_string_representation
import com.maur.commutedocmaker.R.string.night_hours_option_selected_string_representation
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import com.maur.commutedocmaker.ui.views.convertQuartersToStringTime
import com.maur.commutedocmaker.ui.theme.Typography
import com.maur.cupcake.ui.theme.*

fun formatDisplayDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("EEE dd/MM/yyyy", Locale.getDefault() /*Locale.forLanguageTag("pl")*/)
    return date.format(formatter)
}

fun getSelectedRangeString(selectedDates: SnapshotStateList<LocalDate>): String {
    selectedDates.sort()

    return if (selectedDates.elementAtOrNull(1) != null) {
        val firstDate = formatDisplayDate(selectedDates[0])
        val lastDate = formatDisplayDate(selectedDates[selectedDates.lastIndex])
        "$firstDate\n\t- $lastDate"
    } else if (selectedDates.elementAtOrNull(0) != null) {
        formatDisplayDate(selectedDates[0])
    } else {
        ""
    }
}

@Composable
fun ToggleButton(
    text: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = if (isSelected) md_theme_light_primary else Color.Gray
    ),
    elevation: ButtonElevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 12.dp,
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
            ToggleButton(
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
fun CommuteClockTimeRangeSliderWrapper(
    modifier: Modifier = Modifier,
    dayHours: Boolean = true,
    initSliderPosition: ClosedFloatingPointRange<Float>? = if (dayHours) 24f..64f else 68f..100f,
    onUpdateTimeRange: (startTime: String, endTime: String) -> Unit,
) {
    var dayHoursSelected by remember { mutableStateOf(dayHours) }

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
            },
            initSliderPosition = initSliderPosition
        )

        ToggleGroup(
            options = listOf(
                Pair(stringResource(id = day_hours_option_selected_string_representation))
                { dayHoursSelected = true },
                Pair(stringResource(id = night_hours_option_selected_string_representation))
                { dayHoursSelected = false }
            ),
            defaultOption = stringResource(id = day_hours_option_selected_string_representation)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommuteClockTimeRangeSlider(
    modifier: Modifier = Modifier,
    onUpdateTimeRange: (startTime: String, endTime: String) -> Unit,
    onUpdateRangeConstraints: (
        valueRange: MutableState<ClosedFloatingPointRange<Float>>,
        sliderPosition: MutableState<ClosedFloatingPointRange<Float>>,
        steps: MutableIntState
    ) -> Unit,
    initSliderPosition: ClosedFloatingPointRange<Float>? = null,
    color: Color = md_theme_light_primary
) {

    val valueRange = remember { mutableStateOf(20f..96f) }
    val sliderPosition = remember { mutableStateOf(initSliderPosition ?: valueRange.value) }
    val steps = remember { mutableIntStateOf((valueRange.value.endInclusive - valueRange.value.start).toInt()) }
    val sliderViewHeight by remember { mutableStateOf(80.dp) }
    val interactionSource = remember { MutableInteractionSource() }

    onUpdateRangeConstraints(valueRange, sliderPosition, steps)

    Box (
        modifier = Modifier
            .padding(0.dp)
            .fillMaxWidth()
            .height(sliderViewHeight)
//            .border(width = 2.dp, color = Color.Red),
    ) {
        RangeSlider(
            modifier = Modifier.align(Alignment.Center).height(30.dp),
            value = sliderPosition.value,
            steps = steps.intValue,
            valueRange = valueRange.value,
            onValueChange = { range -> sliderPosition.value = range },
            onValueChangeFinished = {
                onUpdateTimeRange(
                    /*startTime=*/  convertQuartersToStringTime(sliderPosition.value.start),
                    /*endTime=*/    convertQuartersToStringTime(sliderPosition.value.endInclusive)
                )
            },
            track = { rangeSliderState ->
                SliderDefaults.Track(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    rangeSliderState = rangeSliderState,
                    colors = SliderDefaults.colors(
                        activeTrackColor = color.copy(alpha = 0.1f),
                        activeTickColor = color,
                        inactiveTrackColor = Color.Black,
                        inactiveTickColor = Color.Black,
                    )
                )
            },
            startThumb = {
                SliderDefaults.Thumb(
                    modifier = Modifier
                        .size(20.dp)
                        .background(color = color, shape = CircleShape),
                    interactionSource = interactionSource
                )
            },
            endThumb = {
                SliderDefaults.Thumb(
                    modifier = Modifier
                        .size(20.dp)
                        .background(color = color, shape = CircleShape),
                    interactionSource = interactionSource
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
                shadowElevation = 6.dp
            ) {
                Text(
                    text = convertQuartersToStringTime(sliderPosition.value.start),
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
                shadowElevation = 6.dp
            ) {
                Text(
                    text = convertQuartersToStringTime(sliderPosition.value.endInclusive),
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



