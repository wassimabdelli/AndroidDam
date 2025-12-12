package tn.esprit.dam.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import tn.esprit.dam.R
import android.app.Application
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.OutlinedTextField

import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tn.esprit.dam.data.AuthRepository
import tn.esprit.dam.data.RetrofitClient
import tn.esprit.dam.api.maillot.MaillotApiService
import tn.esprit.dam.api.maillot.JoueurMaillotDto
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.NumberPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMaillotScreen(
    navController: NavController,
    academieId: String,
    joueurId: String
) {
    val context = LocalContext.current
    var joueurInfo by remember { mutableStateOf<JoueurMaillotDto?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var numeroText by remember { mutableStateOf("") }
    var numeroSelected by remember { mutableStateOf<Int?>(null) }
    var submitMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(academieId, joueurId) {
        isLoading = true
        errorMessage = null
        try {
            val app = context.applicationContext as Application
            val repo = AuthRepository(app)
            val jwt = withContext(Dispatchers.IO) { repo.getToken() }
            val authHeader = if (!jwt.isNullOrBlank()) "Bearer $jwt" else ""
            val api = RetrofitClient.getRetrofit(context).create(MaillotApiService::class.java)
            val res = api.getJoueurMaillot(joueurId, academieId, authHeader)
            if (res.isSuccessful) {
                joueurInfo = res.body()
                numeroText = res.body()?.numero?.toString() ?: ""
                numeroSelected = res.body()?.numero
            } else {
                errorMessage = "Erreur ${res.code()}"
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Maillot", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!errorMessage.isNullOrBlank()) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            Box {
                Image(
                    painter = painterResource(id = R.drawable.editnumber),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                )
                val fullName = listOfNotNull(joueurInfo?.prenom, joueurInfo?.nom).joinToString(" ")
                Text(
                    text = if (fullName.isNotBlank()) fullName else "Chargement...",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = (joueurInfo?.numero?.toString() ?: "—"),
                    modifier = Modifier
                        .align(Alignment.Center),
                    fontSize = 95.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                factory = { ctx ->
                    NumberPicker(ctx).apply {
                        minValue = 0
                        maxValue = 99
                        wrapSelectorWheel = true
                        value = (numeroSelected ?: 0)
                        setOnValueChangedListener { _, _, newVal ->
                            numeroSelected = newVal
                            submitMessage = null
                            errorMessage = null
                            numeroText = newVal.toString()
                        }
                    }
                },
                update = {
                    it.value = (numeroSelected ?: 0)
                }
            )

            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                submitMessage = null
                errorMessage = null
                val numero = numeroSelected ?: numeroText.toIntOrNull()
                if (numero == null) {
                    errorMessage = "Veuillez saisir un numéro valide"
                } else {
                    scope.launch {
                        try {
                            val app = context.applicationContext as Application
                            val repo = AuthRepository(app)
                            val jwt = withContext(Dispatchers.IO) { repo.getToken() }
                            val authHeader = if (!jwt.isNullOrBlank()) "Bearer $jwt" else ""
                            val api = RetrofitClient.getRetrofit(context).create(MaillotApiService::class.java)
                            val res = api.updateMaillot(joueurId, academieId, numero, authHeader)
                            if (res.isSuccessful) {
                                submitMessage = "Numéro enregistré"
                                // Refresh
                                val refresh = api.getJoueurMaillot(joueurId, academieId, authHeader)
                                if (refresh.isSuccessful) {
                                    joueurInfo = refresh.body()
                                    numeroText = refresh.body()?.numero?.toString() ?: ""
                                }
                            } else {
                                errorMessage = "Erreur ${res.code()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message
                        }
                    }
                }
            }) {
                Text("Enregistrer")
            }
            if (!submitMessage.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(submitMessage!!, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
