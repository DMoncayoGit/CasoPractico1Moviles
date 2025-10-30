package com.empresa.reservasalas.ui.reglamento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.empresa.reservasalas.R
import android.content.Intent

class ReglamentoFragmento : Fragment() {

    // URL
    private val PDFMANUAL = "https://caso01dm.alojamiento.monster/manualdmoncayo.pdf"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout que ahora contiene el botón
        return inflater.inflate(R.layout.fragmento_reglamento, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val button: Button = view.findViewById(R.id.btnAbrirReglamentoPdf)

        button.setOnClickListener {

            // Crear el Intent para lanzar la Activity del visor
            val intent = Intent(requireContext(), ActividadPdf::class.java).apply {
                // Pasar la URL del PDF como un extra
                putExtra(ActividadPdf.PDF_URL, PDFMANUAL)
            }
            startActivity(intent)
        }
    }
}