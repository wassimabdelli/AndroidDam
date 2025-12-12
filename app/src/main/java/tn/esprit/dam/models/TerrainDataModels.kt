package tn.esprit.dam.models

import kotlinx.serialization.Serializable

/**
 * Data classes for Terrain (Stadium) management
 */

// Coordinates for GPS location
@Serializable
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

// Terrain entity from backend
@Serializable
data class Terrain(
    val _id: String,
    val id_academie: String,
    val name: String,
    val location_verbal: String,
    val coordinates: Coordinates,
    val capacity: Int,
    val number_of_fields: Int,
    val field_names: List<String> = emptyList(),
    val has_lights: Boolean = false,
    val amenities: List<String> = emptyList(),
    val is_available: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// Request DTO for creating a terrain
@Serializable
data class CreateTerrainRequest(
    val id_academie: String,
    val name: String,
    val location_verbal: String,
    val coordinates: Coordinates,
    val capacity: Int,
    val number_of_fields: Int,
    val field_names: List<String>? = null,
    val has_lights: Boolean? = false,
    val amenities: List<String>? = null,
    val is_available: Boolean? = true
)
