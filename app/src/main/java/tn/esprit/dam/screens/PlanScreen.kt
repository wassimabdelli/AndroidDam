package tn.esprit.dam.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tn.esprit.dam.data.AuthRepository
import tn.esprit.dam.api.equipe.Categorie
import tn.esprit.dam.api.equipe.JoueurDto
import tn.esprit.dam.api.equipe.EquipeApiService
import tn.esprit.dam.data.RetrofitClient
import tn.esprit.dam.components.HomeBottomNavigationBar
import tn.esprit.dam.ui.theme.DAMTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import tn.esprit.dam.R

@Composable
fun PlanScreen(navController: NavHostController, userId: String, nom: String, prenom: String) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { HomeBottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        PlanContent(
            modifier = Modifier.padding(paddingValues),
            userId = userId,
            nom = nom,
            prenom = prenom
        )
    }
}

@Composable
fun PlanContent(modifier: Modifier = Modifier, userId: String, nom: String, prenom: String) {
    val scroll = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var categorie by remember { mutableStateOf(Categorie.MINIM) }
    var role by remember { mutableStateOf("starter") }
    var query by remember { mutableStateOf("") }
    var joueurs by remember { mutableStateOf<List<JoueurDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var joueursRemplacents by remember { mutableStateOf<List<JoueurDto>>(emptyList()) }
    var isLoadingRempl by remember { mutableStateOf(false) }
    var errorRempl by remember { mutableStateOf<String?>(null) }
    var selectedAId by remember { mutableStateOf<String?>(null) }
    var selectedBId by remember { mutableStateOf<String?>(null) }
    var isToggling by remember { mutableStateOf(false) }
    var toggleMessage by remember { mutableStateOf<String?>(null) }
    var catMenuExpanded by remember { mutableStateOf(false) }

    fun selectSwap(id: String?) {
        if (id == null) return
        when {
            selectedAId == null -> selectedAId = id
            selectedAId == id -> selectedAId = null
            selectedBId == null -> {
                selectedBId = id
                if (!isToggling && selectedAId != null && selectedBId != null) {
                    toggleMessage = null
                    scope.launch {
                        togglePlayers(
                            context,
                            userId,
                            selectedAId!!,
                            selectedBId!!,
                            categorie,
                            onState = { isToggling = it },
                            onError = { toggleMessage = it },
                            onSuccess = {
                                toggleMessage = "Changement effectué"
                                fetchPlayers(
                                    context, userId, categorie, role, "",
                                    onResult = { joueurs = it },
                                    onState = { isLoading = it },
                                    onError = { errorMessage = it }
                                )
                                fetchPlayers(
                                    context, userId, categorie, "substitute", "",
                                    onResult = { joueursRemplacents = it },
                                    onState = { isLoadingRempl = it },
                                    onError = { errorRempl = it }
                                )
                                selectedAId = null
                                selectedBId = null
                            }
                        )
                    }
                }
            }
            selectedBId == id -> selectedBId = null
            else -> { selectedAId = id; selectedBId = null }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // **Catégorie + Role**
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dropdown Menu catégorie
            OutlinedButton(onClick = { catMenuExpanded = true }) { Text(categorie.name) }
            DropdownMenu(
                expanded = catMenuExpanded,
                onDismissRequest = { catMenuExpanded = false }
            ) {
                listOf(Categorie.MINIM, Categorie.JUNIOR, Categorie.SENIOR).forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.name) },
                        onClick = {
                            categorie = cat
                            catMenuExpanded = false
                            scope.launch {
                                fetchPlayers(
                                    context, userId, categorie, role, "",
                                    onResult = { joueurs = it },
                                    onState = { isLoading = it },
                                    onError = { errorMessage = it }
                                )
                                fetchPlayers(
                                    context, userId, categorie, "substitute", "",
                                    onResult = { joueursRemplacents = it },
                                    onState = { isLoadingRempl = it },
                                    onError = { errorRempl = it }
                                )
                            }
                        }
                    )
                }
            }

            // FilterChip roles
            FilterChip(
                selected = role == "starter",
                onClick = {
                    role = "starter"
                    scope.launch {
                        fetchPlayers(
                            context, userId, categorie, role, "",
                            onResult = { joueurs = it },
                            onState = { isLoading = it },
                            onError = { errorMessage = it }
                        )
                    }
                },
                label = { Text("starter") }
            )
            FilterChip(
                selected = role == "substitute",
                onClick = {
                    role = "substitute"
                    scope.launch {
                        fetchPlayers(
                            context, userId, categorie, role, "",
                            onResult = { joueurs = it },
                            onState = { isLoading = it },
                            onError = { errorMessage = it }
                        )
                    }
                },
                label = { Text("substitutes") }
            )
        }

        // Remplaçants au-dessus du stade
        Text("substitutes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        when {
            isLoadingRempl -> CircularProgressIndicator()
            !errorRempl.isNullOrBlank() -> Text(errorRempl!!, color = MaterialTheme.colorScheme.error)
            joueursRemplacents.isEmpty() -> Text("Aucun remplaçant", color = MaterialTheme.colorScheme.outline)
            else -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    joueursRemplacents.take(4).forEach { j ->
                        PlayerBadge(
                            j,
                            Color(0xFF9E9E9E),
                            selected = (selectedAId == j._id || selectedBId == j._id),
                            onClick = { selectSwap(j._id) }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (toggleMessage != null) {
            Text(toggleMessage!!, color = if (toggleMessage == "Changement effectué") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))

        // **Carte stade et joueurs**
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .padding(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.planjeu),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )

                when {
                    isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    !errorMessage.isNullOrBlank() -> Text(
                        errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    joueurs.isEmpty() -> Text(
                        "Aucun joueur",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    else -> {
                        if (joueurs.size >= 8) {
                            val gk = joueurs[0]
                            val def = joueurs.slice(1..3)
                            val mid = joueurs.slice(4..6)
                            val fwd = joueurs[7]

                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) { PlayerBadge(gk, Color(0xFFFFD54F), selected = (selectedAId == gk._id || selectedBId == gk._id), onClick = { selectSwap(gk._id) }) }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) { def.forEach { p -> PlayerBadge(p, Color(0xFF42A5F5), selected = (selectedAId == p._id || selectedBId == p._id), onClick = { selectSwap(p._id) }) } }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) { mid.forEach { p -> PlayerBadge(p, Color(0xFF66BB6A), selected = (selectedAId == p._id || selectedBId == p._id), onClick = { selectSwap(p._id) }) } }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) { PlayerBadge(fwd, Color(0xFFEF5350), selected = (selectedAId == fwd._id || selectedBId == fwd._id), onClick = { selectSwap(fwd._id) }) }
                            }
                        } else {
                            Text(
                                "Équipe incomplète (min 8 joueurs requis)",
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // **Load initial players**
        LaunchedEffect(Unit) {
            fetchPlayers(
                context, userId, categorie, role, "",
                onResult = { joueurs = it },
                onState = { isLoading = it },
                onError = { errorMessage = it }
            )
            fetchPlayers(
                context, userId, categorie, "substitute", "",
                onResult = { joueursRemplacents = it },
                onState = { isLoadingRempl = it },
                onError = { errorRempl = it }
            )
        }

        Spacer(Modifier.height(50.dp))
    }
}

@Composable
fun PlayerBadge(joueur: JoueurDto?, color: Color, selected: Boolean = false, onClick: (() -> Unit)? = null) {
    if (joueur == null) return
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color)
                .border(if (selected) 3.dp else 2.dp, Color.White, CircleShape)
                .clickable(enabled = onClick != null) { onClick?.invoke() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            listOfNotNull(joueur.prenom).joinToString(" "),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp
        )
    }
}

