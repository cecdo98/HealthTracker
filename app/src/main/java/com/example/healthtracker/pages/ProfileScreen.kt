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

// ─────────────────────────────────────────────
//  ECRÃ DE PERFIL
// ─────────────────────────────────────────────
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: UserViewModel = viewModel()
) {
    // Lê o estado persistido
    val prefs by viewModel.prefs.collectAsState()

    // Estado local dos campos (para edição antes de guardar)
    var firstName by remember(prefs.firstName) { mutableStateOf(prefs.firstName) }
    var lastName  by remember(prefs.lastName)  { mutableStateOf(prefs.lastName)  }
    var weight    by remember(prefs.weight)    { mutableStateOf(prefs.weight)    }
    var age       by remember(prefs.age)       { mutableStateOf(prefs.age)       }
    var saved     by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileHeader()

        // Avatar
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(colors = listOf(Color(0xFF6AB0F5), PrimaryBlue))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(60.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF718096))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add, contentDescription = "Alterar foto",
                    tint = Color.White, modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        ProfileTextField(
            label = "PRIMEIRO NOME",
            value = firstName,
            onValueChange = { firstName = it; saved = false }
        )
        Spacer(Modifier.height(12.dp))
        ProfileTextField(
            label = "ÚLTIMO NOME",
            value = lastName,
            onValueChange = { lastName = it; saved = false }
        )
        Spacer(Modifier.height(12.dp))
        ProfileTextField(
            label = "PESO",
            value = weight,
            onValueChange = { weight = it; saved = false },
            keyboardType = KeyboardType.Number,
            suffix = "kg"
        )
        Spacer(Modifier.height(12.dp))
        ProfileTextField(
            label = "IDADE",
            value = age,
            onValueChange = { age = it; saved = false },
            keyboardType = KeyboardType.Number
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                viewModel.saveProfile(firstName, lastName, weight, age)
                saved = true
            },
            modifier = Modifier.fillMaxWidth(0.5f).height(44.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6AB0F5))
        ) {
            Text("Gravar", color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        if (saved) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "✓ Perfil guardado!",
                color = Color(0xFF54A3F3),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─────────────────────────────────────────────
//  CABEÇALHO
// ─────────────────────────────────────────────
@Composable
fun ProfileHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Perfil", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

// ─────────────────────────────────────────────
//  CAMPO DE TEXTO REUTILIZÁVEL
// ─────────────────────────────────────────────
@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    suffix: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            color = TextLight, letterSpacing = 0.8.sp
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            suffix = if (suffix.isNotEmpty()) {{ Text(suffix, color = TextLight) }} else null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = PrimaryBlue,
                unfocusedBorderColor    = Color(0xFFCBD5E0),
                focusedContainerColor   = CardColor,
                unfocusedContainerColor = CardColor
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

// ─────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        Scaffold(bottomBar = { BottomNavBar(selectedTab = 1) {} }) { padding ->
            ProfileScreen(modifier = Modifier.padding(padding))
        }
    }
}