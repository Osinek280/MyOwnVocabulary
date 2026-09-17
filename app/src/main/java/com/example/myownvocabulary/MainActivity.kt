package com.example.myownvocabulary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myownvocabulary.ui.navigation.VocabularyNavHost
import com.example.myownvocabulary.ui.theme.MyOwnVocabularyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyOwnVocabularyTheme {
                VocabularyNavHost()
            }
        }
    }
}
