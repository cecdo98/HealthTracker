package com.example.healthtracker.pages

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtracker.services.user.UserViewModel
import com.example.healthtracker.ui.theme.AppTheme
import com.example.healthtracker.ui.theme.DarkColors
import com.example.healthtracker.ui.theme.LightColors
import com.example.healthtracker.ui.theme.LocalAppColors

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
        age       = prefs.age,
        onSave    = { fn, ln, w, a -> viewModel.saveProfile(fn, ln, w, a) }
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
    age: String = "",
    onSave: (String, String, String, String) -> Unit = { _, _, _, _ -> }
) {
    val c = AppTheme.colors

    var firstNameField by remember(firstName) { mutableStateOf(firstName) }
    var lastNameField  by remember(lastName)  { mutableStateOf(lastName)  }
    var weightField    by remember(weight)    { mutableStateOf(weight)    }
    var ageField       by remember(age)       { mutableStateOf(age)       }
    var saved          by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cabeçalho
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center) {
            Text("Perfil", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
        }

        // Avatar
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(100.dp).clip(CircleShape)
                    .background(Brush.radialGradient(colors = listOf(Color(0xFF6AB0F5), c.primary))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(60.dp))
            }
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape)
                    .background(Color(0xFF718096)).border(2.dp, c.card, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Alterar foto",
                    tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.height(32.dp))

        ProfileTextField("PRIMEIRO NOME", firstNameField, c, { firstNameField = it; saved = false })
        Spacer(Modifier.height(12.dp))
        ProfileTextField("ÚLTIMO NOME", lastNameField, c, { lastNameField = it; saved = false })
        Spacer(Modifier.height(12.dp))
        ProfileTextField("PESO", weightField, c, { weightField = it; saved = false },
            keyboardType = KeyboardType.Number, suffix = "kg")
        Spacer(Modifier.height(12.dp))
        ProfileTextField("IDADE", ageField, c, { ageField = it; saved = false },
            keyboardType = KeyboardType.Number)

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = { onSave(firstNameField, lastNameField, weightField, ageField); saved = true },
            modifier = Modifier.fillMaxWidth(0.5f).height(44.dp),
            shape    = RoundedCornerShape(8.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = c.primary)
        ) {
            Text("Gravar", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
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
    suffix: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            color = c.textSecondary, letterSpacing = 0.8.sp)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            suffix = if (suffix.isNotEmpty()) {{ Text(suffix, color = c.textSecondary) }} else null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = c.inputBorderFocused,
                unfocusedBorderColor    = c.inputBorder,
                focusedContainerColor   = c.card,
                unfocusedContainerColor = c.card,
                focusedTextColor        = c.textPrimary,
                unfocusedTextColor      = c.textPrimary
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
                ProfileScreenContent(modifier = Modifier.padding(padding),
                    firstName = "João", lastName = "Silva", weight = "75", age = "28")
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
                ProfileScreenContent(modifier = Modifier.padding(padding),
                    firstName = "João",
                    lastName = "Silva",
                    weight = "75",
                    age = "28"
                )
            }
        }
    }
}