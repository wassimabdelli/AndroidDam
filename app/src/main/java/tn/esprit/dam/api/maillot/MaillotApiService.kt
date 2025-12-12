package tn.esprit.dam.api.maillot

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface MaillotApiService {
    @GET("maillots/joueur")
    suspend fun getJoueurMaillot(
        @Query("idJoueur") idJoueur: String,
        @Query("idAcademie") idAcademie: String,
        @Header("Authorization") auth: String
    ): Response<JoueurMaillotDto>

    @POST("maillots/assign")
    suspend fun assignMaillot(
        @Query("idJoueur") idJoueur: String,
        @Query("idAcademie") idAcademie: String,
        @Query("numero") numero: Int,
        @Header("Authorization") auth: String
    ): Response<Any>

    @PATCH("maillots/update")
    suspend fun updateMaillot(
        @Query("idJoueur") idJoueur: String,
        @Query("idAcademie") idAcademie: String,
        @Query("numero") numero: Int,
        @Header("Authorization") auth: String
    ): Response<Any>
}

data class JoueurMaillotDto(
    val nom: String?,
    val prenom: String?,
    val email: String?,
    val numero: Int?
)

