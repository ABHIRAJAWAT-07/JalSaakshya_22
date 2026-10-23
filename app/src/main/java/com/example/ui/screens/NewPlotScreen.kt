package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewPlotScreen(
    viewModel: JalSaakshyaViewModel,
    modifier: Modifier = Modifier
) {
    val isHindi = viewModel.language.collectAsState().value == "hi"
    val currLoc by viewModel.currentLocation.collectAsState()

    var plotName by remember { mutableStateOf(if (isHindi) "खेत 1" else "Plot 1") }
    var selectedCrop by remember { mutableStateOf(if (isHindi) "धान (Paddy)" else "Paddy") }
    var areaAcre by remember { mutableDoubleStateOf(1.5) }

    val presetNames = listOf(
        if (isHindi) "खेत 1 (उत्तरी)" else "Plot 1 (North)",
        if (isHindi) "खेत 2 (नहर के पास)" else "Plot 2 (Canal)",
        if (isHindi) "खेत 3 (निचला हिस्सा)" else "Plot 3 (Lowland)",
        if (isHindi) "बड़ा खेत" else "Big Field"
    )

    val cropOptions = listOf(
        "धान (Paddy)",
        "मक्का (Maize)",
        "गन्ना (Sugarcane)",
        "सब्जियां (Vegetables)",
        "बाजरा (Millets)",
        "अन्य (Other)"
    )

    val areaOptions = listOf(0.5, 1.0, 1.5, 2.0, 3.0, 5.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9F6)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Voice Guidance Banner
        item {
            VoicePromptCard(
                textHi = "खेत का नाम चुनें या लिखें, फसल चुनें और नीचे हरा बटन दबाकर खेत सेव करें।",
                textEn = "Select or type plot name, crop type and tap the green button below to save plot.",
                viewModel = viewModel
            )
        }

        // Card 1: Plot Name
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isHindi) "1. खेत का नाम (Name This Plot)" else "1. Plot Name",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = FarmGreenPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset chips for ultra-low literacy
                    Text(
                        text = if (isHindi) "त्वरित सुझाव (छूकर चुनें):" else "Quick suggestions (tap to pick):",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetNames.forEach { name ->
                            val isSelected = plotName == name
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) FarmGreenPrimary else Color(0xFFE8F5E9),
                                modifier = Modifier
                                    .clickable { plotName = name }
                                    .testTag("preset_plot_name_$name")
                            ) {
                                Text(
                                    text = name,
                                    color = if (isSelected) Color.White else FarmGreenPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = plotName,
                        onValueChange = { plotName = it },
                        label = { Text(if (isHindi) "खेत का नाम लिखें" else "Custom Plot Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("plot_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Card 2: Crop Type Selection
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isHindi) "2. बोई गई फसल (Standing Crop)" else "2. Standing Crop",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = FarmGreenPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cropOptions.forEach { crop ->
                            val isSelected = selectedCrop == crop
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF1B4D3E) else Color(0xFFF1F3F5),
                                modifier = Modifier
                                    .clickable { selectedCrop = crop }
                                    .testTag("crop_option_$crop")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Grass,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFFFFD166) else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = crop,
                                        color = if (isSelected) Color.White else Color(0xFF333333),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Card 3: Plot Area (Acres)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isHindi) "3. खेत का रकबा (Area in Acres)" else "3. Field Area (Acres)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = FarmGreenPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        areaOptions.forEach { acre ->
                            val isSelected = areaAcre == acre
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF2D6A4F) else Color(0xFFF1F3F5),
                                modifier = Modifier
                                    .clickable { areaAcre = acre }
                                    .testTag("area_option_$acre")
                            ) {
                                Text(
                                    text = "$acre ${if (isHindi) "एकड़" else "Acre"}",
                                    color = if (isSelected) Color.White else Color(0xFF333333),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Card 4: GPS Reference Coordinate
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(FarmGreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MyLocation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isHindi) "खेत का जीपीएस केंद्र" else "Central Plot GPS",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = FarmGreenPrimary
                                )
                                Text(
                                    text = if (isHindi) "स्वतः दर्ज (Auto-Captured)" else "Auto-Captured Location",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF2D6A4F)
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.fetchLocation() },
                            modifier = Modifier.testTag("refresh_location_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh GPS",
                                tint = FarmGreenPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF2D6A4F)
                            )
                            Text(
                                text = "Lat: ${String.format(Locale.US, "%.5f", currLoc.first)}° N  |  Lng: ${String.format(Locale.US, "%.5f", currLoc.second)}° E",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1B4D3E)
                            )
                        }
                    }
                }
            }
        }

        // Save Button (Giant 56dp tall button)
        item {
            Button(
                onClick = {
                    if (plotName.isNotBlank()) {
                        viewModel.createPlot(
                            name = plotName,
                            cropType = selectedCrop,
                            areaAcre = areaAcre,
                            lat = currLoc.first,
                            lng = currLoc.second
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("save_plot_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFFFFD166),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isHindi) "खेत सुरक्षित करें (Save Plot)" else "Save Plot & GPS",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
