package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import com.example.data.database.MathDatabase
import com.example.data.repository.MathRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MathViewModel

class MainActivity : ComponentActivity() {
    private lateinit var database: MathDatabase
    private lateinit var repository: MathRepository
    private lateinit var viewModel: MathViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup local database persistence
        database = Room.databaseBuilder(
            applicationContext,
            MathDatabase::class.java,
            "math_for_ro_database"
        ).build()

        repository = MathRepository(database.mathDao())

        // Build ViewModel using custom Provider Factory
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MathViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return MathViewModel(application, repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        viewModel = ViewModelProvider(this, factory)[MathViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Observe active user states
                    val screenState by viewModel.currentScreen.collectAsStateWithLifecycle()
                    val progress by viewModel.userProgress.collectAsStateWithLifecycle()

                    // Simple high-fidelity children-focused screen switching
                    when (screenState) {
                        "home" -> {
                            KidsHomeScreen(
                                viewModel = viewModel,
                                progress = progress,
                                onNavigateToParent = { viewModel.navigateTo("parent") }
                            )
                        }
                        "game" -> {
                            GamePlayScreen(
                                viewModel = viewModel,
                                onBackToHome = { viewModel.navigateTo("home") }
                            )
                        }
                        "game_over" -> {
                            GameOverScreen(
                                viewModel = viewModel,
                                onBackToHome = { viewModel.navigateTo("home") }
                            )
                        }
                        "parent" -> {
                            ParentDashboardScreen(
                                viewModel = viewModel,
                                progress = progress,
                                onBack = { viewModel.navigateTo("home") }
                            )
                        }
                        "certificates" -> {
                            CertificateScreens(
                                viewModel = viewModel,
                                mode = "list",
                                onBack = { viewModel.navigateTo("home") }
                            )
                        }
                        "verification" -> {
                            CertificateScreens(
                                viewModel = viewModel,
                                mode = "verification",
                                onBack = { viewModel.navigateTo("home") }
                            )
                        }
                        else -> {
                            KidsHomeScreen(
                                viewModel = viewModel,
                                progress = progress,
                                onNavigateToParent = { viewModel.navigateTo("parent") }
                            )
                        }
                    }
                }
            }
        }
    }
}
