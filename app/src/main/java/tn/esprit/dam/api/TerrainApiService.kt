package tn.esprit.dam.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import tn.esprit.dam.models.CreateTerrainRequest
import tn.esprit.dam.models.Terrain

/**
 * Retrofit API service for Terrain (Stadium) endpoints
 */
interface TerrainApiService {
    
    @GET("terrains/academie/{academieId}")
    suspend fun getTerrainsByAcademieId(
        @Path("academieId") academieId: String,
        @Header("Authorization") authHeader: String
    ): Response<List<Terrain>>
    
    @POST("terrains")
    suspend fun createTerrain(
        @Body request: CreateTerrainRequest,
        @Header("Authorization") authHeader: String
    ): Response<Terrain>
}
