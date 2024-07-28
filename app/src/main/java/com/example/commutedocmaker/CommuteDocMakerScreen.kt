package com.example.commutedocmaker

import androidx.compose.animation.core.animate
import androidx.compose.foundation.CombinedClickableNode
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cupcake.ui.theme.CommuteDocMakerTheme
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
                Text("Drawer content", modifier = Modifier.padding(16.dp))
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
fun DocMakerApp(
    startScreen: CommuteDocMakerScreen = CommuteDocMakerScreen.HOME_SCREEN,
    canNavigateBack: Boolean = false,
    navController: NavHostController = rememberNavController(),
    viewModel: CommuteDocMakerViewModel = CommuteDocMakerViewModel()
) {

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = CommuteDocMakerScreen.valueOf(
        backStackEntry?.destination?.route ?: startScreen.name
    )
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

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
                        viewModel.entries.add("new_entry")
                        navController.navigate(CommuteDocMakerScreen.HOME_SCREEN.name)
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add new entry")
                }
            },
            content = { contentPadding ->
                NavHost(
                    navController = navController,
                    startDestination = currentScreen.name,
                    modifier = Modifier.padding(contentPadding),
                ) {
                    composable(CommuteDocMakerScreen.HOME_SCREEN.name) {
                        HomeScreen(
                            viewModel = viewModel
                        )
                    }
                    composable(CommuteDocMakerScreen.COMMUTE_DOC_SHARE_LIST_SCREEN.name) {
                        Text(text = "Commute doc share list screen")
                    }
                    composable(CommuteDocMakerScreen.AUTO_DETAILS_SCREEN.name) {
                        Text("Auto details screen")
                    }
                }
            }
        )

    }
}

@Composable
fun HomeScreen(
    viewModel: CommuteDocMakerViewModel
//    entries: List<@Composable () -> Unit> = emptyList()
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),

    ) {
        viewModel.entries.forEach { entry ->
            item {
                Text(text = entry)
            }
        }
    }
}


@Preview
@Composable
fun PreviewDocMakerSideMenu() {
    CommuteDocMakerTheme {
        DocMakerApp(
            startScreen = CommuteDocMakerScreen.HOME_SCREEN,
        )
    }
}

@Preview
@Composable
fun PreviewDocMakerSideMenu2() {
    CommuteDocMakerTheme {
        DocMakerApp(
            startScreen = CommuteDocMakerScreen.COMMUTE_DOC_SHARE_LIST_SCREEN,
        )
    }
}

@Preview
@Composable
fun PreviewDocMakerSideMenu3() {
    CommuteDocMakerTheme {
        DocMakerApp(
            startScreen = CommuteDocMakerScreen.AUTO_DETAILS_SCREEN,
        )
    }
}