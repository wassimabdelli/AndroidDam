package tn.esprit.dam.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path
import tn.esprit.dam.api.dto.CreateCoupeRequest

// DTO for GET /api/v1/coupes response
// We'll define CoupeDto in a moment

data class CoupeDto(
    val _id: String,
    val nom: String,
    val id_organisateur: OrganisateurDto,
    val participants: List<Any>,
    val matches: List<Any>,
    val date_debut: String,
    val date_fin: String,
    val categorie: String,
    val type: String,
    val tournamentName: String,
    val stadium: String,
    val date: String,
    val time: String,
    val maxParticipants: Int,
    val entryFee: Int?,
    val prizePool: Int?,
    val referee: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,
    val isBracketGenerated: Boolean = false
)

data class OrganisateurDto(
    val _id: String,
    val prenom: String?,
    val nom: String?
)

// DTO for GET /api/v1/users/{id}
data class UserDto(
    val _id: String,
    val prenom: String?,
    val nom: String?
)

// DTO for GET /api/v1/matches/{id}
data class MatchDto(
    val _id: String,
    val id_equipe1: String?,
    val id_equipe2: String?,
    val id_terrain: String?,
    val id_arbitre: String?,
    val date: String,
    val score_eq1: Int,
    val score_eq2: Int,
    val statut: String,
    val round: Int,
    val nextMatch: String?,
    val positionInNextMatch: String?,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)

data class UpdateMatchDto(
    val score_eq1: Int? = null,
    val score_eq2: Int? = null,
    val statut: String? = null,
    val id_equipe1: String? = null,
    val id_equipe2: String? = null
)

data class AddParticipantRequest(val userId: String)

// Data class for the response of generate-bracket
data class GenerateBracketResponse(
    val message: String,
    val matchesCount: Int
)

data class ArbitreExistsResponse(val exists: Boolean)

interface TournamentApiService {
    @Headers("Content-Type: application/json")
    @POST("create-coupe")
    suspend fun createCoupeWithAuth(
        @Body request: CreateCoupeRequest,
        @Header("Authorization") authHeader: String
    ): Response<Unit>

    @GET("coupes")
    suspend fun getCoupesWithAuth(
        @Header("Authorization") authHeader: String
    ): Response<List<CoupeDto>>

    @GET("users/{id}")
    suspend fun getUserByIdWithAuth(
        @Path("id") id: String,
        @Header("Authorization") authHeader: String
    ): Response<UserDto>

    @PATCH("add-participant/{id}")
    suspend fun addParticipantWithAuth(
        @Path("id") coupeId: String,
        @Body request: AddParticipantRequest,
        @Header("Authorization") authHeader: String
    ): Response<Unit>

    @POST("/api/v1/{id}/generate-bracket")
    suspend fun generateBracket(
        @Path("id") coupeId: String,
        @Header("Authorization") authHeader: String
    ): Response<GenerateBracketResponse>

    @GET("/api/v1/staff/exists/{idAcademie}/{idArbitre}")
    suspend fun isArbitreInAcademie(
        @Path("idAcademie") idAcademie: String,
        @Path("idArbitre") idArbitre: String,
        @Header("Authorization") authHeader: String
    ): Response<ArbitreExistsResponse>

    @GET("matches/{id}")
    suspend fun getMatchById(
        @Path("id") id: String,
        @Header("Authorization") authHeader: String
    ): Response<MatchDto>

    @PATCH("matches/{id}")
    suspend fun updateMatch(
        @Path("id") id: String,
        @Body updateMatchDto: UpdateMatchDto,
        @Header("Authorization") authHeader: String
    ): Response<Unit>
}
