package com.example.myownvocabulary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.myownvocabulary.ui.components.QuizOptions
import com.example.myownvocabulary.ui.components.QuizOptionsSaver
import com.example.myownvocabulary.ui.components.QuizOptionsSheet

@Composable
fun QuizScreen(
    onBack: () -> Unit = {},
    onOptionsChanged: (QuizOptions) -> Unit = {},
    onRestart: (QuizOptions) -> Unit = {},
    isError: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    var optionMenuOpen by rememberSaveable { mutableStateOf(false) }
    var options by rememberSaveable(stateSaver = QuizOptionsSaver) { mutableStateOf(QuizOptions()) }

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Zamknij", tint = colors.onBackground)
            }
            IconButton(onClick = { optionMenuOpen = true }) {
                Icon(Icons.Default.Settings, contentDescription = "Opcje quizu", tint = colors.onBackground)
            }
        }
        QuizProgressBar(
            currentIndex = 1,
            totalCount = 28,
            isError = isError,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)
        )
    }

    if (optionMenuOpen) {
        QuizOptionsSheet(
            options = options,
            onChange = {
                options = it
                onOptionsChanged(it)
            },
            onDismiss = { optionMenuOpen = false },
            onRestart = {
                optionMenuOpen = false
                onRestart(options)
            }
        )
    }
}

@Composable
private fun QuizProgressBar(currentIndex: Int, totalCount: Int, isError: Boolean = false, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val safeTotal = totalCount.coerceAtLeast(0)
    // Number of completed questions; zero is the initial state.
    val current = currentIndex.coerceIn(0, safeTotal)
    val progress = if (safeTotal == 0) 0f else current.toFloat() / safeTotal
    val finished = safeTotal > 0 && current == safeTotal
    val activeColor = when {
        current == 0 -> colors.surfaceContainerHighest
        isError -> colors.error
        else -> colors.tertiary
    }
    val activeContent = when {
        current == 0 -> colors.onSurfaceVariant
        isError -> colors.onError
        else -> colors.onTertiary
    }
    val track = colors.surfaceContainerHighest
    var currentBadgeWidth by remember { mutableIntStateOf(0) }
    val fillBrush = Brush.horizontalGradient(
        0f to activeColor.copy(alpha = 0.15f),
        0.15f to activeColor,
        1f to activeColor
    )

    Box(
        modifier = modifier.fillMaxWidth().semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f)
        }
    ) {
        Box(Modifier.matchParentSize(), contentAlignment = Alignment.CenterStart) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (finished) {
                            fillBrush
                        } else {
                            Brush.horizontalGradient(
                                0f to track,
                                0.8f to track,
                                1f to track.copy(alpha = 0.15f)
                            )
                        }
                    )
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (finished) {
                Spacer(Modifier.weight(1f))
            } else {
                if (progress > 0f) {
                    // Extend underneath the badge so the rounded edges meet seamlessly.
                    Box(
                        Modifier
                            .weight(progress)
                            .height(16.dp)
                            .drawBehind {
                                val overlap = currentBadgeWidth / 2f
                                drawRoundRect(
                                    brush = fillBrush,
                                    topLeft = Offset(if (layoutDirection == LayoutDirection.Rtl) -overlap else 0f, 0f),
                                    size = Size(size.width + overlap, size.height),
                                    cornerRadius = CornerRadius(size.height / 2f)
                                )
                            }
                    )
                }
                ProgressBadge(
                    text = current.toString(),
                    background = activeColor,
                    contentColor = activeContent,
                    size = 36.dp,
                    modifier = Modifier.onSizeChanged { currentBadgeWidth = it.width }
                )
                if (progress < 1f) Spacer(Modifier.weight(1f - progress))
                Spacer(Modifier.width(8.dp))
            }
            ProgressBadge(
                text = safeTotal.toString(),
                background = if (finished) activeColor else track,
                contentColor = if (finished) activeContent else colors.onSurfaceVariant,
                size = 36.dp
            )
        }
    }
}

@Composable
private fun ProgressBadge(
    text: String,
    background: Color,
    contentColor: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(size)
            .defaultMinSize(minWidth = size)
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black
        )
    }
}
