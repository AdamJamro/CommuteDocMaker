package com.example.commutedocmaker

import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.text.trimmedLength
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.commutedocmaker.dataSource.SpreadsheetPartialData
import com.example.commutedocmaker.ui.CommuteDocShareListScreen
import com.example.commutedocmaker.ui.HomeScreen
import com.example.commutedocmaker.ui.theme.Typography
import com.example.commutedocmaker.ui.viewModels.DocMakerAppViewModel
import kotlinx.coroutines.launch

enum class CommuteDocMakerScreen {
    HOME_SCREEN,
    COMMUTE_DOC_SHARE_LIST_SCREEN,
    AUTO_DETAILS_SCREEN
}


@Composable
fun DocMakerSideMenu(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    currentScreen: CommuteDocMakerScreen,
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
                Divider()
                NavigationDrawerItem(
                    label = { Text(text = stringResource(id = R.string.drafts_screen_name)) },
                    onClick = onClickHomeScreen,
                    selected = currentScreen == CommuteDocMakerScreen.HOME_SCREEN
                ) 
                NavigationDrawerItem(
                    label = { Text(text = stringResource(id = R.string.generated_files_screen_name)) },
                    onClick = onClickDocShareListScreen,
                    selected = currentScreen == CommuteDocMakerScreen.COMMUTE_DOC_SHARE_LIST_SCREEN
                )
                NavigationDrawerItem(
                    label = { Text(text = stringResource(id = R.string.auto_details_screen_name)) },
                    onClick = onClickAutoDetails,
                    selected = currentScreen == CommuteDocMakerScreen.AUTO_DETAILS_SCREEN
                )
            }
        },
        content = content
    )
}

@Composable
fun CreateDraftAlertDialog(
    shouldShowDialog: Boolean,
    onDismissRequest: () -> Unit,
    onSubmitted: (String) -> Unit
    ) {
    val context = LocalContext.current

    fun nameIsValid(name: String): Boolean {
        if (name.length > 25) return false

        if (name.trimmedLength() == 0) return false

        for (char in name) {
            if ((!char.isLetterOrDigit() && char != ' ') || char == '\n' ) return false
        }

        return true
    }

    var text by remember { mutableStateOf("") }
    val onDismissRequestWrapped = {
        onDismissRequest()
        text = ""
    }
    val onSubmittedWrapped = { str: String ->
        var submittedName: String = str
        submittedName.trim().also { submittedName = it }
        if (nameIsValid(submittedName)) {
            onSubmitted(submittedName)
        } else {
            Toast.makeText(context, "Invalid name!", Toast.LENGTH_SHORT).show()
            onDismissRequest()
        }
        text = ""
    }


    if (shouldShowDialog) {
        Dialog(
            onDismissRequest = onDismissRequestWrapped,
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
                        onClick = { onSubmittedWrapped(text) }
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
}

@Composable
fun DocMakerApp(
    viewModel: DocMakerAppViewModel,
    startScreen: CommuteDocMakerScreen = CommuteDocMakerScreen.HOME_SCREEN,
//    canNavigateBack: Boolean = false,
    onCreateNewDraft: (String) -> Unit,
//    onEditExistingDraft: (String) -> Any,
    navController: NavHostController = rememberNavController()
) {

    val entries by viewModel.entries.collectAsState()
    var shouldShowDialog by remember { mutableStateOf(false) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = CommuteDocMakerScreen.valueOf(
        backStackEntry?.destination?.route ?: startScreen.name
    )
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var spreadsheetData by remember { mutableStateOf<List<SpreadsheetPartialData>>(emptyList()) }

    CreateDraftAlertDialog(
        shouldShowDialog = shouldShowDialog,
        onDismissRequest = { shouldShowDialog = false },
        onSubmitted = { name ->
            shouldShowDialog = false
            onCreateNewDraft(name)
        }
    )

    DocMakerSideMenu(
        drawerState = drawerState,
        currentScreen = currentScreen,
        onClickHomeScreen = {
            navController.navigate(CommuteDocMakerScreen.HOME_SCREEN.name)
            scope.launch { drawerState.close() }
        },
        onClickDocShareListScreen = {
            navController.navigate(CommuteDocMakerScreen.COMMUTE_DOC_SHARE_LIST_SCREEN.name)
            scope.launch { drawerState.close() }
        },
        onClickAutoDetails = {
            navController.navigate(CommuteDocMakerScreen.AUTO_DETAILS_SCREEN.name)
            scope.launch { drawerState.close() }
        }
        ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if (currentScreen != CommuteDocMakerScreen.HOME_SCREEN)
                            navController.navigate(CommuteDocMakerScreen.HOME_SCREEN.name)
                        shouldShowDialog = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add new entry")
                }
            },
            content = { contentPadding ->
//                var uiState: CommuteDocMakerScreenState by remember { mutableStateOf(CommuteDocMakerScreenState()) }


                NavHost(
                    modifier = Modifier.padding(contentPadding),
                    navController = navController,
                    startDestination = currentScreen.name,
                ) {
                    composable(CommuteDocMakerScreen.HOME_SCREEN.name) {
                        HomeScreen(fileItems = entries)
                    }
                    composable(CommuteDocMakerScreen.COMMUTE_DOC_SHARE_LIST_SCREEN.name) {
                        CommuteDocShareListScreen(docViewModel = viewModel)
                    }
                    composable(CommuteDocMakerScreen.AUTO_DETAILS_SCREEN.name) {
                        Text("Auto details screen")
                    }
                }
            }
        )
    }
}

