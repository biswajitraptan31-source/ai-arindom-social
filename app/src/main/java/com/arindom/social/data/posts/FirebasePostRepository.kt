package com.arindom.social.data.posts

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class CloudPost(
    val id: String = "",
    val authorUid: String = "",
    val caption: String = "",
    val mediaUrl: String? = null,
    val createdAt: Timestamp? = null
)

class FirebasePostRepository(context: Context) {
    private val appContext = context.applicationContext
    private val configured: Boolean
        get() = FirebaseApp.getApps(appContext).isNotEmpty()

    fun publishTextPost(authorUid: String, caption: String, callback: (Result<String>) -> Unit) {
        if (!configured) {
            callback(Result.failure(IllegalStateException("Firebase configuration is missing.")))
            return
        }
        val data = hashMapOf(
            "authorUid" to authorUid,
            "caption" to caption,
            "mediaUrl" to null,
            "createdAt" to Timestamp.now()
        )
        FirebaseFirestore.getInstance().collection("posts").add(data)
            .addOnSuccessListener { callback(Result.success(it.id)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun observeRecentPosts(
        onPosts: (List<CloudPost>) -> Unit,
        onError: (Throwable) -> Unit
    ): (() -> Unit)? {
        if (!configured) {
            onError(IllegalStateException("Firebase configuration is missing."))
            return null
        }
        val registration = FirebaseFirestore.getInstance()
            .collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().map { document ->
                    CloudPost(
                        id = document.id,
                        authorUid = document.getString("authorUid").orEmpty(),
                        caption = document.getString("caption").orEmpty(),
                        mediaUrl = document.getString("mediaUrl"),
                        createdAt = document.getTimestamp("createdAt")
                    )
                }
                onPosts(items)
            }
        return { registration.remove() }
    }
}
