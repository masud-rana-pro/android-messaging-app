package com.contactme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.contactme.app.ui.ContactMeApp
import com.contactme.app.ui.theme.ContactMeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ContactMeTheme {
                ContactMeApp()
            }
        }
    }
}
