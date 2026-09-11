package com.example.myownvocabulary.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myownvocabulary.ui.screens.PartOfSpeech
import com.example.myownvocabulary.ui.theme.Gray400
import com.example.myownvocabulary.ui.theme.Primary
import com.example.myownvocabulary.ui.theme.Success


fun posColor(partOfSpeech: PartOfSpeech): Color = when (partOfSpeech) {
    PartOfSpeech.Verb -> Primary
    PartOfSpeech.Noun -> Color(0xFF6B7280)
    PartOfSpeech.Adjective -> Success
    else -> Gray400
}

fun PartOfSpeech.badgeLabel(): String = name.lowercase()

@Composable
fun ChevronRight(color: Color = Color(0xFFD1D5DB)) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(14.dp)) {
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(5.dp.toPx(), 3.dp.toPx())
            lineTo(9.dp.toPx(), 7.dp.toPx())
            lineTo(5.dp.toPx(), 11.dp.toPx())
        }
        drawPath(path, color, style = stroke)
    }
}

@Composable
fun PlusIcon(color: Color = Color.White, iconSize: Int = 18) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(iconSize.dp)) {
        val mid = this.size.width / 2f
        val pad = 3.dp.toPx()
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(mid, pad), androidx.compose.ui.geometry.Offset(mid, this.size.height - pad), stroke.width, androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(pad, mid), androidx.compose.ui.geometry.Offset(this.size.width - pad, mid), stroke.width, androidx.compose.ui.graphics.StrokeCap.Round)
    }
}

@Composable
fun SearchIcon() {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 1.5.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawCircle(
            Gray400,
            radius = 5.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(7.dp.toPx(), 7.dp.toPx()),
            style = stroke
        )
        drawLine(
            Gray400,
            androidx.compose.ui.geometry.Offset(11.dp.toPx(), 11.dp.toPx()),
            androidx.compose.ui.geometry.Offset(14.dp.toPx(), 14.dp.toPx()),
            stroke.width,
            androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}