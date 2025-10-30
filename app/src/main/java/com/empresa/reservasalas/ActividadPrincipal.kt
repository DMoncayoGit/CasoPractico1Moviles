package com.empresa.reservasalas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment // Importación necesaria
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.empresa.reservasalas.data.SessionManager
import com.empresa.reservasalas.databinding.ActividadPrincipalBinding
import com.google.android.material.navigation.NavigationView

class ActividadPrincipal : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActividadPrincipalBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var navController: NavController // Definir NavController a nivel de clase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Session Manager
        sessionManager = SessionManager(applicationContext)

        binding = ActividadPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController

        // Configuración de IDs del Menú
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_reservas, R.id.nav_perfil,
                R.id.nav_mapa, R.id.nav_reglamento
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Click para cerrar sesión
        navView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.nav_logout) {
                logoutUser()
                true
            } else {

                //Cerrar el drawer después de la selección
                drawerLayout.closeDrawers()
                //Navegar al destino correspondiente
                navController.navigate(menuItem.itemId)
                true
            }
        }

        //Actualizar el encabezado con los datos del usuario
        updateNavHeader(navView)
    }

    // Actualizar el Header
    private fun updateNavHeader(navView: NavigationView) {

        // Obtener la vista del encabezado
        val headerView = navView.getHeaderView(0)

        // Asegurar que se encuentra el tipo correcto
        val userNameTextView: TextView? = headerView.findViewById(R.id.nav_header_nombre)
        val userEmailTextView: TextView? = headerView.findViewById(R.id.nav_header_email)

        val userDetails = sessionManager.getProfileDetails()

        // Verificar si los TextViews existen antes de asignar el texto
        if (userNameTextView != null && userEmailTextView != null) {
            // Asignamos el texto usando las llaves literales "nombre" y "email"
            userNameTextView.text = userDetails["nombre"] ?: "Error de Nombre"
            userEmailTextView.text = userDetails["email"] ?: "Error de Email"
        } else {
            Log.e("MainActivity", "Error: TextViews del encabezado no encontrados.")
        }
    }

    // Cerrar la sesión del usuario y redirigir a la pantalla de Login.
    private fun logoutUser() {
        sessionManager.logout() // Usa el método logout de tu SessionManager
        val intent = Intent(this, Autentificacion::class.java)

        // Limpiar la pila de Activities para que el usuario no pueda volver con el botón "atrás"
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    // Crear el menú de opciones en la barra de acciones
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.principal, menu)
        return true
    }

    // Manejar el comportamiento del botón de navegación (Arriba)
    override fun onSupportNavigateUp(): Boolean {
        // Usamos la variable de clase NavController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}