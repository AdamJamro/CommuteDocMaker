package com.example.commutedocmaker.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.commutedocmaker.dataSource.draftEntry.DraftEntry
import com.example.commutedocmaker.ui.theme.Typography
import com.example.commutedocmaker.R
import com.example.commutedocmaker.stringResource

@Composable
fun DraftListView(
//    docViewModel: CommuteDocMakerViewModel
    draftItems: List<DraftEntry>,
    onDeleteDraft: (draftData: DraftEntry) -> Unit,
    onGenerateDoc: (draftData: DraftEntry) -> Unit,
    onEditDraft: (draftData: DraftEntry) -> Unit
) {
//    val fileItems = docViewModel.entries.collectAsState()
//    val currentDialog: MutableState<ItemDialogType> = remember { mutableStateOf(Hidden) }
    var expandedIndex by remember { mutableIntStateOf(-1) }
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDraft(draftItems[expandedIndex])
                        expandedIndex = -1
                        showConfirmDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        expandedIndex = -1
                        showConfirmDialog = false
                    }
                ) {
                    Text(stringResource(R.string.collapse))
                }
            }
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
                    })
                .background(Color.Transparent),
        ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            itemsIndexed(draftItems) { index, item ->
                Surface(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 1.dp,
                    shadowElevation = 2.dp,
                ) {
                    Box(
                        modifier = Modifier.clickable(
                            onClick = {
                                if (expandedIndex == index)
                                    onEditDraft(item)
                                else
                                    expandedIndex = index
                            },
//                    onLongClick = { currentDialog.value = OptionsDialog(item) }
                        )
                    ) {
                        Column {
                            DraftTextDescription(item, expanded = expandedIndex == index)

                            AnimatedVisibility(
                                visible = expandedIndex == index,
                                enter = expandHorizontally(
                                    animationSpec = tween(durationMillis = 700),
                                    initialWidth = { width -> width/3 }
                                ) + fadeIn(spring(stiffness = 1000f)),
                                exit = shrinkVertically(
                                    animationSpec = tween(durationMillis = 500, easing = LinearEasing),
                                    targetHeight = { 0 }
                                ) + slideOutHorizontally(
                                    tween(durationMillis = 400, easing = LinearEasing),
                                    targetOffsetX = { it }
                                )
                            ) {
                                Text(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .height(48.dp)
                                        .padding(8.dp),
                                    text =  item.contentDescription,
                                    textAlign = TextAlign.Center,
                                    fontSize = Typography.titleSmall.fontSize * 1.2,
                                    fontStyle = Typography.titleSmall.fontStyle,
                                    softWrap = true
                                )
                            }

                            AnimatedVisibility(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.End),
                                visible = index == expandedIndex,
                                enter = expandVertically(
                                    animationSpec = spring(stiffness = 10000.0f, dampingRatio = 9f),
                                    initialHeight = { 0 }
                                ),
                                exit = shrinkVertically(
                                    animationSpec = tween(durationMillis = 500, easing = LinearEasing),
                                    targetHeight = { 0 }
                                )
                            ) {
                                Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                    for (option in ItemDialogOptions.entries) {
                                        Button(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .wrapContentWidth(),
                                            onClick = {
                                                if (expandedIndex != index)
                                                    return@Button
                                                when (option) {
                                                    ItemDialogOptions.GENERATE_DOC -> {
                                                        onGenerateDoc(item)
                                                        expandedIndex = -1
                                                    }

                                                    ItemDialogOptions.DELETE -> {
                                                        showConfirmDialog = true
                                                    }

                                                    ItemDialogOptions.HIDE -> {
                                                        expandedIndex = -1
                                                    }
                                                }
                                            }
                                        ) {
                                            Text(
                                                text=stringResource(option.id),
                                                fontSize = Typography.titleMedium.fontSize * 0.8,
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DraftTextDescription(
    item: DraftEntry,
    expanded: Boolean
) {
    val expandedTitleSize: Float = remember {Typography.titleMedium.fontSize.value * 1.6}.toFloat()
    val normalTitleSize: Float = remember {Typography.titleMedium.fontSize.value * 1.2}.toFloat()


    val contentDescription =
        if (!expanded
            && item.contentDescription.length > (40 - 3 / 2 * item.title.length)) {
            item.contentDescription.subSequence(0, 40 - 3 / 2 * item.title.length).toString() + "..."
        } else {
            ""
        }

    val titleSize = remember {
        Animatable(
            if (expanded) {
                normalTitleSize
            } else {
                expandedTitleSize
            }
        )
    }
    LaunchedEffect(expanded) {
        if (expanded) {
            titleSize.animateTo(
                targetValue = expandedTitleSize,
                animationSpec = spring(stiffness = 10000f)
            )
        }
        else {
            titleSize.animateTo(
                targetValue = normalTitleSize,
                animationSpec = tween(easing = FastOutSlowInEasing, durationMillis = 300)
            )
        }

    }

    Row(Modifier.wrapContentSize(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier
                .wrapContentSize()
                .height(48.dp)
                .padding(8.dp),
            text = item.title,
            textAlign = TextAlign.Center,
            fontSize = titleSize.value.sp,
            fontStyle = Typography.titleMedium.fontStyle,
            fontWeight = FontWeight.Bold,
            softWrap = true
        )

        Text(
            modifier = Modifier
                .wrapContentSize()
                .height(48.dp)
                .padding(8.dp),
            text =  contentDescription,
            textAlign = TextAlign.Center,
            fontSize = Typography.titleSmall.fontSize * 1.2,
            fontStyle = Typography.titleSmall.fontStyle,
            softWrap = true
        )
    }


//    ItemDialogHandler(
//        currentDialog,
//        onGenerate = { onGenerateDoc(it) },
//        onDelete = { onDeleteDraft(it) },
//    )
}

//private sealed class ItemDialogType {
//    data object Hidden : ItemDialogType()
//    data class OptionsDialog(val draftData: DraftEntry) : ItemDialogType()
//    data class DeleteDialog(val draftData: DraftEntry) : ItemDialogType()
//}

private enum class ItemDialogOptions(val id: Int) {
    GENERATE_DOC(R.string.generate),
    DELETE(R.string.delete),
    HIDE(R.string.collapse)
}

//@Composable
//private fun ItemDialogHandler(
//    currentDialog: MutableState<ItemDialogType>,
//    onGenerate: (draft: DraftEntry) -> Unit,
//    onDelete: (draft: DraftEntry) -> Unit,
//) {
//    when (currentDialog.value) {
//        is OptionsDialog -> {
//            ItemOptionsDialog(currentDialog, onGenerate, onDelete)
//        }
//        is DeleteDialog -> {
//            ItemDeleteDialog(currentDialog, onDelete)
//        }
//        is Hidden -> {
//            //pass
//        }
//    }
//}
//
//
//
//@Composable
//private fun ItemOptionsDialog(
//    currentDialog: MutableState<ItemDialogType>,
//    onGenerate: (DraftEntry) -> Unit,
//    onDelete: (DraftEntry) -> Unit,
//) {
//    Dialog(onDismissRequest = { currentDialog.value = Hidden }) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            for (option in ItemDialogOptions.entries) {
//                Button(
//                    modifier = Modifier.padding(8.dp),
//                    onClick = {
//                        when (option) {
//                            ItemDialogOptions.GENERATE_DOC -> {
//                                (currentDialog.value as? OptionsDialog)?.let { onGenerate(it.draftData) }
//                            }
//                            ItemDialogOptions.DELETE -> {
//                                currentDialog.value = DeleteDialog((currentDialog.value as OptionsDialog).draftData)
//                            }
//                            ItemDialogOptions.CANCEL -> {
//                                currentDialog.value = Hidden
//                            }
//                        }
//                    }
//                ) {
//                    Text(stringResource(option.id))
//                }
//            }
//        }
//
//    }
//}
//
//@Composable
//private fun ItemDeleteDialog(
//    currentDialog: MutableState<ItemDialogType>,
//    onDelete: (DraftEntry) -> Unit,
//) {
//    AlertDialog(
//        onDismissRequest = {
//           currentDialog.value = Hidden
//        },
//        confirmButton = {
//            Button(
//                onClick = {
//                    (currentDialog.value as? DeleteDialog)?.let { onDelete(it.draftData) }
//                    currentDialog.value = Hidden
//                }
//            ) {
//                Text(stringResource(R.string.delete))
//            }
//        },
//        dismissButton = {
//            Button(
//                onClick = {
//                    currentDialog.value = Hidden
//                }
//            ) {
//                Text(stringResource(R.string.cancel))
//            }
//        })
//}
