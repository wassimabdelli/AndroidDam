package tn.esprit.dam.data

// Centralized API configuration for local development
object ApiConfig {
    // Use local development backend. For emulator use 10.0.2.2 which maps to host's localhost.
    // If you run on a physical device replace with your machine IP (eg. http://192.168.1.100:3001/api/v1)
    const val API_BASE_URL = "http://10.0.2.2:3000/api/v1/"

    // Web (browser) base URL without trailing slash used for social auth redirects
    const val WEB_BASE_URL = "http://10.0.2.2:3000/api/v1"
}

