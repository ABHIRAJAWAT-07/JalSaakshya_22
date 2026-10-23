package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.JalSaakshyaRepository
import com.example.data.model.DossierRecord
import com.example.data.model.EvidenceEntry
import com.example.data.model.FarmerProfile
import com.example.data.model.FloodPhase
import com.example.data.model.Plot
import com.example.util.AudioRecorderHelper
import com.example.util.LocationHelper
import com.example.util.PdfDossierGenerator
import com.example.util.QrCodeGenerator
import com.example.util.VoicePromptHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class Screen {
    HOME,
    NEW_PLOT,
    MY_PLOTS,
    PLOT_DETAIL,
    ADD_EVIDENCE,
    MAP_VIEW,
    DOSSIER_VIEW,
    PROFILE
}

class JalSaakshyaViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = JalSaakshyaRepository(
        context = application,
        plotDao = database.plotDao(),
        evidenceDao = database.evidenceDao(),
        farmerDao = database.farmerDao(),
        dossierDao = database.dossierDao()
    )

    val voicePromptHelper = VoicePromptHelper(application)
    private val audioRecorderHelper = AudioRecorderHelper(application)
    private val locationHelper = LocationHelper(application)

    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedPlotId = MutableStateFlow<String?>(null)
    val selectedPlotId: StateFlow<String?> = _selectedPlotId.asStateFlow()

    private val _selectedPhase = MutableStateFlow(FloodPhase.DURING)
    val selectedPhase: StateFlow<FloodPhase> = _selectedPhase.asStateFlow()

    private val _language = MutableStateFlow("hi") // default Hindi per spec
    val language: StateFlow<String> = _language.asStateFlow()

    private val _voiceGuidanceEnabled = MutableStateFlow(true)
    val voiceGuidanceEnabled: StateFlow<Boolean> = _voiceGuidanceEnabled.asStateFlow()

    private val _currentLocation = MutableStateFlow(Pair(26.7606, 83.3732))
    val currentLocation: StateFlow<Pair<Double, Double>> = _currentLocation.asStateFlow()

    private val _isRecordingAudio = MutableStateFlow(false)
    val isRecordingAudio: StateFlow<Boolean> = _isRecordingAudio.asStateFlow()

    private val _recordingDurationSec = MutableStateFlow(0)
    val recordingDurationSec: StateFlow<Int> = _recordingDurationSec.asStateFlow()

    private val _recordedAudioFile = MutableStateFlow<File?>(null)
    val recordedAudioFile: StateFlow<File?> = _recordedAudioFile.asStateFlow()

    private val _playingAudioPath = MutableStateFlow<String?>(null)
    val playingAudioPath: StateFlow<String?> = _playingAudioPath.asStateFlow()

    private val _generatedPdf = MutableStateFlow<File?>(null)
    val generatedPdf: StateFlow<File?> = _generatedPdf.asStateFlow()

    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap.asStateFlow()

    private val _qrJsonPayload = MutableStateFlow<String?>(null)
    val qrJsonPayload: StateFlow<String?> = _qrJsonPayload.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    private val _infoBannerMessage = MutableStateFlow<String?>(null)
    val infoBannerMessage: StateFlow<String?> = _infoBannerMessage.asStateFlow()

    val plots: StateFlow<List<Plot>> = repository.allPlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val farmerProfile: StateFlow<FarmerProfile?> = repository.farmerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allEvidence: StateFlow<List<EvidenceEntry>> = repository.allEvidence
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDossiers: StateFlow<List<DossierRecord>> = repository.allDossiers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var recordTimerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
            fetchLocation()
        }
    }

    fun navigateTo(screen: Screen, plotId: String? = null, phase: FloodPhase? = null) {
        if (plotId != null) {
            _selectedPlotId.value = plotId
        }
        if (phase != null) {
            _selectedPhase.value = phase
        }
        _currentScreen.value = screen
        stopAudioPlayback()

        // Screen voice prompts
        when (screen) {
            Screen.HOME -> speak(
                "जलसाक्ष्य में आपका स्वागत है। नया खेत जोड़ें या फसल का सबूत दर्ज करें।",
                "Welcome to JalSaakshya. Add a new plot or record flood evidence."
            )
            Screen.NEW_PLOT -> speak(
                "नया खेत जोड़ने के लिए नाम चुनें और जीपीएस स्थान दर्ज करें।",
                "To add a new plot, select a name and save GPS location."
            )
            Screen.ADD_EVIDENCE -> speak(
                "खेत की फसल की स्थिति की फोटो खींचें और बोलकर नुकसान बताएं।",
                "Capture photo of standing or submerged crop and describe damage."
            )
            Screen.MAP_VIEW -> speak(
                "यह आपके खेत और बाढ़ प्रभावित बिंदुओं का नक्शा है।",
                "This is the map of your plots and flood recorded points."
            )
            Screen.DOSSIER_VIEW -> speak(
                "सबूत रिपोर्ट पीडीएफ और क्यूआर कोड के साथ तैयार करने के लिए तैयार है।",
                "Damage dossier report is ready to generate with QR code and PDF."
            )
            else -> {}
        }
    }

    fun toggleLanguage() {
        val newLang = if (_language.value == "hi") "en" else "hi"
        _language.value = newLang
        speak(
            if (newLang == "hi") "भाषा हिंदी चुनी गई" else "Language changed to English",
            if (newLang == "hi") "Language changed to Hindi" else "Language set to English"
        )
    }

    fun toggleVoiceGuidance() {
        _voiceGuidanceEnabled.value = !_voiceGuidanceEnabled.value
        voicePromptHelper.isMuted = !_voiceGuidanceEnabled.value
        if (_voiceGuidanceEnabled.value) {
            speak("आवाज़ मार्गदर्शन चालू है", "Voice guidance turned on")
        }
    }

    fun speak(textHi: String, textEn: String) {
        if (!_voiceGuidanceEnabled.value) return
        voicePromptHelper.speak(textHi, textEn, isHindi = (_language.value == "hi"))
    }

    fun fetchLocation() {
        viewModelScope.launch {
            try {
                val loc = locationHelper.getCurrentLocation()
                if (loc != null) {
                    _currentLocation.value = Pair(loc.latitude, loc.longitude)
                }
            } catch (e: Exception) {
                Log.w("JalSaakshyaVM", "Location retrieval skipped", e)
            }
        }
    }

    fun createPlot(name: String, cropType: String, areaAcre: Double, lat: Double, lng: Double) {
        viewModelScope.launch {
            val plot = Plot(
                name = name,
                cropType = cropType,
                areaAcre = areaAcre,
                latitude = lat,
                longitude = lng
            )
            repository.insertPlot(plot)
            _selectedPlotId.value = plot.id
            _infoBannerMessage.value = "खेत '$name' सुरक्षित हो गया!"
            speak("खेत '$name' सुरक्षित हो गया है।", "Plot '$name' has been saved.")
            navigateTo(Screen.MY_PLOTS)
        }
    }

    fun deletePlot(plotId: String) {
        viewModelScope.launch {
            repository.deletePlot(plotId)
            if (_selectedPlotId.value == plotId) {
                _selectedPlotId.value = null
            }
            speak("खेत हटा दिया गया", "Plot removed")
        }
    }

    fun deleteEvidence(id: String) {
        viewModelScope.launch {
            repository.deleteEvidence(id)
            speak("सबूत हटा दिया गया", "Evidence removed")
        }
    }

    fun startAudioRecording() {
        val file = audioRecorderHelper.startRecording()
        if (file != null) {
            _recordedAudioFile.value = file
            _isRecordingAudio.value = true
            _recordingDurationSec.value = 0
            speak("रिकॉर्डिंग शुरू हो गई है। फसल के बारे में बोलें।", "Recording started. Describe your crop.")

            recordTimerJob?.cancel()
            recordTimerJob = viewModelScope.launch {
                while (_isRecordingAudio.value && _recordingDurationSec.value < 60) {
                    delay(1000)
                    _recordingDurationSec.value += 1
                }
                if (_isRecordingAudio.value) {
                    stopAudioRecording()
                }
            }
        } else {
            speak("माइक शुरू नहीं हो सका", "Could not start microphone")
        }
    }

    fun stopAudioRecording() {
        recordTimerJob?.cancel()
        val (file, duration) = audioRecorderHelper.stopRecording()
        _isRecordingAudio.value = false
        if (file != null) {
            _recordedAudioFile.value = file
            _recordingDurationSec.value = duration
            speak("आवाज सुरक्षित कर ली गई है।", "Voice note recorded.")
        }
    }

    fun playAudio(path: String) {
        if (_playingAudioPath.value == path) {
            stopAudioPlayback()
            return
        }
        stopAudioPlayback()
        _playingAudioPath.value = path
        audioRecorderHelper.playAudio(path) {
            _playingAudioPath.value = null
        }
    }

    fun stopAudioPlayback() {
        audioRecorderHelper.stopPlayback()
        _playingAudioPath.value = null
    }

    fun resetAudioRecording() {
        stopAudioPlayback()
        _recordedAudioFile.value = null
        _recordingDurationSec.value = 0
        _isRecordingAudio.value = false
    }

    fun saveEvidence(
        photoFile: File,
        notes: String,
        lossPercent: Int,
        latitude: Double,
        longitude: Double
    ) {
        viewModelScope.launch {
            val plotId = _selectedPlotId.value ?: return@launch
            val audio = _recordedAudioFile.value
            val audioDuration = _recordingDurationSec.value

            val entry = EvidenceEntry(
                id = UUID.randomUUID().toString(),
                plotId = plotId,
                phase = _selectedPhase.value,
                photoPath = photoFile.absolutePath,
                voiceNotePath = audio?.absolutePath,
                voiceDurationSec = audioDuration,
                latitude = latitude,
                longitude = longitude,
                capturedAt = System.currentTimeMillis(),
                cropDamageNotes = notes,
                estimatedLossPercent = lossPercent
            )
            repository.insertEvidence(entry)

            // Reset recording state
            _recordedAudioFile.value = null
            _recordingDurationSec.value = 0

            _infoBannerMessage.value = "सबूत समय और स्थान के साथ सुरक्षित!"
            speak("फसल का सबूत जीपीएस और समय के साथ सुरक्षित हो गया है।", "Evidence saved with time and location.")
            navigateTo(Screen.PLOT_DETAIL, plotId = plotId)
        }
    }

    fun updateProfile(name: String, village: String, district: String, phone: String) {
        viewModelScope.launch {
            val profile = FarmerProfile(
                id = 1,
                name = name,
                village = village,
                district = district,
                language = _language.value,
                phone = phone
            )
            repository.updateProfile(profile)
            speak("प्रोफ़ाइल सुरक्षित हो गई", "Profile updated")
            navigateTo(Screen.HOME)
        }
    }

    fun generateDossierForPlot(plotId: String, context: Application) {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            speak("सबूत दस्तावेज़ पीडीएफ तैयार किया जा रहा है...", "Generating damage dossier PDF...")

            val plot = plots.value.find { it.id == plotId } ?: return@launch
            val entries = repository.getEvidenceForPlotSync(plotId)
            val farmer = farmerProfile.value
            val dossierId = UUID.randomUUID().toString()

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdf.format(Date())

            // Create compact standard JSON payload for QR code
            val qrJson = JSONObject().apply {
                put("app", "JalSaakshya")
                put("dossierId", dossierId)
                put("farmer", farmer?.name ?: "Farmer")
                put("village", farmer?.village ?: "Village")
                put("plot", plot.name)
                put("crop", plot.cropType)
                put("gps", "${plot.latitude},${plot.longitude}")
                put("entries", entries.size)
                put("date", dateStr)
            }.toString()

            _qrJsonPayload.value = qrJson
            val qr = QrCodeGenerator.generateQrBitmap(qrJson, 300)
            _qrBitmap.value = qr

            val pdf = PdfDossierGenerator.generateDossierPdf(
                context = context,
                farmer = farmer,
                plot = plot,
                entries = entries,
                dossierId = dossierId,
                qrPayload = qrJson
            )

            if (pdf != null) {
                _generatedPdf.value = pdf
                val record = DossierRecord(
                    id = dossierId,
                    plotId = plot.id,
                    plotName = plot.name,
                    farmerName = farmer?.name ?: "Farmer",
                    generatedAt = System.currentTimeMillis(),
                    pdfFilePath = pdf.absolutePath,
                    qrPayload = qrJson,
                    entriesCount = entries.size
                )
                repository.insertDossier(record)
                speak(
                    "सबूत दस्तावेज़ पीडीएफ सफलतापूर्वक बन गया है। आप इसे लेखपाल या बैंक को दिखा सकते हैं।",
                    "Damage dossier PDF generated successfully. You can share or show to officials."
                )
            }

            _isGeneratingPdf.value = false
        }
    }

    fun shareCurrentPdf(context: Application) {
        val pdf = _generatedPdf.value ?: return
        PdfDossierGenerator.sharePdf(context, pdf)
    }

    override fun onCleared() {
        super.onCleared()
        recordTimerJob?.cancel()
        voicePromptHelper.shutdown()
        audioRecorderHelper.release()
    }
}
