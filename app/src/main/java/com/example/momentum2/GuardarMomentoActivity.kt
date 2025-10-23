package com.example.momentum2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.momentum2.databinding.ActivityGuardarMomentoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class GuardarMomentoActivity : AppCompatActivity(), LocationListener {

    private lateinit var binding: ActivityGuardarMomentoBinding
    private lateinit var locationManager: LocationManager
    private val firestore = FirebaseFirestore.getInstance()
    private var fotoUri: Uri? = null
    private var latitud = 0.0
    private var longitud = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardarMomentoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Recibir la imagen desde MainActivity ---
        val bytes = intent.getByteArrayExtra("foto")
        if (bytes != null) {
            val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(bytes))
            binding.imageViewMomento.setImageBitmap(bitmap)
            fotoUri = guardarImagenEnUri(bitmap)
        }

        // --- Configurar LocationManager ---
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        obtenerUbicacion()

        binding.buttonGuardar.setOnClickListener {
            guardarMomentoEnFirestore()
        }
    }

    private fun guardarImagenEnUri(bitmap: android.graphics.Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "momento", null)
        return Uri.parse(path)
    }

    private fun obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )
            return
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L,
            1f,
            this
        )
    }

    override fun onLocationChanged(location: Location) {
        latitud = location.latitude
        longitud = location.longitude
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    private fun guardarMomentoEnFirestore() {
        val descripcion = binding.editTextDescripcion.text.toString().trim()
        if (descripcion.isEmpty() || fotoUri == null) {
            Toast.makeText(this, "Agrega una foto y una descripción", Toast.LENGTH_SHORT).show()
            return
        }

        if (descripcion.length > 140) {
            Toast.makeText(this, "La descripción no puede superar 140 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonimo"
        val momentoId = firestore.collection("momentos").document().id

        val storageRef = FirebaseStorage.getInstance().reference.child("momentos/$momentoId.jpg")
        storageRef.putFile(fotoUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val momento = Moment(
                        id = momentoId,
                        userId = userId,
                        fotoUrl = uri.toString(),
                        descripcion = descripcion,
                        fecha = fechaActual,
                        latitud = latitud,
                        longitud = longitud
                    )

                    firestore.collection("momentos")
                        .document(momentoId)
                        .set(momento)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Momento guardado correctamente en Firestore", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir la foto", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(this)
        }
    }
}
