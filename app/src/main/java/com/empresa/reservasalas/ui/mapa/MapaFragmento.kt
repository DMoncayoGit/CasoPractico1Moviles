package com.empresa.reservasalas.ui.mapa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.empresa.reservasalas.R
import com.empresa.reservasalas.ui.perfil.PerfilViewModel.SaveStatus
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class MapaFragmento : Fragment() {

    private lateinit var mapView: MapView

    // Coordenadas del Campus Rumipamba (Universidad UTE)
    private val latitud = -0.1840
    private val longitud = -78.4931
    private val zoom = 20.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Cargar la configuración de OSMdroid.
        Configuration.getInstance().load(
            context,
            activity?.getPreferences(android.content.Context.MODE_PRIVATE)
        )

        val view = inflater.inflate(R.layout.fragmento_mapa, container, false)
        mapView = view.findViewById(R.id.mapView)

        // Configurar la fuente de mapas a OpenStreetMap estándar
        mapView.setTileSource(TileSourceFactory.MAPNIK)

        // Habilitar gestos de zoom y multi-touch
        mapView.setMultiTouchControls(true)

        // Establecer el punto central y el nivel de zoom inicial
        val mapController = mapView.controller
        mapController.setZoom(zoom)
        val startPoint = GeoPoint(latitud, longitud)
        mapController.setCenter(startPoint)

        return view
    }

    // Manejo del ciclo de vida del fragmento para liberar y reanudar recursos de OSMdroid
    override fun onResume() {
        super.onResume()
        // Cargar los mosaicos del mapa
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        // Detener la carga de mosaicos y liberar la memoria
        mapView.onPause()
    }
}