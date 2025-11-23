package tn.esprit.dam.data

import tn.esprit.dam.models.Arbitre

/**
 * Repository for user-related operations, primarily for searching referees.
 */
class UserRepository {

    private val userService: UserService = RetrofitClient.userService

    /**
     * Searches for referees via the backend API.
     * @param query The search term.
     * @return A list of matching [Arbitre] objects.
     */
    suspend fun searchArbitres(query: String): List<Arbitre> {
        return userService.searchArbitres(query)
    }
}
