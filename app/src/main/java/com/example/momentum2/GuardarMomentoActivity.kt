//package com.example.momentum2
//
//import android.Manifest
//import android.R.attr.bitmap
//import android.annotation.SuppressLint
//import android.content.ContentValues
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.location.Location
//import android.location.LocationListener
//import android.location.LocationManager
//import android.net.Uri
//import android.os.Bundle
//import android.os.Environment
//import android.provider.MediaStore
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import com.cloudinary.android.MediaManager
//import com.example.momentum2.databinding.ActivityGuardarMomentoBinding
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import java.io.ByteArrayInputStream
//import java.io.OutputStream
//import java.text.SimpleDateFormat
//import java.util.*
//import com.example.momentum2.Moment
//
//
//
//
//class GuardarMomentoActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityGuardarMomentoBinding
//    private lateinit var locationManager: LocationManager
//    private val firestore = FirebaseFirestore.getInstance()
//    private var fotoUri: Uri? = null
//    private var latitud = 0.0
//    private var longitud = 0.0
//
//    private var locationCallback: (() -> Unit)? = null
//
//
//    private val locationListener: LocationListener = object : LocationListener {
//        override fun onLocationChanged(location: Location) {
//            latitud = location.latitude
//            longitud = location.longitude
//            locationManager.removeUpdates(this)
//            locationCallback?.invoke()
//        }
//
//        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
//        }
//
//        override fun onProviderEnabled(provider: String) {
//        }
//
//        override fun onProviderDisabled(provider: String) {
//            Toast.makeText(this@GuardarMomentoActivity, "Por favor, activar la ubicación.", Toast.LENGTH_LONG).show()
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityGuardarMomentoBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        val name= BuildConfig.CLOUDINARY_NAME
//        val key = BuildConfig.CLOUDINARY_API_KEY
//        val secret = BuildConfig.CLOUDINARY_API_SECRET
//
//
//
//        val config = mapOf(
//                "cloud_name" to name,
//                "api_key" to key,
//                "api_secret" to secret
//            )
//            MediaManager.init(this, config)
//
//
//        val bytes = intent.getByteArrayExtra("foto")
//        if (bytes != null) {
//            val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(bytes))
//            binding.imageViewMomento.setImageBitmap(bitmap)
//            fotoUri = guardarImagen(bitmap)
//        }
//
//
//
//        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        obtenerUbicacion()
//
//        binding.buttonGuardar.setOnClickListener {
//            guardarMomento()
//        }
//    }
//
//    private fun obtenerUbicacion() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED &&
//            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
//                1
//            )
//            return
//        }
//
//        val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//        if (lastLocation != null) {
//            latitud = lastLocation.latitude
//            longitud = lastLocation.longitude
//        }
//
//        locationManager.requestLocationUpdates(
//            LocationManager.GPS_PROVIDER,
//            1000L,
//            1f,
//            locationListener
//        )
//    }
//
//
//
//
//    private fun guardarImagen(bitmap: Bitmap): Uri? {
//        return try {
//            val resolver = contentResolver
//            val contentValues = ContentValues().apply {
//                put(MediaStore.Images.Media.DISPLAY_NAME, "momento_${System.currentTimeMillis()}.jpg")
//                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/momentos")
//                put(MediaStore.Images.Media.IS_PENDING, 1)
//            }
//
//            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//
//            if (imageUri == null) {
//                Toast.makeText(this, "Error al crear URI en MediaStore", Toast.LENGTH_SHORT).show()
//                return null
//            }
//
//            resolver.openOutputStream(imageUri)?.use { outputStream ->
//                val ok = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//                if (!ok) throw Exception("Error al comprimir la imagen")
//            } ?: throw Exception("No se pudo abrir OutputStream para $imageUri")
//
//            contentValues.clear()
//            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
//            resolver.update(imageUri, contentValues, null, null)
//
//            imageUri
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Toast.makeText(this, "Error guardando imagen: ${e.message}", Toast.LENGTH_SHORT).show()
//            null
//        }
//    }
//
//
//    private fun guardarMomento() {
//        val descripcion = binding.editTextDescripcion.text.toString().trim()
//        if (descripcion.isEmpty() || fotoUri == null) {
//            Toast.makeText(this, "Por favor agregar una descripción", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
//        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonimo"
//        val momentoId = firestore.collection("momentos").document().id
//
//        Toast.makeText(this, "Subiendo imagen", Toast.LENGTH_SHORT).show()
//
//        MediaManager.get().upload(fotoUri)
//            .option("folder", "momentos")
//            .callback(object : com.cloudinary.android.callback.UploadCallback {
//                override fun onStart(requestId: String?) {}
//
//                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
//
//                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
//                    val imageUrl = resultData?.get("secure_url") as? String ?: return
//
//                    val momento = Moment(
//                        id = momentoId,
//                        userId = userId,
//                        fotoUrl = imageUrl,
//                        descripcion = descripcion,
//                        fecha = fechaActual,
//                        latitud = latitud,
//                        longitud = longitud
//                    )
//
//                    firestore.collection("momentos")
//                        .document(momentoId)
//                        .set(momento)
//                        .addOnSuccessListener {
//                            Toast.makeText(this@GuardarMomentoActivity, "Momento guardado correctamente", Toast.LENGTH_LONG).show()
//                            finish()
//                        }
//                        .addOnFailureListener {
//                            Toast.makeText(this@GuardarMomentoActivity, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show()
//                        }
//                }
//
//                override fun onError(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {
//                    Toast.makeText(this@GuardarMomentoActivity, "Error al subir imagen: ${error?.description}", Toast.LENGTH_SHORT).show()
//                }
//
//                override fun onReschedule(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {}
//            }).dispatch()
//    }
//
//
//
//}
//

