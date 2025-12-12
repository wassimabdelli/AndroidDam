package tn.esprit.dam.models

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tn.esprit.dam.api.TerrainApiService
import tn.esprit.dam.data.AuthRepository
import tn.esprit.dam.data.RetrofitClient

/**
 * ViewModel for managing terrain (stadium) state
 */
class TerrainViewModel(private val application: Application) : ViewModel() {
    
    private val _terrains = MutableStateFlow<List<Terrain>>(emptyList())
    val terrains: StateFlow<List<Terrain>> = _terrains
    
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    
    private val authRepository = AuthRepository(application)
    private val terrainApiService: TerrainApiService by lazy {
        RetrofitClient.getRetrofit(application).create(TerrainApiService::class.java)
    }
    
    /**
     * Fetch all terrains for a specific academy
     */
    fun fetchTerrainsByAcademieId(academieId: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            
            try {
                val jwt = withContext(Dispatchers.IO) { authRepository.getToken() }
                
                if (jwt.isNullOrBlank()) {
                    errorMessage.value = "Authentication required"
                    isLoading.value = false
                    return@launch
                }
                
                val response = terrainApiService.getTerrainsByAcademieId(
                    academieId = academieId,
                    authHeader = "Bearer $jwt"
                )
                
                if (response.isSuccessful) {
                    _terrains.value = response.body() ?: emptyList()
                    Log.d("TerrainViewModel", "Fetched ${_terrains.value.size} terrains")
                } else {
                    errorMessage.value = "Failed to fetch stadiums: ${response.code()}"
                    Log.e("TerrainViewModel", "Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
                Log.e("TerrainViewModel", "Exception fetching terrains", e)
            } finally {
                isLoading.value = false
            }
        }
    }
    
    /**
     * Create a new terrain
     */
    fun createTerrain(
        terrain: CreateTerrainRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            
            try {
                val jwt = withContext(Dispatchers.IO) { authRepository.getToken() }
                
                if (jwt.isNullOrBlank()) {
                    onError("Authentication required")
                    isLoading.value = false
                    return@launch
                }
                
                val response = terrainApiService.createTerrain(
                    request = terrain,
                    authHeader = "Bearer $jwt"
                )
                
                if (response.isSuccessful) {
                    Log.d("TerrainViewModel", "Terrain created successfully")
                    onSuccess()
                } else {
                    val error = "Failed to create stadium: ${response.code()}"
                    onError(error)
                    Log.e("TerrainViewModel", "Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                val error = "Error: ${e.message}"
                onError(error)
                Log.e("TerrainViewModel", "Exception creating terrain", e)
            } finally {
                isLoading.value = false
            }
        }
    }
}
