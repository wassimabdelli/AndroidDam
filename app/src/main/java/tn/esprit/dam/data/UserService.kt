package tn.esprit.dam.data

import retrofit2.http.GET
import retrofit2.http.Query
import tn.esprit.dam.models.Arbitre

/**
 * A dedicated service for user-related API calls, including searching for referees.
 */
interface UserService {

    /**
     * Searches for referees based on a query string.
     * @param query The search term for name, firstname, or email.
     * @return A list of [Arbitre] objects matching the query.
     */
    @GET("users/search/arbitres")
    suspend fun searchArbitres(@Query("q") query: String): List<Arbitre>

    @GET("users/search/coachs-arbitres")
    suspend fun searchCoachsArbitres(@Query("q") query: String): List<Arbitre>
}