package com.example.momentum2

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.*

// ─── Colors ───────────────────────────────────────────────────────────────────
private val BgDeep       = Color(0xFF0D1117)
private val BgCard       = Color(0xFF161B22)
private val Accent       = Color(0xFF58A6FF)
private val AccentAlt    = Color(0xFF3FB950)
private val TextPrimary  = Color(0xFFE6EDF3)
private val TextMuted    = Color(0xFF7D8590)
private val FieldBg      = Color(0xFF0D1117)
private val FieldBorder  = Color(0xFF30363D)
private val ErrorColor   = Color(0xFFF85149)
private val WarningColor = Color(0xFFE3B341)

private val accentGradient = Brush.linearGradient(
    colors = listOf(Accent, AccentAlt),
    start  = Offset(0f, 0f),
    end    = Offset(400f, 100f)
)

// ─── Activity ─────────────────────────────────────────────────────────────────
class GuardarMomentoActivity : ComponentActivity() {

    private lateinit var locationManager: LocationManager
    private val firestore = FirebaseFirestore.getInstance()

    // Estado de ubicación compartido con el Composable
    private var latitud  = mutableDoubleStateOf(0.0)
    private var longitud = mutableDoubleStateOf(0.0)
    private var locationReady = mutableStateOf(false)

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            latitud.doubleValue  = location.latitude
            longitud.doubleValue = location.longitude
            locationReady.value  = true
            locationManager.removeUpdates(this)
        }
        override fun onProviderDisabled(provider: String) {}
        override fun onProviderEnabled(provider: String) {}
        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Cloudinary (igual que el original)
        val config = mapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_NAME,
            "api_key"    to BuildConfig.CLOUDINARY_API_KEY,
            "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
        )
        MediaManager.init(this, config)

        // Decodificar bitmap del Intent (igual que el original)
        val bytes  = intent.getByteArrayExtra("foto")
        val bitmap = bytes?.let { BitmapFactory.decodeStream(ByteArrayInputStream(it)) }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        obtenerUbicacion()

        setContent {
            MaterialTheme {
                GuardarMomentoScreen(
                    bitmap        = bitmap,
                    latitud       = latitud.doubleValue,
                    longitud      = longitud.doubleValue,
                    locationReady = locationReady.value,
                    firestore     = firestore,
                    onGuardarMomento = { descripcion, fotoUri, onSuccess, onError ->
                        guardarMomento(descripcion, fotoUri, onSuccess, onError)
                    },
                    guardarImagen = { bmp -> guardarImagen(bmp) },
                    onNavigateUp  = { finish() }
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
            return
        }

        // Última ubicación conocida (igual que el original)
        val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (lastLocation != null) {
            latitud.doubleValue  = lastLocation.latitude
            longitud.doubleValue = lastLocation.longitude
            locationReady.value  = true
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 1000L, 1f, locationListener
        )
    }

    // guardarImagen() — idéntico al original, sin cambios
    fun guardarImagen(bitmap: Bitmap): Uri? {
        return try {
            val resolver      = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "momento_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/momentos")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return null

            resolver.openOutputStream(imageUri)?.use { outputStream ->
                val ok = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                if (!ok) throw Exception("Error al comprimir la imagen")
            } ?: throw Exception("No se pudo abrir OutputStream")

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(imageUri, contentValues, null, null)

            imageUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // guardarMomento() — idéntica lógica al original, expuesta como callback
    fun guardarMomento(
        descripcion: String,
        fotoUri: Uri,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val userId      = FirebaseAuth.getInstance().currentUser?.uid ?: "anonimo"
        val momentoId   = firestore.collection("momentos").document().id

        MediaManager.get().upload(fotoUri)
            .option("folder", "momentos")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val imageUrl = resultData?.get("secure_url") as? String
                        ?: return onError("No se obtuvo la URL de la imagen")

                    val momento = Moment(
                        id          = momentoId,
                        userId      = userId,
                        fotoUrl     = imageUrl,
                        descripcion = descripcion,
                        fecha       = fechaActual,
                        latitud     = latitud.doubleValue,
                        longitud    = longitud.doubleValue
                    )

                    firestore.collection("momentos")
                        .document(momentoId)
                        .set(momento)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onError("Error al guardar en Firestore") }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onError("Error al subir imagen: ${error?.description}")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
    }
}

