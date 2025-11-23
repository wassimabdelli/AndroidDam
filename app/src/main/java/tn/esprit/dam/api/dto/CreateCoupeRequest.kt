package tn.esprit.dam.api.dto

// If you use Gson, make sure the dependency is in your build.gradle:
// implementation 'com.google.code.gson:gson:2.10.1'
// implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

import com.google.gson.annotations.SerializedName

// Data class for the create-coupe API request
// Matches the backend CreateCoupeDto structure

data class CreateCoupeRequest(
    @SerializedName("nom") val nom: String,
    @SerializedName("matches") val matches: List<String> = emptyList(),
    @SerializedName("date_debut") val dateDebut: String,
    @SerializedName("date_fin") val dateFin: String,
    @SerializedName("tournamentName") val tournamentName: String,
    @SerializedName("stadium") val stadium: String,
    @SerializedName("date") val date: String,
    @SerializedName("time") val time: String,
    @SerializedName("maxParticipants") val maxParticipants: Int,
    @SerializedName("referee") val referee: List<String>,
    @SerializedName("participants") val participants: List<String>? = null,
    @SerializedName("entryFee") val entryFee: Int? = null,
    @SerializedName("prizePool") val prizePool: Int? = null,
    @SerializedName("statut") val statut: String? = null,
    @SerializedName("categorie") val categorie: String,
    @SerializedName("type") val type: String
)
