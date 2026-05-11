package com.example.momentum2

import android.Manifest
import android.R.attr.bitmap
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import com.example.momentum2.Moment




class GuardarMomentoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardarMomentoBinding
    private lateinit var locationManager: LocationManager
    private val firestore = FirebaseFirestore.getInstance()
    private var fotoUri: Uri? = null
    private var latitud = 0.0
    private var longitud = 0.0

    private var locationCallback: (() -> Unit)? = null


    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            latitud = location.latitude
            longitud = location.longitude
            locationManager.removeUpdates(this)
            locationCallback?.invoke()
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String) {
        }

        override fun onProviderDisabled(provider: String) {
            Toast.makeText(this@GuardarMomentoActivity, "Por favor, activar la ubicación.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardarMomentoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val name= BuildConfig.CLOUDINARY_NAME
        val key = BuildConfig.CLOUDINARY_API_KEY
        val secret = BuildConfig.CLOUDINARY_API_SECRET



        val config = mapOf(
                "cloud_name" to name,
                "api_key" to key,
                "api_secret" to secret
            )
            MediaManager.init(this, config)


        val bytes = intent.getByteArrayExtra("foto")
        if (bytes != null) {
            val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(bytes))
            binding.imageViewMomento.setImageBitmap(bitmap)
            fotoUri = guardarImagen(bitmap) 
        }



        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        obtenerUbicacion()

        binding.buttonGuardar.setOnClickListener {
            guardarMomento()
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

        val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (lastLocation != null) {
            latitud = lastLocation.latitude
            longitud = lastLocation.longitude
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L,
            1f,
            locationListener
        )
    }




    private fun guardarImagen(bitmap: Bitmap): Uri? {
        return try {
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "momento_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/momentos")
                put(MediaStore.Images.Media.IS_PENDING, 1) 
            }

            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (imageUri == null) {
                Toast.makeText(this, "Error al crear URI en MediaStore", Toast.LENGTH_SHORT).show()
                return null
            }

            resolver.openOutputStream(imageUri)?.use { outputStream ->
                val ok = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                if (!ok) throw Exception("Error al comprimir la imagen")
            } ?: throw Exception("No se pudo abrir OutputStream para $imageUri")

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(imageUri, contentValues, null, null)

            imageUri
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error guardando imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }


    private fun guardarMomento() {
        val descripcion = binding.editTextDescripcion.text.toString().trim()
        if (descripcion.isEmpty() || fotoUri == null) {
            Toast.makeText(this, "Por favor agregar una descripción", Toast.LENGTH_SHORT).show()
            return
        }

        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonimo"
        val momentoId = firestore.collection("momentos").document().id

        Toast.makeText(this, "Subiendo imagen", Toast.LENGTH_SHORT).show()

        MediaManager.get().upload(fotoUri)
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

                    firestore.collection("momentos")
                        .document(momentoId)
                        .set(momento)
                        .addOnSuccessListener {
                            Toast.makeText(this@GuardarMomentoActivity, "Momento guardado correctamente", Toast.LENGTH_LONG).show()
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
            }).dispatch()
    }



}

