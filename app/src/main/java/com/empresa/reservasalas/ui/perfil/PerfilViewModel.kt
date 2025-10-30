package com.empresa.reservasalas.ui.perfil

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.empresa.reservasalas.data.UserRepository
import kotlinx.coroutines.launch

// Clase Factory requerida para construir el ViewModel
class PerfilViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PerfilViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PerfilViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// ViewModel para manejar la lógica y el estado de la pantalla de Perfil.
class PerfilViewModel(private val userRepository: UserRepository) : ViewModel() {

    // LiveData para manejar los datos del perfil que se mostrarán en la UI
    private val _profileDetails = MutableLiveData<Map<String, String?>>()
    val profileDetails: LiveData<Map<String, String?>> = _profileDetails

    // LiveData para comunicar el estado de la operación de guardado a la UI
    private val _saveStatus = MutableLiveData<SaveStatus>(SaveStatus.Idle)
    val saveStatus: LiveData<SaveStatus> = _saveStatus

    init {
        // Cargar los datos iniciales al crear el ViewModel
        loadProfile()
    }

    // Cargar los datos del perfil desde el UserRepository que usa SessionManager.
    fun loadProfile() {
        _profileDetails.value = userRepository.getProfileDetailsFromSession()
    }

    // Guardar el perfil de forma asíncrona
    fun saveProfile(nombre: String, email: String, telefono: String) {

        // Evitar múltiples guardados si ya está cargando
        if (_saveStatus.value == SaveStatus.Loading) return

        // Verificar que el email no esté vacío antes de continuar.
        if (email.isEmpty()) {
            _saveStatus.value = SaveStatus.Error("Error: El email es obligatorio para guardar.")
            return
        }
        _saveStatus.value = SaveStatus.Loading

        viewModelScope.launch {
            // El UserRepository.saveUserProfile ejecuta el guardado en SQLite y luego actualiza SessionManager.
            val success = userRepository.saveUserProfile(
                nombre = nombre,
                email = email,
                telefono = telefono
            )

            if (success) {
                // Recargar los datos desde SessionManager
                loadProfile()
                _saveStatus.value = SaveStatus.Success("¡Perfil actualizado!")
            } else {
                _saveStatus.value = SaveStatus.Error("Fallo al guardar. Revisa si el email existe en la DB.")
            }
        }
    }

    // Estados para la operación de guardado, lo que facilita el manejo de la UI.
    sealed class SaveStatus {
        object Idle : SaveStatus()
        object Loading : SaveStatus()
        data class Success(val message: String) : SaveStatus()
        data class Error(val message: String) : SaveStatus()
    }
}
