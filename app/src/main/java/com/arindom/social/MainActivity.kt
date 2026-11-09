package com.arindom.social

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

private val Ink = Color(0xFF17152B)
private val Violet = Color(0xFF7357F6)
private val Surface = Color(0xFFF6F5FA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SocialApp() }
    }
}

data class Post(
    val id: Int,
    val author: String,
    val handle: String,
    val caption: String,
    val likes: Int,
    val liked: Boolean = false,
    val mediaUri: Uri? = null,
    val mediaType: String? = null
)

@Composable
fun SocialApp() {
    var selected by remember { mutableStateOf("Home") }
    var caption by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    var selectedMedia by remember { mutableStateOf<Uri?>(null) }
    var selectedMediaType by remember { mutableStateOf<String?>(null) }
    var posts by remember {
        mutableStateOf(listOf(
            Post(1, "Arindom", "@arindom", "Welcome to AI ARINDOM SOCIAL! ✨", 128),
            Post(2, "Creative Studio", "@creative", "Share your ideas, photos and videos.", 86),
            Post(3, "Community", "@community", "Your social space starts here. 👋", 54)
        ))
    }
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { selectedMedia = uri; selectedMediaType = "image" }
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { selectedMedia = uri; selectedMediaType = "video" }
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
                    listOf("Home", "Search", "Create", "Reels", "Profile").forEach { item ->
                        val icon = when (item) {
                            "Home" -> Icons.Default.Home
                            "Search" -> Icons.Default.Search
                            "Create" -> Icons.Default.Add
                            "Reels" -> Icons.Default.PlayArrow
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
                            listOf("＋ Your story", "Friends", "Explore").forEach { name ->
                                AssistChip(onClick = { if (name.contains("story")) selected = "Create" }, label = { Text(name) })
                            }
                        }
                        Text("Latest posts", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(vertical = 10.dp))
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
                    OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Search people and posts") }, singleLine = true)
                    Spacer(Modifier.height(12.dp))
                    posts.filter { it.author.contains(search, true) || it.caption.contains(search, true) }.forEach {
                        Text("${it.author}  •  ${it.caption}", modifier = Modifier.padding(vertical = 8.dp), color = Ink)
                    }
                }
                "Create" -> Column(Modifier.padding(padding).fillMaxSize().padding(20.dp)) {
                    Text("Create a post", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = caption, onValueChange = { caption = it }, modifier = Modifier.fillMaxWidth(), minLines = 4, label = { Text("Write a caption...") })
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { imagePicker.launch("image/*") }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Image, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Photo")
                        }
                        OutlinedButton(onClick = { videoPicker.launch("video/*") }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Video")
                        }
                    }
                    selectedMedia?.let { uri ->
                        Spacer(Modifier.height(12.dp))
                        Text("Selected ${selectedMediaType ?: "media"}", color = Violet, fontWeight = FontWeight.SemiBold)
                        MediaPreview(uri = uri, type = selectedMediaType ?: "image", autoplay = false)
                        TextButton(onClick = { selectedMedia = null; selectedMediaType = null }) { Text("Remove media") }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        if (caption.isNotBlank() || selectedMedia != null) {
                            posts = listOf(Post((posts.maxOfOrNull { it.id } ?: 0) + 1, "You", "@you", caption.trim(), 0, mediaUri = selectedMedia, mediaType = selectedMediaType)) + posts
                            caption = ""
                            selectedMedia = null
                            selectedMediaType = null
                            selected = "Home"
                        } else {
                            Toast.makeText(context, "Write a caption or choose a photo/video", Toast.LENGTH_SHORT).show()
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Share post") }
                    Text("Choose a photo or video from your phone. Posts are stored only in this app session for now.", color = Color.Gray, modifier = Modifier.padding(top = 12.dp), fontSize = 12.sp)
                }
                "Reels" -> {
                    val videos = posts.filter { it.mediaType == "video" && it.mediaUri != null }
                    if (videos.isEmpty()) {
                        Column(Modifier.padding(padding).fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Violet, modifier = Modifier.size(64.dp))
                            Text("Your Reels", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Text("No videos yet. Create a post and choose a video to see it here.", color = Color.Gray)
                            Spacer(Modifier.height(14.dp))
                            Button(onClick = { selected = "Create" }) { Text("Create video post") }
                        }
                    } else {
                        LazyColumn(Modifier.padding(padding).fillMaxSize()) {
                            items(videos, key = { it.id }) { post ->
                                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text("${post.author}  ${post.handle}", fontWeight = FontWeight.Bold, color = Ink)
                                    MediaPreview(uri = post.mediaUri!!, type = "video", autoplay = true)
                                    Text(post.caption, modifier = Modifier.padding(8.dp), color = Ink)
                                }
                            }
                        }
                    }
                }
                else -> Column(Modifier.padding(padding).fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(30.dp))
                    Box(Modifier.size(86.dp).background(Violet.copy(alpha = .13f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Violet, modifier = Modifier.size(48.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("Your profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text("@you", color = Color.Gray)
                    Spacer(Modifier.height(20.dp))
                    Text("${posts.size} posts", color = Ink, fontWeight = FontWeight.SemiBold)
                    Text("Account sign-in, following and cloud profiles need backend integration.", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun MediaPreview(uri: Uri, type: String, autoplay: Boolean) {
    val context = LocalContext.current
    if (type == "video") {
        val player = remember(uri) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                playWhenReady = autoplay
                repeatMode = ExoPlayer.REPEAT_MODE_ONE
            }
        }
        DisposableEffect(player) { onDispose { player.release() } }
        AndroidView(
            factory = { ctx -> PlayerView(ctx).apply { this.player = player; useController = true } },
            update = { it.player = player },
            modifier = Modifier.fillMaxWidth().height(260.dp).background(Color.Black, RoundedCornerShape(14.dp))
        )
    } else {
        AndroidView(
            factory = { ctx -> ImageView(ctx).apply { scaleType = ImageView.ScaleType.CENTER_CROP; setImageURI(uri) } },
            update = { it.setImageURI(uri) },
            modifier = Modifier.fillMaxWidth().height(260.dp).background(Color.LightGray, RoundedCornerShape(14.dp))
        )
    }
}

@Composable
fun PostCard(post: Post, onLike: () -> Unit) {
    val context = LocalContext.current
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
            if (post.caption.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(post.caption, color = Ink, fontSize = 16.sp)
            }
            post.mediaUri?.let { uri ->
                Spacer(Modifier.height(12.dp))
                MediaPreview(uri = uri, type = post.mediaType ?: "image", autoplay = false)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onLike) {
                    Icon(Icons.Default.Favorite, contentDescription = "Like", tint = if (post.liked) Violet else Color.Gray)
                    Spacer(Modifier.width(5.dp))
                    Text("${post.likes} likes")
                }
                TextButton(onClick = { Toast.makeText(context, "Comments will be available with accounts and backend.", Toast.LENGTH_SHORT).show() }) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comment")
                    Spacer(Modifier.width(4.dp)); Text("Comment")
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "${post.author}: ${post.caption}") }
                    context.startActivity(Intent.createChooser(send, "Share post"))
                }) { Icon(Icons.Default.Send, contentDescription = "Share", tint = Violet) }
            }
        }
    }
}
