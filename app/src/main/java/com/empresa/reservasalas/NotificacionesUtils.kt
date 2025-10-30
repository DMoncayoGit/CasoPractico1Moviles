package com.empresa.reservasalas

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object NotificacionesUtils {

    // Constante clave para la acción del BroadcastReceiver y AndroidManifest
    const val ACTION_RESERVATION_REMINDER = "com.empresa.reservasalas.ACTION_RESERVATION_REMINDER"

    // Claves estándar para pasar datos entre componentes (Broadcast y Notificación)
    const val EXTRA_TITLE = "RESERVA_TITULO"
    const val EXTRA_DETAIL = "RESERVA_DETALLE"
    const val NOTIFICATION_ID = "NOTIFICACION_ID"

    fun scheduleReservationReminders(context: Context, reservaTitulo: String, reservaTime: Calendar) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //Checar el permiso de alarma exacta
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("NotificationUtils", "Permiso de alarma exacta no concedido. Solicitando.")
                Toast.makeText(context, "Por favor, otorga el permiso de alarmas exactas para recibir recordatorios.", Toast.LENGTH_LONG).show()
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                return
            }
        }

        // Si el permiso está OK o no es necesario, procedemos
        val minutoAntes = reservaTime.clone() as Calendar
        minutoAntes.add(Calendar.MINUTE, -1) // La alarma se dispara 1 minuto antes

        // Generar un ID único para poder cancelar la alarma
        // Usar el hash del título + tiempo en milisegundos para generar un ID único para la reserva
        val notificationId = reservaTitulo.hashCode() + minutoAntes.timeInMillis.toInt()

        // Preparar el mensaje de detalle
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaStr = timeFormatter.format(reservaTime.time)
        val detailMessage = "Tu reserva de $reservaTitulo comienza en 1 minuto (a las $horaStr)."

        scheduleSingleAlarm(
            context,
            alarmManager,
            reservaTitulo,
            detailMessage,
            minutoAntes.timeInMillis,
            notificationId
        )
    }

    // Programar alarma
    private fun scheduleSingleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        titulo: String,
        detail: String,
        triggerAtMillis: Long,
        notificationId: Int
    ) {
        val intent = Intent(context, Recordatorio::class.java).apply {
            action = ACTION_RESERVATION_REMINDER

            // Usamos las constantes
            putExtra(EXTRA_TITLE, titulo)
            putExtra(EXTRA_DETAIL, detail)
            putExtra(NOTIFICATION_ID, notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Usar setExactAndAllowWhileIdle para máxima precisión
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        Log.d("NotificationUtils", "Alarma programada con ID $notificationId para ${Calendar.getInstance().apply { timeInMillis = triggerAtMillis }.time}")
    }

    // Cancelar una alarma de recordatorio pendiente usando el mismo ID y acción.
    fun cancelReservationReminder(context: Context, reservaTitulo: String, reservaTime: Calendar) {
        val minutoAntes = reservaTime.clone() as Calendar
        minutoAntes.add(Calendar.MINUTE, -1)
        val notificationId = reservaTitulo.hashCode() + minutoAntes.timeInMillis.toInt()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, Recordatorio::class.java).apply {
            action = ACTION_RESERVATION_REMINDER
        }

        // Buscar el PendingIntent
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Log.d("NotificationUtils", "Alarma con ID $notificationId cancelada.")
        } else {
            Log.d("NotificationUtils", "No se encontró alarma con ID $notificationId para cancelar.")
        }
    }
}
