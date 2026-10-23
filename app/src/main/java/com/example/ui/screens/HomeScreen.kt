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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EvidenceCard
import com.example.ui.components.VoicePromptCard
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FloodBlue
import com.example.ui.viewmodel.JalSaakshyaViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun HomeScreen(
    viewModel: JalSaakshyaViewModel,
    modifier: Modifier = Modifier
) {
    val isHindi = viewModel.language.collectAsState().value == "hi"
    val plots by viewModel.plots.collectAsState()
    val allEvidence by viewModel.allEvidence.collectAsState()
    val farmer by viewModel.farmerProfile.collectAsState()
    val allDossiers by viewModel.allDossiers.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9F6)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Voice Prompt Banner for Ultra-Low Literacy
        item {
            VoicePromptCard(
                textHi = "नमस्ते! नीचे 4 बड़े बटन हैं। नया खेत जोड़ें, फोटो-आवाज़ रिकॉर्ड करें, नक्शा देखें या नुकसान का सरकारी दस्तावेज़ बनाएं।",
                textEn = "Welcome! Tap big buttons below to add a plot, record flood evidence, view map, or generate official damage dossier.",
                viewModel = viewModel
            )
        }

        // Farmer Status Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_farmer_banner")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1B4D3E), Color(0xFF0F382A))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2D6A4F)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Agriculture,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD166),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = farmer?.name?.ifBlank { if (isHindi) "किसान साथी" else "Farmer Partner" }
                                        ?: (if (isHindi) "किसान साथी" else "Farmer Partner"),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = farmer?.village?.ifBlank { if (isHindi) "बाढ़ प्रभावित क्षेत्र" else "Flood Affected Zone" }
                                        ?: (if (isHindi) "बाढ़ प्रभावित क्षेत्र" else "Flood Affected Zone"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFD8F3DC)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats counters row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatCounter(
                                count = plots.size.toString(),
                                label = if (isHindi) "कुल खेत" else "Total Plots",
                                icon = Icons.Filled.Grass
                            )
                            StatCounter(
                                count = allEvidence.size.toString(),
                                label = if (isHindi) "दर्ज सबूत" else "Evidences",
                                icon = Icons.Filled.Water
                            )
                            StatCounter(
                                count = allDossiers.size.toString(),
                                label = if (isHindi) "दस्तावेज़" else "Dossiers",
                                icon = Icons.Filled.Description
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Primary Actions
        item {
            Text(
                text = if (isHindi) "मुख्य कार्य (टैप करें)" else "Primary Actions (Tap)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = Color(0xFF1B4D3E),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // 2x2 Big Buttons Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Button 1: New Plot
                    BigActionCard(
                        title = if (isHindi) "+ नया खेत जोड़ें" else "+ New Plot",
                        subtext = if (isHindi) "खेत का जीपीएस और नाम" else "Plot GPS & Name",
                        icon = Icons.Filled.Add,
                        bgColor = Color(0xFF1B4D3E),
                        iconBg = Color(0xFF2D6A4F),
                        textColor = Color.White,
                        testTag = "home_new_plot_button",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.NEW_PLOT) }
                    )

                    // Button 2: My Plots
                    BigActionCard(
                        title = if (isHindi) "मेरे खेत" else "My Plots",
                        subtext = if (isHindi) "${plots.size} खेत पंजीकृत" else "${plots.size} Registered",
                        icon = Icons.Filled.Grass,
                        bgColor = Color(0xFFE8F5E9),
                        iconBg = Color(0xFFC8E6C9),
                        textColor = Color(0xFF1B4D3E),
                        testTag = "home_my_plots_button",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.MY_PLOTS) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Button 3: Map View
                    BigActionCard(
                        title = if (isHindi) "नक्शा देखें" else "Map View",
                        subtext = if (isHindi) "खेत और बाढ़ बिंदु" else "Plot & Flood Pins",
                        icon = Icons.Filled.Map,
                        bgColor = Color(0xFFE0F2FE),
                        iconBg = Color(0xFFBAE6FD),
                        textColor = Color(0xFF0369A1),
                        testTag = "home_map_view_button",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.MAP_VIEW) }
                    )

                    // Button 4: Generate Dossier
                    BigActionCard(
                        title = if (isHindi) "सबूत डॉसियर" else "Get Dossier",
                        subtext = if (isHindi) "पीडीएफ + क्यूआर कोड" else "PDF & QR Code",
                        icon = Icons.Filled.Description,
                        bgColor = Color(0xFFFEF3C7),
                        iconBg = Color(0xFFFDE68A),
                        textColor = Color(0xFF92400E),
                        testTag = "home_generate_dossier_button",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.DOSSIER_VIEW) }
                    )
                }
            }
        }

        // Recent Evidences Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHindi) "हाल ही में दर्ज फसल साक्ष्य (${allEvidence.size})" else "Recent Crop Evidences (${allEvidence.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = Color(0xFF1B4D3E)
                )
                if (allEvidence.isNotEmpty()) {
                    Text(
                        text = if (isHindi) "सभी देखें" else "View All",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = FloodBlue,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(Screen.MY_PLOTS) }
                            .padding(4.dp)
                    )
                }
            }
        }

        if (allEvidence.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Agriculture,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = if (isHindi) "अभी तक कोई सबूत दर्ज नहीं है।" else "No damage evidence recorded yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FarmGreenPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.navigateTo(Screen.NEW_PLOT) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = if (isHindi) "+ पहला खेत जोड़ें" else "+ Add First Plot",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            items(allEvidence.take(4)) { entry ->
                EvidenceCard(
                    entry = entry,
                    viewModel = viewModel,
                    onDelete = { viewModel.deleteEvidence(entry.id) }
                )
            }
        }
    }
}

@Composable
fun StatCounter(
    count: String,
    label: String,
    icon: ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFFD166),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = count,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Text(
            text = label,
            color = Color(0xFFD8F3DC),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun BigActionCard(
    title: String,
    subtext: String,
    icon: ImageVector,
    bgColor: Color,
    iconBg: Color,
    textColor: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .height(125.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = textColor
                )
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp
                    ),
                    color = textColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}
