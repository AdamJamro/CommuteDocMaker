package com.example.commutedocmaker


import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.commutedocmaker.dataSource.draftEntry.DraftEntry
import com.example.commutedocmaker.ui.AutoDetailsView
import com.example.commutedocmaker.ui.DocMakerAppLoadingView
import com.example.commutedocmaker.ui.DocShareListView
import com.example.commutedocmaker.ui.DraftListView
import com.example.commutedocmaker.ui.theme.Typography
import com.example.commutedocmaker.ui.theme.docAppTextStyles
import com.example.commutedocmaker.ui.viewModels.DocMakerAppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.example.commutedocmaker.DocMakerAppViews.DOC_SHARE_LIST_VIEW
import com.example.commutedocmaker.dataSource.document.Document
import kotlin.math.min
import kotlin.math.roundToInt

enum class DocMakerAppViews {
    DRAFT_LIST_VIEW,
    DOC_SHARE_LIST_VIEW,
    AUTO_DETAILS_VIEW
}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun DocMakerAppView(
    viewModel: DocMakerAppViewModel,
    startScreen: DocMakerAppViews = DocMakerAppViews.DRAFT_LIST_VIEW,
//    canNavigateBack: Boolean = false,
    onOpenDraftEditor: (
        draftTitle: String,
        draftData: DraftEntry?,
        draftIndex: Int
            ) -> Unit,
    onSendDocument: (Document) -> Unit,
    onGenerateDocument: (DraftEntry) -> Boolean,
    navController: NavHostController = rememberNavController()
) {

    val draftEntries by viewModel.entries.collectAsState()
    val autoDetails by viewModel.autoDetails.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    val getCurrentScreen = {
        DocMakerAppViews.valueOf(
            backStackEntry?.destination?.route ?: startScreen.name
        )
    }
    val showLoadingAnimation = viewModel.isLoading.value

    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var spreadsheetData by remember { mutableStateOf<List<DraftEntry>>(emptyList()) }

    DraftPickNameDialog(
        shouldShowDialog = showDialog,
        onDismissRequest = { showDialog = false },
        onSubmitted = { draftTitle ->
            showDialog = false
            onOpenDraftEditor(draftTitle, null, -1)
        }
    )

    AnimatedContent(
        targetState = showLoadingAnimation,
        label = "mainAppViewWrap",
        transitionSpec = {
            fadeIn(animationSpec = tween(500)) togetherWith
                    fadeOut(animationSpec = tween(1500))
        }
    ) { targetState->
        if (targetState)
            DocMakerAppLoadingView()
        else{
            DocMakerSideMenu(
                drawerState = drawerState,
                currentScreen = getCurrentScreen(),
                onClickHomeScreen = {
                    navController.navigate(DocMakerAppViews.DRAFT_LIST_VIEW.name)
                    scope.launch { drawerState.close() }
                },
                onClickDocShareListScreen = {
                    navController.navigate(DOC_SHARE_LIST_VIEW.name)
                    scope.launch { drawerState.close() }
                },
                onClickAutoDetails = {
                    navController.navigate(DocMakerAppViews.AUTO_DETAILS_VIEW.name)
                    scope.launch { drawerState.close() }
                }
            ) {
                Scaffold(
                    floatingActionButton = {
                        AnimatedFab(
                            modifier = Modifier.padding(8.dp),
                            onClick = {
                                if (getCurrentScreen() != DocMakerAppViews.DRAFT_LIST_VIEW)
                                    navController.navigate(DocMakerAppViews.DRAFT_LIST_VIEW.name)
                                showDialog = true
                            }
                        )
                    },
                    content = { contentPadding ->
//                var uiState: CommuteDocMakerScreenState by remember { mutableStateOf(CommuteDocMakerScreenState()) }

                        NavHost(
                            modifier = Modifier
                                .padding(contentPadding)
                                .fillMaxSize(),
                            navController = navController,
                            startDestination = startScreen.name,
                        ) {
                            composable(DocMakerAppViews.DRAFT_LIST_VIEW.name) {
                                DraftListView(
                                    draftItems = draftEntries,
                                    onEditDraft = { draftData: DraftEntry ->
                                        onOpenDraftEditor(
                                            draftData.title,
                                            draftData,
                                            draftEntries.indexOf(draftData)
                                        )
                                    },
                                    onGenerateDoc = { draftData: DraftEntry ->
                                        if (onGenerateDocument(draftData)) {
                                            navController.navigate(DOC_SHARE_LIST_VIEW.name)
                                        }
                                    },
                                    onDeleteDraft = { draftData: DraftEntry ->
                                        viewModel.deleteEntry(context, draftData)
                                    }
                                )
                            }
                            composable(DOC_SHARE_LIST_VIEW.name) {
                                DocShareListView(
                                    viewModel = viewModel,
                                    onDocClick = { document ->
                                        onSendDocument(document)
                                    }
                                )
                            }
                            composable(DocMakerAppViews.AUTO_DETAILS_VIEW.name) {
                                AutoDetailsView(autoDetails) { detail, value ->
                                    viewModel.updateAutoDetails(detail, value)
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DocMakerSideMenu(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    currentScreen: DocMakerAppViews,
    onClickHomeScreen: () -> Unit,
    onClickDocShareListScreen: () -> Unit,
    onClickAutoDetails: () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        modifier = modifier,
        gesturesEnabled = true,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Drawer content",
                    modifier = Modifier.padding(16.dp),
                    fontSize = Typography.titleLarge.fontSize
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(text = stringResource(id = R.string.drafts_screen_name)) },
                    onClick = onClickHomeScreen,
                    selected = currentScreen == DocMakerAppViews.DRAFT_LIST_VIEW
                ) 
                NavigationDrawerItem(
                    label = { Text(text = stringResource(id = R.string.generated_files_screen_name)) },
                    onClick = onClickDocShareListScreen,
                    selected = currentScreen == DOC_SHARE_LIST_VIEW
                )
                NavigationDrawerItem(
                    label = { Text(text = stringResource(id = R.string.auto_details_screen_name)) },
                    onClick = onClickAutoDetails,
                    selected = currentScreen == DocMakerAppViews.AUTO_DETAILS_VIEW
                )
            }
        },
        content = content
    )
}


@Composable
fun DraftPickNameDialog(
    shouldShowDialog: Boolean,
    onDismissRequest: () -> Unit,
    onSubmitted: (String) -> Unit,
    previousName: String = ""
    ) {
    if (!shouldShowDialog) return
    val context = LocalContext.current
    var text by remember { mutableStateOf(previousName) }
    val onSubmitWithValidityCheck = { str: String ->
        var submittedName: String = str
        submittedName.trim().also { submittedName = it }
        if (isFileNameValid(submittedName)) {
            onSubmitted(submittedName)
        } else {
            Toast.makeText(context, "Invalid name!", Toast.LENGTH_SHORT).show()
            onDismissRequest()
        }
        text = ""
    }

    Dialog(
        onDismissRequest = {
            onDismissRequest()
            text = ""
        },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
    ) {
        Surface(
            modifier = Modifier.wrapContentSize(),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.background
        ) {
            Column (modifier = Modifier.padding(8.dp)) {

                Text(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.CenterHorizontally),
                    text = "Enter new draft's name")

                Divider()

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.CenterHorizontally),
                    value = text,
                    onValueChange = { newText: String ->
                        text = newText
                    },
                    label = { Text("name") },
                    singleLine = true
                )

                Button(
                    modifier = Modifier
                        .wrapContentHeight()
                        .align(alignment = Alignment.End),
                    onClick = { onSubmitWithValidityCheck(text) }
                ) {
                    Text(
                        text = "Confirm",
                        color = Color.White
                    )
                }
            }
        }
    }
}


@Composable
fun AnimatedFab(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val fabWidth: Dp by animateDpAsState(
        targetValue = if (expanded) 200.dp else 56.dp,
        animationSpec = spring(
            stiffness = 250f,
            dampingRatio = 0.7f
        ),
        label = "fabWidth"
    )

    LaunchedEffect(Unit) {
        delay(2500)
        expanded = true
        delay(3000)
        expanded = false
    }

    FloatingActionButton(
        onClick = {
            onClick()
        },
        modifier = modifier.width(fabWidth)
    ) {
        Surface(
            color = Color.Transparent,
            modifier = Modifier.wrapContentSize()
        ) {
            AnimatedVisibility(
                visible = !expanded,
                enter = fadeIn(animationSpec = tween(2000)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
            Box(
                modifier = Modifier.wrapContentSize(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    modifier = Modifier.wrapContentSize(),
                    visible = expanded,
                    enter = expandHorizontally(
                        animationSpec = tween(durationMillis = 3000)
                    ),
                    exit = shrinkHorizontally(animationSpec = tween(100))
                ) {
                    Text(
                        text = stringResource(R.string.fab_add_new_draft),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .width(fabWidth),
                        fontWeight = docAppTextStyles["fab"]?.fontWeight,
                        color = docAppTextStyles["fab"]?.color ?: Color.Black,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                }
            }

        }

    }


}
