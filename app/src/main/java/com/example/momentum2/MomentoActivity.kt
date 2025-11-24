package com.example.momentum2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.momentum2.databinding.ActivityMomentoBinding
import com.example.momentum2.ui.login.LoginActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import com.google.firebase.firestore.FirebaseFirestore
import org.maplibre.android.BuildConfig
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.maps.Style
import com.example.momentum2.Moment



class MomentoActivity : AppCompatActivity() {


    private lateinit var mapView: MapView



    private val firestore = FirebaseFirestore.getInstance()



    private lateinit var binding: ActivityMomentoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(this)

        binding = ActivityMomentoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = binding.mapView

        val editDesc = binding.textViewDescripcion

        val lat = intent.getDoubleExtra("latitud", 0.0)
        val lon = intent.getDoubleExtra("longitud", 0.0)
        val desc = intent.getStringExtra("descripcion")


        editDesc.setOnClickListener { view ->
            editDescripcion()
        }


        mapView.getMapAsync { map ->

            val styleUrl = "https://api.maptiler.com/maps/streets-v2/style.json?key=YlHNYGKTfem7dvGeeE3c"

            map.setStyle(styleUrl)
            val pos= LatLng(lat, lon) // Example: Los Angeles
            val marker = MarkerOptions()
                .position(pos)
                .title(desc)

            map.addMarker(marker)
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(lat, lon))
                .zoom(15.0)
                .build()

        }

        val momentoId = intent.getStringExtra("id")
        if (momentoId == null) {
            Toast.makeText(this, "Error de ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            cargarMomento(momentoId)
        }

        binding.swipeRefreshLayout.post {
            binding.swipeRefreshLayout.isRefreshing = true
            cargarMomento(momentoId)
        }



        cargarMomento(momentoId)

    }



    private fun editDescripcion() {
        val momentoId = intent.getStringExtra("id") ?: return
        val descripcionActual = binding.textViewDescripcion.text.toString()

        val intentDesc = Intent(this, EditDescripcion::class.java).apply {
            putExtra("id", momentoId)
            putExtra("descripcion", descripcionActual)
        }

        startActivity(intentDesc)
    }



    private fun cargarMomento(id: String) {

        Handler(Looper.getMainLooper()).postDelayed({
            binding.swipeRefreshLayout.isRefreshing = false

        }, 2000)

        firestore.collection("momentos").document(id)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Momento no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }



                val descripcion = doc.getString("descripcion") ?: ""
                val fotoUrl = doc.getString("fotoUrl") ?: ""
                val lat = doc.getDouble("latitud") ?: 0.0
                val lon = doc.getDouble("longitud") ?: 0.0
                val fecha = doc.getString("fecha")?: ""

                binding.textViewDescripcion.text = descripcion
                binding.textViewFecha.text = "Fecha: $fecha"
                binding.textViewCoordenadas.text = "Coordenadas: $lat, $lon"



                Glide.with(this).load(fotoUrl).into(binding.imageViewFoto)
                binding.swipeRefreshLayout.isRefreshing = false

            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false

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
