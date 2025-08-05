package com.example.fluirapp.screens.settings

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SettingsScreenViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth

    fun signOut(onSignOut: () -> Unit){
        try {
            auth.signOut()
            onSignOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}