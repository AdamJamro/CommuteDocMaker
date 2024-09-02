package com.example.commutedocmaker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.commutedocmaker.ui.viewModels.DocMakerAppViewModel
import com.example.commutedocmaker.ui.theme.Typography

@Composable
fun DocShareListView (
    viewModel: DocMakerAppViewModel
//    fileItem: List<DraftEntry>
) {
    val fileItems = viewModel.entries.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(fileItems.value) { item ->
            Surface(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(8.dp),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 1.dp,
                    shadowElevation = 2.dp,
                    onClick = { /* Handle item click */ }
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
                        text = if (item.contentDescription.length > (40 - 3 / 2 * item.title.length)) {
                            item.contentDescription.subSequence(0, 40 - 3 / 2 * item.title.length).toString() + "..."
                        } else {
                            item.contentDescription
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