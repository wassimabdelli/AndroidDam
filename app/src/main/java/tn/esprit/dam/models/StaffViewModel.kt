package tn.esprit.dam.models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.StaffRepository
import tn.esprit.dam.data.UserRepository
import android.app.Application
import androidx.compose.ui.platform.AndroidUriHandler

class StaffViewModel : ViewModel() {

    private val staffRepository = StaffRepository()
    private val userRepository = UserRepository()

    private val _myStaff = MutableStateFlow<List<Arbitre>>(emptyList())
    val myStaff: StateFlow<List<Arbitre>> = _myStaff
    
    private val _myCoaches = MutableStateFlow<List<Coach>>(emptyList())
    val myCoaches: StateFlow<List<Coach>> = _myCoaches

    private val _searchResults = MutableStateFlow<List<Arbitre>>(emptyList())
    val searchResults: StateFlow<List<Arbitre>> = _searchResults

    private val _coachMembership = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val coachMembership: StateFlow<Map<String, Boolean>> = _coachMembership

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    private var searchJob: Job? = null

    fun fetchMyStaff(idAcademie: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                // Fetch both arbitres and coaches in parallel or sequentially
                val arbitres = staffRepository.getArbitres(idAcademie)
                val coaches = staffRepository.getCoachs(idAcademie)
                
                _myStaff.value = arbitres
                _myCoaches.value = coaches
            } catch (e: Exception) {
                errorMessage.value = "Failed to fetch staff: ${e.message}"
                _myStaff.value = emptyList()
                _myCoaches.value = emptyList()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun searchArbitres(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.length < 2) {
                _searchResults.value = emptyList()
                isLoading.value = false
                return@launch
            }
            isLoading.value = true
            errorMessage.value = null
            delay(300)
            try {
                _searchResults.value = userRepository.searchArbitres(query)
            } catch (e: Exception) {
                errorMessage.value = "Failed to search referees: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun searchCoachsArbitres(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.length < 2) {
                _searchResults.value = emptyList()
                isLoading.value = false
                return@launch
            }
            isLoading.value = true
            errorMessage.value = null
            delay(300)
            try {
                _searchResults.value = userRepository.searchCoachsArbitres(query)
            } catch (e: Exception) {
                errorMessage.value = "Failed to search users: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun addArbitreToMyStaff(idAcademie: String, idArbitre: String) {
        viewModelScope.launch {
            try {
                staffRepository.addArbitre(idAcademie, idArbitre)
                fetchMyStaff(idAcademie)
            } catch (e: Exception) {
                errorMessage.value = "Failed to add referee: ${e.message}"
            }
        }
    }

    fun removeArbitreFromMyStaff(idAcademie: String, idArbitre: String) {
        viewModelScope.launch {
            try {
                staffRepository.removeArbitre(idAcademie, idArbitre)
                fetchMyStaff(idAcademie)
            } catch (e: Exception) {
                errorMessage.value = "Failed to remove referee: ${e.message}"
            }
        }
    }

    fun addCoachToMyStaff(app: Application, idAcademie: String, idCoach: String) {
        viewModelScope.launch {
            try {
                staffRepository.addCoach(app, idAcademie, idCoach)
                _coachMembership.value = _coachMembership.value.toMutableMap().apply { put(idCoach, true) }
            } catch (e: Exception) {
                errorMessage.value = "Failed to add coach: ${e.message}"
            }
        }
    }

    fun removeCoachFromMyStaff(app: Application, idAcademie: String, idCoach: String) {
        viewModelScope.launch {
            try {
                staffRepository.removeCoach(app, idAcademie, idCoach)
                _coachMembership.value = _coachMembership.value.toMutableMap().apply { put(idCoach, false) }
                // Refresh the list to reflect changes
                fetchMyStaff(idAcademie)
            } catch (e: Exception) {
                errorMessage.value = "Failed to remove coach: ${e.message}"
            }
        }
    }

    fun checkCoachMembership(app: Application, idAcademie: String, idCoach: String) {
        viewModelScope.launch {
            try {
                val exists = staffRepository.isCoachInAcademie(app, idAcademie, idCoach)
                _coachMembership.value = _coachMembership.value.toMutableMap().apply { put(idCoach, exists) }
            } catch (e: Exception) {
            }
        }
    }
}
