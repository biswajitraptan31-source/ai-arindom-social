package com.arindom.social.data.auth

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

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
                    if (!profileTask.isSuccessful) {
                        callback(Result.failure(profileTask.exception ?: IllegalStateException("Profile setup failed.")))
                        return@addOnCompleteListener
                    }
                    val name = displayName.ifBlank { email.substringBefore("@") }
                    val now = Timestamp.now()
                    val profileData = hashMapOf(
                        "uid" to user.uid,
                        "displayName" to name,
                        "email" to email.trim(),
                        "createdAt" to now,
                        "updatedAt" to now
                    )
                    FirebaseFirestore.getInstance().collection("users").document(user.uid)
                        .set(profileData)
                        .addOnSuccessListener { callback(Result.success(user.uid)) }
                        .addOnFailureListener { error ->
                            callback(Result.failure(IllegalStateException("Account created, but profile could not be saved: ${error.message}", error)))
                        }
                }
            }
    }

    fun signOut() {
        if (isConfigured) FirebaseAuth.getInstance().signOut()
    }
}
