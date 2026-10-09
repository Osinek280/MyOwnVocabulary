package com.example.myownvocabulary.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// The parent row handles clicks and selection semantics.
@Composable
fun SelectionCheckbox(
    checked: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier.size(18.dp),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            CheckIcon(color = color)
        }
    }
}
