package tn.esprit.dam.api


import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import tn.esprit.dam.models.AddArbitreRequest
import tn.esprit.dam.models.Arbitre
import tn.esprit.dam.models.StaffUpdateResponse

interface StaffApiService {

    @PATCH("staff/add-arbitre/{idAcademie}")
    suspend fun addArbitre(
        @Path("idAcademie") idAcademie: String,
        @Body request: AddArbitreRequest
    ): StaffUpdateResponse // Use updated response model

    @GET("staff/arbitres/{idAcademie}")
    suspend fun getArbitres(@Path("idAcademie") idAcademie: String): List<Arbitre>

    @DELETE("staff/remove-arbitre/{idAcademie}/{idArbitre}")
    suspend fun removeArbitre(
        @Path("idAcademie") idAcademie: String,
        @Path("idArbitre") idArbitre: String
    ): StaffUpdateResponse // Use updated response model
}