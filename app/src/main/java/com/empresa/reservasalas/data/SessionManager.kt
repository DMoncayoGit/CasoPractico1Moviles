package com.empresa.reservasalas.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val preferencias = "ReservaSalasAppPrefs"

    companion object {
        const val IS_LOGGED_IN = "isLoggedIn"
        const val USER_ID = "userId"
        const val USER_NAME = "userName"
        const val USER_EMAIL = "userEmail"
        const val USER_PHONE = "userPhone"
    }

    // Inicializar SharedPreferences
    private val prefs: SharedPreferences = context.getSharedPreferences(preferencias, Context.MODE_PRIVATE)

    // Crear un editor para modificar los valores en SharedPreferences
    private val editor = prefs.edit()

    // Función para guardar la información de un usuario
    fun createLoginSession(id: String, nombre: String, email: String, telefono: String) {
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(USER_ID, id)
        editor.putString(USER_NAME, nombre)
        editor.putString(USER_EMAIL, email)
        editor.putString(USER_PHONE, telefono)
        editor.apply()
    }

    // Función para verificar si el usuario ya inició sesión
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    // Función cerrar sesión
    fun logout() {
        editor.clear()
        editor.apply()
    }

    // Función para devolver el nombre del usuario logeado.
    fun getUserName(): String? {
        return prefs.getString(USER_NAME, null)
    }

    // Función para obtener la información del del usuario
    fun getProfileDetails(): Map<String, String?> {
        return mapOf(
            "nombre" to prefs.getString(USER_NAME, ""),
            "email" to prefs.getString(USER_EMAIL, ""),
            "telefono" to prefs.getString(USER_PHONE, "")
        )
    }

    // Función para actualizar los datos del usuario
    fun updateProfile(nombre: String, email: String, telefono: String) {
        editor.putString(USER_NAME, nombre)
        editor.putString(USER_EMAIL, email)
        editor.putString(USER_PHONE, telefono)
        editor.apply()
    }
}
