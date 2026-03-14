package com.example.healthtracker.pages

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtracker.services.user.UserViewModel

// ─────────────────────────────────────────────
//  ECRÃ DE DEFINIÇÕES
// ─────────────────────────────────────────────
@Composable
fun SettingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        SettingHeader()
        Dailytarget()
        Notifications()
        Reports()
    }

}


// ─────────────────────────────────────────────
//  CABEÇALHO
// ─────────────────────────────────────────────
@Composable
fun SettingHeader() {
    val displayName = "Definições"
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(" $displayName", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark )

        }
    }
}

// ─────────────────────────────────────────────
//  CARD META DIARIA
// ─────────────────────────────────────────────
@Composable
fun Dailytarget() {
    var stepTarget: Int
    var waterTarget: Int

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = CardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Definir metas diárias", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Spacer(Modifier.height(12.dp))

        }
    }
}

// ─────────────────────────────────────────────
//  CARD NOTIFICAÇÕES
// ─────────────────────────────────────────────
@Composable
fun Notifications() {

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = CardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Notificações", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Spacer(Modifier.height(12.dp))

        }
    }
}

// ─────────────────────────────────────────────
//  CARD Relatórios
// ─────────────────────────────────────────────
@Composable
fun Reports() {

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = CardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Exportar relatórios", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Spacer(Modifier.height(12.dp))

        }
    }
}


// ─────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun  SettingScreenPreview() {
    MaterialTheme {
        Scaffold(
            bottomBar = { BottomNavBar(selectedTab = 2) {} }
        ) { padding ->
            SettingScreen(modifier = Modifier.padding(padding))
        }
    }
}