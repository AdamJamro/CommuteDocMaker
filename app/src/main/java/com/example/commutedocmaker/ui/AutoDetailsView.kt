package com.example.commutedocmaker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.commutedocmaker.R
import com.example.commutedocmaker.dataSource.autoDetailsData.Details
import com.example.commutedocmaker.stringResource
import com.example.commutedocmaker.ui.theme.Typography

//TODO(make persistent)
@Composable
fun AutoDetailsView(
    injectDetails: List<String> = List(Details.entries.size) { "" },
    onUpdateViewModelDetails: (Details, String) -> Unit
) {
    val details = remember {
        try {
            val iterator = injectDetails.iterator()
            List(Details.entries.size) { mutableStateOf(iterator.next()) }
        } catch (e: NoSuchElementException) {
            List(Details.entries.size) { mutableStateOf("") }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.auto_details_view_title),
            modifier = Modifier.fillMaxWidth(),
            style = Typography.titleLarge
        )

        details.forEachIndexed { index, detail ->
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = detail.value,
                onValueChange = {
                    detail.value = it
                    onUpdateViewModelDetails(Details.entries[index], it)
                },
                label = { Text(stringResource(Details.entries[index].id)) },
                singleLine = true
            )
        }
    }
}