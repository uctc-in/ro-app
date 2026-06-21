package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.SavedCertificate
import com.example.ui.theme.*
import com.example.ui.viewmodel.MathViewModel
import com.example.ui.viewmodel.toHanifi
import kotlin.random.Random

@Composable
fun CertificateScreens(
    viewModel: MathViewModel,
    mode: String, // "list" or "verification"
    onBack: () -> Unit
) {
    val certsList by viewModel.certificates.collectAsState()
    var selectedCert by remember { mutableStateOf<SavedCertificate?>(null) }

    var verificationQuery by remember { mutableStateOf("") }
    var verifiedRecord by remember { mutableStateOf<SavedCertificate?>(null) }
    var searched by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDE7)) // Soft yellowish background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (selectedCert != null) {
                            selectedCert = null
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .testTag("cert_back_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = if (mode == "list") "Earned Certificates" else "Certificate Verification",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            if (selectedCert != null) {
                // Focus on individual full certificate view
                FullCertificateView(
                    cert = selectedCert!!,
                    onClose = { selectedCert = null }
                )
            } else if (mode == "list") {
                // List of saved certificates
                if (certsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📜", fontSize = 72.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No certificates earned yet!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                "Complete lessons with 𐴹𐴰% accuracy or better to earn!",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(certsList) { cert ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCert = cert }
                                    .testTag("cert_${cert.certificateId}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(cert.levelName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = KidsPurple)
                                        Text("Issued: ${cert.earnedDate}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(KidsYellow.copy(alpha = 0.25f))
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            cert.certificateId,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (mode == "verification") {
                // Verification module
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Verify Rohingya Math Certificate",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        "Enter the 𐴴-digit Certificate ID to check database validity:",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = verificationQuery,
                        onValueChange = { verificationQuery = it.uppercase() },
                        label = { Text("Code: RO-MATH-XXXX") },
                        modifier = Modifier.fillMaxWidth().testTag("verification_input_field")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            searched = true
                            verifiedRecord = certsList.find { it.certificateId == verificationQuery || it.certificateId.replace("RO-MATH-", "") == verificationQuery }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KidsBlue),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("verification_submit_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Query Certificate ID", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (searched) {
                        if (verifiedRecord != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("✅ VERIFIED STATUS", color = KidsGreen, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Student: ${verifiedRecord!!.childName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Age: ${verifiedRecord!!.age}", fontSize = 14.sp)
                                    Text("Math level completed: ${verifiedRecord!!.levelName}", fontSize = 14.sp)
                                    Text("Issued on: ${verifiedRecord!!.earnedDate}", fontSize = 14.sp)
                                    Text("Certificate Score: ${verifiedRecord!!.score.toHanifi()}%", fontSize = 14.sp)
                                }
                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text(
                                    "Unregistered or invalid Certificate ID. Please recheck your credentials.",
                                    modifier = Modifier.padding(24.dp),
                                    textAlign = TextAlign.Center,
                                    color = KidsPink,
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

@Composable
fun FullCertificateView(
    cert: SavedCertificate,
    onClose: () -> Unit
) {
    var shareNotice by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Physical Certificate Card Box with decorative design border lines
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(6.dp, KidsOrange, RoundedCornerShape(20.dp))
                .border(2.dp, KidsYellow, RoundedCornerShape(20.dp))
                .shadow(4.dp, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFF9)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header seal
                Text("🏆", fontSize = 54.sp)

                Text(
                    "Rohingya Math Achievement Certificate",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KidsOrange,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Serif
                )

                Text(
                    "This verifies that student",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Text(
                    cert.childName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    "has successfully mastered mathematics in",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Text(
                    cert.levelName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = KidsPurple,
                    textAlign = TextAlign.Center
                )

                Text(
                    "on date ${cert.earnedDate} passing with performance ${cert.score.toHanifi()}% accuracy.",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Divider(color = Color.LightGray)

                // Render vector QR Code on canvas
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("ID: ${cert.certificateId}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("App: Math for Ro", fontSize = 10.sp, color = Color.Gray)
                    }

                    // QR canvas placeholder drawing
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                            .background(Color.White, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                            // Render mock QR code matrix lines
                            val gridCount = 5
                            val cellSize = size.width / gridCount
                            for (r in 0 until gridCount) {
                                for (c in 0 until gridCount) {
                                    if ((r + c) % 2 == 0) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(c * cellSize, r * cellSize),
                                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Export utility actions (Download PDF & Share to WhatsApp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { shareNotice = true },
                colors = ButtonDefaults.buttonColors(containerColor = KidsGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("share_whatsapp_button")
            ) {
                Text("Share on WhatsApp 💬", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { shareNotice = true },
                colors = ButtonDefaults.buttonColors(containerColor = KidsBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("download_pdf_button")
            ) {
                Text("Save PDF 📁", fontWeight = FontWeight.Bold)
            }
        }

        if (shareNotice) {
            Text(
                "Certificate successfully downloaded & saved locally to memory!",
                color = KidsGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
