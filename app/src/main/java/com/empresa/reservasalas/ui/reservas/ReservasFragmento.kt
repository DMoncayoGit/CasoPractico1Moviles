package com.empresa.reservasalas.ui.reservas

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.empresa.reservasalas.R
import com.empresa.reservasalas.data.Reserva
import com.empresa.reservasalas.data.ReservaRepository
import com.empresa.reservasalas.data.SessionManager
import com.empresa.reservasalas.NotificacionesUtils
import java.text.SimpleDateFormat
import java.util.*

// Constante para saber si estamos creando o editando, para el diálogo
private const val ACTION_CREATE = 0
private const val ACTION_EDIT = 1

class ReservasFragmento : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var reservaRepository: ReservaRepository
    private lateinit var reservaAdapter: ReservaAdapter
    private lateinit var rvReservas: RecyclerView
    private lateinit var btnNuevaReserva: Button

    // Calendario para manejar correctamente la fecha y hora seleccionadas
    private var finalReservaCalendar: Calendar = Calendar.getInstance()

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Reserva pendiente mientras se esperan los permisos
    private var pendingReserva: Reserva? = null

    // Acción pendiente
    private var pendingAction: Int = ACTION_CREATE

    // Manejar el resultado de la solicitud del permiso POST_NOTIFICATIONS
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("ReservasFragment", "Permiso de Notificaciones concedido.")

                // Si se concede, se procede con la acción pendiente
                if (pendingAction == ACTION_EDIT) {
                    attemptToUpdateReserva()
                } else {
                    attemptToCreateReserva()
                }
            } else {
                Toast.makeText(context, "Permiso de notificaciones denegado. No se programarán recordatorios.", Toast.LENGTH_LONG).show()

                // Aunque se deniegue, se procede con la acción sin notificación
                if (pendingAction == ACTION_EDIT) {
                    attemptToUpdateReserva(scheduleNotification = false)
                } else {
                    attemptToCreateReserva(scheduleNotification = false)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragmento_reservas, container, false)

        sessionManager = SessionManager(requireContext())
        reservaRepository = ReservaRepository(requireContext())

        rvReservas = root.findViewById(R.id.rv_reservas)
        btnNuevaReserva = root.findViewById(R.id.btn_nueva_reserva)

        setupRecyclerView()
        setupListeners()

        return root
    }

    override fun onResume() {
        super.onResume()
        loadReservas()
    }

    private fun setupRecyclerView() {
        reservaAdapter = ReservaAdapter(
            emptyList(),
            // Manejar el clic de cancelar
            onCancelClick = { reserva -> confirmCancel(reserva)
            },
            // Manejar el clic de edición
            onEditClick = { reserva -> showEditReservaDialog(reserva.id)
            }
        )
        rvReservas.layoutManager = LinearLayoutManager(context)
        rvReservas.adapter = reservaAdapter
    }

    private fun setupListeners() {
        btnNuevaReserva.setOnClickListener {
            showCreateReservaDialog()
        }
    }

    // Cargar Reservas
    private fun loadReservas() {
        val userEmail = sessionManager.getProfileDetails()["email"]
        if (!userEmail.isNullOrEmpty()) {
            val reservas = reservaRepository.getReservasByUser(userEmail)
            reservaAdapter.updateReservas(reservas)
        } else {
            Toast.makeText(context, "Error: No se encontró el email de sesión.", Toast.LENGTH_SHORT).show()
        }
    }

    // Cancelar la Reserva
    private fun confirmCancel(reserva: Reserva) {
        AlertDialog.Builder(context)
            .setTitle("Confirmar Cancelación")
            .setMessage("¿Estás seguro de que deseas cancelar la reserva de la sala ${reserva.salaNombre} el ${reserva.fecha}?")
            .setPositiveButton("Sí, Cancelar") { dialog, _ ->
                val rowsAffected = reservaRepository.cancelReserva(reserva.id)
                if (rowsAffected > 0) {
                    Toast.makeText(context, "Reserva cancelada con éxito.", Toast.LENGTH_SHORT).show()

                    // Cancelar la alarma
                    cancelReminderForReserva(reserva)

                    loadReservas()
                } else {
                    Toast.makeText(context, "Error al cancelar la reserva.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("No", null)
            .show()
    }

    // Crear la reserva pero primero se pide permisos si es necesario.
    private fun attemptCreateReserva(nuevaReserva: Reserva) {
        pendingReserva = nuevaReserva
        pendingAction = ACTION_CREATE
        requestNotificationPermission()
    }

    // Finalizar la creación de la reserva y programar la notificación.
    private fun attemptToCreateReserva(scheduleNotification: Boolean = true) {
        val reserva = pendingReserva ?: return

        // Creación de la reserva en la base de datos
        val newRowId = reservaRepository.createReserva(reserva)

        if (newRowId > 0) {
            val createdReservaWithId = reserva.copy(id = newRowId.toInt())
            handleNotificationScheduling(createdReservaWithId, scheduleNotification, "creada")

            loadReservas()
            pendingReserva = null
        } else {
            Toast.makeText(context, "Error al crear la reserva. Intente de nuevo.", Toast.LENGTH_LONG).show()
        }
    }

    // Mostrar el diálogo de edición con los datos precargados
    private fun showEditReservaDialog(reservaId: Int) {
        val reserva = reservaRepository.getReservaById(reservaId)

        if (reserva == null) {
            Toast.makeText(context, "No se encontró la reserva para editar.", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialogo_crear_reserva, null)
        builder.setView(dialogView)
        builder.setTitle("Editar Reserva de Sala")

        val spinnerSalas = dialogView.findViewById<Spinner>(R.id.spinner_salas)
        val etFecha = dialogView.findViewById<EditText>(R.id.et_reserva_fecha)
        val etHoraInicio = dialogView.findViewById<EditText>(R.id.et_hora_inicio)
        val etHoraFin = dialogView.findViewById<EditText>(R.id.et_hora_fin)

        val salaNames = reservaRepository.getAvailableSalaNames()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, salaNames)
        spinnerSalas.adapter = adapter

        // Precargar datos
        etFecha.setText(reserva.fecha)
        etHoraInicio.setText(reserva.horaInicio)
        etHoraFin.setText(reserva.horaFin)

        // Establecer el spinner en la sala actual
        val salaIndex = salaNames.indexOf(reserva.salaNombre)
        if (salaIndex >= 0) {
            spinnerSalas.setSelection(salaIndex)
        }

        // Configurar el calendario global con la fecha/hora de la reserva
        val fullDateTimeStr = "${reserva.fecha} ${reserva.horaInicio}"
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        try {
            val dateObj = dateTimeFormat.parse(fullDateTimeStr)
            if (dateObj != null) {
                finalReservaCalendar.time = dateObj
            }
        } catch (e: Exception) {
            Log.e("ReservasFragment", "Error al parsear fecha para edición: ${e.message}")
            finalReservaCalendar = Calendar.getInstance() // Fallback
        }


        etFecha.setOnClickListener {
            val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                finalReservaCalendar.set(Calendar.YEAR, year)
                finalReservaCalendar.set(Calendar.MONTH, month)
                finalReservaCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                etFecha.setText(dateFormatter.format(finalReservaCalendar.time))
            }, finalReservaCalendar.get(Calendar.YEAR), finalReservaCalendar.get(Calendar.MONTH), finalReservaCalendar.get(Calendar.DAY_OF_MONTH))

            datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
        }

        etHoraInicio.setOnClickListener { showTimePicker(etHoraInicio) }
        etHoraFin.setOnClickListener { showTimePicker(etHoraFin) }

        val dialog = builder.create()

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Guardar Cambios") { _, _ ->
            val salaNombre = spinnerSalas.selectedItem.toString()
            val fecha = etFecha.text.toString()
            val horaInicio = etHoraInicio.text.toString()
            val horaFin = etHoraFin.text.toString()
            val userEmail = sessionManager.getProfileDetails()["email"] ?: ""

            if (userEmail.isEmpty() || fecha.isEmpty() || horaInicio.isEmpty() || horaFin.isEmpty()) {
                Toast.makeText(context, "Complete todos los campos de fecha y hora.", Toast.LENGTH_LONG).show()
                return@setButton
            }

            if (horaInicio >= horaFin) {
                Toast.makeText(context, "La hora de inicio debe ser anterior a la hora de fin.", Toast.LENGTH_LONG).show()
                return@setButton
            }

            // Crear una nueva instancia de Reserva con el ID existente
            val reservaActualizada = reserva.copy(
                salaNombre = salaNombre,
                fecha = fecha,
                horaInicio = horaInicio,
                horaFin = horaFin
            )

            attemptUpdateReserva(reservaActualizada)
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancelar") { d, _ -> d.dismiss() }
        dialog.show()
    }

    // Intenta actualizar la reserva
    private fun attemptUpdateReserva(reservaActualizada: Reserva) {
        pendingReserva = reservaActualizada
        pendingAction = ACTION_EDIT

        requestNotificationPermission()
    }

    // Finalizar la actualización de la reserva y reprograma la notificación
    private fun attemptToUpdateReserva(scheduleNotification: Boolean = true) {
        val reservaActualizada = pendingReserva ?: return

        // Cancelar la alarma antigua antes de actualizar.
        val oldReserva = reservaRepository.getReservaById(reservaActualizada.id)
        if (oldReserva != null) {
            cancelReminderForReserva(oldReserva)
        }

        //Actualización en la base de datos
        val rowsAffected = reservaRepository.updateReserva(reservaActualizada)

        if (rowsAffected > 0) {
            handleNotificationScheduling(reservaActualizada, scheduleNotification, "actualizada")
            loadReservas()
            pendingReserva = null
        } else {
            Toast.makeText(context, "Error al actualizar la reserva. Intente de nuevo.", Toast.LENGTH_LONG).show()
        }
    }

    // Pedir permiso de notificación
    private fun requestNotificationPermission() {
        // Android 13+ (API 33): Requiere solicitar POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Solicitar permiso de notificaciones
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        // Si no se necesita el permiso o ya está concedido, se continua con la acción pendiente
        if (pendingAction == ACTION_EDIT) {
            attemptToUpdateReserva()
        } else {
            attemptToCreateReserva()
        }
    }

    // Función para manejar la programación de la notificación
    private fun handleNotificationScheduling(reserva: Reserva, schedule: Boolean, action: String) {
        if (schedule) {
            val fullDateTimeStr = "${reserva.fecha} ${reserva.horaInicio}"
            val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            try {
                val dateObj = dateTimeFormat.parse(fullDateTimeStr)
                val reservaCalendar = Calendar.getInstance().apply {
                    if (dateObj != null) {
                        time = dateObj
                    }
                }

                val notificationTimeMs = reservaCalendar.timeInMillis - (60 * 1000) // 1 minuto antes

                if (notificationTimeMs <= System.currentTimeMillis() + 5000) {
                    Log.w("ReservaFragment", "La hora de la reserva está demasiado cerca o en el pasado. Saltando la programación de recordatorio.")
                    Toast.makeText(context, "Reserva $action. El tiempo de inicio es demasiado cercano para programar el recordatorio.", Toast.LENGTH_LONG).show()

                } else {
                    // Programar la alarma
                    NotificacionesUtils.scheduleReservationReminders(
                        requireContext(),
                        reserva.salaNombre,
                        reservaCalendar
                    )
                    Toast.makeText(context, "Reserva de ${reserva.salaNombre} $action y recordatorio programado!", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("ReservaFragment", "Error al programar recordatorio. String: '$fullDateTimeStr'", e)
                Toast.makeText(context, "Reserva $action, pero error al programar recordatorio.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Reserva de ${reserva.salaNombre} $action. Recordatorios deshabilitados por falta de permisos.", Toast.LENGTH_LONG).show()
        }
    }

    // Cancelar la alarma de una reserva específica
    private fun cancelReminderForReserva(reserva: Reserva) {
        try {
            val fullDateTimeStr = "${reserva.fecha} ${reserva.horaInicio}"
            val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dateObj = dateTimeFormat.parse(fullDateTimeStr)
            val reservaCalendar = Calendar.getInstance().apply {
                if (dateObj != null) {
                    time = dateObj
                }
            }

            // Llamar a la función de cancelación en NotificationUtils
            NotificacionesUtils.cancelReservationReminder(requireContext(), reserva.salaNombre, reservaCalendar)
            Log.i("ReservasFragment", "Alarma de reserva ID ${reserva.id} cancelada.")
        } catch (e: Exception) {
            Log.e("ReservasFragment", "Error al intentar cancelar la alarma para ID ${reserva.id}.", e)
        }
    }

    // Mostrar el diálogo para crear una nueva reserva
    private fun showCreateReservaDialog() {
        val builder = AlertDialog.Builder(context)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialogo_crear_reserva, null)
        builder.setView(dialogView)
        builder.setTitle("Nueva Reserva de Sala")

        val spinnerSalas = dialogView.findViewById<Spinner>(R.id.spinner_salas)
        val etFecha = dialogView.findViewById<EditText>(R.id.et_reserva_fecha)
        val etHoraInicio = dialogView.findViewById<EditText>(R.id.et_hora_inicio)
        val etHoraFin = dialogView.findViewById<EditText>(R.id.et_hora_fin)

        val salaNames = reservaRepository.getAvailableSalaNames()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, salaNames)
        spinnerSalas.adapter = adapter

        // Inicializar el calendario global con la hora actual
        finalReservaCalendar = Calendar.getInstance()
        etFecha.setText(dateFormatter.format(finalReservaCalendar.time))
        etHoraInicio.setText("")
        etHoraFin.setText("")

        etFecha.setOnClickListener {
            val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                finalReservaCalendar.set(Calendar.YEAR, year)
                finalReservaCalendar.set(Calendar.MONTH, month)
                finalReservaCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                etFecha.setText(dateFormatter.format(finalReservaCalendar.time))
            }, finalReservaCalendar.get(Calendar.YEAR), finalReservaCalendar.get(Calendar.MONTH), finalReservaCalendar.get(Calendar.DAY_OF_MONTH))

            datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
        }

        etHoraInicio.setOnClickListener { showTimePicker(etHoraInicio) }
        etHoraFin.setOnClickListener { showTimePicker(etHoraFin) }

        val dialog = builder.create()

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Crear") { _, _ ->
            val salaNombre = spinnerSalas.selectedItem.toString()
            val fecha = etFecha.text.toString()
            val horaInicio = etHoraInicio.text.toString()
            val horaFin = etHoraFin.text.toString()
            val userEmail = sessionManager.getProfileDetails()["email"] ?: ""

            if (userEmail.isEmpty() || fecha.isEmpty() || horaInicio.isEmpty() || horaFin.isEmpty()) {
                Toast.makeText(context, "Complete todos los campos de fecha y hora.", Toast.LENGTH_LONG).show()
                return@setButton
            }

            if (horaInicio >= horaFin) {
                Toast.makeText(context, "La hora de inicio debe ser anterior a la hora de fin.", Toast.LENGTH_LONG).show()
                return@setButton
            }

            val nuevaReserva = Reserva(
                usuarioEmail = userEmail,
                salaNombre = salaNombre,
                fecha = fecha,
                horaInicio = horaInicio,
                horaFin = horaFin
            )

            attemptCreateReserva(nuevaReserva)
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancelar") { d, _ -> d.dismiss() }
        dialog.show()
    }

    // Mostrar el TimePicker y actualizar la hora
    private fun showTimePicker(editText: EditText) {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            finalReservaCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            finalReservaCalendar.set(Calendar.MINUTE, minute)
            finalReservaCalendar.set(Calendar.SECOND, 0)
            finalReservaCalendar.set(Calendar.MILLISECOND, 0)

            editText.setText(timeFormatter.format(finalReservaCalendar.time))
        }

        TimePickerDialog(
            requireContext(),
            timeSetListener,
            finalReservaCalendar.get(Calendar.HOUR_OF_DAY),
            finalReservaCalendar.get(Calendar.MINUTE),
            true
        ).show()
    }
}