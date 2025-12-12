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

import tn.esprit.dam.models.Coach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStaffScreen(
    navController: NavController,
    viewModel: StaffViewModel = viewModel()
) {
    val myStaff by viewModel.myStaff.collectAsState()
    val myCoaches by viewModel.myCoaches.collectAsState()
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    var localSearchQuery by remember { mutableStateOf("") }
    var idAcademie by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    var arbitreToDelete by remember { mutableStateOf<Arbitre?>(null) }
    var coachToDelete by remember { mutableStateOf<Coach?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 for Arbitres, 1 for Coaches

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

    val filteredLocalCoaches = remember(localSearchQuery, myCoaches) {
        if (localSearchQuery.isBlank()) {
            myCoaches
        } else {
            myCoaches.filter {
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

    if (coachToDelete != null) {
        AlertDialog(
            onDismissRequest = { coachToDelete = null },
            title = { Text("Confirmer la suppression") },
            text = { Text("Voulez-vous vraiment retirer ${coachToDelete!!.prenom} ${coachToDelete!!.nom} de votre staff ?") },
            confirmButton = {
                Button(
                    onClick = {
                        idAcademie?.let {
                            val app = context.applicationContext as Application
                            viewModel.removeCoachFromMyStaff(app, it, coachToDelete!!.id)
                        }
                        coachToDelete = null
                    }
                ) { Text("Supprimer") }
            },
            dismissButton = {
                Button(onClick = { coachToDelete = null }) { Text("Annuler") }
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
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Arbitres") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Coachs") }
                )
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                        else -> {
                            if (selectedTab == 0) {
                                if (filteredLocalArbitres.isEmpty()) {
                                    Text(
                                        "Aucun arbitre trouvé.",
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                } else {
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
                            } else {
                                if (filteredLocalCoaches.isEmpty()) {
                                    Text(
                                        "Aucun coach trouvé.",
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                } else {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                                    ) {
                                        items(filteredLocalCoaches, key = { it.id }) { coach ->
                                            CoachInfoCard(
                                                coach = coach,
                                                onDeleteClick = { coachToDelete = coach }
                                            )
                                        }
                                    }
                                }
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
        shape = RoundedCornerShape(12.dp),
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

@Composable
private fun CoachInfoCard(coach: Coach, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = coach.prenom.take(1).uppercase() + coach.nom.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${coach.prenom} ${coach.nom}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    coach.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    coach.role,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, "Supprimer", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