private suspend fun fetchPlayers(
    context: android.content.Context,
    userId: String,
    categorie: Categorie,
    role: String,
    query: String,
    onResult: (List<JoueurDto>) -> Unit,
    onState: (Boolean) -> Unit,
    onError: (String?) -> Unit
) {
    onState(true)
    onError(null)
    try {
        val api = RetrofitClient.getRetrofit(context).create(EquipeApiService::class.java)
        val app = context.applicationContext as android.app.Application
        val repo = tn.esprit.dam.data.AuthRepository(app)
        val jwt = withContext(Dispatchers.IO) { repo.getToken() }
        val authHeader = if (!jwt.isNullOrBlank()) "Bearer $jwt" else ""
        val response = if (query.isNotBlank()) {
            api.searchJoueursTitulaireOuRemplacent(userId, categorie.name, query, authHeader)
        } else {
            api.getJoueursByRole(userId, categorie.name, role, authHeader)
        }

        if (response.isSuccessful) {
            onResult(response.body() ?: emptyList())
        } else {
            onError("Erreur ${response.code()}")
            onResult(emptyList())
        }
    } catch (e: Exception) {
        onError(e.message)
        onResult(emptyList())
    } finally {
        onState(false)
    }
}

private suspend fun togglePlayers(
    context: android.content.Context,
    idAcademie: String,
    idStarter: String,
    idSubstitute: String,
    categorie: Categorie,
    onState: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    onSuccess: suspend () -> Unit
) {
    onState(true)
    onError(null)
    try {
        val api = RetrofitClient.getRetrofit(context).create(EquipeApiService::class.java)
        val app = context.applicationContext as android.app.Application
        val repo = tn.esprit.dam.data.AuthRepository(app)
        val jwt = withContext(Dispatchers.IO) { repo.getToken() }
        val authHeader = if (!jwt.isNullOrBlank()) "Bearer $jwt" else ""
        val body = mapOf(
            "idStarter" to idStarter,
            "idSubstitute" to idSubstitute,
            "categorie" to categorie.name
        )
        val response = api.toggleTitulaireRemplacent(idAcademie, body, authHeader)
        if (response.isSuccessful) {
            onSuccess()
        } else {
            onError("Erreur ${response.code()}")
        }
    } catch (e: Exception) {
        onError(e.message)
    } finally {
        onState(false)
    }
}

@Preview(showBackground = true)
@Composable
fun PlanScreenPreview() {
    DAMTheme {
        PlanContent(userId = "demo-id", nom = "Doe", prenom = "John")
    }
}
