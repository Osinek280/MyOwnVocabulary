package com.example.myownvocabulary.ui.components.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.GregorianCalendar

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LearningCalendarSheet(months: List<LearningCalendarMonth>, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val today = Calendar.getInstance()
    val currentMonth = CalendarMonthId(
        year = today.get(Calendar.YEAR),
        month = today.get(Calendar.MONTH) + 1
    )
    var selectedMonthSerial by rememberSaveable {
        mutableIntStateOf(currentMonth.toSerial())
    }
    val selectedMonth = calendarMonthIdFromSerial(selectedMonthSerial)
    val completedDays = months
        .firstOrNull { it.id == selectedMonth }
        ?.completedDays
        .orEmpty()
    val todayDay = today.get(Calendar.DAY_OF_MONTH).takeIf { selectedMonth == currentMonth }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Kalendarz nauki",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Każdy zaznaczony dzień oznacza wykonany cel.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(
                    onClick = { selectedMonthSerial = currentMonth.toSerial() },
                    enabled = selectedMonth != currentMonth
                ) {
                    Text("Dzisiaj")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedMonthSerial-- }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Poprzedni miesiąc"
                    )
                }
                Text(
                    text = selectedMonth.displayLabel(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { selectedMonthSerial++ }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Następny miesiąc"
                    )
                }
            }

            CalendarMonthGrid(
                month = selectedMonth,
                completedDays = completedDays,
                todayDay = todayDay
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CalendarLegendDot(color = MaterialTheme.colorScheme.tertiary)
                Text(
                    text = "${completedDays.size} dni nauki",
                    modifier = Modifier.padding(start = 7.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CalendarMonthGrid(month: CalendarMonthId, completedDays: Set<Int>, todayDay: Int?) {
    val colors = MaterialTheme.colorScheme
    val labels = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")
    val calendar = GregorianCalendar(month.year, month.month - 1, 1)
    val firstDayOffset = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val cells = List(firstDayOffset) { null } + (1..daysInMonth).map { it }
    val paddedCells = cells + List(42 - cells.size) { null }

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            labels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
        paddedCells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                week.forEach { day ->
                    CalendarDay(
                        day = day,
                        completed = day != null && day in completedDays,
                        isToday = day != null && day == todayDay,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(day: Int?, completed: Boolean, isToday: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                when {
                    completed -> colors.tertiary
                    isToday -> colors.primaryContainer
                    day != null -> colors.surfaceContainerLow
                    else -> Color.Transparent
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (day != null) {
            Text(
                text = "$day",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (completed || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    completed -> colors.onTertiary
                    isToday -> colors.primary
                    else -> colors.onSurfaceVariant
                }
            )
        }
        if (isToday) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (completed) colors.onTertiary else colors.primary)
            )
        }
    }
}

@Composable
private fun CalendarLegendDot(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

private fun CalendarMonthId.toSerial(): Int = year * 12 + month - 1

private fun calendarMonthIdFromSerial(serial: Int): CalendarMonthId {
    val year = Math.floorDiv(serial, 12)
    val month = Math.floorMod(serial, 12) + 1
    return CalendarMonthId(year = year, month = month)
}

private fun CalendarMonthId.displayLabel(): String {
    val monthNames = listOf(
        "Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec",
        "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"
    )
    return "${monthNames[month - 1]} $year"
}
