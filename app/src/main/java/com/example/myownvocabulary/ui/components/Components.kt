package com.example.myownvocabulary.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myownvocabulary.data.word.PartOfSpeech

@Composable
fun posColor(partOfSpeech: PartOfSpeech): Color {
    val colors = MaterialTheme.colorScheme
    return when (partOfSpeech) {
        PartOfSpeech.Verb -> colors.primary
        PartOfSpeech.Noun -> colors.onSurfaceVariant
        PartOfSpeech.Adjective -> colors.tertiary
        else -> colors.outline
    }
}

fun PartOfSpeech.badgeLabel(): String = name.lowercase()

@Composable
fun BackButton(
    onPress: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onBackground,
) {
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .size(24.dp)
            .clickable(onClick = onPress),
    ) {
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 2.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round,
        )
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(14.dp.toPx(), 5.dp.toPx())
            lineTo(7.dp.toPx(), 12.dp.toPx())
            lineTo(14.dp.toPx(), 19.dp.toPx())
        }
        drawPath(path, color, style = stroke)
    }
}

@Composable
fun ChevronRight(color: Color = MaterialTheme.colorScheme.outlineVariant) {
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
fun PlusIcon(color: Color = MaterialTheme.colorScheme.onPrimary, iconSize: Int = 18) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(iconSize.dp)) {
        val mid = this.size.width / 2f
        val pad = 3.dp.toPx()
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(mid, pad), androidx.compose.ui.geometry.Offset(mid, this.size.height - pad), stroke.width, androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(pad, mid), androidx.compose.ui.geometry.Offset(this.size.width - pad, mid), stroke.width, androidx.compose.ui.graphics.StrokeCap.Round)
    }
}

@Composable
fun SearchIcon(color: Color = MaterialTheme.colorScheme.outline) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 1.5.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawCircle(
            color,
            radius = 5.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(7.dp.toPx(), 7.dp.toPx()),
            style = stroke
        )
        drawLine(
            color,
            androidx.compose.ui.geometry.Offset(11.dp.toPx(), 11.dp.toPx()),
            androidx.compose.ui.geometry.Offset(14.dp.toPx(), 14.dp.toPx()),
            stroke.width,
            androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}


@Composable
fun ListIcon(color: Color) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(22.dp)) {
        val r = 1.dp.toPx()
        drawRoundRect(color, androidx.compose.ui.geometry.Offset(3.dp.toPx(), 4.dp.toPx()), androidx.compose.ui.geometry.Size(7.dp.toPx(), 2.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(r))
        drawRoundRect(color, androidx.compose.ui.geometry.Offset(3.dp.toPx(), 10.dp.toPx()), androidx.compose.ui.geometry.Size(16.dp.toPx(), 2.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(r))
        drawRoundRect(color, androidx.compose.ui.geometry.Offset(3.dp.toPx(), 16.dp.toPx()), androidx.compose.ui.geometry.Size(12.dp.toPx(), 2.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(r))
    }
}

@Composable
fun LearnIcon(color: Color) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(22.dp)) {
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 1.8.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round,
        )
        val p1 = androidx.compose.ui.graphics.Path().apply {
            moveTo(11.dp.toPx(), 3.dp.toPx())
            lineTo(3.dp.toPx(), 7.dp.toPx())
            lineTo(11.dp.toPx(), 11.dp.toPx())
            lineTo(19.dp.toPx(), 7.dp.toPx())
            close()
        }
        drawPath(p1, color, style = stroke)
        val p2 = androidx.compose.ui.graphics.Path().apply {
            moveTo(3.dp.toPx(), 11.dp.toPx())
            lineTo(11.dp.toPx(), 15.dp.toPx())
            lineTo(19.dp.toPx(), 11.dp.toPx())
        }
        drawPath(p2, color, style = stroke)
        val p3 = androidx.compose.ui.graphics.Path().apply {
            moveTo(3.dp.toPx(), 15.dp.toPx())
            lineTo(11.dp.toPx(), 19.dp.toPx())
            lineTo(19.dp.toPx(), 15.dp.toPx())
        }
        drawPath(p3, color, style = stroke)
    }
}

@Composable
fun SettingsIcon(color: Color) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(22.dp)) {
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawCircle(color, radius = 3.dp.toPx(), center = androidx.compose.ui.geometry.Offset(11.dp.toPx(), 11.dp.toPx()), style = stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset(11.dp.toPx(), 2.dp.toPx()), androidx.compose.ui.geometry.Offset(11.dp.toPx(), 4.dp.toPx()), stroke.width, androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(11.dp.toPx(), 18.dp.toPx()), androidx.compose.ui.geometry.Offset(11.dp.toPx(), 20.dp.toPx()), stroke.width, androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(2.dp.toPx(), 11.dp.toPx()), androidx.compose.ui.geometry.Offset(4.dp.toPx(), 11.dp.toPx()), stroke.width, androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(18.dp.toPx(), 11.dp.toPx()), androidx.compose.ui.geometry.Offset(20.dp.toPx(), 11.dp.toPx()), stroke.width, androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(4.22.dp.toPx(), 4.22.dp.toPx()), androidx.compose.ui.geometry.Offset(5.64.dp.toPx(), 5.64.dp.toPx()), stroke.width, androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(16.36.dp.toPx(), 16.36.dp.toPx()), androidx.compose.ui.geometry.Offset(17.78.dp.toPx(), 17.78.dp.toPx()), stroke.width, androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(4.22.dp.toPx(), 17.78.dp.toPx()), androidx.compose.ui.geometry.Offset(5.64.dp.toPx(), 16.36.dp.toPx()), stroke.width, androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(16.36.dp.toPx(), 5.64.dp.toPx()), androidx.compose.ui.geometry.Offset(17.78.dp.toPx(), 4.22.dp.toPx()), stroke.width, androidx.compose.ui.graphics.StrokeCap.Round)
    }
}