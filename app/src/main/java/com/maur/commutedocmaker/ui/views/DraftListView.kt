package com.maur.commutedocmaker.ui.views
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBoxValue.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.maur.commutedocmaker.dataSource.draftEntry.DraftEntry
import com.maur.commutedocmaker.ui.theme.Typography
import com.maur.commutedocmaker.R
import com.maur.commutedocmaker.stringResource
import kotlinx.coroutines.launch

@Composable
fun DraftListView(
//    docViewModel: CommuteDocMakerViewModel
    draftItems: List<DraftEntry>,
    expandedIndex: Int,
    onSelectItem: (Int) -> Unit,
    onDeleteDraft: (draftData: DraftEntry) -> Unit,
    onGenerateDoc: (draftData: DraftEntry) -> Unit,
    onEditDraft: (draftData: DraftEntry) -> Unit
) {
//    val fileItems = docViewModel.entries.collectAsState()
//    val currentDialog: MutableState<ItemDialogType> = remember { mutableStateOf(Hidden) }
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Column(Modifier
                    .wrapContentSize()
                    .padding(4.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(12.dp),
                        text = stringResource(R.string.confirm_deletion),
                        fontSize = Typography.titleMedium.fontSize,

                    )

                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDraft(draftItems[expandedIndex])
                        showConfirmDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(6.dp)
        .clickableWithoutRipple(
            interactionSource = interactionSource,
            onClick = {
                onSelectItem(-1)
            })
        .background(Color.Transparent)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(draftItems, key = {_, item -> item.title}) { index: Int, item: DraftEntry ->
                val itemBorderAlpha = remember(index) { Animatable(0f) }
                LaunchedEffect(expandedIndex) {
                    itemBorderAlpha.animateTo(
                        targetValue = if (expandedIndex == index) 1f else 0f,
                        animationSpec = tween(durationMillis = 500)
                    )
                }
//                val charPool = remember { ('a'..'z') + ('0'..'9') }
//                val randomTitleLength = Random.nextInt(5,21)
//                val randomDescriptionLength = Random.nextInt(5,11)
//                val rememberItemId = remember(item.draftId) { item.draftId }
//                LaunchedEffect(Unit){
//                    draftItems[index] = item
//                    draftItems.add(0,
//                        DraftEntry(
//                            (1..randomTitleLength).map { charPool.random() }.joinToString(""),
//                            (1..randomTitleLength).map { charPool.random() }.joinToString(""),
//                        )
//                    )
//                }
                Surface(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 1.dp,
                    shadowElevation = 2.dp,
                ) {
                    val swipeState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            when(it) {
                                EndToStart -> { showConfirmDialog = true }
                                else -> { /*nothing*/ }
                            }
                            false
                        },
                        positionalThreshold = { totalDistance -> totalDistance / 3.5f}
                    )
                    SwipeToDismissBox(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(2.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = itemBorderAlpha.value), RoundedCornerShape(8.dp))
                            .clickableWithoutRipple(
                                interactionSource = interactionSource,
                                onClick = {
                                    if (expandedIndex == index)
                                        onEditDraft(item)
                                    else {
                                        onSelectItem(index)
                                    }
                                }
                            ),
//                    onLongClick = { currentDialog.value = OptionsDialog(item) }
                        state = swipeState,
                        enableDismissFromEndToStart = true,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color by
                            animateColorAsState(
                                when (swipeState.targetValue) {
                                    StartToEnd -> MaterialTheme.colorScheme.surfaceDim
                                    EndToStart -> MaterialTheme.colorScheme.errorContainer
                                    Settled -> MaterialTheme.colorScheme.surfaceDim
                                },
                                animationSpec = tween(durationMillis = 400),
                                label = "swipe dismiss background color"
                            )
                            val iconSaturation by
                            animateFloatAsState(
                                when (swipeState.targetValue) {
                                    StartToEnd -> 0.15f
                                    EndToStart -> 1f
                                    Settled -> 0.15f
                                },
                                animationSpec = tween(durationMillis = 400),
                                label = "swipe dismiss icon saturation"
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    modifier = Modifier.padding(12.dp).alpha(iconSaturation),
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                    ) {
                        when (swipeState.currentValue) {
                            StartToEnd -> {
                                //pass
                            }
                            EndToStart -> {
                                onDeleteDraft(draftItems[index])
                            }
                            Settled -> {
                                Column(
                                    Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clip(RoundedCornerShape(8.dp))
                                        .padding(start = 4.dp, end = 4.dp),
                                ) {
                                    DraftTextDescription(item, isExpanded = expandedIndex == index)

                                    AnimatedVisibility(
                                        modifier = Modifier.fillMaxSize(),
                                        visible = expandedIndex == index,
                                        enter = slideInHorizontally(
                                            animationSpec = tween(durationMillis = 500),
                                            initialOffsetX = { -it }
                                        ) + fadeIn(spring(stiffness = 5000f)),
                                        exit = shrinkVertically(
                                            animationSpec = tween(durationMillis = 500, easing = LinearEasing),
                                            targetHeight = { 0 }
                                        ) + slideOutHorizontally(
                                            tween(durationMillis = 400, easing = LinearEasing),
                                            targetOffsetX = { -it }
                                        )
                                    ) {
                                        Text(
                                            modifier = Modifier
                                                .wrapContentSize()
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
                                        Row(
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            for (option in ItemDialogOptions.entries) {
                                                Button(
                                                    modifier = Modifier
                                                        .padding(4.dp)
                                                        .wrapContentWidth(),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    ),
                                                    onClick = {
                                                        if (expandedIndex != index)
                                                            return@Button
                                                        when (option) {
                                                            ItemDialogOptions.GENERATE_DOC -> {
                                                                onGenerateDoc(item)
                                                                onSelectItem(-1)
                                                            }

                                                            ItemDialogOptions.DELETE -> {
                                                                showConfirmDialog = true
                                                                // don't roll back expandedIndex here
                                                                // dialog needs to be closed first
                                                            }

                                                            ItemDialogOptions.HIDE -> {
                                                                onSelectItem(-1)
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Text(
                                                        text=stringResource(option.id),
                                                        fontSize = Typography.titleMedium.fontSize * 0.8,
                                                        textAlign = TextAlign.Center,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
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

@Composable
fun DraftTextDescription(
    item: DraftEntry,
    isExpanded: Boolean
) {
    val expandedTitleSize: Float = remember { (Typography.titleMedium.fontSize.value * 1.6).toFloat() }
    val normalTitleSize: Float = remember { (Typography.titleMedium.fontSize.value * 1.2).toFloat() }
    val density = LocalDensity.current
    val transition = updateTransition(isExpanded, label = "expand draft description")
    val configuration = LocalConfiguration.current

    // Get screen width and height in pixels
    val screenWidth = configuration.screenWidthDp.dp
//    val screenHeight = configuration.screenHeightDp.dp

//    val contentDescription = item.contentDescription
//        if (item.contentDescription.length > (FILENAME_MAX_LENGTH - item.title.length)) {
//            item.contentDescription.subSequence(0, FILENAME_MAX_LENGTH - item.title.length).toString() + "..."
//        } else {
//            item.contentDescription
//        }

    val animatedTitleSize by
            transition.animateFloat(
                transitionSpec = { tween(durationMillis = 100) },
                label = "animate draft title size",
                targetValueByState = { state ->
                    when (state) {
                        true -> expandedTitleSize
                        false -> normalTitleSize
                    }
                }
            )




//    LaunchedEffect(expanded) {
//        if (expanded) {
//            animatedTitleSize.animateTo(
//                targetValue = expandedTitleSize,
//                animationSpec = spring(stiffness = 10000f)
//            )
//        }
//        else {
//            animatedTitleSize.animateTo(
//                targetValue = normalTitleSize,
//                animationSpec = tween(easing = FastOutSlowInEasing, durationMillis = 300)
//            )
//        }
//
//    }
    var titleWidth by remember { mutableStateOf(100.dp) }
    var descriptionWidth by remember { mutableStateOf(0.dp) }
    var parentHeight by remember { mutableStateOf(0.dp) }
    var parentWidth by remember { mutableStateOf(0.dp) }
    val animatedOffset = remember { Animatable(
        Offset(screenWidth.value, 1.dp.value),
        Offset.VectorConverter
    ) }

    Box(
        Modifier
            .fillMaxWidth()
//            .onGloballyPositioned { layoutCoordinates ->
//                parentHeight = with(density) {
//                    layoutCoordinates.parentLayoutCoordinates?.size?.height?.toDp() ?:
//                    layoutCoordinates.size.height.toDp()
//                }
//                parentWidth = with(density) {
//                    layoutCoordinates.parentLayoutCoordinates?.size?.width?.toDp() ?:
//                    layoutCoordinates.size.width.toDp()
//                }
//                Log.d("DEBUG - DRAFT TITLE BOX", "parentWidth: $parentWidth, titleWidth: $titleWidth, descriptionWidth: $descriptionWidth")
//            }

            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                Log.d("DEBUG - DRAFT TITLE BOX", "parentWidth: $parentWidth, titleWidth: $titleWidth, descriptionWidth: $descriptionWidth")
                parentHeight = with(density) {
                    placeable.height.toDp()
                }
                parentWidth = with(density) {
                    placeable.width.toDp()
                }
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            }
//            .border(1.dp, Color.White, RoundedCornerShape(8.dp))
            .padding(top = 4.dp, bottom = 8.dp)
    ) {
        Text(
            modifier = Modifier
                .wrapContentWidth()
                .fillMaxHeight()
//                .border(1.dp, Color.Red, RoundedCornerShape(8.dp))
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    titleWidth = with(density) { placeable.width.toDp() }
                    if ((titleWidth > normalTitleSize.dp  || descriptionWidth <= 0.dp) && !isExpanded) {
                        descriptionWidth = parentWidth - titleWidth
                    }
                    Log.d("DEBUG - DRAFT TITLE", "parentWidth: $parentWidth, titleWidth: $titleWidth, descriptionWidth: $descriptionWidth")
                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, 0)
                    }
                }
//                .onPlaced { layoutCoordinates ->
//                    titleWidth = with(density) { layoutCoordinates.size.width.toDp() }
//                    if (descriptionWidth <= 1.dp) {
//                        descriptionWidth = parentWidth - titleWidth - 12.dp
//                    }
//                    Log.d(
//                        "DEBUG - DRAFT TITLE",
//                        "parentWidth: $parentWidth, titleWidth: $titleWidth, descriptionWidth: $descriptionWidth"
//                    )
//                }
                .padding(8.dp),
//            onTextLayout = { textLayoutResult ->
//                titleWidth = textLayoutResult.size.width.dp
//                descriptionWidth = parentWidth - titleWidth - 12.dp
//                if (descriptionWidth <= 1.dp) {
//                }
//                Log.d("DEBUG - DRAFT TITLE", "parentWidth: $parentWidth, titleWidth: $titleWidth, descriptionWidth: $descriptionWidth")
//            },
            text = item.title,
            textAlign = TextAlign.Center,
            fontSize = animatedTitleSize.sp,
            fontStyle = Typography.titleMedium.fontStyle,
            fontWeight = FontWeight.Bold,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )

//        Spacer(modifier = Modifier.size(4.dp))
        val displayItemDescription = item.contentDescription.removePrefix(stringResource(R.string.period) + ":\n")
//        val animatedOffset by
//            transition.animateOffset(
//                transitionSpec = { tween(300) },
//                label = "draft description offset",
//                targetValueByState = { state ->
//                    when (state) {
//                        true -> Offset(parentWidth.value, 1.dp.value)
//                        false -> Offset(titleWidth.value, 1.dp.value)
//                    }
//                })

        Log.d("DEBUG - DRAFT DESCRIPTION", "parentWidth: $parentWidth, titleWidth: $titleWidth, descriptionWidth: $descriptionWidth" +
                "\n animatedOffset: $animatedOffset")
        AnimatedVisibility(
            visible = !isExpanded,
            enter = slideInHorizontally(
                animationSpec = tween(durationMillis = 600),
                initialOffsetX = { it }
            ),
            exit = slideOutHorizontally(
                animationSpec = tween(durationMillis = 1000,
                    easing = FastOutSlowInEasing),
                targetOffsetX = { it }
            )
        ) {

            Text(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(descriptionWidth)
//                    .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                    .offset(titleWidth, 1.dp)
//                    .border(1.dp, Color.Green, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                text = displayItemDescription,
                textAlign = TextAlign.Start,
                fontSize = Typography.titleSmall.fontSize * 1.2,
                fontStyle = Typography.titleSmall.fontStyle,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
//    Spacer(modifier = Modifier.size(12.dp))


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