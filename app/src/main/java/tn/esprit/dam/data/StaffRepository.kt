package tn.esprit.dam.data

import tn.esprit.dam.api.StaffApiService
import tn.esprit.dam.models.AddArbitreRequest
import tn.esprit.dam.models.Arbitre
import tn.esprit.dam.models.Coach
import tn.esprit.dam.models.StaffUpdateResponse
import tn.esprit.dam.data.AuthRepository
import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StaffRepository {

    private val staffApiService: StaffApiService = RetrofitClient.staffApiService

    suspend fun addArbitre(idAcademie: String, idArbitre: String): StaffUpdateResponse {
        val request = AddArbitreRequest(idArbitre = idArbitre)
        return staffApiService.addArbitre(idAcademie, request)
    }

    suspend fun getArbitres(idAcademie: String): List<Arbitre> {
        return staffApiService.getArbitres(idAcademie)
    }

    suspend fun getCoachs(idAcademie: String): List<Coach> {
        return staffApiService.getCoachs(idAcademie)
    }

    suspend fun removeArbitre(idAcademie: String, idArbitre: String): StaffUpdateResponse {
        return staffApiService.removeArbitre(idAcademie, idArbitre)
    }

    suspend fun addCoach(app: Application, idAcademie: String, idCoach: String) {
        val jwt = withContext(Dispatchers.IO) { AuthRepository(app).getToken() }
        staffApiService.addCoach(idAcademie, idCoach, "Bearer $jwt")
    }

    suspend fun removeCoach(app: Application, idAcademie: String, idCoach: String) {
        val jwt = withContext(Dispatchers.IO) { AuthRepository(app).getToken() }
        staffApiService.removeCoach(idAcademie, idCoach, "Bearer $jwt")
    }

    suspend fun isCoachInAcademie(app: Application, idAcademie: String, idCoach: String): Boolean {
        val jwt = withContext(Dispatchers.IO) { AuthRepository(app).getToken() }
        val res = staffApiService.isCoachInAcademie(idAcademie, idCoach, "Bearer $jwt")
        return res.isSuccessful && (res.body()?.exists == true)
    }
}
