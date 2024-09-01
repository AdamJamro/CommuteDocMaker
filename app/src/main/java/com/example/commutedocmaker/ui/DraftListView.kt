package com.example.commutedocmaker.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.commutedocmaker.dataSource.DraftEntry
import com.example.commutedocmaker.ui.ItemDialogType.*
import com.example.commutedocmaker.ui.theme.Typography
import com.example.commutedocmaker.R
import com.example.commutedocmaker.stringResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraftListView(
//    docViewModel: CommuteDocMakerViewModel
    draftItems: List<DraftEntry>,
    onDeleteDraft: (draftData: DraftEntry) -> Unit,
    onGenerateDoc: (draftData: DraftEntry) -> Unit,
    onEditDraft: (draftData: DraftEntry) -> Unit
) {
//    val fileItems = docViewModel.entries.collectAsState()
    val currentDialog: MutableState<ItemDialogType> = remember { mutableStateOf(Hidden) }



    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(draftItems) { item ->
            Surface(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 1.dp,
                    shadowElevation = 2.dp,
                ) {
                Box(modifier = Modifier.combinedClickable(
                    onClick = { onEditDraft(item) },
                    onLongClick = { currentDialog.value = OptionsDialog(item) }
                )
                ) {
                    Row {
                        Text(
                            modifier = Modifier
                                .wrapContentSize()
                                .height(48.dp)
                                .padding(8.dp),
                            text = item.title,
                            textAlign = TextAlign.Center,
                            fontSize = Typography.titleMedium.fontSize,
                            fontStyle = Typography.titleMedium.fontStyle,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxSize()
                                .height(48.dp)
                                .padding(8.dp),
                            text =  "${item.draftId}" +
                            if (item.content.length > (40 - 3 / 2 * item.title.length)) {
                                item.content.subSequence(0, 40 - 3 / 2 * item.title.length).toString() + "..."
                            } else {
                                item.content
                            },
                            textAlign = TextAlign.Center,
                            fontSize = Typography.titleSmall.fontSize,
                            fontStyle = Typography.titleSmall.fontStyle
                        )
                    }
                }
            }
        }
    }

    ItemOptionsDialogHandler(
        currentDialog,
        onGenerate = { onGenerateDoc(it) },
        onDelete = { onDeleteDraft(it) },
    )

}

private sealed class ItemDialogType {
    data object Hidden : ItemDialogType()
    data class OptionsDialog(val draftData: DraftEntry) : ItemDialogType()
    data class DeleteDialog(val draftData: DraftEntry) : ItemDialogType()
}

private enum class ItemDialogOptions(val id: Int) {
    GENERATE_DOC(R.string.generate),
    DELETE(R.string.delete),
    CANCEL(R.string.cancel)
}

@Composable
private fun ItemOptionsDialogHandler(
    currentDialog: MutableState<ItemDialogType>,
    onGenerate: (draft: DraftEntry) -> Unit,
    onDelete: (draft: DraftEntry) -> Unit,
) {
    when (currentDialog.value) {
        is OptionsDialog -> {
            ItemOptionsDialog(currentDialog, onGenerate, onDelete)
        }
        is DeleteDialog -> {
            ItemDeleteDialog(currentDialog, onDelete)
        }
        is Hidden -> {
            //pass
        }
    }
}



@Composable
private fun ItemOptionsDialog(
    currentDialog: MutableState<ItemDialogType>,
    onGenerate: (DraftEntry) -> Unit,
    onDelete: (DraftEntry) -> Unit,
) {
    Column {
        for (option in ItemDialogOptions.entries) {
            Button(
                modifier = Modifier.padding(8.dp),
                onClick = {
                    when (option) {
                        ItemDialogOptions.GENERATE_DOC -> {
                            (currentDialog.value as? OptionsDialog)?.let { onGenerate(it.draftData) }
                        }
                        ItemDialogOptions.DELETE -> {
                            currentDialog.value = DeleteDialog((currentDialog.value as OptionsDialog).draftData)
                        }
                        ItemDialogOptions.CANCEL -> {
                            currentDialog.value = Hidden
                        }
                    }
                }
            ) {
                Text(stringResource(option.id))
            }
        }
    }
}

@Composable
private fun ItemDeleteDialog(
    currentDialog: MutableState<ItemDialogType>,
    onDelete: (DraftEntry) -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
           currentDialog.value = Hidden
        },
        confirmButton = {
            Button(
                onClick = {
                    (currentDialog.value as? DeleteDialog)?.let { onDelete(it.draftData) }
                    currentDialog.value = Hidden
                }
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    currentDialog.value = Hidden
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        })
}
