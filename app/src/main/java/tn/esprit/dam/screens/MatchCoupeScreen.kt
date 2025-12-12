package tn.esprit.dam.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tn.esprit.dam.api.MatchDto
import tn.esprit.dam.api.TournamentApiService
import tn.esprit.dam.api.UpdateMatchDto
import tn.esprit.dam.data.AuthRepository
import tn.esprit.dam.data.RetrofitClient
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchCoupeScreen(navController: NavHostController, coupeId: String, matches: List<Any>, onBackClick: () -> Unit) {
    var matchesDetails by remember { mutableStateOf<List<MatchDto>>(emptyList()) }
    var teamNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isArbitre by remember { mutableStateOf(false) }
    var coupeCategorie by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun refreshData() {
        coroutineScope.launch {
            val repo = AuthRepository(context.applicationContext as android.app.Application)
            val jwt = withContext(Dispatchers.IO) { repo.getToken() }
            val api = RetrofitClient.getRetrofit(context).create(TournamentApiService::class.java)

            val fetchedMatches = mutableListOf<MatchDto>()
            // When refreshing, we want to get the latest state of all matches in the bracket
            val allMatchIds = matches.mapNotNull { it as? String }

            val tempMatches = allMatchIds.mapNotNull { matchId ->
                val response = api.getMatchById(matchId, "Bearer $jwt")
                if (response.isSuccessful) response.body() else null
            }
            fetchedMatches.addAll(tempMatches)

            // Now, fetch names for any team ID we haven't seen before
            val allTeamIds = tempMatches.flatMap { listOfNotNull(it.id_equipe1, it.id_equipe2) }.distinct()
            val newTeamNames = teamNames.toMutableMap()
            for (teamId in allTeamIds) {
                if (!newTeamNames.containsKey(teamId)) {
                    val teamResponse = api.getUserByIdWithAuth(teamId, "Bearer $jwt")
                    if (teamResponse.isSuccessful) {
                        teamResponse.body()?.let { user ->
                            newTeamNames[teamId] = user.nom ?: "N/A"
                        }
                    }
                }
            }

            matchesDetails = fetchedMatches.sortedBy { it.round }
            teamNames = newTeamNames
        }
    }

    LaunchedEffect(Unit) {
        val repo = AuthRepository(context.applicationContext as android.app.Application)
        val user = withContext(Dispatchers.IO) { repo.getUser() }
        isArbitre = user?.role == "ARBITRE"
        refreshData()
        try {
            val jwt = withContext(Dispatchers.IO) { repo.getToken() }
            val api = RetrofitClient.getRetrofit(context).create(TournamentApiService::class.java)
            val coupesResponse = api.getCoupesWithAuth("Bearer $jwt")
            coupeCategorie = coupesResponse.body()?.find { it._id == coupeId }?.categorie
        } catch (_: Exception) { }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendrier du Tournoi") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (matchesDetails.isEmpty() && matches.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (matches.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Aucun match disponible pour le moment.")
            }
        } else {
            val groupedMatches = matchesDetails.groupBy { it.round }.toSortedMap()

            LazyRow(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(groupedMatches.entries.toList()) { (round, matchesInRound) ->
                    RoundColumn(
                        round = round,
                        matches = matchesInRound,
                        teamNames = teamNames,
                        isFinalRound = round == groupedMatches.keys.last(),
                        isArbitre = isArbitre,
                        onMatchClick = { match ->
                            val eq1 = match.id_equipe1 ?: ""
                            val eq2 = match.id_equipe2 ?: ""
                            if (isArbitre && match.statut != "TERMINE") {
                                navController.navigate("DetailMatch/${match._id}/$eq1/$eq2/${coupeCategorie ?: ""}")
                            } else {
                                navController.navigate("SeeMatch/${match._id}/$eq1/$eq2/${coupeCategorie ?: ""}")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RoundColumn(
    round: Int,
    matches: List<MatchDto>,
    teamNames: Map<String, String>,
    isFinalRound: Boolean,
    isArbitre: Boolean,
    onMatchClick: (MatchDto) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text(
            text = when {
                isFinalRound -> "Finale"
                matches.size == 2 -> "Demi-finales"
                else -> "Round $round"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        matches.forEach { match ->
            MatchCard(match = match, teamNames = teamNames, isArbitre = isArbitre, onMatchClick = onMatchClick)
        }
    }
}

@Composable
fun MatchCard(match: MatchDto, teamNames: Map<String, String>, isArbitre: Boolean, onMatchClick: (MatchDto) -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { onMatchClick(match) },
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                Text(text = teamNames[match.id_equipe1] ?: "Sera Programmé", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(text = match.score_eq1.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Divider()
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                Text(text = teamNames[match.id_equipe2] ?: "Sera Programmé", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(text = match.score_eq2.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EditMatchDialog(match: MatchDto, teamNames: Map<String, String>, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    var score1 by remember { mutableStateOf(match.score_eq1.toString()) }
    var score2 by remember { mutableStateOf(match.score_eq2.toString()) }
    var isUpdating by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!isUpdating) onDismiss() },
        title = { Text("Mettre à jour le score") },
        text = {
            Column {
                OutlinedTextField(
                    value = score1,
                    onValueChange = { score1 = it },
                    label = { Text(teamNames[match.id_equipe1] ?: "Équipe 1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isUpdating
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = score2,
                    onValueChange = { score2 = it },
                    label = { Text(teamNames[match.id_equipe2] ?: "Équipe 2") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isUpdating
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s1 = score1.toIntOrNull()
                    val s2 = score2.toIntOrNull()
                    if (s1 == null || s2 == null || s1 == s2) {
                        Toast.makeText(context, "Scores invalides ou match nul non autorisé", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isUpdating = true
                    coroutineScope.launch {
                        val repo = AuthRepository(context.applicationContext as android.app.Application)
                        val jwt = withContext(Dispatchers.IO) { repo.getToken() }
                        val api = RetrofitClient.getRetrofit(context).create(TournamentApiService::class.java)

                        // Step 1: Update the current match score and status
                        val currentMatchResponse = api.updateMatch(
                            id = match._id,
                            updateMatchDto = UpdateMatchDto(score_eq1 = s1, score_eq2 = s2, statut = "TERMINE"),
                            authHeader = "Bearer $jwt"
                        )

                        if (currentMatchResponse.isSuccessful) {
                            Toast.makeText(context, "Score enregistré. Qualification...", Toast.LENGTH_SHORT).show()

                            // Step 2: Determine winner and qualify for the next match
                            val winnerId = if (s1 > s2) match.id_equipe1 else match.id_equipe2
                            val nextMatchId = match.nextMatch
                            val position = match.positionInNextMatch

                            if (winnerId != null && nextMatchId != null && position != null) {
                                val nextMatchDto = if (position == "eq1") {
                                    UpdateMatchDto(id_equipe1 = winnerId)
                                } else {
                                    UpdateMatchDto(id_equipe2 = winnerId)
                                }

                                val nextMatchResponse = api.updateMatch(
                                    id = nextMatchId,
                                    updateMatchDto = nextMatchDto,
                                    authHeader = "Bearer $jwt"
                                )

                                if (nextMatchResponse.isSuccessful) {
                                    Toast.makeText(context, "Équipe qualifiée!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Erreur de qualification: ${nextMatchResponse.message()}", Toast.LENGTH_LONG).show()
                                }
                            }
                            
                            // Step 3: Refresh UI
                            onConfirm()
                        } else {
                            Toast.makeText(context, "Erreur de mise à jour: ${currentMatchResponse.message()}", Toast.LENGTH_LONG).show()
                        }
                        isUpdating = false
                    }
                },
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Confirmer")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!isUpdating) onDismiss() }, enabled = !isUpdating) {
                Text("Annuler")
            }
        }
    )
}
