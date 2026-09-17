package com.example.myownvocabulary.ui.components.states

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myownvocabulary.ui.components.BackButton

@Composable
fun EmptyVocabularyState(onAddClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Brak słówek",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Dodaj pierwsze słówko, żeby zacząć naukę.",
            fontSize = 14.sp,
            color = colors.outline
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onAddClick) {
            Text("Dodaj słówko")
        }
    }
}

@Composable
fun EmptySearchState(query: String) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Nic nie znaleziono",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Brak wyników dla „$query”.",
            fontSize = 14.sp,
            color = colors.outline
        )
    }
}

@Composable
fun WordNotFoundState(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onPress = onBack)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Nie znaleziono słówka",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "To słówko zostało usunięte albo link jest nieprawidłowy.",
                fontSize = 14.sp,
                color = colors.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = onBack) {
                Text("Wróć")
            }
        }
    }
}
