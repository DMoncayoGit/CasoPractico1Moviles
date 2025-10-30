package com.empresa.reservasalas.data

// Enumeración para el estado de la reserva
enum class EstadoReserva(val value: String) {
    ACTIVA("Activa"),
    CANCELADA("Cancelada")
}

// Modelo de la clase Reserva
data class Reserva(
    val id: Int = 0,           // 0 indica que es una nueva reserva (ID autoincremental)
    val usuarioEmail: String,  // El email del usuario que realiza la reserva
    val salaNombre: String,    // Nombre de la sala reservada
    val fecha: String,         // Formato YYYY-MM-DD
    val horaInicio: String,    // Formato HH:MM
    val horaFin: String,       // Formato HH:MM
    val estado: EstadoReserva = EstadoReserva.ACTIVA
)

