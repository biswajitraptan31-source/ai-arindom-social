package com.arindom.social

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arindom.social.data.auth.AuthRepository
import com.arindom.social.data.posts.FirebasePostRepository
import com.google.firebase.FirebaseApp

private val NeonBackground = Color(0xFF080914)
private val NeonPanel = Color(0xFF15182A)
private val NeonLime = Color(0xFFB8FF3B)
private val NeonPurple = Color(0xFF9275FF)
private val NeonText = Color(0xFFF6F7FF)
private val NeonMuted = Color(0xFF969BB5)

class NeonNativeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(applicationContext)
        val authRepository = AuthRepository(applicationContext)
        val postRepository = FirebasePostRepository(applicationContext)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = NeonLime, background = NeonBackground, surface = NeonPanel, onSurface = NeonText, onBackground = NeonText)) {
                Surface(color = NeonBackground, modifier = Modifier.fillMaxSize()) {
                    NeonApp(authRepository, postRepository)
                }
            }
        }
    }
}

@Composable
private fun NeonApp(auth: AuthRepository, posts: FirebasePostRepository) {
    var page by remember { mutableStateOf("Home") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf(auth.configurationMessage) }
    var caption by remember { mutableStateOf("") }
    var isBusy by remember { mutableStateOf(false) }
    val currentUser = auth.currentUser

    Scaffold(
        containerColor = NeonBackground,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(NeonBackground).padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("NX", color = NeonLime, fontSize = 27.sp, fontWeight = FontWeight.Black)
                Column(Modifier.padding(start = 10.dp)) {
                    Text("ARINDOM NEON X", color = NeonText, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                    Text("YOUR SOCIAL UNIVERSE", color = NeonMuted, fontSize = 9.sp, letterSpacing = 2.sp)
                }
                Spacer(Modifier.weight(1f))
                Text(if (auth.isConfigured) "CLOUD READY" else "SETUP NEEDED", color = if (auth.isConfigured) NeonLime else NeonMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = {
            NavigationBar(containerColor = NeonPanel) {
                listOf(
                    "Home" to Icons.Default.Home,
                    "Explore" to Icons.Default.Search,
                    "Create" to Icons.Default.Add,
                    "Reels" to Icons.Default.PlayArrow,
                    "Inbox" to Icons.Default.Mail,
                    "Profile" to Icons.Default.Person
                ).forEach { (label, icon) ->
                    NavigationBarItem(
                        selected = page == label,
                        onClick = { page = label },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 9.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(NeonBackground).padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (page) {
                "Home" -> {
                    Text("YOUR WORLD · YOUR RULES", color = NeonLime, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Discover your universe.", color = NeonText, fontSize = 32.sp, lineHeight = 36.sp, fontWeight = FontWeight.Black)
                    Text("A native Android foundation for the next generation of social.", color = NeonMuted)
                    StatusCard(status)
                    FeatureCard("Your feed", "Cloud posts will appear here after Firebase is configured and the first account signs in.")
                    FeatureCard("Creator space", "The existing prototype is preserved in the repository while the native app becomes the production foundation.")
                }
                "Explore" -> {
                    PageTitle("Explore", "Find creators, topics and communities.")
                    FeatureCard("Discovery", "Search indexing, recommendations and trending topics are the next implementation milestone.")
                }
                "Create" -> {
                    PageTitle("Create", "Share something with your universe.")
                    OutlinedTextField(value = caption, onValueChange = { caption = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Write a caption") }, minLines = 3)
                    Button(
                        onClick = {
                            val user = auth.currentUser
                            if (user == null) status = "Sign in first. Configure Firebase to enable real accounts."
                            else if (caption.isBlank()) status = "Add a caption before publishing."
                            else {
                                isBusy = true
                                posts.publishTextPost(user.uid, caption.trim()) { result ->
                                    isBusy = false
                                    status = result.fold({ caption = ""; "Post published to cloud." }, { "Post failed: ${it.message ?: "unknown error"}" })
                                }
                            }
                        },
                        enabled = !isBusy,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = NeonBackground)
                    ) { Text(if (isBusy) "Publishing…" else "Publish to cloud") }
                    Text("Photo/video upload will be connected after Firebase Storage configuration.", color = NeonMuted, fontSize = 12.sp)
                }
                "Reels" -> {
                    PageTitle("Reels", "Short-form video is part of the full product scope.")
                    FeatureCard("Video pipeline", "Upload processing, playback delivery, moderation and creator controls are planned for the media milestone.")
                }
                "Inbox" -> {
                    PageTitle("Messages", "Private conversations.")
                    FeatureCard("Real-time chat", "Conversation lists, message delivery, read receipts and push alerts will be added after accounts and security rules are live.")
                }
                else -> {
                    PageTitle("Your profile", currentUser?.email ?: "Sign in or create an account to establish your profile.")
                    if (currentUser != null) {
                        Text("Signed in as ${currentUser.email ?: currentUser.uid}", color = NeonText)
                        Button(onClick = { auth.signOut(); status = "Signed out." }) { Text("Sign out") }
                    } else {
                        OutlinedTextField(value = displayName, onValueChange = { displayName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Display name (registration)") })
                        OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Email address") })
                        OutlinedTextField(value = password, onValueChange = { password = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Password") })
                        Button(
                            onClick = {
                                if (email.isBlank() || password.length < 6) status = "Enter a valid email and a password of at least 6 characters."
                                else {
                                    isBusy = true
                                    val callback: (Result<String>) -> Unit = { result ->
                                        isBusy = false
                                        status = result.fold({ "Account ready: $it" }, { it.message ?: "Authentication failed." })
                                    }
                                    if (isRegister) auth.createAccount(email.trim(), password, displayName.trim(), callback)
                                    else auth.signIn(email.trim(), password, callback)
                                }
                            },
                            enabled = !isBusy && auth.isConfigured,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = NeonBackground)
                        ) { Text(if (isBusy) "Please wait…" else if (isRegister) "Create account" else "Sign in") }
                        TextButton(onClick = { isRegister = !isRegister }) {
                            Text(if (isRegister) "Already registered? Sign in" else "New here? Create an account", color = NeonPurple)
                        }
                        if (!auth.isConfigured) Text("Firebase setup is required before account creation can work.", color = NeonMuted, fontSize = 12.sp)
                    }
                    StatusCard(status)
                }
            }
        }
    }
}

@Composable
private fun PageTitle(title: String, subtitle: String) {
    Text(title, color = NeonText, fontSize = 30.sp, fontWeight = FontWeight.Black)
    Text(subtitle, color = NeonMuted)
}

@Composable
private fun StatusCard(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = NeonPanel), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("BACKEND STATUS", color = NeonLime, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Text(message, color = NeonText, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun FeatureCard(title: String, description: String) {
    Card(colors = CardDefaults.cardColors(containerColor = NeonPanel), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, color = NeonText, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(description, color = NeonMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 7.dp))
        }
    }
}
