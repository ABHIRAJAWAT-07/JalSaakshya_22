package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.PhaseAfterColor
import com.example.ui.theme.PhaseBeforeColor
import com.example.ui.theme.PhaseDuringColor
import com.example.ui.viewmodel.JalSaakshyaViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FloodPhaseBadge(phase: FloodPhase, isHindi: Boolean, modifier: Modifier = Modifier) {
    val (color, icon, text) = when (phase) {
        FloodPhase.BEFORE -> Triple(
            PhaseBeforeColor,
            Icons.Filled.WbSunny,
            if (isHindi) "बाढ़ से पहले (सामान्य)" else "Before Flood"
        )
        FloodPhase.DURING -> Triple(
            PhaseDuringColor,
            Icons.Filled.Water,
            if (isHindi) "बाढ़ के दौरान (जलमग्न)" else "During Flood"
        )
        FloodPhase.AFTER -> Triple(
            PhaseAfterColor,
            Icons.Filled.CloudQueue,
            if (isHindi) "बाढ़ के बाद (कीचड़/क्षति)" else "After Flood"
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun EvidenceCard(
    entry: EvidenceEntry,
    viewModel: JalSaakshyaViewModel,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isHindi = viewModel.language.value == "hi"
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val formattedDate = sdf.format(Date(entry.capturedAt))
    val isPlayingThis = viewModel.playingAudioPath.value == entry.voiceNotePath

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("evidence_card_${entry.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Phase Badge + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloodPhaseBadge(phase = entry.phase, isHindi = isHindi)

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_evidence_button_${entry.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Evidence",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body: Photo + Metadata Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Photo Thumbnail with geo badge
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE9ECEF))
                ) {
                    val file = File(entry.photoPath)
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(file)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Evidence Crop Photo",
                        modifier = Modifier.size(110.dp),
                        contentScale = ContentScale.Crop
                    )

                    // Geo tag watermark pill on photo
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "GPS: ${String.format(Locale.US, "%.4f", entry.latitude)}°",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Details Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Date & Time
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF555555),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF333333),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Coordinates
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF2D6A4F),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.4f", entry.latitude)}° N, ${String.format(Locale.US, "%.4f", entry.longitude)}° E",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2D6A4F),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Loss Estimate Chip
                    if (entry.estimatedLossPercent > 0) {
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isHindi) "अनुमानित क्षति: ${entry.estimatedLossPercent}%" else "Est. Damage: ${entry.estimatedLossPercent}%",
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Notes preview
                    if (entry.cropDamageNotes.isNotBlank()) {
                        Text(
                            text = entry.cropDamageNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF424242),
                            maxLines = 2
                        )
                    }
                }
            }

            // Voice Note Player Row
            if (entry.voiceNotePath != null || entry.voiceDurationSec > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isPlayingThis) Color(0xFFE8F5E9) else Color(0xFFF1F3F5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = "Voice Note",
                                tint = if (isPlayingThis) Color(0xFF2D6A4F) else Color(0xFF666666),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isPlayingThis) {
                                    if (isHindi) "आवाज़ चल रही है..." else "Playing audio..."
                                } else {
                                    if (isHindi) "किसान की आवाज़ (${entry.voiceDurationSec} सेकंड)" else "Farmer Voice (${entry.voiceDurationSec}s)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPlayingThis) Color(0xFF2D6A4F) else Color(0xFF444444)
                            )
                        }

                        // Play/Stop Button
                        IconButton(
                            onClick = {
                                if (entry.voiceNotePath != null) {
                                    viewModel.playAudio(entry.voiceNotePath)
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isPlayingThis) Color(0xFF2D6A4F) else Color(0xFF4A90E2))
                                .testTag("play_audio_button_${entry.id}")
                        ) {
                            Icon(
                                imageVector = if (isPlayingThis) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = "Play/Stop",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
