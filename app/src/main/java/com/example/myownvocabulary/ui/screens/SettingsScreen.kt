package com.example.myownvocabulary.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myownvocabulary.ui.components.ChevronRight
import com.example.myownvocabulary.ui.components.SectionLabel
import com.example.myownvocabulary.ui.text.PolishNoun
import com.example.myownvocabulary.ui.viewmodel.TransferResult

@Composable
fun SettingsScreen(
    canExport: Boolean,
    error: String?,
    result: TransferResult?,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onDismissFeedback: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    if (error != null) {
        AlertDialog(
            onDismissRequest = onDismissFeedback,
            title = { Text("Nie udało się") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = onDismissFeedback) { Text("OK") }
            }
        )
    } else if (result != null) {
        AlertDialog(
            onDismissRequest = onDismissFeedback,
            title = { Text("Gotowe") },
            text = { Text(result.message()) },
            confirmButton = {
                TextButton(onClick = onDismissFeedback) { Text("OK") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Text(
            "Opcje",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp)
        )
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionLabel("Dane")
            SettingsAction(
                title = "Eksportuj",
                subtitle = if (canExport) {
                    "Wszystkie albo wybrane według języka i typu."
                } else {
                    "Brak wpisów do eksportu."
                },
                up = true,
                enabled = canExport,
                onClick = onExport
            )
            SettingsAction(
                title = "Importuj",
                subtitle = "Wczytaj wpisy z pliku JSON.",
                up = false,
                enabled = true,
                onClick = onImport
            )
        }
    }
}

@Composable
private fun SettingsAction(title: String, subtitle: String, up: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val iconBackground = if (enabled) colors.primary else colors.surface
    val iconColor = if (enabled) colors.onPrimary else colors.outline
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            DirectionArrow(up = up, color = iconColor)
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) colors.onSurface else colors.outline
            )
            Text(subtitle, fontSize = 13.sp, color = colors.outline)
        }
        ChevronRight()
    }
}

@Composable
private fun DirectionArrow(up: Boolean, color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        val mid = size.width / 2f
        val top = 2.dp.toPx()
        val bottom = size.height - top
        drawLine(color, Offset(mid, top), Offset(mid, bottom), stroke.width, StrokeCap.Round)
        val head = if (up) top else bottom
        val towardShaft = if (up) 1f else -1f
        val path = Path().apply {
            moveTo(mid - 5.dp.toPx(), head + towardShaft * 6.dp.toPx())
            lineTo(mid, head)
            lineTo(mid + 5.dp.toPx(), head + towardShaft * 6.dp.toPx())
        }
        drawPath(path, color, style = stroke)
    }
}

private fun TransferResult.message(): String = when (this) {
    is TransferResult.Exported -> "Wyeksportowano ${EntryNoun.withCount(count)}."
    is TransferResult.Imported -> {
        val parts = buildList {
            if (added > 0) add("Dodano ${EntryNoun.withCount(added)}")
            if (updated > 0) add("Zaktualizowano ${EntryNoun.withCount(updated)}")
            if (skipped > 0) add("Pominięto ${EntryNoun.withCount(skipped)}")
        }
        if (parts.isEmpty()) "Nie zmieniono żadnego wpisu." else parts.joinToString(". ") + "."
    }
}

private val EntryNoun = PolishNoun(
    nominativeSingular = "wpis",
    nominativePlural = "wpisy",
    genitivePlural = "wpisów"
)
