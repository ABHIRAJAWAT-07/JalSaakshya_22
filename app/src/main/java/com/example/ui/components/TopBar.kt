package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FloodBlue
import com.example.ui.theme.OnFarmGreen
import com.example.ui.viewmodel.JalSaakshyaViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JalSaakshyaTopBar(
    viewModel: JalSaakshyaViewModel,
    title: String,
    currentScreen: Screen,
    onBack: (() -> Unit)? = null
) {
    val isHindi = viewModel.language.value == "hi"
    val voiceEnabled = viewModel.voiceGuidanceEnabled.value

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = FarmGreenPrimary,
            titleContentColor = OnFarmGreen,
            navigationIconContentColor = OnFarmGreen,
            actionIconContentColor = OnFarmGreen
        ),
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = OnFarmGreen,
                    maxLines = 1
                )
                Text(
                    text = if (isHindi) "जलसाक्ष्य • बाढ़ फसल क्षति साक्ष्य" else "JalSaakshya • Flood Evidence",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFD8F3DC)
                )
            }
        },
        navigationIcon = {
            if (onBack != null && currentScreen != Screen.HOME) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = OnFarmGreen
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 6.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2D6A4F)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        },
        actions = {
            // Speak guidance button
            IconButton(
                onClick = { viewModel.toggleVoiceGuidance() },
                modifier = Modifier.testTag("toggle_voice_button")
            ) {
                Icon(
                    imageVector = if (voiceEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                    contentDescription = "Toggle Voice Guidance",
                    tint = if (voiceEnabled) Color(0xFFFFD166) else Color.White.copy(alpha = 0.6f)
                )
            }

            // Language Toggle
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { viewModel.toggleLanguage() }
                    .testTag("language_toggle_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Translate,
                        contentDescription = "Language",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isHindi) "हिन्दी" else "EN",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Profile Button
            IconButton(
                onClick = { viewModel.navigateTo(Screen.PROFILE) },
                modifier = Modifier.testTag("profile_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Farmer Profile",
                    tint = OnFarmGreen
                )
            }
        }
    )
}

@Composable
fun VoicePromptCard(
    textHi: String,
    textEn: String,
    viewModel: JalSaakshyaViewModel,
    modifier: Modifier = Modifier
) {
    val isHindi = viewModel.language.value == "hi"
    val displayText = if (isHindi) textHi else textEn

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { viewModel.speak(textHi, textEn) }
            .testTag("voice_prompt_card"),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE8F5E9),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(FarmGreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.VolumeUp,
                    contentDescription = "Listen instruction",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isHindi) "आवाज़ मार्गदर्शन (सुनने के लिए टैप करें):" else "Audio Guidance (Tap to hear):",
                    style = MaterialTheme.typography.labelSmall,
                    color = FarmGreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = Color(0xFF1B4332)
                )
            }
        }
    }
}
