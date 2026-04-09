package com.example.healthtracker.pages

import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.healthtracker.services.user.UserViewModel
import com.example.healthtracker.ui.theme.AppTheme
import com.example.healthtracker.ui.theme.DarkColors
import com.example.healthtracker.ui.theme.LightColors
import com.example.healthtracker.ui.theme.LocalAppColors
import java.util.Locale

// ─────────────────────────────────────────────
//  WRAPPER
// ─────────────────────────────────────────────
@Composable
fun ProfileScreen(modifier: Modifier = Modifier, viewModel: UserViewModel = viewModel()) {
    val prefs by viewModel.prefs.collectAsState()
    ProfileScreenContent(
        modifier  = modifier,
        firstName = prefs.firstName,
        lastName  = prefs.lastName,
        weight    = prefs.weight,
        height    = prefs.height,
        age       = prefs.age,
        isMetric  = prefs.isMetric,
        profilePictureUri = prefs.profilePictureUri,
        onSave    = { fn, ln, w, h, a, m -> viewModel.saveProfile(fn, ln, w, h, a, m) },
        onPhotoSelected = { uri -> viewModel.saveProfilePicture(uri?.toString()) }
    )
}

// ─────────────────────────────────────────────
//  CONTENT
// ─────────────────────────────────────────────
@Composable
fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    firstName: String = "",
    lastName: String = "",
    weight: String = "",
    height: String = "",
    age: String = "",
    isMetric: Boolean = true,
    profilePictureUri: String? = null,
    onSave: (String, String, String, String, String, Boolean) -> Unit = { _, _, _, _, _, _ -> },
    onPhotoSelected: (Uri?) -> Unit = {}
) {
    val c = AppTheme.colors
    val context = LocalContext.current

    var firstNameField by remember(firstName) { mutableStateOf(firstName) }
    var lastNameField  by remember(lastName)  { mutableStateOf(lastName)  }
    var weightField    by remember(weight)    { mutableStateOf(weight)    }
    var heightField    by remember(height)    { mutableStateOf(height)    }
    var ageField       by remember(age)       { mutableStateOf(age)       }
    var isMetricState  by remember(isMetric)  { mutableStateOf(isMetric)  }
    var saved          by remember { mutableStateOf(false) }

    // Estado para saber qual campo está a ser preenchido por voz
    var activeFieldForSpeech by remember { mutableStateOf<String?>(null) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
                if (spokenText.isNotEmpty()) {
                    when (activeFieldForSpeech) {
                        "firstName" -> { firstNameField = spokenText; saved = false }
                        "lastName" -> { lastNameField = spokenText; saved = false }
                        "weight" -> { weightField = spokenText.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.'); saved = false }
                        "height" -> { heightField = spokenText.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.'); saved = false }
                        "age" -> { ageField = spokenText.filter { it.isDigit() }; saved = false }
                    }
                }
            }
        }
    )

    val startVoiceInput = { fieldName: String ->
        activeFieldForSpeech = fieldName
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale os dados...")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            // Caso o dispositivo não suporte reconhecimento de voz
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                onPhotoSelected(it)
            }
        }
    )

    val bmiValue = remember(weightField, heightField, isMetricState) {
        val wRaw = weightField.toFloatOrNull() ?: 0f
        val hRaw = heightField.toFloatOrNull() ?: 0f
        if (isMetricState) {
            val hMetros = hRaw / 100f
            if (hMetros > 0) wRaw / (hMetros * hMetros) else 0f
        } else {
            if (hRaw > 0) (wRaw / (hRaw * hRaw)) * 703f else 0f
        }
    }

    val (bmiCategory, bmiColor, bmiIconColor) = when {
        bmiValue <= 0 -> Triple("", c.textSecondary, c.textSecondary)
        bmiValue < 18.5 -> Triple("Abaixo do peso", Color(0xFF4299E1), Color(0xFFEBF8FF))
        bmiValue < 25.0 -> Triple("Peso normal", Color(0xFF48BB78), Color(0xFFF0FFF4))
        bmiValue < 30.0 -> Triple("Sobrepeso", Color(0xFFECC94B), Color(0xFFFFFFF0))
        else -> Triple("Obesidade", Color(0xFFF56565), Color(0xFFFFF5F5))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center) {
            Text("Perfil", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
        }
        Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.clickable { photoLauncher.launch(arrayOf("image/*")) }) {
            Box(
                modifier = Modifier
                    .size(100.dp).clip(CircleShape)
                    .background(Brush.radialGradient(colors = listOf(Color(0xFF6AB0F5), c.primary))),
                contentAlignment = Alignment.Center
            ) {
                if (profilePictureUri != null) {
                    AsyncImage(
                        model = profilePictureUri,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(60.dp))
                }
            }
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF718096)).border(2.dp, c.card, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Alterar foto", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(24.dp))

        if (bmiValue > 0) {
            ElevatedCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.elevatedCardColors(containerColor = c.card), elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Índice de Massa Corporal", fontSize = 13.sp, color = c.textSecondary, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("%.1f".format(bmiValue), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = bmiColor)
                                Text(" kg/m²", fontSize = 14.sp, color = c.textSecondary, modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))
                            }
                        }
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(if (c.isDark) bmiColor.copy(alpha = 0.2f) else bmiIconColor), contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = bmiColor, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(if (c.isDark) Color(0xFF4A5568) else Color(0xFFEDF2F7))) {
                        val progress = (bmiValue / 40f).coerceIn(0f, 1f)
                        Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().clip(CircleShape).background(bmiColor))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Badge(containerColor = bmiColor, contentColor = Color.White) {
                            Text(bmiCategory.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(text = when {
                            bmiValue < 18.5 -> "Precisa de ganhar peso de forma saudável."
                            bmiValue < 25.0 -> "Excelente! Continue assim."
                            bmiValue < 30.0 -> "Atenção à alimentação e exercício."
                            else -> "Considere consultar um especialista."
                        }, fontSize = 11.sp, color = c.textSecondary)
                    }
                }
            }
        }

        ProfileTextField("PRIMEIRO NOME", firstNameField, c, { firstNameField = it; saved = false }, onMicClick = { startVoiceInput("firstName") })
        Spacer(Modifier.height(12.dp))
        ProfileTextField("ÚLTIMO NOME", lastNameField, c, { lastNameField = it; saved = false }, onMicClick = { startVoiceInput("lastName") })
        Spacer(Modifier.height(24.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            SegmentedButton(selected = isMetricState, onClick = { isMetricState = true; saved = false }, shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)) {
                Text("Métrico (kg, cm)", fontSize = 12.sp)
            }
            SegmentedButton(selected = !isMetricState, onClick = { isMetricState = false; saved = false }, shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)) {
                Text("Imperial (lbs, in)", fontSize = 12.sp)
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                ProfileTextField(label = if (isMetricState) "PESO (kg)" else "PESO (lbs)", value = weightField, c = c, onValueChange = { weightField = it; saved = false }, keyboardType = KeyboardType.Number, suffix = if (isMetricState) "kg" else "lbs", onMicClick = { startVoiceInput("weight") })
            }
            Box(modifier = Modifier.weight(1f)) {
                ProfileTextField(label = if (isMetricState) "ALTURA (cm)" else "ALTURA (in)", value = heightField, c = c, onValueChange = { heightField = it; saved = false }, keyboardType = KeyboardType.Number, suffix = if (isMetricState) "cm" else "in", onMicClick = { startVoiceInput("height") })
            }
        }
        Spacer(Modifier.height(12.dp))
        ProfileTextField("IDADE", ageField, c, { ageField = it; saved = false }, keyboardType = KeyboardType.Number, onMicClick = { startVoiceInput("age") })
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { onSave(firstNameField, lastNameField, weightField, heightField, ageField, isMetricState); saved = true },
            modifier = Modifier.fillMaxWidth(0.5f).height(48.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = c.primary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
        ) {
            Text("Gravar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        if (saved) {
            Spacer(Modifier.height(12.dp))
            Text("✓ Perfil guardado!", color = c.primary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ─────────────────────────────────────────────
//  CAMPO DE TEXTO
// ─────────────────────────────────────────────
@Composable
fun ProfileTextField(
    label: String,
    value: String,
    c: com.example.healthtracker.ui.theme.AppColors,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    suffix: String = "",
    onMicClick: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary, letterSpacing = 0.8.sp)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = if (onMicClick != null) {
                {
                    IconButton(onClick = onMicClick) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Entrada por voz",
                            tint = c.primary
                        )
                    }
                }
            } else null,
            suffix = if (suffix.isNotEmpty()) {{ Text(suffix, color = c.textSecondary) }} else null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = c.inputBorderFocused,
                unfocusedBorderColor = c.inputBorder,
                focusedContainerColor = c.card,
                unfocusedContainerColor = c.card,
                focusedTextColor = c.textPrimary,
                unfocusedTextColor = c.textPrimary
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

// ─────────────────────────────────────────────
//  PREVIEWS
// ─────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenLightPreview() {
    CompositionLocalProvider(LocalAppColors provides LightColors) {
        MaterialTheme {
            Scaffold(bottomBar = { BottomNavBar(selectedTab = 1) {} }) { padding ->
                ProfileScreenContent(modifier = Modifier.padding(padding), firstName = "João", lastName = "Silva", weight = "75", height = "175", age = "28", isMetric = true)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenDarkPreview() {
    CompositionLocalProvider(LocalAppColors provides DarkColors) {
        MaterialTheme(colorScheme = darkColorScheme()) {
            Scaffold(bottomBar = { BottomNavBar(selectedTab = 1) {} }) { padding ->
                ProfileScreenContent(modifier = Modifier.padding(padding), firstName = "João", lastName = "Silva", weight = "165", height = "69", age = "28", isMetric = false)
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_TABLET)
@Composable
fun ProfileScreenLightTabletPreview() {
    CompositionLocalProvider(LocalAppColors provides LightColors) {
        MaterialTheme {
            Scaffold(bottomBar = { BottomNavBar(selectedTab = 1) {} }) { padding ->
                ProfileScreenContent(modifier = Modifier.padding(padding), firstName = "João", lastName = "Silva", weight = "75", height = "175", age = "28", isMetric = true)
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_TABLET)
@Composable
fun ProfileScreenDarkTabletPreview() {
    CompositionLocalProvider(LocalAppColors provides DarkColors) {
        MaterialTheme(colorScheme = darkColorScheme()) {
            Scaffold(bottomBar = { BottomNavBar(selectedTab = 1) {} }) { padding ->
                ProfileScreenContent(modifier = Modifier.padding(padding), firstName = "João", lastName = "Silva", weight = "165", height = "69", age = "28", isMetric = false)
            }
        }
    }
}