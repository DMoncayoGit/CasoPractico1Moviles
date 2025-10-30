package com.empresa.reservasalas.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NOMBRE, null, DB_VERSION) {

    companion object {

        // Versión de la base de datos
        const val DB_VERSION = 3
        const val DB_NOMBRE = "reservas.db"

        // Tablas
        const val TABLA_USUARIOS = "usuarios"
        const val TABLA_SALAS = "salas"
        const val TABLA_RESERVAS = "reservas"

        // Columnas comunes
        const val COLUMNA_ID = "id"

        // Columnas de RESERVAS
        const val RES_USUARIO_EMAIL = "usuario_email"
        const val RES_SALA_NOMBRE = "sala_nombre"
        const val RES_FECHA = "fecha"
        const val RES_HORA_INICIO = "hora_inicio"
        const val RES_HORA_FIN = "hora_fin"
        const val RES_ESTADO = "estado"

        // Columnas de USUARIOS
        const val USU_NOMBRE = "nombre"
        const val USU_EMAIL = "email"
        const val USU_CONTRASENIA = "contrasena"
        const val USU_TELEFONO = "telefono"

        // Columnas de SALAS
        const val SAL_NOMBRE = "nombre"
        const val SAL_CAPACIDAD = "capacidad"
        const val SAL_UBICACION = "ubicacion"

    }

    // Consulta para crear la tabla de RESERVAS
    private val reservas =
        "CREATE TABLE $TABLA_RESERVAS (" +
                "$COLUMNA_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$RES_USUARIO_EMAIL TEXT," +
                "$RES_SALA_NOMBRE TEXT," +
                "$RES_FECHA TEXT," +
                "$RES_HORA_INICIO TEXT," +
                "$RES_HORA_FIN TEXT," +
                "$RES_ESTADO TEXT," +
                "FOREIGN KEY($RES_USUARIO_EMAIL) REFERENCES $TABLA_USUARIOS($USU_EMAIL) " +
                "ON DELETE CASCADE ON UPDATE CASCADE," +
                "FOREIGN KEY($RES_SALA_NOMBRE) REFERENCES $TABLA_SALAS($SAL_NOMBRE) " +
                "ON DELETE CASCADE ON UPDATE CASCADE )"

    // Consulta para crear la tabla de USUARIOS
    private val usuarios =
        "CREATE TABLE $TABLA_USUARIOS (" +
                "$COLUMNA_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$USU_NOMBRE TEXT," +
                "$USU_EMAIL TEXT UNIQUE," +
                "$USU_CONTRASENIA TEXT," +
                "$USU_TELEFONO TEXT)"

    // Consulta para crear la tabla de SALAS
    private val salas =
        "CREATE TABLE $TABLA_SALAS (" +
                "$COLUMNA_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$SAL_NOMBRE TEXT UNIQUE," +
                "$SAL_CAPACIDAD INTEGER," +
                "$SAL_UBICACION TEXT)"

    // Creación de las Tablas
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(usuarios)
        db.execSQL(salas)
        db.execSQL(reservas)

        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        /* Reseteo completo (eliminar todo)
             if (oldVersion < 3) {
                 db.execSQL("DROP TABLE IF EXISTS $TABLE_RESERVAS")
                 db.execSQL("DROP TABLE IF EXISTS $TABLE_SALAS")
                 db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
                 onCreate(db)
             }
         */
    }

    // Inserta datos iniciales en las tablas Usuarios y Salas.
    private fun seedInitialData(db: SQLiteDatabase) {

        // Insertar usuario de prueba
        val userValues = ContentValues().apply {
            put(USU_NOMBRE, "Empleado ")
            put(USU_EMAIL, "empleado@empresa.com")
            put(USU_CONTRASENIA, "1234")
            put(USU_TELEFONO, "0990000001")
        }
        db.insert(TABLA_USUARIOS, null, userValues)
        Log.d("DatabaseHelper", "Usuario inicial 'empleado@empresa.com' insertado.")

        // Insertar salas iniciales
        val sala1 = ContentValues().apply {
            put(SAL_NOMBRE, "Sala A")
            put(SAL_CAPACIDAD, 10)
            put(SAL_UBICACION, "Piso 1")
        }
        db.insert(TABLA_SALAS, null, sala1)

        val sala2 = ContentValues().apply {
            put(SAL_NOMBRE, "Sala B")
            put(SAL_CAPACIDAD, 25)
            put(SAL_UBICACION, "Piso 2")
        }
        db.insert(TABLA_SALAS, null, sala2)

        val sala3 = ContentValues().apply {
            put(SAL_NOMBRE, "Sala C")
            put(SAL_CAPACIDAD, 5)
            put(SAL_UBICACION, "Piso 3")
        }
        db.insert(TABLA_SALAS, null, sala3)
        Log.d("DatabaseHelper", "Salas iniciales insertadas.")
    }

    // Actualizar la información de perfil de un usuario
    fun updateUserProfile(email: String, nuevoNombre: String, nuevoTelefono: String): Boolean {

        val db = this.writableDatabase

        val values = ContentValues().apply {
            put(USU_NOMBRE, nuevoNombre)
            put(USU_TELEFONO, nuevoTelefono)
        }

        val whereClause = "$USU_EMAIL = ?"
        val whereArgs = arrayOf(email)

        val count = db.update(TABLA_USUARIOS, values, whereClause, whereArgs)

        // El count es el número de filas afectadas. Si es > 0, fue exitoso.
        return count > 0
    }
}
