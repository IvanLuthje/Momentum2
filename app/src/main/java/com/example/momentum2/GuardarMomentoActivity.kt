package com.example.momentum2

import android.Manifest
import android.R.attr.bitmap
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.cloudinary.android.MediaManager
import com.example.momentum2.databinding.ActivityGuardarMomentoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayInputStream
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

        val config = HashMap<String, String>()
        config["cloud_name"] = "dbcbevbty"
        config["api_key"] = "687437519863617"
        config["api_secret"] = "IvNX9eP8pSV22c4LS4ZNn3-vZQM"

        MediaManager.init(this, config)



        fotoUri = guardarImagenEnUri(bitmap)

        // --- Recibir la imagen desde MainActivity ---
        val bytes = intent.getByteArrayExtra("foto")
        if (bytes != null) {
            val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(bytes))
            binding.imageViewMomento.setImageBitmap(bitmap)
        }

        // --- Configurar LocationManager ---
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        obtenerUbicacion()

        binding.buttonGuardar.setOnClickListener {
            guardarMomento()
        }
    }

    private fun guardarImagenEnUri(bitmap: Int): Uri? {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "momento_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/momentos")
            }

            val resolver = contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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

    private fun guardarMomento() {
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
        val momentoId = FirebaseFirestore.getInstance().collection("momentos").document().id

        Toast.makeText(this, "Subiendo imagen", Toast.LENGTH_SHORT).show()

        // Configuración Cloudinary


        // Subir imagen
       MediaManager.get().upload(fotoUri)
            .option("upload_preset", "momento_preset")
            .option("folder", "momentos")
            .callback(object : com.cloudinary.android.callback.UploadCallback {
                override fun onStart(requestId: String?) {}

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val imageUrl = resultData?.get("secure_url") as? String ?: return

                    val momento = Moment(
                        id = momentoId,
                        userId = userId,
                        fotoUrl = imageUrl,
                        descripcion = descripcion,
                        fecha = fechaActual,
                        latitud = latitud,
                        longitud = longitud
                    )

                    FirebaseFirestore.getInstance().collection("momentos")
                        .document(momentoId)
                        .set(momento)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@GuardarMomentoActivity,
                                "Momento guardado correctamente",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@GuardarMomentoActivity, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show()
                        }
                }

                override fun onError(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {
                    Toast.makeText(this@GuardarMomentoActivity, "Error al subir imagen: ${error?.description}", Toast.LENGTH_SHORT).show()
                }

                override fun onReschedule(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {}
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(this)
        }
    }
}

data class Moment(
    val id: String = "",
    val userId: String = "",
    val fotoUrl: String = "",
    val descripcion: String = "",
    val fecha: String,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)