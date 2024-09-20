package com.maur.commutedocmaker.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun DocMakerAppLoadingView() {
//    var displayText by remember { mutableStateOf("Loading") }

//    LaunchedEffect(Unit) {
//        while(true) {
//            displayText = "Loading."
//            delay(300)
//            displayText = "Loading.."
//            delay(300)
//            displayText = "Loading..."
//            delay(600)
//        }
//    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
//        Text(displayText)
    }
}