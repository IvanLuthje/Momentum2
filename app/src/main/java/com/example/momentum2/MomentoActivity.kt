package com.example.momentum2

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.momentum2.databinding.ActivityMomentoBinding
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import com.google.firebase.firestore.FirebaseFirestore


class MomentoActivity : AppCompatActivity() {


    private lateinit var mapView: MapView

    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var binding: ActivityMomentoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(this)

        binding = ActivityMomentoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Accede al mapView a través del binding
        mapView = binding.mapView

        mapView.getMapAsync { map ->
            map.setStyle("https://demotiles.maplibre.org/style.json")
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(0.0, 0.0))
                .zoom(14.0)
                .build()
        }
        


        //cargarMomento()
    }



    private fun cargarMomento(id: String) {
        firestore.collection("momentos").document(id)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Momento no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                // Leer datos
                val descripcion = doc.getString("descripcion") ?: ""
                val fotoUrl = doc.getString("fotoUrl") ?: ""
                val tags = doc.get("tags") as? List<String> ?: emptyList()
                val lat = doc.getDouble("latitud") ?: 0.0
                val lon = doc.getDouble("longitud") ?: 0.0
                val fecha = doc.getTimestamp("fecha")?.toDate()

                // Mostrar en UI
                binding.textViewDescripcion.text = descripcion
                binding.textViewFecha.text = "Fecha: $fecha"

                Glide.with(this).load(fotoUrl).into(binding.imageViewFoto)

               // mostrarMapa(lat, lon)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}