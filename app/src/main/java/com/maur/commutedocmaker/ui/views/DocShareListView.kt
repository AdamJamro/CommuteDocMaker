package com.maur.commutedocmaker.ui.views

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maur.commutedocmaker.R
import com.maur.commutedocmaker.dataSource.document.Document
import com.maur.commutedocmaker.viewModels.DocMakerAppViewModel
import com.maur.commutedocmaker.ui.theme.Typography

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun DocShareListView (
    viewModel: DocMakerAppViewModel,
    onDocClick: (Document) -> Unit,
    documents : List<Document>
) {
//    val documents by viewModel.documents.collectAsState()
    var expandedIndex by remember { mutableIntStateOf(-1) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }

    if (showConfirmDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.delete_document)) },
            onDismissRequest = { showConfirmDialog = false },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteDocument(documents[expandedIndex])
                    showConfirmDialog = false
                }) {
                    Text(text = stringResource(R.string.delete))
                }
            },
            dismissButton = {
                Button(onClick = {
                    showConfirmDialog = false
                    expandedIndex = -1
                }) {
                    Text(text = stringResource(R.string.cancel))
                }

            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    expandedIndex = -1
                    Log.d("DEBUG", "expandedIndex: $expandedIndex")
                })
            .background(Color.Transparent),
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(documents) { index, item ->
                Surface(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(4.dp)
                        .animateItem(),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 1.dp,
                    shadowElevation = 2.dp,
                    onClick = {
                        if (expandedIndex == index) {
                            onDocClick(item)
                        }
                        expandedIndex = index
                    }
                ) {
                    Column {
                        Row {
                            Text(
                                modifier = Modifier
                                    .padding(6.dp),
                                text = item.title,
                                textAlign = TextAlign.Center,
                                fontSize = Typography.titleLarge.fontSize * 0.75,
                                fontStyle = Typography.titleMedium.fontStyle,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                modifier = Modifier
                                    .padding(8.dp),
                                text = item.date.toString(),
                                textAlign = TextAlign.Center,
                                fontSize = Typography.titleSmall.fontSize,
                                fontStyle = Typography.titleSmall.fontStyle,
                                fontWeight = FontWeight.ExtraLight,
                                color = Color.Black
                            )
                        }

                        AnimatedVisibility(visible = expandedIndex == index) {
                            Column{
                                item.documentSummaryInformation?.also {
                                    for (information in
                                    listOf(
                                        "${stringResource(R.string.period)}: ${it.dayRange} - ${it.month}",
                                        "${stringResource(R.string.amount_of_transfers)}: ${it.amountOfTransfers}",
                                        "${stringResource(R.string.payable_amount)}: ${it.payableAmount}€"
                                    )
                                    ) {
                                        Text(
                                            modifier = Modifier
                                                .fillParentMaxWidth()
                                                .padding(2.dp),
                                            textAlign = TextAlign.Center,
                                            text = information,
                                            softWrap = true,
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Button(onClick = { onDocClick(item) }) {
                                        Text(text = stringResource(R.string.share_doc))
                                    }
                                    Button(onClick = { showConfirmDialog = true }) {
                                        Text(text = stringResource(R.string.delete))
                                    }
                                    Button(onClick = { expandedIndex = -1 }) {
                                        Text(text = stringResource(R.string.collapse))

                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                if (expandedIndex == -1) {
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Spacer(modifier = Modifier.height(62.dp))
                }
            }

        }
    }
}