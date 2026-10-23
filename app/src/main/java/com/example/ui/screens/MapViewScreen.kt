package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.EvidenceEntry
import com.example.data.model.FloodPhase
import com.example.data.model.Plot
import com.example.ui.components.FloodPhaseBadge
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FloodBlue
import com.example.ui.theme.PhaseAfterColor
import com.example.ui.theme.PhaseBeforeColor
import com.example.ui.theme.PhaseDuringColor
import com.example.ui.viewmodel.JalSaakshyaViewModel
import com.example.ui.viewmodel.Screen
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapViewScreen(
    viewModel: JalSaakshyaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isHindi = viewModel.language.collectAsState().value == "hi"
    val plots by viewModel.plots.collectAsState()
    val allEvidence by viewModel.allEvidence.collectAsState()
    val currLoc by viewModel.currentLocation.collectAsState()

    var selectedPlotForSheet by remember { mutableStateOf<Plot?>(null) }
    var filterFloodOnly by remember { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE9F5ED))
    ) {
        // Custom Interactive Vector Canvas Map
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(plots) {
                    detectTapGestures { tapOffset ->
                        // Detect tap within radius of any plot marker
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()

                        plots.forEachIndexed { index, plot ->
                            val markerX = (w * 0.25f) + (index % 3) * (w * 0.28f)
                            val markerY = (h * 0.22f) + (index / 3) * (h * 0.25f)

                            val distance = (tapOffset.x - markerX) * (tapOffset.x - markerX) +
                                    (tapOffset.y - markerY) * (tapOffset.y - markerY)
                            if (distance < 50f * 50f) {
                                selectedPlotForSheet = plot
                            }
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw Field parcels grid lines (light agricultural terrain grid)
            val gridColor = Color(0xFFCCE8D7)
            for (x in 0..width.toInt() step 90) {
                drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), height), 1.2f)
            }
            for (y in 0..height.toInt() step 90) {
                drawLine(gridColor, Offset(0f, y.toFloat()), Offset(width, y.toFloat()), 1.2f)
            }

            // 2. Draw Meandering River / Canal path (Rapti river basin flood zone)
            val riverPath = Path().apply {
                moveTo(0f, height * 0.15f)
                cubicTo(
                    width * 0.35f, height * 0.05f,
                    width * 0.65f, height * 0.35f,
                    width, height * 0.25f
                )
            }
            drawPath(
                path = riverPath,
                color = Color(0xFF64B5F6).copy(alpha = 0.5f),
                style = Stroke(width = 44f)
            )
            drawPath(
                path = riverPath,
                color = Color(0xFF1976D2).copy(alpha = 0.7f),
                style = Stroke(width = 16f)
            )

            // 3. Draw Flood inundated risk zone
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF0288D1).copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(width * 0.65f, height * 0.32f),
                    radius = width * 0.45f
                ),
                center = Offset(width * 0.65f, height * 0.32f),
                radius = width * 0.45f
            )

            // 4. Draw Farmer Live Location Radar (blue pulsing dot)
            val userX = width * 0.5f
            val userY = height * 0.72f

            drawCircle(
                color = Color(0xFF1976D2).copy(alpha = 0.2f),
                radius = 36f,
                center = Offset(userX, userY)
            )
            drawCircle(
                color = Color(0xFF1976D2),
                radius = 12f,
                center = Offset(userX, userY)
            )
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = Offset(userX, userY)
            )

            // 5. Draw Plot Markers with flood phase color dots
            plots.forEachIndexed { index, plot ->
                val markerX = (width * 0.25f) + (index % 3) * (width * 0.28f)
                val markerY = (height * 0.22f) + (index / 3) * (height * 0.25f)

                val plotEvidences = allEvidence.filter { it.plotId == plot.id }
                val hasFlood = plotEvidences.any { it.phase == FloodPhase.DURING || it.phase == FloodPhase.AFTER }

                if (!filterFloodOnly || hasFlood) {
                    val pinColor = if (hasFlood) PhaseDuringColor else FarmGreenPrimary

                    // Pin shadow
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.25f),
                        radius = 22f,
                        center = Offset(markerX, markerY + 4f)
                    )
                    // Pin outer
                    drawCircle(
                        color = pinColor,
                        radius = 20f,
                        center = Offset(markerX, markerY)
                    )
                    // Pin inner
                    drawCircle(
                        color = Color.White,
                        radius = 10f,
                        center = Offset(markerX, markerY)
                    )
                    // Evidence phase mini badge dot
                    if (plotEvidences.isNotEmpty()) {
                        val latestPhase = plotEvidences.maxByOrNull { it.capturedAt }?.phase ?: FloodPhase.BEFORE
                        val dotColor = when (latestPhase) {
                            FloodPhase.BEFORE -> PhaseBeforeColor
                            FloodPhase.DURING -> PhaseDuringColor
                            FloodPhase.AFTER -> PhaseAfterColor
                        }
                        drawCircle(
                            color = dotColor,
                            radius = 6f,
                            center = Offset(markerX + 16f, markerY - 16f)
                        )
                    }
                }
            }
        }

        // Top Floating HUD Card (Legend & GPS Info)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isHindi) "आपकी जीपीएस स्थिति (GPS Live)" else "Live GPS Location",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1B4D3E)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (filterFloodOnly) PhaseDuringColor else Color(0xFFE8F5E9),
                        modifier = Modifier
                            .clickable { filterFloodOnly = !filterFloodOnly }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = if (filterFloodOnly) (if (isHindi) "केवल बाढ़ प्रभावित" else "Flood Only") else (if (isHindi) "सभी खेत" else "All Plots"),
                            color = if (filterFloodOnly) Color.White else FarmGreenPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Legend row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LegendItem(color = PhaseBeforeColor, label = if (isHindi) "सामान्य" else "Normal")
                    LegendItem(color = PhaseDuringColor, label = if (isHindi) "जलमग्न" else "Submerged")
                    LegendItem(color = PhaseAfterColor, label = if (isHindi) "क्षतिग्रस्त" else "Damaged")
                    LegendItem(color = Color(0xFF1976D2), label = if (isHindi) "आप (GPS)" else "You")
                }
            }
        }

        // Bottom Tap Prompt Pill
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1B4D3E),
            tonalElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFFFD166),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isHindi) "खेत का विवरण देखने के लिए पिन पर टैप करें" else "Tap any pin on map for plot details",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }

        // Bottom Sheet for selected plot details
        if (selectedPlotForSheet != null) {
            val plot = selectedPlotForSheet!!
            val plotEvidences = allEvidence.filter { it.plotId == plot.id }

            ModalBottomSheet(
                onDismissRequest = { selectedPlotForSheet = null },
                sheetState = bottomSheetState,
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                                color = Color(0xFF555555)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "${plotEvidences.size} ${if (isHindi) "सबूत दर्ज" else "Evidences"}",
                                color = FarmGreenPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // GPS Coordinates
                    Surface(
                        color = Color(0xFFF1F3F5),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "GPS: ${String.format(Locale.US, "%.5f", plot.latitude)}° N, ${String.format(Locale.US, "%.5f", plot.longitude)}° E",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF2D6A4F),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    // Evidence thumbnails row
                    if (plotEvidences.isNotEmpty()) {
                        Text(
                            text = if (isHindi) "हाल के फोटो साक्ष्य:" else "Recent Photos:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF333333)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(plotEvidences) { ev ->
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFE9ECEF))
                                ) {
                                    val file = File(ev.photoPath)
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(file)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                    ) {
                                        Text(
                                            text = ev.phase.name,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Action Buttons: Add Evidence + View Dossier
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                selectedPlotForSheet = null
                                viewModel.navigateTo(Screen.ADD_EVIDENCE, plotId = plot.id)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("map_sheet_add_evidence_button")
                        ) {
                            Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "+ सबूत जोड़ें" else "+ Evidence",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                selectedPlotForSheet = null
                                viewModel.navigateTo(Screen.DOSSIER_VIEW, plotId = plot.id)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("map_sheet_dossier_button")
                        ) {
                            Icon(imageVector = Icons.Filled.Description, contentDescription = null, tint = FloodBlue)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "डॉसियर / PDF" else "Dossier",
                                color = FloodBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF444444),
            fontWeight = FontWeight.Medium
        )
    }
}
