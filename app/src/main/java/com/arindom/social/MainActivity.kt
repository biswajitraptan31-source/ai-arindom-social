package com.arindom.social

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF17152B)
private val Violet = Color(0xFF7357F6)
private val Surface = Color(0xFFF6F5FA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SocialApp() }
    }
}

data class Post(val id: Int, val author: String, val handle: String, val caption: String, val likes: Int, val liked: Boolean = false)

@Composable
fun SocialApp() {
    var selected by remember { mutableStateOf("Home") }
    var caption by remember { mutableStateOf("") }
    var posts by remember {
        mutableStateOf(listOf(
            Post(1, "Arindom", "@arindom", "Welcome to AI ARINDOM SOCIAL! ✨", 128),
            Post(2, "Creative Studio", "@creative", "Sharing ideas, moments and creativity.", 86),
            Post(3, "Community", "@community", "Your new social space starts here. 👋", 54)
        ))
    }

    MaterialTheme(colorScheme = lightColorScheme(primary = Violet, background = Surface)) {
        Scaffold(
            containerColor = Surface,
            topBar = {
                Row(
                    Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AI", color = Violet, fontSize = 25.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.width(7.dp))
                    Text("ARINDOM SOCIAL", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Favorite, contentDescription = "Activity", tint = Violet)
                }
            },
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    listOf("Home", "Search", "Create", "Profile").forEach { item ->
                        val icon = when (item) {
                            "Home" -> Icons.Default.Home
                            "Search" -> Icons.Default.Search
                            "Create" -> Icons.Default.Add
                            else -> Icons.Default.Person
                        }
                        NavigationBarItem(
                            selected = selected == item,
                            onClick = { selected = item },
                            icon = { Icon(icon, contentDescription = item) },
                            label = { Text(item) }
                        )
                    }
                }
            }
        ) { padding ->
            when (selected) {
                "Home" -> LazyColumn(Modifier.padding(padding).fillMaxSize().padding(horizontal = 14.dp)) {
                    item {
                        Text("Your feed", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(vertical = 16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("Your story", "Friends", "Explore").forEach { name ->
                                AssistChip(onClick = {}, label = { Text(name) })
                            }
                        }
                    }
                    items(posts, key = { it.id }) { post ->
                        PostCard(post = post, onLike = {
                            posts = posts.map { if (it.id == post.id) it.copy(liked = !it.liked, likes = it.likes + if (it.liked) -1 else 1) else it }
                        })
                    }
                }
                "Search" -> Column(Modifier.padding(padding).fillMaxSize().padding(20.dp)) {
                    Text("Discover", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = caption, onValueChange = { caption = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Search people and posts") }, singleLine = true)
                    Text("Search will connect to your community when backend services are added.", color = Color.Gray, modifier = Modifier.padding(top = 12.dp))
                }
                "Create" -> Column(Modifier.padding(padding).fillMaxSize().padding(20.dp)) {
                    Text("Create a post", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = caption, onValueChange = { caption = it }, modifier = Modifier.fillMaxWidth(), minLines = 4, label = { Text("What's happening?") })
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        if (caption.isNotBlank()) {
                            posts = listOf(Post((posts.maxOfOrNull { it.id } ?: 0) + 1, "You", "@you", caption.trim(), 0)) + posts
                            caption = ""
                            selected = "Home"
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Share post") }
                    Text("Posts are currently saved only while the app is open. Photo/video upload comes in a later version.", color = Color.Gray, modifier = Modifier.padding(top = 12.dp))
                }
                else -> Column(Modifier.padding(padding).fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(30.dp))
                    Box(Modifier.size(86.dp).background(Violet, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("Your profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text("@you", color = Color.Gray)
                    Spacer(Modifier.height(20.dp))
                    Text("Profile editing and account sign-in will be added with backend integration.", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun PostCard(post: Post, onLike: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 13.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).background(Violet.copy(alpha = .13f), CircleShape), contentAlignment = Alignment.Center) {
                    Text(post.author.take(1).uppercase(), color = Violet, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(post.author, fontWeight = FontWeight.Bold, color = Ink)
                    Text(post.handle, color = Color.Gray, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(post.caption, color = Ink, fontSize = 16.sp)
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onLike) {
                    Icon(Icons.Default.Favorite, contentDescription = "Like", tint = if (post.liked) Violet else Color.Gray)
                    Spacer(Modifier.width(5.dp))
                    Text("${post.likes} likes")
                }
                Spacer(Modifier.width(8.dp))
                Text("Comments", color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}
