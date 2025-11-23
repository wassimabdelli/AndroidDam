package tn.esprit.dam.data

import tn.esprit.dam.api.StaffApiService
import tn.esprit.dam.models.AddArbitreRequest
import tn.esprit.dam.models.Arbitre
import tn.esprit.dam.models.StaffUpdateResponse

class StaffRepository {

    private val staffApiService: StaffApiService = RetrofitClient.staffApiService

    suspend fun addArbitre(idAcademie: String, idArbitre: String): StaffUpdateResponse {
        val request = AddArbitreRequest(idArbitre = idArbitre)
        return staffApiService.addArbitre(idAcademie, request)
    }

    suspend fun getArbitres(idAcademie: String): List<Arbitre> {
        return staffApiService.getArbitres(idAcademie)
    }

    suspend fun removeArbitre(idAcademie: String, idArbitre: String): StaffUpdateResponse {
        return staffApiService.removeArbitre(idAcademie, idArbitre)
    }
}
