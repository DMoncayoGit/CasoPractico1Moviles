package com.empresa.reservasalas.data

// Repositorio que maneja las operaciones de datos del usuario.
class UserRepository(
    private val sessionManager: SessionManager,
    private val databaseHelper: DatabaseHelper
) {

    // Obtener detalles del perfil cargados desde la sesión local.
    fun getProfileDetailsFromSession(): Map<String, String?> {
        return sessionManager.getProfileDetails()
    }

    // Guardar el perfil de forma asíncrona en la base de datos (DatabaseHelper) y actualiza la sesión local (SessionManager).
    fun saveUserProfile(
        nombre: String,
        email: String,
        telefono: String
    ): Boolean {

        // Intentar actualizar en la base de datos permanente (SQLite)
        val saveSuccessful = databaseHelper.updateUserProfile(
            email = email,
            nuevoNombre = nombre,
            nuevoTelefono = telefono
        )

        // Si funciono
        if (saveSuccessful) {
            // Actualizar SessionManager (caché local).
            sessionManager.updateProfile(nombre, email, telefono)
            return true
        } else {
           return false
        }
    }
}
