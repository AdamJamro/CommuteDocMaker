package com.maur.commutedocmaker.ui.uiUtils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.maur.commutedocmaker.R
import com.maur.commutedocmaker.ui.views.clickableWithoutRipple
import com.maur.cupcake.ui.theme.md_theme_dark_primaryContainer
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

@Composable
fun DatePickerDialog(
    onDatesSelected: (Collection<LocalDate>) -> Unit,
    onDismissRequest: () -> Unit,
    currentSelectedDates: Collection<LocalDate> = emptyList()
) {
    val selectedDates: SnapshotStateList<LocalDate> = remember { mutableStateListOf() }
    selectedDates.addAll(currentSelectedDates)

    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties( /*default*/)
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
                    .wrapContentHeight()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_dates),
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
            }
            MultiDatePicker(selectedDates = selectedDates)
            Spacer(modifier = Modifier.size(8.dp))

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .wrapContentSize()
                    .padding(end = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(end = 16.dp),
                    onClick = onDismissRequest,
                ) {
                    //TODO - hardcode string
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(end = 16.dp),
                    onClick = {
                        onDatesSelected(selectedDates)
                        onDismissRequest()
                    }
                ) {
                    //TODO - hardcode string
                    Text(
                        text = stringResource(R.string.ok),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.size(12.dp))
        }
    }
}

@Composable
fun MultiDatePicker(
    selectedDates: SnapshotStateList<LocalDate> = remember { mutableStateListOf() }
) {
    val today = remember { LocalDate.now() }
    var currentMonth by remember { mutableStateOf(today.month) }

//    val scale by animateFloatAsState(targetValue = )

    Column(
        modifier = Modifier
            .wrapContentSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 72.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                modifier = Modifier.wrapContentSize(),
                onClick = { currentMonth = currentMonth.minus(1) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back_24),
                    contentDescription = "ArrowLeft",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            Surface(
                modifier = Modifier.clickableWithoutRipple(
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        val datesWithinCurrentMonth =
                            (1..currentMonth.maxLength()).map { LocalDate.of(today.year, currentMonth, it) }
                        val datesToAdd: List<LocalDate> = datesWithinCurrentMonth.filter {
                            !selectedDates.contains(it) && it.dayOfWeek.value < 6
                        }
                        val datesToRemove: List<LocalDate> = selectedDates.filter {
                            it.month == currentMonth && it.dayOfWeek.value < 6
                        }
                        selectedDates.addAll(datesToAdd)
                        selectedDates.removeAll(datesToRemove)
                    }
                ),
                color = md_theme_dark_primaryContainer,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(30)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentMonth.toString(),
                        fontSize = 20.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = today.year.toString(),
                        fontSize = 16.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Button(
                modifier = Modifier.wrapContentSize(),
                onClick = { currentMonth = currentMonth.plus(1) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_forward_24),
                    contentDescription = "ArrowRight",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        GridWithSelectableDatesLayout(YearMonth.of(today.year, currentMonth), selectedDates)
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(
//            text = "Selected Dates: ${selectedDates.joinToString { it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }}",
//            fontSize = 18.sp,
//            color = Color.Black
//        )
    }
}

@Composable
fun GridWithSelectableDatesLayout(currentYearMonth: YearMonth, selectedDates: SnapshotStateList<LocalDate>) {
    val firstDayOfMonth = currentYearMonth.atDay(1)
    val firstDayOfFirstWeek = firstDayOfMonth.dayOfWeek.value - 1

    var daysOfMonth: List<LocalDate> = (1..currentYearMonth.month.maxLength()).map {
        LocalDate.of(
            currentYearMonth.year,
            currentYearMonth.month,
            it
        )
    }

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

    Surface(
        shape = CircleShape,
        shadowElevation = 4.dp,
        modifier = Modifier
            .size(40.dp)
            .padding(4.dp),

        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
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