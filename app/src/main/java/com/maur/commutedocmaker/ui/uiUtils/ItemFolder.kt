package com.maur.commutedocmaker.ui.uiUtils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maur.commutedocmaker.ui.theme.Typography

@Composable
fun ItemFolder(
    modifier: Modifier = Modifier,
    emptyFolderLabel: String = "This Folder is Empty",
    emptyFolderIcon: @Composable () -> Unit = {},
    items: List<Any> = emptyList(),
    content: @Composable () -> Unit
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                .background(Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            emptyFolderIcon()
            Text(
                text = emptyFolderLabel,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp),
                textAlign = TextAlign.Center,
                fontSize = Typography.titleMedium.fontSize,
                fontStyle = Typography.titleMedium.fontStyle,
                fontWeight = FontWeight.Light
            )
        }
    }
    else {
        content()
    }
}