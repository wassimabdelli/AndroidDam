package tn.esprit.dam.screens

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tn.esprit.dam.api.MatchDto
import tn.esprit.dam.api.TournamentApiService
import tn.esprit.dam.api.UpdateMatchDto
import tn.esprit.dam.data.AuthRepository
import tn.esprit.dam.data.RetrofitClient
import tn.esprit.dam.api.equipe.EquipeApiService
import tn.esprit.dam.api.equipe.JoueurDto
import tn.esprit.dam.api.AddStatRequest
import tn.esprit.dam.api.AddCartonRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailMatchScreen(
    navController: NavController,
    matchId: String,
    eq1Id: String,
    eq2Id: String,
    coupeCategorie: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var match by remember { mutableStateOf<MatchDto?>(null) }
    var teamNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isArbitre by remember { mutableStateOf(false) }

    var score1 by remember { mutableStateOf("") }
    var score2 by remember { mutableStateOf("") }
    var statut by remember { mutableStateOf("PROGRAMME") }
    var isLoading by remember { mutableStateOf(false) }
    var isUpdatingCorner by remember { mutableStateOf(false) }
    var isUpdatingPenalty by remember { mutableStateOf(false) }
    var showTeam1Dialog by remember { mutableStateOf(false) }
    var showTeam2Dialog by remember { mutableStateOf(false) }
    var playersTeam1 by remember { mutableStateOf<List<JoueurDto>>(emptyList()) }
    var playersTeam2 by remember { mutableStateOf<List<JoueurDto>>(emptyList()) }
    var isLoadingPlayersTeam1 by remember { mutableStateOf(false) }
    var isLoadingPlayersTeam2 by remember { mutableStateOf(false) }
    var errorPlayersTeam1 by remember { mutableStateOf<String?>(null) }
    var errorPlayersTeam2 by remember { mutableStateOf<String?>(null) }
    var selectedPlayerTeam1Id by remember { mutableStateOf<String?>(null) }
    var selectedPlayerTeam2Id by remember { mutableStateOf<String?>(null) }
    var isSubmittingStatTeam1 by remember { mutableStateOf(false) }
    var isSubmittingStatTeam2 by remember { mutableStateOf(false) }
    var submitErrorTeam1 by remember { mutableStateOf<String?>(null) }
    var submitErrorTeam2 by remember { mutableStateOf<String?>(null) }
    var selectedStatType by remember { mutableStateOf("but") }
    var selectedCardColor by remember { mutableStateOf("yellow") }

    LaunchedEffect(Unit) {
        val app = context.applicationContext as Application
        val repo = AuthRepository(app)
        val user = withContext(Dispatchers.IO) { repo.getUser() }
        isArbitre = user?.role == "ARBITRE"
        val jwt = withContext(Dispatchers.IO) { repo.getToken() }
        val api = RetrofitClient.getRetrofit(context).create(TournamentApiService::class.java)

        val resMatch = api.getMatchById(matchId, "Bearer $jwt")
        if (resMatch.isSuccessful) {
            val m = resMatch.body()
            match = m
            if (m != null) {
                score1 = m.score_eq1.toString()
                score2 = m.score_eq2.toString()
                statut = m.statut
            }
        }

        val names = mutableMapOf<String, String>()
        if (eq1Id.isNotBlank()) {
            val r1 = api.getUserByIdWithAuth(eq1Id, "Bearer $jwt")
            if (r1.isSuccessful) names[eq1Id] = r1.body()?.nom ?: "Équipe 1"
        }
        if (eq2Id.isNotBlank()) {
            val r2 = api.getUserByIdWithAuth(eq2Id, "Bearer $jwt")
            if (r2.isSuccessful) names[eq2Id] = r2.body()?.nom ?: "Équipe 2"
        }
        teamNames = names
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détail du Match", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        if (match == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Catégorie: ${coupeCategorie ?: "TEST"}", fontWeight = FontWeight.SemiBold)
                ScoreInputCard(
                    teamName = teamNames[match!!.id_equipe1] ?: "Équipe 1",
                    score = if (isArbitre) score1 else "${match!!.score_eq1}",
                    editable = isArbitre,
                    onChange = { score1 = it }
                )

                Spacer(Modifier.height(8.dp))

                ScoreInputCard(
                    teamName = teamNames[match!!.id_equipe2] ?: "Équipe 2",
                    score = if (isArbitre) score2 else "${match!!.score_eq2}",
                    editable = isArbitre,
                    onChange = { score2 = it }
                )

                Spacer(Modifier.height(8.dp))

                Text("Statut: ${match!!.statut}")
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Statut du match", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = match!!.statut, style = MaterialTheme.typography.titleMedium)
                            if (isArbitre) {
                                var expanded by remember { mutableStateOf(false) }
                                Box {
                                    OutlinedButton(onClick = { expanded = true }) { Text(statut) }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        listOf("PROGRAMME", "EN_COURS", "TERMINE").forEach { s ->
                                            DropdownMenuItem(text = { Text(s) }, onClick = { statut = s; expanded = false })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Corners", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(teamNames[match!!.id_equipe1] ?: "Équipe 1", fontWeight = FontWeight.SemiBold)
                                Text("${match!!.corner_eq1}", style = MaterialTheme.typography.headlineSmall)
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    enabled = isArbitre && !isUpdatingCorner && eq1Id.isNotBlank(),
                                    onClick = {
                                        scope.launch {
                                            isUpdatingCorner = true
                                            try {
                                                val app = context.applicationContext as Application
                                                val repo = AuthRepository(app)
                                                val jwt = withContext(Dispatchers.IO) { repo.getToken() }
                                                val api = RetrofitClient.getRetrofit(context).create(TournamentApiService::class.java)
                                                val res = api.incrementCorner(matchId, eq1Id, "Bearer $jwt")
                                                if (res.isSuccessful) match = res.body() else Toast.makeText(context, "Erreur: ${res.message()}", Toast.LENGTH_LONG).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, e.message ?: "Erreur", Toast.LENGTH_LONG).show()
                                            } finally {
                                                isUpdatingCorner = false
                                            }
                                        }
                                    }
                                ) { Text("+1") }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(teamNames[match!!.id_equipe2] ?: "Équipe 2", fontWeight = FontWeight.SemiBold)
                                Text("${match!!.corner_eq2}", style = MaterialTheme.typography.headlineSmall)
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    enabled = isArbitre && !isUpdatingCorner && eq2Id.isNotBlank(),
                                    onClick = {
                                        scope.launch {
                                            isUpdatingCorner = true
                                            try {
                                                val app = context.applicationContext as Application
                                                val repo = AuthRepository(app)
                                                val jwt = withContext(Dispatchers.IO) { repo.getToken() }
                                                val api = RetrofitClient.getRetrofit(context).create(TournamentApiService::class.java)
                                                val res = api.incrementCorner(matchId, eq2Id, "Bearer $jwt")
                                                if (res.isSuccessful) match = res.body() else Toast.makeText(context, "Erreur: ${res.message()}", Toast.LENGTH_LONG).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, e.message ?: "Erreur", Toast.LENGTH_LONG).show()
                                            } finally {
                                                isUpdatingCorner = false
                                            }
                                        }
                                    }
                                ) { Text("+1") }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Penalties", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(teamNames[match!!.id_equipe1] ?: "Équipe 1", fontWeight = FontWeight.SemiBold)
                                Text("${match!!.penalty_eq1}", style = MaterialTheme.typography.headlineSmall)
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    enabled = isArbitre && !isUpdatingPenalty && eq1Id.isNotBlank(),
                                    onClick = {
                                        scope.launch {
                                            isUpdatingPenalty = true
                                            try {
                                                val app = context.applicationContext as Application
                                                val repo = AuthRepository(app)
                                                val jwt = withContext(Dispatchers.IO) { repo.getToken() }
                                                val api = RetrofitClient.getRetrofit(context).create(TournamentApiService::class.java)
                                                val res = api.incrementPenalty(matchId, eq1Id, "Bearer $jwt")
                                                if (res.isSuccessful) match = res.body() else Toast.makeText(context, "Erreur: ${res.message()}", Toast.LENGTH_LONG).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, e.message ?: "Erreur", Toast.LENGTH_LONG).show()
                                            } finally {
                                                isUpdatingPenalty = false
                                            }
                                        }
                                    }
                                ) { Text("+1") }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(teamNames[match!!.id_equipe2] ?: "Équipe 2", fontWeight = FontWeight.SemiBold)
                                Text("${match!!.penalty_eq2}", style = MaterialTheme.typography.headlineSmall)
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    enabled = isArbitre && !isUpdatingPenalty && eq2Id.isNotBlank(),
                                    onClick = {
                                        scope.launch {
                                            isUpdatingPenalty = true
                                            try {
                                                val app = context.applicationContext as Application
                                                val repo = AuthRepository(app)
                                                val jwt = withContext(Dispatchers.IO) { repo.getToken() }
                                                val api = RetrofitClient.getRetrofit(context).create(TournamentApiService::class.java)
                                                val res = api.incrementPenalty(matchId, eq2Id, "Bearer $jwt")
                                                if (res.isSuccessful) match = res.body() else Toast.makeText(context, "Erreur: ${res.message()}", Toast.LENGTH_LONG).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, e.message ?: "Erreur", Toast.LENGTH_LONG).show()
                                            } finally {
                                                isUpdatingPenalty = false
                                            }
                                        }
                                    }
                                ) { Text("+1") }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Stats", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(selected = selectedStatType == "but", onClick = { selectedStatType = "but" }, label = { Text("But") })
                            FilterChip(selected = selectedStatType == "assist", onClick = { selectedStatType = "assist" }, label = { Text("Assist") })
                            FilterChip(selected = selectedStatType == "offside", onClick = { selectedStatType = "offside" }, label = { Text("Offside") })
                            FilterChip(selected = selectedStatType == "carton", onClick = { selectedStatType = "carton" }, label = { Text("Carton") })
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                        enabled = eq1Id.isNotBlank(),
                        onClick = {
                            showTeam1Dialog = true
                            scope.launch {
                                isLoadingPlayersTeam1 = true
                                errorPlayersTeam1 = null
                                try {
                                    val app = context.applicationContext as Application
                                    val repo = AuthRepository(app)
                                    val jwt = withContext(Dispatchers.IO) { repo.getToken() }
                                    val api = RetrofitClient.getRetrofit(context).create(EquipeApiService::class.java)
                                    val authHeader = "Bearer $jwt"
                                    val cat = coupeCategorie ?: "SENIOR"
                                    val res = api.getJoueursByRole(eq1Id, cat, "starter", authHeader)
                                    playersTeam1 = if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
                                    if (!res.isSuccessful) errorPlayersTeam1 = "Erreur ${res.code()}"
                                } catch (e: Exception) {
                                    errorPlayersTeam1 = e.message
                                } finally {
                                    isLoadingPlayersTeam1 = false
                                }
                            }
                        }
                    ) { Text("Choisir joueur (Eq1)") }
                            Button(
                        enabled = eq2Id.isNotBlank(),
                        onClick = {
                            showTeam2Dialog = true
                            scope.launch {
                                isLoadingPlayersTeam2 = true
                                errorPlayersTeam2 = null
                                try {
                                    val app = context.applicationContext as Application
                                    val repo = AuthRepository(app)
                                    val jwt = withContext(Dispatchers.IO) { repo.getToken() }
                                    val api = RetrofitClient.getRetrofit(context).create(EquipeApiService::class.java)
                                    val authHeader = "Bearer $jwt"
                                    val cat = coupeCategorie ?: "SENIOR"
                                    val res = api.getJoueursByRole(eq2Id, cat, "starter", authHeader)
                                    playersTeam2 = if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
                                    if (!res.isSuccessful) errorPlayersTeam2 = "Erreur ${res.code()}"
                                } catch (e: Exception) {
                                    errorPlayersTeam2 = e.message
                                } finally {
                                    isLoadingPlayersTeam2 = false
                                }
                            }
                        }
                    ) { Text("Choisir joueur (Eq2)") }
                        }
                    }
                }

                if (showTeam1Dialog) {
                    AlertDialog(
                        onDismissRequest = { showTeam1Dialog = false },
                        title = { Text("Titulaires Équipe 1") },
                        text = {
                            when {
                                isLoadingPlayersTeam1 -> CircularProgressIndicator()
                                errorPlayersTeam1 != null -> Text(errorPlayersTeam1 ?: "")
                                playersTeam1.isEmpty() -> Text("Aucun joueur")
                                else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    playersTeam1.take(8).forEach { j ->
                                        val id = j._id ?: ""
                                        OutlinedButton(onClick = { selectedPlayerTeam1Id = id }, enabled = !isSubmittingStatTeam1) {
                                            Text(listOfNotNull(j.prenom, j.nom).joinToString(" ") + if (selectedPlayerTeam1Id == id) " ✓" else "")
                                        }
                                    }
                                    if (selectedStatType == "carton") {
                                        Spacer(Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            FilterChip(selected = selectedCardColor == "yellow", onClick = { selectedCardColor = "yellow" }, label = { Text("Jaune") })
                                            FilterChip(selected = selectedCardColor == "red", onClick = { selectedCardColor = "red" }, label = { Text("Rouge") })
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val sel = selectedPlayerTeam1Id
                                            if (sel.isNullOrBlank()) return@launch
                                            isSubmittingStatTeam1 = true
                                            submitErrorTeam1 = null
                                            try {
                                                val app = context.applicationContext as Application
                                                val repo = AuthRepository(app)
                                                val jwt = withContext(Dispatchers.IO) { repo.getToken() }
                                                val api = RetrofitClient.getRetrofit(context).create(TournamentApiService::class.java)
                                                val cat = coupeCategorie ?: "SENIOR"
                                                when (selectedStatType) {
                                                    "but", "assist" -> {
                                                        val res = api.addStatToMatch(matchId, AddStatRequest(idJoueur = sel, equipe = "eq1", type = selectedStatType), "Bearer $jwt")
                                                        if (res.isSuccessful) {
                                                            match = res.body()
                                                            Toast.makeText(context, "${selectedStatType.uppercase()} ajouté", Toast.LENGTH_SHORT).show()
                                                            showTeam1Dialog = false
                                                            selectedPlayerTeam1Id = null
                                                        } else {
                                                            submitErrorTeam1 = "Erreur ${res.code()}"
                                                            Toast.makeText(context, submitErrorTeam1, Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                    "offside" -> {
                                                        val res = api.addOffside(matchId, eq1Id, sel, "Bearer $jwt")
                                                        if (res.isSuccessful) {
                                                            match = res.body()
                                                            Toast.makeText(context, "Offside ajouté", Toast.LENGTH_SHORT).show()
                                                            showTeam1Dialog = false
                                                            selectedPlayerTeam1Id = null
                                                        } else {
                                                            submitErrorTeam1 = "Erreur ${res.code()}"
                                                            Toast.makeText(context, submitErrorTeam1, Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                    "carton" -> {
                                                        val res = api.addCartonToMatch(matchId, AddCartonRequest(idJoueur = sel, categorie = cat, color = selectedCardColor), "Bearer $jwt")
                                                        if (res.isSuccessful) {
                                                            match = res.body()
                                                            Toast.makeText(context, "Carton ${selectedCardColor}", Toast.LENGTH_SHORT).show()
                                                            showTeam1Dialog = false
                                                            selectedPlayerTeam1Id = null
                                                        } else {
                                                            submitErrorTeam1 = "Erreur ${res.code()}"
                                                            Toast.makeText(context, submitErrorTeam1, Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                submitErrorTeam1 = e.message
                                                Toast.makeText(context, e.message ?: "Erreur", Toast.LENGTH_LONG).show()
                                            } finally {
                                                isSubmittingStatTeam1 = false
                                            }
                                        }
                                    },
                                    enabled = selectedPlayerTeam1Id != null && !isSubmittingStatTeam1
                                ) { Text("Ajouter") }
                                TextButton(onClick = { showTeam1Dialog = false }) { Text("Fermer") }
                            }
                        }
                    )
                }

                if (showTeam2Dialog) {
                    AlertDialog(
                        onDismissRequest = { showTeam2Dialog = false },
                        title = { Text("Titulaires Équipe 2") },
                        text = {
                            when {
                                isLoadingPlayersTeam2 -> CircularProgressIndicator()
                                errorPlayersTeam2 != null -> Text(errorPlayersTeam2 ?: "")
                                playersTeam2.isEmpty() -> Text("Aucun joueur")
                                else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    playersTeam2.take(8).forEach { j ->
                                        val id = j._id ?: ""
                                        OutlinedButton(onClick = { selectedPlayerTeam2Id = id }, enabled = !isSubmittingStatTeam2) {
                                            Text(listOfNotNull(j.prenom, j.nom).joinToString(" ") + if (selectedPlayerTeam2Id == id) " ✓" else "")
                                        }
                                    }
                                    if (selectedStatType == "carton") {
                                        Spacer(Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            FilterChip(selected = selectedCardColor == "yellow", onClick = { selectedCardColor = "yellow" }, label = { Text("Jaune") })
                                            FilterChip(selected = selectedCardColor == "red", onClick = { selectedCardColor = "red" }, label = { Text("Rouge") })
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val sel = selectedPlayerTeam2Id
                                            if (sel.isNullOrBlank()) return@launch
                                            isSubmittingStatTeam2 = true
                                            submitErrorTeam2 = null
                                            try {
                                                val app = context.applicationContext as Application
                                                val repo = AuthRepository(app)
                                                val jwt = withContext(Dispatchers.IO) { repo.getToken() }
                                                val api = RetrofitClient.getRetrofit(context).create(TournamentApiService::class.java)
                                                val cat = coupeCategorie ?: "SENIOR"
                                                when (selectedStatType) {
                                                    "but", "assist" -> {
                                                        val res = api.addStatToMatch(matchId, AddStatRequest(idJoueur = sel, equipe = "eq2", type = selectedStatType), "Bearer $jwt")
                                                        if (res.isSuccessful) {
                                                            match = res.body()
                                                            Toast.makeText(context, "${selectedStatType.uppercase()} ajouté", Toast.LENGTH_SHORT).show()
                                                            showTeam2Dialog = false
                                                            selectedPlayerTeam2Id = null
                                                        } else {
                                                            submitErrorTeam2 = "Erreur ${res.code()}"
                                                            Toast.makeText(context, submitErrorTeam2, Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                    "offside" -> {
                                                        val res = api.addOffside(matchId, eq2Id, sel, "Bearer $jwt")
                                                        if (res.isSuccessful) {
                                                            match = res.body()
                                                            Toast.makeText(context, "Offside ajouté", Toast.LENGTH_SHORT).show()
                                                            showTeam2Dialog = false
                                                            selectedPlayerTeam2Id = null
                                                        } else {
                                                            submitErrorTeam2 = "Erreur ${res.code()}"
                                                            Toast.makeText(context, submitErrorTeam2, Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                    "carton" -> {
                                                        val res = api.addCartonToMatch(matchId, AddCartonRequest(idJoueur = sel, categorie = cat, color = selectedCardColor), "Bearer $jwt")
                                                        if (res.isSuccessful) {
                                                            match = res.body()
                                                            Toast.makeText(context, "Carton ${selectedCardColor}", Toast.LENGTH_SHORT).show()
                                                            showTeam2Dialog = false
                                                            selectedPlayerTeam2Id = null
                                                        } else {
                                                            submitErrorTeam2 = "Erreur ${res.code()}"
                                                            Toast.makeText(context, submitErrorTeam2, Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                submitErrorTeam2 = e.message
                                                Toast.makeText(context, e.message ?: "Erreur", Toast.LENGTH_LONG).show()
                                            } finally {
                                                isSubmittingStatTeam2 = false
                                            }
                                        }
                                    },
                                    enabled = selectedPlayerTeam2Id != null && !isSubmittingStatTeam2
                                ) { Text("Ajouter") }
                                TextButton(onClick = { showTeam2Dialog = false }) { Text("Fermer") }
                            }
                        }
                    )
                }

                if (isArbitre) {
                    Button(
                        enabled = !isLoading,
                        onClick = {
                            scope.launch {
                                val s1 = score1.toIntOrNull()
                                val s2 = score2.toIntOrNull()
                                if (s1 == null || s2 == null) {
                                    Toast.makeText(context, "Scores invalides", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                if (statut == "TERMINE" && s1 == s2) {
                                    Toast.makeText(context, "Match nul non autorisé pour qualification", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                isLoading = true
                                try {
                                    val app = context.applicationContext as Application
                                    val repo = AuthRepository(app)
                                    val jwt = withContext(Dispatchers.IO) { repo.getToken() }
                                    val api = RetrofitClient.getRetrofit(context).create(TournamentApiService::class.java)

                                    val updateRes = api.updateMatch(
                                        id = matchId,
                                        updateMatchDto = UpdateMatchDto(score_eq1 = s1, score_eq2 = s2, statut = statut),
                                        authHeader = "Bearer $jwt"
                                    )
                                    if (!updateRes.isSuccessful) {
                                        Toast.makeText(context, "Erreur de mise à jour: ${updateRes.message()}", Toast.LENGTH_LONG).show()
                                        isLoading = false
                                        return@launch
                                    }

                                    if (statut == "TERMINE") {
                                        val winnerId = if (s1 > s2) match!!.id_equipe1 else match!!.id_equipe2
                                        val nextMatchId = match!!.nextMatch
                                        val position = match!!.positionInNextMatch
                                        if (!winnerId.isNullOrBlank() && !nextMatchId.isNullOrBlank() && !position.isNullOrBlank()) {
                                            val nextDto = if (position == "eq1") {
                                                UpdateMatchDto(id_equipe1 = winnerId)
                                            } else {
                                                UpdateMatchDto(id_equipe2 = winnerId)
                                            }
                                            val promoteRes = api.updateMatch(nextMatchId, nextDto, "Bearer $jwt")
                                            if (!promoteRes.isSuccessful) {
                                                Toast.makeText(context, "Erreur de qualification", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Qualification validée", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }

                                    val refreshed = api.getMatchById(matchId, "Bearer $jwt")
                                    if (refreshed.isSuccessful) {
                                        match = refreshed.body()
                                    }
                                    Toast.makeText(context, "Enregistré", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, e.message ?: "Erreur", Toast.LENGTH_LONG).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Valider")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreInputCard(
    teamName: String,
    score: String,
    editable: Boolean,
    onChange: (String) -> Unit
) {
    val headerGradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(headerGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = teamName,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(12.dp))
            if (editable) {
                OutlinedTextField(
                    value = score,
                    onValueChange = { s -> onChange(s.filter { it.isDigit() }) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Score") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        IconButton(onClick = {
                            val v = score.toIntOrNull() ?: 0
                            onChange((v - 1).coerceAtLeast(0).toString())
                        }) {
                            Icon(Icons.Filled.Remove, contentDescription = null)
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            val v = score.toIntOrNull() ?: 0
                            onChange((v + 1).toString())
                        }) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                        }
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = score,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
