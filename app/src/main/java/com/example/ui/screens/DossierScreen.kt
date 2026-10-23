package com.example.ui.screens

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.FloodPhaseBadge
import com.example.ui.components.VoicePromptCard
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FloodBlue
import com.example.ui.viewmodel.JalSaakshyaViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DossierScreen(
    viewModel: JalSaakshyaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val isHindi = viewModel.language.collectAsState().value == "hi"
    val plots by viewModel.plots.collectAsState()
    val allEvidence by viewModel.allEvidence.collectAsState()
    val farmer by viewModel.farmerProfile.collectAsState()
    val selectedPlotId by viewModel.selectedPlotId.collectAsState()

    val isGenerating by viewModel.isGeneratingPdf.collectAsState()
    val generatedPdf by viewModel.generatedPdf.collectAsState()
    val qrBitmap by viewModel.qrBitmap.collectAsState()

    val currentPlot = plots.find { it.id == selectedPlotId } ?: plots.firstOrNull()
    val plotEvidence = allEvidence.filter { it.plotId == currentPlot?.id }

    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

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
                textHi = "यह फसल क्षति का आधिकारिक साक्ष्य दस्तावेज़ है। नीचे हरा बटन दबाकर क्यूआर कोड और फोटो सहित पीडीएफ बनाएं या व्हाट्सएप पर भेजें।",
                textEn = "This is official crop damage dossier. Tap the green button below to generate PDF with QR code and share to officials.",
                viewModel = viewModel
            )
        }

        // Section 1: Plot Selector
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isHindi) "खेत चुनें जिसका डॉसियर बनाना है:" else "Select plot for dossier:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = FarmGreenPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        plots.forEach { plot ->
                            val isSelected = plot.id == currentPlot?.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) FarmGreenPrimary else Color(0xFFF1F3F5),
                                modifier = Modifier
                                    .clickable {
                                        viewModel.navigateTo(viewModel.currentScreen.value, plotId = plot.id)
                                    }
                                    .testTag("dossier_select_plot_${plot.id}")
                            ) {
                                Text(
                                    text = plot.name,
                                    color = if (isSelected) Color.White else Color(0xFF333333),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (currentPlot == null) {
            item {
                Text(if (isHindi) "कोई खेत उपलब्ध नहीं है।" else "No plot available.")
            }
        } else {
            // Section 2: Big Generate / Share PDF Action Button
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = null,
                                tint = FarmGreenPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHindi) "आधिकारिक क्षति साक्ष्य प्रतिवेदन (PDF)" else "Official Crop Damage Dossier",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = FarmGreenPrimary
                                )
                                Text(
                                    text = if (isHindi) "लेखपाल, बीमा कंपनी (PMFBY) एवं बैंक हेतु मान्य" else "Valid for Patwari/Lekhpal, PMFBY & Bank Relief",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2D6A4F)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Button to generate or share
                        Button(
                            onClick = {
                                viewModel.generateDossierForPlot(currentPlot.id, app)
                            },
                            enabled = !isGenerating,
                            colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("generate_pdf_button")
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isHindi) "पीडीएफ बन रहा है..." else "Generating PDF...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Description,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD166),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isHindi) "सरकारी पीडीएफ रिपोर्ट बनाएं" else "Generate Official PDF",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                        }

                        if (generatedPdf != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.shareCurrentPdf(app) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp / Share green
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("share_pdf_button")
                            ) {
                                Icon(imageVector = Icons.Filled.Share, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isHindi) "पीडीएफ शेयर करें (व्हाट्सएप / ईमेल)" else "Share PDF via WhatsApp / Email",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Official Dossier Preview Document Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Official Header Banner inside preview
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FarmGreenPrimary,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "JAL-SAAKSHYA (जलसाक्ष्य) • DAMAGE DOSSIER",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Objective Time-Stamped & Geo-Tagged Loss Record",
                                    color = Color(0xFFD8F3DC),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Farmer & Plot info with QR Code side by side
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHindi) "किसान: ${farmer?.name ?: "पंजीकृत कृषक"}" else "Farmer: ${farmer?.name ?: "Registered Farmer"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1B4D3E)
                                )
                                Text(
                                    text = if (isHindi) "गाँव/तहसील: ${farmer?.village ?: "बाढ़ क्षेत्र"}" else "Village: ${farmer?.village ?: "Flood Zone"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF555555)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isHindi) "खेत: ${currentPlot.name} (${currentPlot.cropType})" else "Plot: ${currentPlot.name} (${currentPlot.cropType})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    text = if (isHindi) "रकबा: ${currentPlot.areaAcre} एकड़" else "Area: ${currentPlot.areaAcre} Acre",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF555555)
                                )
                                Text(
                                    text = "GPS: ${String.format(Locale.US, "%.4f", currentPlot.latitude)}°, ${String.format(Locale.US, "%.4f", currentPlot.longitude)}°",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF2D6A4F),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // QR Code
                            if (qrBitmap != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(
                                        bitmap = qrBitmap!!.asImageBitmap(),
                                        contentDescription = "Verification QR Code",
                                        modifier = Modifier
                                            .size(84.dp)
                                            .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(4.dp))
                                            .testTag("dossier_qr_code_image")
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isHindi) "सत्यापन QR" else "Scan to Verify",
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Timeline entries header
                        Text(
                            text = if (isHindi) "प्रमाणित साक्ष्य प्रविष्टियां (${plotEvidence.size}):" else "Certified Evidence Entries (${plotEvidence.size}):",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = FarmGreenPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (plotEvidence.isEmpty()) {
                            Text(
                                text = if (isHindi) "इस खेत के लिए अभी कोई सबूत दर्ज नहीं है।" else "No evidence entries for this plot yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        } else {
                            plotEvidence.forEach { ev ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAF9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val photoFile = File(ev.photoPath)
                                        AsyncImage(
                                            model = photoFile,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(6.dp)),
                                            contentScale = ContentScale.Crop
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                FloodPhaseBadge(phase = ev.phase, isHindi = isHindi)
                                                if (ev.estimatedLossPercent > 0) {
                                                    Text(
                                                        text = "${ev.estimatedLossPercent}% क्षति",
                                                        color = Color(0xFFD90429),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                            Text(
                                                text = sdf.format(Date(ev.capturedAt)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF555555)
                                            )
                                            Text(
                                                text = "GPS: ${String.format(Locale.US, "%.4f", ev.latitude)}° N, ${String.format(Locale.US, "%.4f", ev.longitude)}° E",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF2D6A4F)
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
    }
}
