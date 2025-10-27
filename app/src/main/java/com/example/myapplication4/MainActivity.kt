package com.example.myapplication4

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.myapplication4.ui.theme.MyApplication4Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class UserProfile(val email: String = "", val name: String = "", val role: String = "")

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted.")
        } else {
            Log.d("MainActivity", "Notification permission denied.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = Firebase.auth
        firestore = Firebase.firestore

        askNotificationPermission()

        setContent {
            MyApplication4Theme {
                AppContent(auth, firestore)
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun AppContent(auth: FirebaseAuth, firestore: FirebaseFirestore) {
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    // Listen to auth state changes
    DisposableEffect(auth) {
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(authListener)
        onDispose {
            auth.removeAuthStateListener(authListener)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (currentUser == null) {
            AuthScreen(
                onLogin = { email, password ->
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnFailureListener { e -> Log.w("Auth", "signIn:failure", e) }
                },
                onSignUp = { email, password ->
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { result ->
                            val newUser = result.user
                            newUser?.let {
                                val userProfile = hashMapOf("email" to it.email, "name" to "New User", "role" to "student")
                                firestore.collection("users").document(it.uid).set(userProfile)
                            }
                        }
                        .addOnFailureListener { e -> Log.w("Auth", "signUp:failure", e) }
                }
            )
        } else {
            LoggedInFlow(user = currentUser!!, firestore = firestore) {
                auth.signOut()
            }
        }
    }
}

@Composable
fun LoggedInFlow(user: FirebaseUser, firestore: FirebaseFirestore, onSignOut: () -> Unit) {
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }

    // Fetch user profile from Firestore
    LaunchedEffect(user) {
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    userProfile = document.toObject(UserProfile::class.java)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "get failed with ", exception)
            }
    }

    if (userProfile == null) {
        // Loading state while profile is being fetched
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LoggedInScreen(userProfile = userProfile!!, onSignOut = onSignOut)
    }
}

@Composable
fun LoggedInScreen(userProfile: UserProfile, onSignOut: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome, ${userProfile.name}", style = MaterialTheme.typography.headlineSmall)
        Text("Role: ${userProfile.role}", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(32.dp))

        // Role-based UI
        when (userProfile.role) {
            "student" -> {
                Button(onClick = { /* TODO: Navigate to grades screen */ }) {
                    Text("View My Grades")
                }
            }
            "teacher" -> {
                Button(onClick = { /* TODO: Navigate to manage grades screen */ }) {
                    Text("Manage Student Grades")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onSignOut) {
            Text("Sign Out")
        }
    }
}
