package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MathViewModel
import com.example.ui.viewmodel.toHanifi

@Composable
fun ParentDashboardScreen(
    viewModel: MathViewModel,
    progress: com.example.data.database.UserProgress,
    onBack: () -> Unit
) {
    var isAuthenticated by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var showPinError by remember { mutableStateOf(false) }

    // If not authenticated, prompt children-safe parent lock pin (default "1234")
    if (!isAuthenticated) {
        ParentPinGate(
            enteredPin = enteredPin,
            showPinError = showPinError,
            onPinChanged = {
                enteredPin = it
                showPinError = false
            },
            onConfirm = {
                if (enteredPin == "1234" || enteredPin == "𐴱𐴲𐴳𐴴") {
                    isAuthenticated = true
                } else {
                    showPinError = true
                    enteredPin = ""
                }
            },
            onBack = onBack
        )
    } else {
        ParentControlContent(
            viewModel = viewModel,
            progress = progress,
            onBack = onBack
        )
    }
}

@Composable
fun ParentPinGate(
    enteredPin: String,
    showPinError: Boolean,
    onPinChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDE7F6)), // Warm lilac
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .shadow(6.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Parents Secure Gate", // Parents Area
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = KidsPurple,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Enter parent PIN 𐴱𐴲𐴳𐴴 (1234) to unlock parent options:", // Enter pin 1234 to verify you are a parent!
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Render secure preview in Hanifi
                Text(
                    text = enteredPin.toHanifi().let { if(it.isEmpty()) "𐴰𐴰𐴰𐴰" else it },
                    fontSize = 32.sp,
                    letterSpacing = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KidsPurple,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (showPinError) {
                    Text(
                        "Incorrect PIN! Please try again.", // Error incorrect PIN
                        color = KidsPink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Custom Keypad
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 1..9) {
                        item {
                            KeyButton(symbol = i.toString(), onClick = { if(enteredPin.length < 4) onPinChanged(enteredPin + i) })
                        }
                    }
                    item {
                        KeyButton(symbol = "❌", onClick = { onPinChanged("") })
                    }
                    item {
                        KeyButton(symbol = "0", onClick = { if(enteredPin.length < 4) onPinChanged(enteredPin + "0") })
                    }
                    item {
                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(containerColor = KidsPurple),
                            shape = CircleShape,
                            modifier = Modifier.size(60.dp).testTag("pin_confirm_submit")
                        ) {
                            Text("OK", color = Color.White, fontWeight = FontWeight.Bold) // YES / CONFIRM
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onBack, modifier = Modifier.testTag("pin_gate_back")) {
                    Text("Go Back", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun KeyButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(Color(0xFFF3E5F5), CircleShape)
            .clickable { onClick() }
            .testTag("keypad_btn_$symbol"),
        contentAlignment = Alignment.Center
    ) {
        Text(symbol.toHanifi(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = KidsPurple)
    }
}

@Composable
fun ParentControlContent(
    viewModel: MathViewModel,
    progress: com.example.data.database.UserProgress,
    onBack: () -> Unit
) {
    val gameStats by viewModel.gameHistory.collectAsState()
    
    var editName by remember { mutableStateOf(progress.childName) }
    var editAge by remember { mutableStateOf(progress.age.toString()) }
    var showSavedToast by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .testTag("parent_back_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }

                Text(
                    "Parents Configuration Dashboard", // Rohingya parents cabinet
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                IconButton(
                    onClick = { viewModel.resetProgress() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(KidsPink.copy(alpha = 0.1f), CircleShape)
                        .testTag("factory_reset_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Reset", tint = KidsPink)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Configuration Edit widget
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Update Child Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KidsPurple)
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Student Name") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_child_name_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = editAge,
                                onValueChange = { editAge = it },
                                label = { Text("Student Age") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_child_age_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    val parsedAge = editAge.toIntOrNull() ?: 6
                                    viewModel.modifyProfile(editName, parsedAge)
                                    showSavedToast = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = KidsPurple),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("save_profile_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Edit, contentDescription = "Save")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Apply Profile Updates", fontWeight = FontWeight.Bold)
                                }
                            }

                            if (showSavedToast) {
                                Text("Profile modifications saved successfully!", color = KidsGreen, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }

                // Game Accuracy analytics overview list
                item {
                    Text(
                        "📊 Student Achievements & Stats Summary",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                if (gameStats.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            Text(
                                "No game history logged. Complete sessions to view graphs!",
                                modifier = Modifier.padding(24.dp),
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    items(gameStats) { stat ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(stat.gameType, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Group: ${stat.levelAgeGroup} years", fontSize = 11.sp, color = Color.Gray)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Progress pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (stat.scorePercentage >= 80) KidsGreen.copy(alpha = 0.15f)
                                                else KidsPink.copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "${stat.scorePercentage.toHanifi()}% Accuracy", // Display percent in Hanifi
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (stat.scorePercentage >= 80) KidsGreen else KidsPink
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        "🎯 ${stat.problemsCorrect.toHanifi()}/${stat.problemsAttempted.toHanifi()}",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
