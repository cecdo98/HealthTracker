package com.example.healthtracker.services.user

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class UserViewModel : ViewModel() {
    var firstName by mutableStateOf("")
    var lastName  by mutableStateOf("")
    var weight    by mutableStateOf("")
    var age       by mutableStateOf("")
}
