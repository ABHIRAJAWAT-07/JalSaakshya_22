package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VoicePromptCard
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.viewmodel.JalSaakshyaViewModel

@Composable
fun ProfileScreen(
    viewModel: JalSaakshyaViewModel,
    modifier: Modifier = Modifier
) {
    val isHindi = viewModel.language.collectAsState().value == "hi"
    val farmer by viewModel.farmerProfile.collectAsState()
    val voiceEnabled by viewModel.voiceGuidanceEnabled.collectAsState()

    var name by remember(farmer) { mutableStateOf(farmer?.name ?: "रामेश्वर यादव") }
    var village by remember(farmer) { mutableStateOf(farmer?.village ?: "सुजानपुर") }
    var district by remember(farmer) { mutableStateOf(farmer?.district ?: "गोरखपुर (उ.प्र.)") }
    var phone by remember(farmer) { mutableStateOf(farmer?.phone ?: "9876543210") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9F6)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Voice Prompt Banner
        item {
            VoicePromptCard(
                textHi = "यह किसान प्रोफ़ाइल और ऐप सेटिंग है। अपनी जानकारी दर्ज करें और भाषा चुनें।",
                textEn = "This is your farmer profile and settings. Update your details and audio preferences.",
                viewModel = viewModel
            )
        }

        // Profile Form Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(FarmGreenPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isHindi) "किसान विवरण (Farmer Details)" else "Farmer Profile",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1B4D3E)
                            )
                            Text(
                                text = if (isHindi) "साक्ष्य प्रतिवेदन में यही नाम दर्ज होगा" else "Will appear on official damage dossier",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(if (isHindi) "किसान का नाम (Farmer Name)" else "Full Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = village,
                        onValueChange = { village = it },
                        label = { Text(if (isHindi) "गाँव / ग्राम पंचायत (Village/Panchayat)" else "Village") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_village_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text(if (isHindi) "तहसील व जिला (District & State)" else "District / Tehsil") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_district_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(if (isHindi) "मोबाइल नंबर (Mobile Number)" else "Phone Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_phone_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.updateProfile(name, village, district, phone)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_profile_button")
                    ) {
                        Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFFFFD166))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "प्रोफ़ाइल सुरक्षित करें" else "Save Details",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Settings & Accessibility Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isHindi) "ऐप सेटिंग्स एवं सुगमता (Preferences)" else "App Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1B4D3E)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Language Selection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Translate, contentDescription = null, tint = FarmGreenPrimary)
                            Text(
                                text = if (isHindi) "भाषा (Language)" else "Language",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isHindi) FarmGreenPrimary else Color(0xFFF1F3F5),
                                modifier = Modifier
                                    .clickable { if (!isHindi) viewModel.toggleLanguage() }
                                    .padding(2.dp)
                            ) {
                                Text(
                                    text = "हिन्दी",
                                    color = if (isHindi) Color.White else Color(0xFF333333),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!isHindi) FarmGreenPrimary else Color(0xFFF1F3F5),
                                modifier = Modifier
                                    .clickable { if (isHindi) viewModel.toggleLanguage() }
                                    .padding(2.dp)
                            ) {
                                Text(
                                    text = "English",
                                    color = if (!isHindi) Color.White else Color(0xFF333333),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Voice Guidance Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.VolumeUp, contentDescription = null, tint = FarmGreenPrimary)
                            Column {
                                Text(
                                    text = if (isHindi) "बोलकर निर्देश देना (Voice Guide)" else "Audio Instructions",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isHindi) "स्क्रीन पर लिखा बोलकर सुनाएगा" else "Reads screen prompts aloud",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Switch(
                            checked = voiceEnabled,
                            onCheckedChange = { viewModel.toggleVoiceGuidance() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FarmGreenPrimary)
                        )
                    }
                }
            }
        }

        // JalSaakshya Info Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = Color(0xFF0369A1),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = if (isHindi) "जलसाक्ष्य किसान सुरक्षा गारंटी" else "About JalSaakshya",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isHindi)
                                "यह ऐप पूरी तरह से बिना इंटरनेट (Offline) काम करता है। आपका फोटो, जीपीएस लोकेशन, समय और आवाज फोन में सुरक्षित रहता है। इसका पीडीएफ दस्तावेज लेखपाल, पटवारी और प्रधानमंत्री फसल बीमा योजना (PMFBY) क्लेम में कानूनी साक्ष्य के रूप में प्रस्तुत किया जा सकता है।"
                            else
                                "JalSaakshya works 100% offline. Time-stamped and geo-tagged damage evidence generated with QR codes empowers farmers against arbitrary claim rejections.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF075985)
                        )
                    }
                }
            }
        }
    }
}
