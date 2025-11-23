package tn.esprit.dam.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import tn.esprit.dam.data.AuthRepository
import tn.esprit.dam.models.Arbitre
import tn.esprit.dam.models.StaffViewModel
import kotlin.collections.filter
import kotlin.let
import kotlin.text.contains
import kotlin.text.isBlank

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStaffScreen(
    navController: NavController,
    viewModel: StaffViewModel = viewModel()
) {
    val myStaff by viewModel.myStaff.collectAsState()
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    var localSearchQuery by remember { mutableStateOf("") }
    var idAcademie by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    var arbitreToDelete by remember { mutableStateOf<Arbitre?>(null) }

    LaunchedEffect(Unit) {
        val user = try {
            val app = context.applicationContext as Application
            AuthRepository(app).getUser()
        } catch (e: Exception) {
            null
        }
        idAcademie = user?._id
    }

    LaunchedEffect(idAcademie) {
        idAcademie?.let { viewModel.fetchMyStaff(it) }
    }

    val filteredLocalArbitres = remember(localSearchQuery, myStaff) {
        if (localSearchQuery.isBlank()) {
            myStaff
        } else {
            myStaff.filter {
                val fullName = "${it.prenom} ${it.nom}"
                fullName.contains(localSearchQuery, ignoreCase = true) ||
                        it.email.contains(localSearchQuery, ignoreCase = true)
            }
        }
    }

    if (arbitreToDelete != null) {
        AlertDialog(
            onDismissRequest = { arbitreToDelete = null },
            title = { Text("Confirmer la suppression") },
            text = { Text("Voulez-vous vraiment retirer ${arbitreToDelete!!.prenom} ${arbitreToDelete!!.nom} de votre staff ?") },
            confirmButton = {
                Button(
                    onClick = {
                        idAcademie?.let {
                            viewModel.removeArbitreFromMyStaff(it, arbitreToDelete!!.id)
                        }
                        arbitreToDelete = null
                    }
                ) { Text("Supprimer") }
            },
            dismissButton = {
                Button(onClick = { arbitreToDelete = null }) { Text("Annuler") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon Staff", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = localSearchQuery,
                onValueChange = { localSearchQuery = it },
                label = { Text("Chercher dans mon staff...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search Icon") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    errorMessage != null -> Text(
                        "Erreur: $errorMessage",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    myStaff.isEmpty() && !isLoading -> Text(
                        "Votre staff est vide.",
                        modifier = Modifier.align(Alignment.Center)
                    )

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                        ) {
                            items(filteredLocalArbitres, key = { it.id }) { arbitre ->
                                ArbitreInfoCard(
                                    arbitre = arbitre,
                                    onDeleteClick = { arbitreToDelete = arbitre }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArbitreInfoCard(arbitre: Arbitre, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    "Profile Picture",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${arbitre.prenom} ${arbitre.nom}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    arbitre.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, "Supprimer", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
