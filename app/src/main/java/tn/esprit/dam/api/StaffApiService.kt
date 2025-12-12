package tn.esprit.dam.api


import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Header
import retrofit2.Response
import tn.esprit.dam.models.AddArbitreRequest
import tn.esprit.dam.models.Arbitre
import tn.esprit.dam.models.Coach
import tn.esprit.dam.models.CoachExistsResponse
import tn.esprit.dam.models.StaffUpdateResponse

interface StaffApiService {

    @PATCH("staff/add-arbitre/{idAcademie}")
    suspend fun addArbitre(
        @Path("idAcademie") idAcademie: String,
        @Body request: AddArbitreRequest
    ): StaffUpdateResponse // Use updated response model

    @GET("staff/arbitres/{idAcademie}")
    suspend fun getArbitres(@Path("idAcademie") idAcademie: String): List<Arbitre>

    @GET("staff/coachs/{idAcademie}")
    suspend fun getCoachs(@Path("idAcademie") idAcademie: String): List<Coach>

    @DELETE("staff/remove-arbitre/{idAcademie}/{idArbitre}")
    suspend fun removeArbitre(
        @Path("idAcademie") idAcademie: String,
        @Path("idArbitre") idArbitre: String
    ): StaffUpdateResponse // Use updated response model

    @POST("staff/{idAcademie}/coach/{idCoach}")
    suspend fun addCoach(
        @Path("idAcademie") idAcademie: String,
        @Path("idCoach") idCoach: String,
        @Header("Authorization") authHeader: String
    ): Response<Unit>

    @DELETE("staff/{idAcademie}/coach/{idCoach}")
    suspend fun removeCoach(
        @Path("idAcademie") idAcademie: String,
        @Path("idCoach") idCoach: String,
        @Header("Authorization") authHeader: String
    ): Response<Unit>

    @GET("staff/{idAcademie}/coach/{idCoach}/check")
    suspend fun isCoachInAcademie(
        @Path("idAcademie") idAcademie: String,
        @Path("idCoach") idCoach: String,
        @Header("Authorization") authHeader: String
    ): Response<CoachExistsResponse>
}
