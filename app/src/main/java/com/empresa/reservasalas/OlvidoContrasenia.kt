package com.empresa.reservasalas

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OlvidoContrasenia : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_olvido_contrasenia)

        val btnBack = findViewById<Button>(R.id.btn_regresar_al_login)
        btnBack.setOnClickListener {
            finish()
        }
    }
}