package tn.esprit.dam.api.equipe
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Body
import retrofit2.http.HTTP

interface EquipeApiService {

    // Rechercher des joueurs dans une équipe avec un texte de recherche
    @GET("equipes/search-joueurs-equipe/{idAcademie}")
    suspend fun searchJoueursTitulaireOuRemplacent(
        @Path("idAcademie") idAcademie: String,
        @Query("categorie") categorie: String, // On peut aussi utiliser Categorie.name
        @Query("query") query: String,
        @Header("Authorization") auth: String
    ): Response<List<JoueurDto>>

    // Récupérer les joueurs selon leur rôle (titulaire / remplaçant)
    @GET("equipes/{idAcademie}/joueurs")
    suspend fun getJoueursByRole(
        @Path("idAcademie") idAcademie: String,
        @Query("categorie") categorie: String, // On peut envoyer Categorie.name
        @Query("role") role: String, // "titulaire" ou "remplacent"
        @Header("Authorization") auth: String
    ): Response<List<JoueurDto>>

    @PATCH("equipes/toggle-starter-substitute/{idAcademie}")
    suspend fun toggleTitulaireRemplacent(
        @Path("idAcademie") idAcademie: String,
        @Body body: Map<String, String>,
        @Header("Authorization") auth: String
    ): Response<Any>

    @GET("equipes/membres/{idAcademie}/{categorie}")
    suspend fun getMembres(
        @Path("idAcademie") idAcademie: String,
        @Path("categorie") categorie: String,
        @Header("Authorization") auth: String
    ): Response<List<MembreDto>>

    @GET("users/search/joueurs")
    suspend fun searchJoueurs(
        @Query("q") q: String,
        @Header("Authorization") auth: String
    ): Response<List<MembreDto>>

    @PATCH("equipes/add-joueur/{idAcademie}")
    suspend fun addJoueur(
        @Path("idAcademie") idAcademie: String,
        @Body body: Map<String, String>,
        @Header("Authorization") auth: String
    ): Response<Any>

    @HTTP(method = "DELETE", path = "equipes/remove-joueur/{idAcademie}/{idJoueur}", hasBody = true)
    suspend fun removeJoueur(
        @Path("idAcademie") idAcademie: String,
        @Path("idJoueur") idJoueur: String,
        @Body body: Map<String, String>,
        @Header("Authorization") auth: String
    ): Response<Any>
}

data class MembreDto(
    val _id: String,
    val nom: String?,
    val prenom: String?,
    val email: String?
)
