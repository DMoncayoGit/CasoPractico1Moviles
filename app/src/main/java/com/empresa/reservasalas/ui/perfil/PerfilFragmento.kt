package com.empresa.reservasalas.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.empresa.reservasalas.Autentificacion
import com.empresa.reservasalas.R
import com.empresa.reservasalas.data.DatabaseHelper
import com.empresa.reservasalas.data.SessionManager
import com.empresa.reservasalas.data.UserRepository

class PerfilFragmento : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: PerfilViewModel

    // Declaraciones de vistas
    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var etTelefono: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragmento_perfil, container, false)

        // Inicialización de dependencias
        sessionManager = SessionManager(requireContext())
        val dbHelper = DatabaseHelper(requireContext())
        val userRepository = UserRepository(sessionManager, dbHelper)

        // Inicialización del ViewModel usando Factory
        val factory = PerfilViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory)[PerfilViewModel::class.java]

        // Inicialización de vistas
        etNombre = root.findViewById(R.id.et_perfil_nombre)
        etEmail = root.findViewById(R.id.et_perfil_email)
        etTelefono = root.findViewById(R.id.et_perfil_telefono)
        btnGuardar = root.findViewById(R.id.btn_perfil_guardar)
        btnLogout = root.findViewById(R.id.btn_perfil_logout)

        setupObservers()
        setupListeners()

        return root
    }

    private fun setupObservers() {
        viewModel.profileDetails.observe(viewLifecycleOwner) { details ->
            if (details != null) {
                etNombre.setText(details["nombre"])
                etEmail.setText(details["email"])
                etTelefono.setText(details["telefono"])
            }
        }

        // Mensajes
        viewModel.saveStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                PerfilViewModel.SaveStatus.Idle -> {}
                PerfilViewModel.SaveStatus.Loading -> {
                    btnGuardar.isEnabled = false
                    Toast.makeText(context, "Guardando...", Toast.LENGTH_SHORT).show()
                }
                is PerfilViewModel.SaveStatus.Success -> {
                    btnGuardar.isEnabled = true
                    Toast.makeText(context, status.message, Toast.LENGTH_SHORT).show()
                }
                is PerfilViewModel.SaveStatus.Error -> {
                    btnGuardar.isEnabled = true
                    Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupListeners() {
        btnGuardar.setOnClickListener { saveProfileData() }
        btnLogout.setOnClickListener { logoutUser() }
    }

    // Guardar información del Usuario
    private fun saveProfileData() {
        val nombre = etNombre.text.toString()
        val email = etEmail.text.toString()
        val telefono = etTelefono.text.toString()

        if (nombre.isNotEmpty() && email.isNotEmpty()) {
            viewModel.saveProfile(nombre, email, telefono)
        } else {
            Toast.makeText(context, "Nombre y Email son obligatorios.", Toast.LENGTH_SHORT).show()
        }
    }

    // Cerrar sesión
    private fun logoutUser() {
        sessionManager.logout()
        val intent = Intent(activity, Autentificacion::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}