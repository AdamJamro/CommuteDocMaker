package com.example.commutedocmaker.ui

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.commutedocmaker.R
import com.example.commutedocmaker.dataSource.DraftEntry
import com.example.commutedocmaker.dataSource.SpreadsheetPartialData
import com.example.commutedocmaker.ui.viewModels.CreateNewDraftViewModel
import com.example.commutedocmaker.ui.viewModels.UIState


@Composable
fun CreateNewDraft (
    modifier: Modifier = Modifier,
    viewModel: CreateNewDraftViewModel = remember { CreateNewDraftViewModel() },
    onFinishActivity: (filepath: String?) -> Unit
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsState()
    val forthSwitchChecked = remember { mutableStateOf(true) }
    val backSwitchChecked = remember { mutableStateOf(true) }
    var sliderPosition by remember { mutableStateOf(0f..100f) }
    var distanceTravelledAsString by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.value.baseAddress,
            onValueChange = { viewModel.setBaseAddress(it) },
            label = { Text(stringResource(id = R.string.base_address_string_representation)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.value.destinationAddress,
            onValueChange = { viewModel.setDestinationAddress(it) },
            label = { Text(stringResource(id = R.string.destination_address_string_representation)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = distanceTravelledAsString,
            onValueChange = { distanceTravelledAsString = it },
            label = { Text(stringResource(id = R.string.distance_travelled_string_representation)) },
            modifier = Modifier.fillMaxWidth()
        )

        Row {
            Text(stringResource(id = R.string.forth_commute_included_string_representation))
            Switch(
                checked = forthSwitchChecked.value,
                onCheckedChange = { forthSwitchChecked.value = it },
            )
        }

        Row {
            Text(stringResource(id = R.string.back_commute_included_string_representation))
            Switch(
                checked = backSwitchChecked.value,
                onCheckedChange = { backSwitchChecked.value = it },
            )
        }

        Column {
            RangeSlider(
                value = sliderPosition,
                steps = 5,
                onValueChange = { range -> sliderPosition = range },
                valueRange = 0f..100f,
                onValueChangeFinished = {
                    // launch some business logic update with the state you hold
                    // viewModel.updateSelectedSliderValue(sliderPosition)
                },
            )
            Text(text = sliderPosition.toString())
        }


        Button(
            onClick = {
                try {
                    viewModel.setDistanceTravelled(distanceTravelledAsString.toFloat())
                    viewModel.submitDraft(uiState.value)
                } catch (e: NumberFormatException) {
                    Toast.makeText(null, "Invalid distance!", Toast.LENGTH_SHORT).show()
                }
                val spreadsheetPartialData = SpreadsheetPartialData(
                    title = "Title",
                    content = "Content",
                    uiState = uiState.value
                )
                val filepath = spreadsheetPartialData.filepath
                onFinishActivity(filepath)
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Submit")
        }
    }
}