// ─── Composable principal ─────────────────────────────────────────────────────
@Composable
fun GuardarMomentoScreen(
    bitmap: Bitmap?,
    latitud: Double,
    longitud: Double,
    locationReady: Boolean,
    firestore: FirebaseFirestore,
    onGuardarMomento: (String, Uri, () -> Unit, (String) -> Unit) -> Unit,
    guardarImagen: (Bitmap) -> Uri?,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current

    var descripcion  by remember { mutableStateOf("") }
    var fotoUri      by remember { mutableStateOf<Uri?>(null) }
    var isUploading  by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) }
    var errorMsg     by remember { mutableStateOf<String?>(null) }
    var successMsg   by remember { mutableStateOf<String?>(null) }

    // Guardar imagen en MediaStore al iniciar (igual que el original en onCreate)
    LaunchedEffect(bitmap) {
        if (bitmap != null && fotoUri == null) {
            fotoUri = guardarImagen(bitmap)
        }
    }

    val canGuardar = descripcion.isNotBlank() && fotoUri != null && !isUploading

    // Orb animado
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val orbY by infiniteTransition.animateFloat(
        initialValue = -20f, targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation  = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        // Orb decorativo
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (orbY - 60).dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(AccentAlt.copy(alpha = 0.14f), Color.Transparent)
                        )
                    )
                }
                .blur(90.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── TopBar ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = TextPrimary)
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    "Guardar momento",
                    fontSize      = 20.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = TextPrimary,
                    modifier      = Modifier.weight(1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // ── Imagen (equivalente a imageViewMomento) ───────────────────
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    shape  = RoundedCornerShape(20.dp),
                    color  = BgCard,
                    border = BorderStroke(1.dp, FieldBorder)
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap             = bitmap.asImageBitmap(),
                            contentDescription = "Foto del momento",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.BrokenImage, null, tint = TextMuted, modifier = Modifier.size(40.dp))
                                Text("Sin imagen", color = TextMuted, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // ── Card ubicación ────────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    color    = BgCard,
                    border   = BorderStroke(1.dp, FieldBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    if (locationReady) Accent.copy(0.12f) else WarningColor.copy(0.12f),
                                    CircleShape
                                )
                        ) {
                            if (locationReady) {
                                Icon(Icons.Default.LocationOn, null, tint = Accent, modifier = Modifier.size(20.dp))
                            } else {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(18.dp),
                                    color       = WarningColor,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                        Column {
                            Text(
                                text  = if (locationReady) "Ubicación obtenida" else "Obteniendo ubicación...",
                                color = if (locationReady) Accent else WarningColor,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            AnimatedVisibility(visible = locationReady) {
                                Text(
                                    "%.6f, %.6f".format(latitud, longitud),
                                    color    = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // ── Card descripción (equivalente a editTextDescripcion) ───────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(20.dp),
                    color    = BgCard,
                    border   = BorderStroke(1.dp, FieldBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier              = Modifier.padding(bottom = 10.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Accent, modifier = Modifier.size(16.dp))
                            Text(
                                "Descripción",
                                fontSize      = 12.sp,
                                color         = TextMuted,
                                fontWeight    = FontWeight.Medium,
                                letterSpacing = 0.4.sp
                            )
                        }
                        OutlinedTextField(
                            value         = descripcion,
                            onValueChange = { if (it.length <= 500) { descripcion = it; errorMsg = null } },
                            modifier      = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            placeholder   = { Text("Describí este momento...", color = TextMuted.copy(0.5f)) },
                            shape         = RoundedCornerShape(12.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = FieldBg,
                                unfocusedContainerColor = FieldBg,
                                focusedBorderColor      = Accent,
                                unfocusedBorderColor    = FieldBorder,
                                focusedTextColor        = TextPrimary,
                                unfocusedTextColor      = TextPrimary,
                                cursorColor             = Accent
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction      = ImeAction.Default
                            ),
                            maxLines = 8,
                            enabled  = !isUploading
                        )
                        Row(
                            modifier              = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                "${descripcion.length}/500",
                                fontSize = 11.sp,
                                color    = if (descripcion.length > 450) ErrorColor else TextMuted
                            )
                        }
                    }
                }

                // ── Progreso de subida ────────────────────────────────────────
                AnimatedVisibility(visible = isUploading) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(16.dp),
                        color    = BgCard,
                        border   = BorderStroke(1.dp, FieldBorder)
                    ) {
                        Column(
                            modifier              = Modifier.padding(16.dp),
                            verticalArrangement   = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(18.dp),
                                    color       = Accent,
                                    strokeWidth = 2.dp
                                )
                                Text("Subiendo imagen", color = TextMuted, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // ── Error banner ──────────────────────────────────────────────
                AnimatedVisibility(
                    visible = errorMsg != null,
                    enter   = fadeIn() + expandVertically(),
                    exit    = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ErrorColor.copy(0.12f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = ErrorColor, modifier = Modifier.size(16.dp))
                        Text(errorMsg ?: "", color = ErrorColor, fontSize = 13.sp)
                    }
                }

                // ── Éxito ─────────────────────────────────────────────────────
                AnimatedVisibility(
                    visible = successMsg != null,
                    enter   = fadeIn() + expandVertically(),
                    exit    = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AccentAlt.copy(0.12f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = AccentAlt, modifier = Modifier.size(16.dp))
                        Text(successMsg ?: "", color = AccentAlt, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Botón Guardar (equivalente a buttonGuardar) ───────────────
                Button(
                    onClick = {
                        val uri = fotoUri
                        if (descripcion.isBlank() || uri == null) {
                            errorMsg = "Por favor agregar una descripción"
                            return@Button
                        }
                        isUploading = true
                        errorMsg    = null

                        onGuardarMomento(
                            descripcion,
                            uri,
                            {   // onSuccess
                                isUploading = false
                                successMsg  = "Momento guardado correctamente"
                                android.os.Handler(android.os.Looper.getMainLooper())
                                    .postDelayed({ onNavigateUp() }, 900)
                            },
                            {   // onError
                                isUploading = false
                                errorMsg    = it
                            }
                        )
                    },
                    modifier       = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape          = RoundedCornerShape(16.dp),
                    colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    enabled        = canGuardar
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = if (!canGuardar)
                                    Brush.linearGradient(listOf(Accent.copy(0.4f), AccentAlt.copy(0.4f)))
                                else accentGradient,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState    = isUploading,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label          = "saveBtn"
                        ) { uploading ->
                            if (uploading) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(22.dp),
                                    color       = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CloudUpload, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Text(
                                        "Guardar momento",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize   = 16.sp,
                                        color      = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}