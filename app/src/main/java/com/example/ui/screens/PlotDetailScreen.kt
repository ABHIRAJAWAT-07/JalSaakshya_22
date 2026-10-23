package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import java.util.Locale

@Composable
fun PlotDetailScreen(
    viewModel: JalSaakshyaViewModel,
    modifier: Modifier = Modifier
) {
    val isHindi = viewModel.language.collectAsState().value == "hi"
    val plots by viewModel.plots.collectAsState()
    val allEvidence by viewModel.allEvidence.collectAsState()
    val selectedPlotId by viewModel.selectedPlotId.collectAsState()

    val plot = plots.find { it.id == selectedPlotId } ?: plots.firstOrNull()
    val plotEvidence = allEvidence.filter { it.plotId == plot?.id }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9F6)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (plot == null) {
            item {
                Text(if (isHindi) "खेत नहीं मिला।" else "Plot not found.")
            }
        } else {
            item {
                VoicePromptCard(
                    textHi = "यह ${plot.name} का विवरण है। नीचे इस खेत के सभी दर्ज सबूत हैं। नया सबूत जोड़ने के लिए हरा बटन दबाएं।",
                    textEn = "This is ${plot.name}. Below are all recorded flood evidences. Tap green button to add new evidence.",
                    viewModel = viewModel
                )
            }

            // Plot Details Header Card
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
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Grass,
                                    contentDescription = null,
                                    tint = FarmGreenPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = plot.name,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = Color(0xFF1B4D3E)
                                )
                                Text(
                                    text = "${plot.cropType} • ${plot.areaAcre} ${if (isHindi) "एकड़" else "Acre"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF444444)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = Color(0xFFF1F3F5),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF2D6A4F),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "GPS: ${String.format(Locale.US, "%.5f", plot.latitude)}° N, ${String.format(Locale.US, "%.5f", plot.longitude)}° E",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF2D6A4F),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Add Evidence + Generate Dossier
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.navigateTo(Screen.ADD_EVIDENCE, plotId = plot.id) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("plot_detail_add_evidence_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHindi) "+ सबूत जोड़ें" else "+ Evidence",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.navigateTo(Screen.DOSSIER_VIEW, plotId = plot.id) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("plot_detail_dossier_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Description,
                                    contentDescription = null,
                                    tint = FloodBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHindi) "डॉसियर / PDF" else "Dossier",
                                    color = FloodBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Evidence Timeline Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "दर्ज साक्ष्य कालक्रम (${plotEvidence.size})" else "Evidence Timeline (${plotEvidence.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = Color(0xFF1B4D3E)
                    )
                }
            }

            if (plotEvidence.isEmpty()) {
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
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (isHindi) "इस खेत के लिए अभी कोई सबूत नहीं है।" else "No evidence recorded for this plot yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Button(
                                onClick = { viewModel.navigateTo(Screen.ADD_EVIDENCE, plotId = plot.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary)
                            ) {
                                Text(if (isHindi) "+ पहला सबूत फोटो खींचें" else "+ Capture First Evidence")
                            }
                        }
                    }
                }
            } else {
                items(plotEvidence) { entry ->
                    EvidenceCard(
                        entry = entry,
                        viewModel = viewModel,
                        onDelete = { viewModel.deleteEvidence(entry.id) }
                    )
                }
            }
        }
    }
}
