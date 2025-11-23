package tn.esprit.dam.models

import com.google.gson.annotations.SerializedName

data class Arbitre(
    @SerializedName("_id") val id: String,
    val nom: String,
    val prenom: String,
    val picture: String?,
    val email: String,
    val role: String
)

// Response for GET requests, expects full Arbitre objects
data class StaffResponse(
    @SerializedName("id_academie") val idAcademie: String,
    @SerializedName("id_arbitres") val idArbitres: List<Arbitre>
)

// Specific response for PATCH/DELETE, which returns a list of IDs (Strings)
data class StaffUpdateResponse(
    @SerializedName("id_academie") val idAcademie: String,
    @SerializedName("id_arbitres") val idArbitres: List<String>
)


data class AddArbitreRequest(
    @SerializedName("idArbitre") val idArbitre: String
)
