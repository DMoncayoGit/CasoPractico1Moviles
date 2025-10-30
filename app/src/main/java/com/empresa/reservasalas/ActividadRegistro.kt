package com.empresa.reservasalas

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.empresa.reservasalas.data.DatabaseHelper

class ActividadRegistro : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_registro)

        dbHelper = DatabaseHelper(this)

        val etName = findViewById<EditText>(R.id.et_registrar_nombre)
        val etEmail = findViewById<EditText>(R.id.et_registrar_email)
        val etPhone = findViewById<EditText>(R.id.et_registrar_telefono)
        val etPassword = findViewById<EditText>(R.id.et_registrar_contrasenia)
        val btnRegister = findViewById<Button>(R.id.btn_registrar)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (registerNewUser(name, email, phone, password)) {
                Toast.makeText(this, "Registro exitoso. ¡Inicie sesión!", Toast.LENGTH_LONG).show()
                finish()

            } else {
                Toast.makeText(this, "Error: El email ya está registrado o hay un problema.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Registrar un nuevo Usuario
    private fun registerNewUser(name: String, email: String, phone: String, password: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.USU_NOMBRE, name)
            put(DatabaseHelper.USU_EMAIL, email)
            put(DatabaseHelper.USU_TELEFONO, phone)
            put(DatabaseHelper.USU_CONTRASENIA, password)
        }

        // Si insert devuelve -1, falló
        val newRowId = db.insert(DatabaseHelper.TABLA_USUARIOS, null, values)
        return newRowId != -1L
    }
}