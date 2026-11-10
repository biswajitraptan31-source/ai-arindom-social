package com.arindom.social.data.auth

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class AuthRepository(context: Context) {
    private val appContext = context.applicationContext
    val isConfigured: Boolean
        get() = FirebaseApp.getApps(appContext).isNotEmpty()

    val configurationMessage: String
        get() = if (isConfigured) {
            "Firebase is connected. Enable Email/Password sign-in in Firebase Console to use accounts."
        } else {
            "Firebase is not connected yet. Add app/google-services.json to enable real accounts and cloud data."
        }

    val currentUser
        get() = if (isConfigured) FirebaseAuth.getInstance().currentUser else null

    fun signIn(email: String, password: String, callback: (Result<String>) -> Unit) {
        if (!isConfigured) {
            callback(Result.failure(IllegalStateException("Firebase configuration is missing.")))
            return
        }
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) callback(Result.success(task.result.user?.uid.orEmpty()))
                else callback(Result.failure(task.exception ?: IllegalStateException("Sign-in failed.")))
            }
    }

    fun createAccount(email: String, password: String, displayName: String, callback: (Result<String>) -> Unit) {
        if (!isConfigured) {
            callback(Result.failure(IllegalStateException("Firebase configuration is missing.")))
            return
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    callback(Result.failure(task.exception ?: IllegalStateException("Registration failed.")))
                    return@addOnCompleteListener
                }
                val user = task.result.user
                if (user == null) {
                    callback(Result.failure(IllegalStateException("Firebase returned no user.")))
                    return@addOnCompleteListener
                }
                val profile = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.ifBlank { email.substringBefore("@") })
                    .build()
                user.updateProfile(profile).addOnCompleteListener { profileTask ->
                    if (profileTask.isSuccessful) callback(Result.success(user.uid))
                    else callback(Result.failure(profileTask.exception ?: IllegalStateException("Profile setup failed.")))
                }
            }
    }

    fun signOut() {
        if (isConfigured) FirebaseAuth.getInstance().signOut()
    }
}
