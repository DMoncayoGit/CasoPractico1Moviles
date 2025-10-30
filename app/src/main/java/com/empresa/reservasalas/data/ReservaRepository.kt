package com.empresa.reservasalas.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log

class ReservaRepository(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    private val tabla = DatabaseHelper.TABLA_RESERVAS
    private val id = DatabaseHelper.COLUMNA_ID
    private val usuarioEmail = DatabaseHelper.RES_USUARIO_EMAIL
    private val nombreSala = DatabaseHelper.RES_SALA_NOMBRE
    private val fecha = DatabaseHelper.RES_FECHA
    private val horaInicio = DatabaseHelper.RES_HORA_INICIO
    private val horaFin = DatabaseHelper.RES_HORA_FIN
    private val estado = DatabaseHelper.RES_ESTADO

    // Obtener la lista de nombres de salas disponibles
    fun getAvailableSalaNames(): List<String> {
        val db = dbHelper.readableDatabase
        val salaNames = mutableListOf<String>()
        val projection = arrayOf(DatabaseHelper.SAL_NOMBRE)

        val cursor = db.query(DatabaseHelper.TABLA_SALAS, projection, null, null, null, null, null)

        with(cursor) {
            while (moveToNext()) {
                val name = getString(getColumnIndexOrThrow(DatabaseHelper.SAL_NOMBRE))
                salaNames.add(name)
            }
        }
        cursor.close()
        return salaNames
    }

    // Crear Reserva
    fun createReserva(reserva: Reserva): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(usuarioEmail, reserva.usuarioEmail)
            put(nombreSala, reserva.salaNombre)
            put(fecha, reserva.fecha)
            put(horaInicio, reserva.horaInicio)
            put(horaFin, reserva.horaFin)
            put(estado, reserva.estado.value)
        }

        val newRowId = db.insert(tabla, null, values)
        Log.d("ReservaRepository", "Reserva creada con ID: $newRowId")
        return newRowId
    }

    // Ver Reservas
    // Validación: cada usuario solo ve sus propias reservas
    fun getReservasByUser(userEmail: String): List<Reserva> {
        val db = dbHelper.readableDatabase
        val reservasList = mutableListOf<Reserva>()

        val selection = "$usuarioEmail = ?"
        val selectionArgs = arrayOf(userEmail)

        val cursor: Cursor = db.query(
            tabla,
            null, // Devolver todas las columnas
            selection,
            selectionArgs,
            null,
            null,
            "$fecha DESC, $horaInicio ASC" // Ordenar por fecha y hora
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(id))
                val salaNombre = getString(getColumnIndexOrThrow(nombreSala))
                val fecha = getString(getColumnIndexOrThrow(fecha))
                val horaInicio = getString(getColumnIndexOrThrow(horaInicio))
                val horaFin = getString(getColumnIndexOrThrow(horaFin))
                val estadoString = getString(getColumnIndexOrThrow(estado))

                val estado = EstadoReserva.valueOf(estadoString.uppercase())

                reservasList.add(
                    Reserva(id, userEmail, salaNombre, fecha, horaInicio, horaFin, estado)
                )
            }
        }
        cursor.close()
        return reservasList
    }

    // Cancelar Reserva
    fun cancelReserva(reservaId: Int): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(estado, EstadoReserva.CANCELADA.value)
        }

        val selection = "$id = ?"
        val selectionArgs = arrayOf(reservaId.toString())

        val count = db.update(
            tabla,
            values,
            selection,
            selectionArgs)

        Log.d("ReservaRepository", "Reserva $reservaId cancelada. Filas afectadas: $count")
        return count
    }


    // Obtener una Reserva por ID
    fun getReservaById(reservaId: Int): Reserva? {
        val db = dbHelper.readableDatabase
        var reserva: Reserva? = null

        val selection = "$id = ?"
        val selectionArgs = arrayOf(reservaId.toString())

        val cursor: Cursor = db.query(
            tabla,
            null, // Devolver todas las columnas
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        with(cursor) {
            if (moveToFirst()) {
                val userEmail = getString(getColumnIndexOrThrow(usuarioEmail))
                val salaNombre = getString(getColumnIndexOrThrow(nombreSala))
                val fecha = getString(getColumnIndexOrThrow(fecha))
                val horaInicio = getString(getColumnIndexOrThrow(horaInicio))
                val horaFin = getString(getColumnIndexOrThrow(horaFin))
                val estadoString = getString(getColumnIndexOrThrow(estado))

                val estado = EstadoReserva.valueOf(estadoString.uppercase())

                reserva = Reserva(reservaId, userEmail, salaNombre, fecha, horaInicio, horaFin, estado)
            }
        }
        cursor.close()
        return reserva
    }

    // Editar/Actualizar Reserva
    fun updateReserva(reserva: Reserva): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(usuarioEmail, reserva.usuarioEmail)
            put(nombreSala, reserva.salaNombre)
            put(fecha, reserva.fecha)
            put(horaInicio, reserva.horaInicio)
            put(horaFin, reserva.horaFin)
            put(estado, reserva.estado.value)
        }

        val selection = "$id = ?"
        val selectionArgs = arrayOf(reserva.id.toString())

        val count = db.update(
            tabla,
            values,
            selection,
            selectionArgs)

        Log.d("ReservaRepository", "Reserva ${reserva.id} actualizada. Filas afectadas: $count")
        return count
    }

}