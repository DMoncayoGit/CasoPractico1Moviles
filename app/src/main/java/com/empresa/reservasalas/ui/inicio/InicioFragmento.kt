package com.empresa.reservasalas.ui.inicio

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController // Importación crucial
import com.empresa.reservasalas.R
import com.empresa.reservasalas.data.SessionManager

class InicioFragmento : Fragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragmento_inicio, container, false)

        // Mostrar Mensaje de Bienvenida
        val sessionManager = SessionManager(requireContext())
        val userName = sessionManager.getUserName() ?: "Empleado"
        root.findViewById<TextView>(R.id.tv_mensaje_bienvenida).text = "Bienvenido, $userName"

        // Obtener el NavController
        val navController = findNavController()

        // Botón 1: Reservas
        root.findViewById<Button>(R.id.btn_nav_reservas).setOnClickListener {
            // R.id.nav_reservas es el ID del destino en mobile_navigation.xml
            navController.navigate(R.id.nav_reservas)
        }

        // Botón 2: Perfil
        root.findViewById<Button>(R.id.btn_nav_perfil).setOnClickListener {
            navController.navigate(R.id.nav_perfil)
        }


        // Botón 3: Ubicación/Mapa
        root.findViewById<Button>(R.id.btn_nav_mapa).setOnClickListener {
            navController.navigate(R.id.nav_mapa)
        }

        // Botón 4: Reglamento
        root.findViewById<Button>(R.id.btn_nav_reglamento).setOnClickListener {
            navController.navigate(R.id.nav_reglamento)
        }

        return root
    }
}