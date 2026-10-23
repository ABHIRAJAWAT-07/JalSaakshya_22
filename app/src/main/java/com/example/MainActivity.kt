package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.JalSaakshyaTopBar
import com.example.ui.screens.AddEvidenceScreen
import com.example.ui.screens.DossierScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MapViewScreen
import com.example.ui.screens.MyPlotsScreen
import com.example.ui.screens.NewPlotScreen
import com.example.ui.screens.PlotDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.JalSaakshyaViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: JalSaakshyaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                JalSaakshyaApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun JalSaakshyaApp(viewModel: JalSaakshyaViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isHindi = viewModel.language.collectAsState().value == "hi"

    // Permission handling for Camera, Audio, Location
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.fetchLocation()
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val missingPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            permissionsLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    // Back handling
    BackHandler(enabled = currentScreen != Screen.HOME) {
        when (currentScreen) {
            Screen.ADD_EVIDENCE, Screen.DOSSIER_VIEW -> {
                val selectedId = viewModel.selectedPlotId.value
                if (selectedId != null) {
                    viewModel.navigateTo(Screen.PLOT_DETAIL, plotId = selectedId)
                } else {
                    viewModel.navigateTo(Screen.HOME)
                }
            }
            Screen.PLOT_DETAIL -> viewModel.navigateTo(Screen.MY_PLOTS)
            else -> viewModel.navigateTo(Screen.HOME)
        }
    }

    val screenTitle = when (currentScreen) {
        Screen.HOME -> if (isHindi) "जलसाक्ष्य" else "JalSaakshya"
        Screen.NEW_PLOT -> if (isHindi) "नया खेत जोड़ें" else "Add New Plot"
        Screen.MY_PLOTS -> if (isHindi) "मेरे खेत" else "My Plots"
        Screen.PLOT_DETAIL -> if (isHindi) "खेत का विवरण" else "Plot Details"
        Screen.ADD_EVIDENCE -> if (isHindi) "सबूत दर्ज करें" else "Record Evidence"
        Screen.MAP_VIEW -> if (isHindi) "खेत का नक्शा" else "Field Map"
        Screen.DOSSIER_VIEW -> if (isHindi) "फसल नुकसान डॉसियर" else "Damage Dossier"
        Screen.PROFILE -> if (isHindi) "किसान प्रोफाइल" else "Farmer Profile"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            JalSaakshyaTopBar(
                viewModel = viewModel,
                title = screenTitle,
                currentScreen = currentScreen,
                onBack = {
                    when (currentScreen) {
                        Screen.ADD_EVIDENCE, Screen.DOSSIER_VIEW -> {
                            val selectedId = viewModel.selectedPlotId.value
                            if (selectedId != null) {
                                viewModel.navigateTo(Screen.PLOT_DETAIL, plotId = selectedId)
                            } else {
                                viewModel.navigateTo(Screen.HOME)
                            }
                        }
                        Screen.PLOT_DETAIL -> viewModel.navigateTo(Screen.MY_PLOTS)
                        else -> viewModel.navigateTo(Screen.HOME)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // Home
                NavigationBarItem(
                    selected = currentScreen == Screen.HOME,
                    onClick = { viewModel.navigateTo(Screen.HOME) },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = {
                        Text(
                            text = if (isHindi) "होम" else "Home",
                            fontWeight = if (currentScreen == Screen.HOME) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FarmGreenPrimary,
                        selectedTextColor = FarmGreenPrimary,
                        indicatorColor = Color(0xFFE8F5E9)
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                // My Plots
                NavigationBarItem(
                    selected = currentScreen == Screen.MY_PLOTS || currentScreen == Screen.PLOT_DETAIL || currentScreen == Screen.NEW_PLOT,
                    onClick = { viewModel.navigateTo(Screen.MY_PLOTS) },
                    icon = { Icon(Icons.Filled.Grass, contentDescription = "Plots") },
                    label = {
                        Text(
                            text = if (isHindi) "खेत" else "Plots",
                            fontWeight = if (currentScreen == Screen.MY_PLOTS) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FarmGreenPrimary,
                        selectedTextColor = FarmGreenPrimary,
                        indicatorColor = Color(0xFFE8F5E9)
                    ),
                    modifier = Modifier.testTag("nav_plots")
                )

                // Map View
                NavigationBarItem(
                    selected = currentScreen == Screen.MAP_VIEW,
                    onClick = { viewModel.navigateTo(Screen.MAP_VIEW) },
                    icon = { Icon(Icons.Filled.Map, contentDescription = "Map") },
                    label = {
                        Text(
                            text = if (isHindi) "नक्शा" else "Map",
                            fontWeight = if (currentScreen == Screen.MAP_VIEW) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FarmGreenPrimary,
                        selectedTextColor = FarmGreenPrimary,
                        indicatorColor = Color(0xFFE8F5E9)
                    ),
                    modifier = Modifier.testTag("nav_map")
                )

                // Dossier
                NavigationBarItem(
                    selected = currentScreen == Screen.DOSSIER_VIEW,
                    onClick = { viewModel.navigateTo(Screen.DOSSIER_VIEW) },
                    icon = { Icon(Icons.Filled.Description, contentDescription = "Dossier") },
                    label = {
                        Text(
                            text = if (isHindi) "डॉसियर" else "Dossier",
                            fontWeight = if (currentScreen == Screen.DOSSIER_VIEW) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FarmGreenPrimary,
                        selectedTextColor = FarmGreenPrimary,
                        indicatorColor = Color(0xFFE8F5E9)
                    ),
                    modifier = Modifier.testTag("nav_dossier")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { targetScreen ->
                when (targetScreen) {
                    Screen.HOME -> HomeScreen(viewModel = viewModel)
                    Screen.NEW_PLOT -> NewPlotScreen(viewModel = viewModel)
                    Screen.MY_PLOTS -> MyPlotsScreen(viewModel = viewModel)
                    Screen.PLOT_DETAIL -> PlotDetailScreen(viewModel = viewModel)
                    Screen.ADD_EVIDENCE -> AddEvidenceScreen(viewModel = viewModel)
                    Screen.MAP_VIEW -> MapViewScreen(viewModel = viewModel)
                    Screen.DOSSIER_VIEW -> DossierScreen(viewModel = viewModel)
                    Screen.PROFILE -> ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
