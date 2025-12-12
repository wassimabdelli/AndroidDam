package tn.esprit.dam.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tn.esprit.dam.api.MatchDto
import tn.esprit.dam.api.TournamentApiService
import tn.esprit.dam.api.UserDto
import tn.esprit.dam.data.AuthRepository
import tn.esprit.dam.data.RetrofitClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeeMatchScreen(
    navController: NavController,
    matchId: String,
    eq1Id: String,
    eq2Id: String,
    coupeCategorie: String? = null
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var match by remember { mutableStateOf<MatchDto?>(null) }
    var teamNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var scorersTeam1 by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var scorersTeam2 by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var yellowCardsTeam1 by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var yellowCardsTeam2 by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var redCardsTeam1 by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var redCardsTeam2 by remember { mutableStateOf<List<UserDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        val app = context.applicationContext as Application
        val repo = AuthRepository(app)
        val jwt = withContext(Dispatchers.IO) { repo.getToken() }
        val api = RetrofitClient.getRetrofit(context).create(TournamentApiService::class.java)

        val resMatch = api.getMatchById(matchId, "Bearer $jwt")
        if (resMatch.isSuccessful) {
            match = resMatch.body()
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

        if (match != null) {
            val auth = "Bearer $jwt"
            val acad1 = if (eq1Id.isNotBlank()) eq1Id else (match!!.id_equipe1 ?: "")
            val acad2 = if (eq2Id.isNotBlank()) eq2Id else (match!!.id_equipe2 ?: "")
            runCatching { api.getScorersByAcademie(matchId, acad1, auth) }.getOrNull()?.let {
                if (it.isSuccessful) scorersTeam1 = it.body() ?: emptyList()
            }
            runCatching { api.getScorersByAcademie(matchId, acad2, auth) }.getOrNull()?.let {
                if (it.isSuccessful) scorersTeam2 = it.body() ?: emptyList()
            }
            runCatching { api.getCardsByAcademie(matchId, acad1, "yellow", auth) }.getOrNull()?.let {
                if (it.isSuccessful) yellowCardsTeam1 = it.body() ?: emptyList()
            }
            runCatching { api.getCardsByAcademie(matchId, acad2, "yellow", auth) }.getOrNull()?.let {
                if (it.isSuccessful) yellowCardsTeam2 = it.body() ?: emptyList()
            }
            runCatching { api.getCardsByAcademie(matchId, acad1, "red", auth) }.getOrNull()?.let {
                if (it.isSuccessful) redCardsTeam1 = it.body() ?: emptyList()
            }
            runCatching { api.getCardsByAcademie(matchId, acad2, "red", auth) }.getOrNull()?.let {
                if (it.isSuccessful) redCardsTeam2 = it.body() ?: emptyList()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails du Match", fontWeight = FontWeight.Bold) },
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
                Text("Catégorie: ${coupeCategorie ?: ""}", fontWeight = FontWeight.SemiBold)

                ScoreViewerCard(
                    teamName = teamNames[match!!.id_equipe1] ?: "Équipe 1",
                    score = match!!.score_eq1
                )

                ScoreViewerCard(
                    teamName = teamNames[match!!.id_equipe2] ?: "Équipe 2",
                    score = match!!.score_eq2
                )

                StatusViewerCard(statut = match!!.statut)

                StatBlockCard(
                    title = "Corners",
                    leftLabel = teamNames[match!!.id_equipe1] ?: "Équipe 1",
                    leftValue = match!!.corner_eq1,
                    rightLabel = teamNames[match!!.id_equipe2] ?: "Équipe 2",
                    rightValue = match!!.corner_eq2
                )

                StatBlockCard(
                    title = "Penalties",
                    leftLabel = teamNames[match!!.id_equipe1] ?: "Équipe 1",
                    leftValue = match!!.penalty_eq1,
                    rightLabel = teamNames[match!!.id_equipe2] ?: "Équipe 2",
                    rightValue = match!!.penalty_eq2
                )

                PlayersListCard(
                    title = "Buteurs",
                    leftLabel = teamNames[match!!.id_equipe1] ?: "Équipe 1",
                    rightLabel = teamNames[match!!.id_equipe2] ?: "Équipe 2",
                    leftPlayers = scorersTeam1,
                    rightPlayers = scorersTeam2,
                    leftColor = Color(0xFFEA4335),
                    rightColor = Color(0xFF4285F4)
                )

                PlayersListCard(
                    title = "Cartons Jaunes",
                    leftLabel = teamNames[match!!.id_equipe1] ?: "Équipe 1",
                    rightLabel = teamNames[match!!.id_equipe2] ?: "Équipe 2",
                    leftPlayers = yellowCardsTeam1,
                    rightPlayers = yellowCardsTeam2,
                    leftColor = MaterialTheme.colorScheme.tertiary,
                    rightColor = MaterialTheme.colorScheme.tertiary
                )

                PlayersListCard(
                    title = "Cartons Rouges",
                    leftLabel = teamNames[match!!.id_equipe1] ?: "Équipe 1",
                    rightLabel = teamNames[match!!.id_equipe2] ?: "Équipe 2",
                    leftPlayers = redCardsTeam1,
                    rightPlayers = redCardsTeam2,
                    leftColor = Color(0xFFD32F2F),
                    rightColor = Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
private fun ScoreViewerCard(teamName: String, score: Int) {
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = score.toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StatusViewerCard(statut: String) {
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
                Text(text = statut, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun StatBlockCard(
    title: String,
    leftLabel: String,
    leftValue: Int,
    rightLabel: String,
    rightValue: Int
) {
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
                Text(title, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(leftLabel, fontWeight = FontWeight.SemiBold)
                    Text(leftValue.toString(), style = MaterialTheme.typography.headlineSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(rightLabel, fontWeight = FontWeight.SemiBold)
                    Text(rightValue.toString(), style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}

@Composable
private fun PlayersListCard(
    title: String,
    leftLabel: String,
    rightLabel: String,
    leftPlayers: List<UserDto>,
    rightPlayers: List<UserDto>,
    leftColor: Color,
    rightColor: Color
) {
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
                Text(title, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(leftLabel, fontWeight = FontWeight.SemiBold, color = leftColor)
                    Spacer(Modifier.height(8.dp))
                    if (leftPlayers.isEmpty()) {
                        Text("Aucun", color = MaterialTheme.colorScheme.outline)
                    } else {
                        leftPlayers.forEach { p ->
                            Text(listOfNotNull(p.prenom, p.nom).joinToString(" "))
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(rightLabel, fontWeight = FontWeight.SemiBold, color = rightColor)
                    Spacer(Modifier.height(8.dp))
                    if (rightPlayers.isEmpty()) {
                        Text("Aucun", color = MaterialTheme.colorScheme.outline)
                    } else {
                        rightPlayers.forEach { p ->
                            Text(listOfNotNull(p.prenom, p.nom).joinToString(" "))
                        }
                    }
                }
            }
        }
    }
}
