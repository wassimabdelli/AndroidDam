package tn.esprit.dam.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Header
import tn.esprit.dam.api.equipe.Categorie
import tn.esprit.dam.api.equipe.EquipeApiService
import tn.esprit.dam.api.equipe.MembreDto
import tn.esprit.dam.data.AuthRepository
import tn.esprit.dam.data.RetrofitClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Negotiation(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var idAcademie by remember { mutableStateOf<String?>(null) }
    var categorie by remember { mutableStateOf(Categorie.MINIM) }
    var catMenuExpanded by remember { mutableStateOf(false) }

    var membres by remember { mutableStateOf<List<MembreDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<MembreDto>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }

    var isAdding by remember { mutableStateOf(false) }
    var addError by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isRemoving by remember { mutableStateOf(false) }
    var removeError by remember { mutableStateOf<String?>(null) }

    val memberIds = remember(membres) { membres.map { it._id }.toSet() }

    LaunchedEffect(Unit) {
        val app = context.applicationContext as Application
        val user = try { AuthRepository(app).getUser() } catch (e: Exception) { null }
        idAcademie = user?._id
        if (idAcademie != null) {
            scope.launch {
                fetchMembres(context, idAcademie!!, categorie, onResult = { membres = it }, onState = { isLoading = it }, onError = { errorMessage = it })
            }
        }
    }

    LaunchedEffect(categorie, idAcademie) {
        if (idAcademie != null) {
            fetchMembres(context, idAcademie!!, categorie, onResult = { membres = it }, onState = { isLoading = it }, onError = { errorMessage = it })
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            searchResults = emptyList()
            searchError = null
        } else {
            fetchSearch(context, searchQuery, onResult = { searchResults = it }, onState = { isSearching = it }, onError = { searchError = it })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Négociation", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Rechercher des joueurs...") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                singleLine = true
            )

            if (searchQuery.isNotBlank()) {
                Text("Résultats de recherche", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                when {
                    isSearching -> CircularProgressIndicator()
                    !searchError.isNullOrBlank() -> Text(searchError!!, color = MaterialTheme.colorScheme.error)
                    searchResults.isEmpty() -> Text("Aucun joueur trouvé", color = MaterialTheme.colorScheme.outline)
                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(searchResults, key = { it._id }) { m ->
                                ListItem(
                                    headlineContent = { Text(listOfNotNull(m.prenom, m.nom).joinToString(" ")) },
                                    supportingContent = { Text(m.email ?: "") },
                                    leadingContent = { Icon(Icons.Filled.Person, contentDescription = null) },
                                    trailingContent = {
                                        if (memberIds.contains(m._id)) {
                                            Icon(Icons.Filled.Check, contentDescription = "Déjà dans l'équipe", tint = MaterialTheme.colorScheme.primary)
                                        } else {
                                            Button(
                                                enabled = !isAdding && idAcademie != null,
                                                onClick = {
                                                    scope.launch {
                                                        addError = null
                                                        if (idAcademie != null) {
                                                            fetchAddJoueur(
                                                                context = context,
                                                                idAcademie = idAcademie!!,
                                                                idJoueur = m._id,
                                                                categorie = categorie,
                                                                onState = { isAdding = it },
                                                                onError = { addError = it }
                                                            )
                                                            if (addError == null) {
                                                                showSuccessDialog = true
                                                                fetchMembres(context, idAcademie!!, categorie, onResult = { membres = it }, onState = { isLoading = it }, onError = { errorMessage = it })
                                                            }
                                                        }
                                                    }
                                                }
                                            ) { Text("Ajouter") }
                                        }
                                    }
                                )
                                Divider()
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                if (showSuccessDialog) {
                    AlertDialog(
                        onDismissRequest = { showSuccessDialog = false },
                        title = { Text("Succès") },
                        text = { Text("Le joueur a été ajouté avec succès.") },
                        confirmButton = {
                            TextButton(onClick = { showSuccessDialog = false }) { Text("OK") }
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { catMenuExpanded = true }) { Text(categorie.name) }
                DropdownMenu(expanded = catMenuExpanded, onDismissRequest = { catMenuExpanded = false }) {
                    listOf(Categorie.SENIOR, Categorie.JUNIOR, Categorie.MINIM).forEach { cat ->
                        DropdownMenuItem(text = { Text(cat.name) }, onClick = {
                            categorie = cat
                            catMenuExpanded = false
                        })
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Mes joueurs", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    !errorMessage.isNullOrBlank() -> Text(errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                    membres.isEmpty() -> Text("Aucun membre", color = MaterialTheme.colorScheme.outline, modifier = Modifier.align(Alignment.Center))
                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(membres, key = { it._id }) { m ->
                                ListItem(
                                    headlineContent = { Text(listOfNotNull(m.prenom, m.nom).joinToString(" ")) },
                                    supportingContent = { Text(m.email ?: "") },
                                    leadingContent = { Icon(Icons.Filled.Person, contentDescription = null) },
                                    trailingContent = {
                                        Row {
                                            IconButton(enabled = idAcademie != null, onClick = {
                                                idAcademie?.let { academie ->
                                                    navController.navigate("EditMaillot/$academie/${m._id}")
                                                }
                                            }) {
                                                Icon(Icons.Filled.Edit, contentDescription = "Modifier maillot")
                                            }
                                            IconButton(
                                                enabled = !isRemoving && idAcademie != null,
                                                onClick = {
                                                    if (idAcademie != null) {
                                                        scope.launch {
                                                            removeError = null
                                                            fetchRemoveJoueur(
                                                                context = context,
                                                                idAcademie = idAcademie!!,
                                                                idJoueur = m._id,
                                                                categorie = categorie,
                                                                onState = { isRemoving = it },
                                                                onError = { removeError = it }
                                                            )
                                                            if (removeError == null) {
                                                                fetchMembres(context, idAcademie!!, categorie, onResult = { membres = it }, onState = { isLoading = it }, onError = { errorMessage = it })
                                                            }
                                                        }
                                                    }
                                                }
                                            ) { Icon(Icons.Filled.Delete, contentDescription = "Supprimer du effectif") }
                                        }
                                    }
                                )
                                Divider()
                            }
                        }
                    }
                }
            }
        }
    }
}





private suspend fun fetchMembres(
    context: android.content.Context,
    idAcademie: String,
    categorie: Categorie,
    onResult: (List<MembreDto>) -> Unit,
    onState: (Boolean) -> Unit,
    onError: (String?) -> Unit
) {
    onState(true)
    onError(null)
    try {
        val api = RetrofitClient.getRetrofit(context).create(EquipeApiService::class.java)
        val app = context.applicationContext as Application
        val repo = AuthRepository(app)
        val jwt = withContext(Dispatchers.IO) { repo.getToken() }
        val authHeader = if (!jwt.isNullOrBlank()) "Bearer $jwt" else ""
        val res = api.getMembres(idAcademie, categorie.name, authHeader)
        if (res.isSuccessful) onResult(res.body() ?: emptyList()) else onError("Erreur ${res.code()}")
    } catch (e: Exception) {
        onError(e.message)
        onResult(emptyList())
    } finally {
        onState(false)
    }
}

private suspend fun fetchSearch(
    context: android.content.Context,
    query: String,
    onResult: (List<MembreDto>) -> Unit,
    onState: (Boolean) -> Unit,
    onError: (String?) -> Unit
) {
    onState(true)
    onError(null)
    try {
        val api = RetrofitClient.getRetrofit(context).create(EquipeApiService::class.java)
        val app = context.applicationContext as Application
        val repo = AuthRepository(app)
        val jwt = withContext(Dispatchers.IO) { repo.getToken() }
        val authHeader = if (!jwt.isNullOrBlank()) "Bearer $jwt" else ""
        val res = api.searchJoueurs(query, authHeader)
        if (res.isSuccessful) onResult(res.body() ?: emptyList()) else onError("Erreur ${res.code()}")
    } catch (e: Exception) {
        onError(e.message)
        onResult(emptyList())
    } finally {
        onState(false)
    }
}

private suspend fun fetchAddJoueur(
    context: android.content.Context,
    idAcademie: String,
    idJoueur: String,
    categorie: Categorie,
    onState: (Boolean) -> Unit,
    onError: (String?) -> Unit
) {
    onState(true)
    onError(null)
    try {
        val api = RetrofitClient.getRetrofit(context).create(EquipeApiService::class.java)
        val app = context.applicationContext as Application
        val repo = AuthRepository(app)
        val jwt = withContext(Dispatchers.IO) { repo.getToken() }
        val authHeader = if (!jwt.isNullOrBlank()) "Bearer $jwt" else ""
        val body = mapOf("idJoueur" to idJoueur, "categorie" to categorie.name)
        val res = api.addJoueur(idAcademie, body, authHeader)
        if (!res.isSuccessful) onError("Erreur ${res.code()}")
    } catch (e: Exception) {
        onError(e.message)
    } finally {
        onState(false)
    }
}

private suspend fun fetchRemoveJoueur(
    context: android.content.Context,
    idAcademie: String,
    idJoueur: String,
    categorie: Categorie,
    onState: (Boolean) -> Unit,
    onError: (String?) -> Unit
) {
    onState(true)
    onError(null)
    try {
        val api = RetrofitClient.getRetrofit(context).create(EquipeApiService::class.java)
        val app = context.applicationContext as Application
        val repo = AuthRepository(app)
        val jwt = withContext(Dispatchers.IO) { repo.getToken() }
        val authHeader = if (!jwt.isNullOrBlank()) "Bearer $jwt" else ""
        val body = mapOf("categorie" to categorie.name)
        val res = api.removeJoueur(idAcademie, idJoueur, body, authHeader)
        if (!res.isSuccessful) onError("Erreur ${res.code()}")
    } catch (e: Exception) {
        onError(e.message)
    } finally {
        onState(false)
    }
}

