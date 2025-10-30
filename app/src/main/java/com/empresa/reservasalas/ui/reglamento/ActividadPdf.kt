package com.empresa.reservasalas.ui.reglamento

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.empresa.reservasalas.R

class ActividadPdf : AppCompatActivity() {

    private lateinit var webView: WebView

    companion object {
        // Clave para pasar la URL del PDF
        const val PDF_URL = "pdf_url"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.actividad_pdf)

        // Obtener la URL del PDF del Intent
        val pdfBaseUrl = intent.getStringExtra(PDF_URL)

        if (pdfBaseUrl.isNullOrEmpty()) {
            // Si la URL es nula o vacía, simplemente cerramos la Activity
            finish()
            return
        }

        // Obtener la referencia a la WebView
        webView = findViewById(R.id.webViewPdfActivity)

        // Construir la URL para el visor de Google Docs.
        val googleDocsViewerUrl = "https://docs.google.com/gview?embedded=true&url=$pdfBaseUrl"

        // Configurar y cargar el WebView
        webView.settings.javaScriptEnabled = true
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.webViewClient = WebViewClient()

        // Cargar la URL 
        webView.loadUrl(googleDocsViewerUrl)
    }

}