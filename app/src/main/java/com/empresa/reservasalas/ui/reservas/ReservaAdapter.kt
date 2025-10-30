package com.empresa.reservasalas.ui.reservas

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.empresa.reservasalas.R
import com.empresa.reservasalas.data.EstadoReserva
import com.empresa.reservasalas.data.Reserva
import androidx.core.graphics.toColorInt

class ReservaAdapter(
    private var reservas: List<Reserva>,
    private val onCancelClick: (Reserva) -> Unit,
    private val onEditClick: (Reserva) -> Unit
) : RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder>() {

    class ReservaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sala: TextView = view.findViewById(R.id.tv_reserva_sala)
        val fechaHora: TextView = view.findViewById(R.id.tv_reserva_fecha_hora)
        val estado: TextView = view.findViewById(R.id.tv_reserva_estado)
        val cancelarBoton: Button = view.findViewById(R.id.btn_reserva_cancelar)
        val editarBoton: Button = view.findViewById(R.id.btn_reserva_editar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reserva, parent, false)
        return ReservaViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ReservaViewHolder, position: Int) {
        val reserva = reservas[position]

        holder.sala.text = "Sala: ${reserva.salaNombre}"
        holder.fechaHora.text = "Fecha: ${reserva.fecha} | Hora: ${reserva.horaInicio} - ${reserva.horaFin}"
        holder.estado.text = "Estado: ${reserva.estado.value}"

        if (reserva.estado == EstadoReserva.ACTIVA) {
            holder.estado.setTextColor("#007F00".toColorInt()) // Verde

            // Lógica para el botón Cancelar
            holder.cancelarBoton.visibility = View.VISIBLE
            holder.cancelarBoton.setOnClickListener {
                onCancelClick(reserva)
            }

            // Lógica para el botón Editar
            holder.editarBoton.visibility = View.VISIBLE
            holder.editarBoton.setOnClickListener {
                onEditClick(reserva)
            }

        } else {
            holder.estado.setTextColor(Color.RED)

            // Ocultar botones si está cancelada
            holder.cancelarBoton.visibility = View.GONE
            holder.editarBoton.visibility = View.GONE

            holder.cancelarBoton.setOnClickListener(null)
            holder.editarBoton.setOnClickListener(null)
        }
    }

    override fun getItemCount() = reservas.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateReservas(newReservas: List<Reserva>) {
        reservas = newReservas
        notifyDataSetChanged()
    }
}