package com.example.myapplication4

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
            val context = LocalContext.current
            AuthScreen(
                onLogin = { email, password ->
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnFailureListener { e ->
                            Log.w("Auth", "signIn:failure", e)
                            val errorMessage = "Login Failed: ${e::class.java.simpleName}"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
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
                        .addOnFailureListener { e ->
                            Log.w("Auth", "signUp:failure", e)
                            val errorMessage = "Sign Up Failed: ${e::class.java.simpleName}"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Welcome, ${userProfile.name}", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Default.Person, contentDescription = "Role Icon")
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Your Role", style = MaterialTheme.typography.bodySmall)
                    Text(userProfile.role.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("Dashboard", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        when (userProfile.role) {
            "student" -> {
                ActionCard(
                    title = "View My Grades",
                    icon = Icons.Default.Face, 
                    onClick = { /* TODO: Navigate to grades screen */ }
                )
            }
            "teacher" -> {
                ActionCard(
                    title = "Manage Student Grades",
                    icon = Icons.Default.Face, // Consider a different icon
                    onClick = { /* TODO: Navigate to manage grades screen */ }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
            Text("Sign Out")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(24.dp))
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
    }
}
