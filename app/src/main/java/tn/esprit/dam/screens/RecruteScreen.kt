package tn.esprit.dam.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Groups
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
import kotlin.collections.map
import kotlin.collections.toSet
import kotlin.let
import kotlin.text.isBlank
import kotlin.text.isNotBlank

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecruteScreen(
    navController: NavController,
    viewModel: StaffViewModel = viewModel()
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val myStaff by viewModel.myStaff.collectAsState()
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    var searchQuery by remember { mutableStateOf("") }
    var idAcademie by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Get user ID and fetch the initial staff list
    LaunchedEffect(Unit) {
        val user = try {
            val app = context.applicationContext as Application
            AuthRepository(app).getUser()
        } catch (e: Exception) {
            null
        }

        user?._id?.let {
            idAcademie = it
            viewModel.fetchMyStaff(it)
        }
    }

    // Trigger search when query changes
    LaunchedEffect(searchQuery) {
        viewModel.searchArbitres(searchQuery)
    }

    // A set of my staff IDs for efficient lookup
    val myStaffIds = remember(myStaff) {
        myStaff.map { it.id }.toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chercher un Arbitre", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("MyStaffScreen") }) {
                        Icon(Icons.Default.Groups, "Mon Staff")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Rechercher par nom, prénom ou email...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search Icon") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading && searchQuery.isNotBlank() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    errorMessage != null -> {
                        Text(
                            "Erreur: $errorMessage",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    searchResults.isEmpty() && searchQuery.isNotBlank() -> {
                        Text(
                            "Aucun arbitre trouvé pour \"$searchQuery\".",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    searchQuery.isBlank() -> {
                        Text(
                            "Entrez un terme pour commencer la recherche.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                        ) {
                            items(searchResults, key = { it.id }) { arbitre ->
                                ArbitreCard(
                                    arbitre = arbitre,
                                    isAlreadyInStaff = arbitre.id in myStaffIds,
                                    onAddClick = {
                                        idAcademie?.let { academieId ->
                                            viewModel.addArbitreToMyStaff(academieId, arbitre.id)
                                        }
                                    }
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
private fun ArbitreCard(
    arbitre: Arbitre,
    isAlreadyInStaff: Boolean,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${arbitre.prenom} ${arbitre.nom}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = arbitre.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            if (isAlreadyInStaff) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Déjà dans le staff",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            } else {
                IconButton(onClick = onAddClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter au staff",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
