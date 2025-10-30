package com.empresa.reservasalas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView // <-- ¡IMPORTANTE! Asegúrate de tener este import
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.empresa.reservasalas.data.DatabaseHelper
import com.empresa.reservasalas.data.SessionManager

class Autentificacion : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_autentificacion)

        // Inicializar helpers
        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(applicationContext)

        // Si ya hay una sesión activa, saltar el login
        if (sessionManager.isLoggedIn()) {
            navigateToHome()
            return
        }

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_contrasenia)
        val btnLogin = findViewById<Button>(R.id.btn_login)

        val tvForgotPassword = findViewById<TextView>(R.id.tv_olvido_contrasenia)
        val tvCreateAccount = findViewById<TextView>(R.id.tv_crear_cuenta)

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, OlvidoContrasenia::class.java))
        }

        tvCreateAccount.setOnClickListener {
            startActivity(Intent(this, ActividadRegistro::class.java))
        }


        // Login
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar contra la base de datos
            val userDetails = validateUser(email, password)
            if (userDetails != null) {

                // Si el usuario es válido, crear la sesión
                sessionManager.createLoginSession(
                    id = userDetails["id"]!!,
                    nombre = userDetails["nombre"]!!,
                    email = userDetails["email"]!!,
                    telefono = userDetails["telefono"]!!
                )
                Toast.makeText(this, "Bienvenido, ${userDetails["nombre"]}", Toast.LENGTH_SHORT).show()
                navigateToHome()
            } else {
                Toast.makeText(this, "Credenciales incorrectas.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Validar usuario
    private fun validateUser(email: String, password: String): Map<String, String>? {
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            DatabaseHelper.COLUMNA_ID,
            DatabaseHelper.USU_NOMBRE,
            DatabaseHelper.USU_EMAIL,
            DatabaseHelper.USU_TELEFONO
        )

        val selection = "${DatabaseHelper.USU_EMAIL} = ? AND ${DatabaseHelper.USU_CONTRASENIA} = ?"
        val selectionArgs = arrayOf(email, password)

        val cursor = db.query(
            DatabaseHelper.TABLA_USUARIOS,
            projection, selection, selectionArgs, null, null, null
        )

        var userDetails: Map<String, String>? = null
        with(cursor) {
            if (moveToFirst()) {
                val id = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMNA_ID))
                val nombre = getString(getColumnIndexOrThrow(DatabaseHelper.USU_NOMBRE))
                val userEmail = getString(getColumnIndexOrThrow(DatabaseHelper.USU_EMAIL))
                val telefono = getString(getColumnIndexOrThrow(DatabaseHelper.USU_TELEFONO)) ?: ""

                userDetails = mapOf(
                    "id" to id,
                    "nombre" to nombre,
                    "email" to userEmail,
                    "telefono" to telefono
                )
            }
        }
        cursor.close()
        return userDetails
    }

    private fun navigateToHome() {
        startActivity(Intent(this, ActividadPrincipal::class.java))
        finish()
    }
}