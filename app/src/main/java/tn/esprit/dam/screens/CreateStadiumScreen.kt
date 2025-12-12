package tn.esprit.dam.screens

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tn.esprit.dam.data.AuthRepository
import tn.esprit.dam.models.Coordinates
import tn.esprit.dam.models.CreateTerrainRequest
import tn.esprit.dam.models.TerrainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStadiumScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: TerrainViewModel = viewModel(
        factory = TerrainViewModelFactory(application)
    )
    
    val scrollState = rememberScrollState()
    val isLoading by viewModel.isLoading
    
    // Form state
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var numberOfFields by remember { mutableStateOf("") }
    var fieldNames by remember { mutableStateOf("") }
    var hasLights by remember { mutableStateOf(false) }
    var amenitiesInput by remember { mutableStateOf("") }
    
    // Form validation
    val isFormValid = name.isNotBlank() &&
            location.isNotBlank() &&
            latitude.isNotBlank() &&
            longitude.isNotBlank() &&
            capacity.isNotBlank() &&
            numberOfFields.isNotBlank() &&
            latitude.toDoubleOrNull() != null &&
            longitude.toDoubleOrNull() != null &&
            capacity.toIntOrNull() != null &&
            numberOfFields.toIntOrNull() != null &&
            (numberOfFields.toIntOrNull() ?: 0) in 1..10
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Créer un Stade",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    val academieId = try {
                        kotlinx.coroutines.runBlocking {
                            withContext(Dispatchers.IO) {
                                AuthRepository(application).getUser()?._id
                            }
                        }
                    } catch (e: Exception) {
                        null
                    }
                    
                    if (academieId == null) {
                        Toast.makeText(context, "Erreur: utilisateur non connecté", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    val fieldNamesList = if (fieldNames.isNotBlank()) {
                        fieldNames.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    } else {
                        emptyList()
                    }
                    
                    val amenitiesList = if (amenitiesInput.isNotBlank()) {
                        amenitiesInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    } else {
                        emptyList()
                    }
                    
                    val terrain = CreateTerrainRequest(
                        id_academie = academieId,
                        name = name,
                        location_verbal = location,
                        coordinates = Coordinates(
                            latitude = latitude.toDouble(),
                            longitude = longitude.toDouble()
                        ),
                        capacity = capacity.toInt(),
                        number_of_fields = numberOfFields.toInt(),
                        field_names = fieldNamesList.ifEmpty { null },
                        has_lights = hasLights,
                        amenities = amenitiesList.ifEmpty { null },
                        is_available = true
                    )
                    
                    viewModel.createTerrain(
                        terrain = terrain,
                        onSuccess = {
                            Toast.makeText(context, "Stade créé avec succès!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Créer le Stade", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Section Header
            Text(
                text = "Informations du Stade",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Remplissez les informations ci-dessous",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Form fields
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Stadium Name
                StadiumFormField(
                    label = "Nom du Stade",
                    placeholder = "ex: Stade Municipal",
                    value = name,
                    onValueChange = { name = it },
                    leadingIcon = Icons.Default.Stadium
                )
                
                // Location
                StadiumFormField(
                    label = "Adresse",
                    placeholder = "ex: 123 Rue Principale, Tunis",
                    value = location,
                    onValueChange = { location = it },
                    leadingIcon = Icons.Default.LocationOn
                )
                
                // GPS Coordinates Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StadiumFormField(
                        label = "Latitude",
                        placeholder = "ex: 36.8065",
                        value = latitude,
                        onValueChange = { latitude = it },
                        leadingIcon = Icons.Default.MyLocation,
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    
                    StadiumFormField(
                        label = "Longitude",
                        placeholder = "ex: 10.1815",
                        value = longitude,
                        onValueChange = { longitude = it },
                        leadingIcon = Icons.Default.MyLocation,
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Capacity
                StadiumFormField(
                    label = "Capacité",
                    placeholder = "ex: 100",
                    value = capacity,
                    onValueChange = { capacity = it },
                    leadingIcon = Icons.Default.People,
                    keyboardType = KeyboardType.Number
                )
                
                // Number of Fields
                StadiumFormField(
                    label = "Nombre de Terrains (1-10)",
                    placeholder = "ex: 2",
                    value = numberOfFields,
                    onValueChange = { numberOfFields = it },
                    leadingIcon = Icons.Default.GridOn,
                    keyboardType = KeyboardType.Number
                )
                
                // Field Names (optional)
                StadiumFormField(
                    label = "Noms des Terrains (optionnel)",
                    placeholder = "ex: Terrain A, Terrain B",
                    value = fieldNames,
                    onValueChange = { fieldNames = it },
                    leadingIcon = Icons.Default.Label,
                    helperText = "Séparez les noms par des virgules"
                )
                
                // Has Lights Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = "Lights",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Éclairage nocturne",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = hasLights,
                        onCheckedChange = { hasLights = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                
                // Amenities (optional)
                StadiumFormField(
                    label = "Équipements (optionnel)",
                    placeholder = "ex: parking, vestiaires, café",
                    value = amenitiesInput,
                    onValueChange = { amenitiesInput = it },
                    leadingIcon = Icons.Default.LocalParking,
                    helperText = "Séparez les équipements par des virgules"
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StadiumFormField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    helperText: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { 
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            leadingIcon = {
                Icon(
                    leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            singleLine = keyboardType != KeyboardType.Text,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        if (helperText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = helperText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
