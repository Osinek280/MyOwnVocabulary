package com.example.myownvocabulary.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myownvocabulary.ui.components.LearnIcon
import com.example.myownvocabulary.ui.components.ListIcon
import com.example.myownvocabulary.ui.components.SettingsIcon
import com.example.myownvocabulary.ui.theme.Gray100
import com.example.myownvocabulary.ui.theme.Gray400
import com.example.myownvocabulary.ui.theme.Primary

@Composable
fun BottomNav(currentRoute: String,  onNavigate: (String) -> Unit,) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(width = 1.dp, brush = SolidColor(Gray100), shape = RoundedCornerShape(0.dp))
    ) {
        NavItem(
            label = "Lista",
            selected = currentRoute == Routes.Words,
            onClick = { onNavigate(Routes.Words) },
        ) { color -> ListIcon(color) }
        NavItem(
            label = "Nauka",
            selected = currentRoute == Routes.Learn,
            onClick = { onNavigate(Routes.Learn) },
        ) { color -> LearnIcon(color) }
        NavItem(
            label = "Opcje",
            selected = currentRoute == Routes.Settings,
            onClick = { onNavigate(Routes.Settings) },
        ) { color -> SettingsIcon(color) }
    }
}

@Composable
private fun RowScope.NavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
) {
    val color = if (selected) Primary else Gray400
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        icon(color)
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}