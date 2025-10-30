package com.empresa.reservasalas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class Recordatorio : BroadcastReceiver() {

    private val idCanal = "CanalReserva"

    override fun onReceive(context: Context, intent: Intent) {

        // Verificar la acción del Intent
        if (intent.action != NotificacionesUtils.ACTION_RESERVATION_REMINDER) {
            Log.e("ReminderBroadcast", "Acción de Intent incorrecta, ignorando. Recibida: ${intent.action}")
            return
        }

        // Obtener datos y crear el canal (usando las constantes de NotificationUtils)
        val reservaTitle = intent.getStringExtra(NotificacionesUtils.EXTRA_TITLE) ?: "Reserva de Sala"
        val detailText = intent.getStringExtra(NotificacionesUtils.EXTRA_DETAIL) ?: "Tu reserva va a comenzar."
        val notificationId = intent.getIntExtra(NotificacionesUtils.NOTIFICATION_ID, 1)

        createNotificationChannel(context)

        // Construir la notificación
        val builder = NotificationCompat.Builder(context, idCanal)
            // Usamos un ícono estándar de Android para evitar fallos por recursos perdidos
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("¡Recordatorio de Reserva: $reservaTitle!")
            .setContentText(detailText)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // PRIORITY_HIGH para asegurar el pop-up (Heads-Up)
            .setAutoCancel(true)

        // Disparar Notificación
        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(notificationId, builder.build())
            Log.d("ReminderBroadcast", "Notificación $notificationId disparada.")
        } catch (e: SecurityException) {
            Log.e("ReminderBroadcast", "Error de seguridad al mostrar la notificación. Revise permisos.", e)
        }
    }

    // Crear un canal de Notificación
    private fun createNotificationChannel(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Reservas"
            val descriptionText = "Notificaciones para alertar sobre el inicio de una reserva de sala."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(idCanal, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
