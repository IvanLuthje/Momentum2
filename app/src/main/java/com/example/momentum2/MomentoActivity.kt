//package com.example.momentum2
//
//
//import android.content.Intent
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.bumptech.glide.Glide
//import com.example.momentum2.databinding.ActivityMomentoBinding
//import com.google.firebase.firestore.FirebaseFirestore
//import org.maplibre.android.BuildConfig
//import org.maplibre.android.MapLibre
//import org.maplibre.android.annotations.MarkerOptions
//import org.maplibre.android.camera.CameraPosition
//import org.maplibre.android.geometry.LatLng
//import org.maplibre.android.maps.MapView
//
//
//
//class MomentoActivity : AppCompatActivity() {
//
//
//    private lateinit var mapView: MapView
//
//
//
//    private val firestore = FirebaseFirestore.getInstance()
//
//
//
//    private lateinit var binding: ActivityMomentoBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        MapLibre.getInstance(this)
//
//        binding = ActivityMomentoBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        mapView = binding.mapView
//
//        val editDesc = binding.textViewDescripcion
//
//        val lat = intent.getDoubleExtra("latitud", 0.0)
//        val lon = intent.getDoubleExtra("longitud", 0.0)
//        val desc = intent.getStringExtra("descripcion")
//        val key = com.example.momentum2.BuildConfig.MAPTILER_API_KEY
//
//
//        editDesc.setOnClickListener { view ->
//            editDescripcion()
//        }
//
//
//        mapView.getMapAsync { map ->
//
//            val styleUrl = "https://api.maptiler.com/maps/streets-v2/style.json?key=$key"
//
//            map.setStyle(styleUrl)
//            val pos= LatLng(lat, lon) // Example: Los Angeles
//            val marker = MarkerOptions()
//                .position(pos)
//                .title(desc)
//
//            map.addMarker(marker)
//            map.cameraPosition = CameraPosition.Builder()
//                .target(LatLng(lat, lon))
//                .zoom(15.0)
//                .build()
//
//        }
//
//        val momentoId = intent.getStringExtra("id")
//        if (momentoId == null) {
//            Toast.makeText(this, "Error de ID", Toast.LENGTH_SHORT).show()
//            finish()
//            return
//        }
//        binding.swipeRefreshLayout.setOnRefreshListener {
//            cargarMomento(momentoId)
//        }
//
//        binding.swipeRefreshLayout.post {
//            binding.swipeRefreshLayout.isRefreshing = true
//            cargarMomento(momentoId)
//        }
//
//
//
//        cargarMomento(momentoId)
//
//    }
//
//    override fun onSupportNavigateUp(): Boolean {
//        return super.onSupportNavigateUp()
//    }
//
//
//    private fun editDescripcion() {
//        val momentoId = intent.getStringExtra("id") ?: return
//        val descripcionActual = binding.textViewDescripcion.text.toString()
//
//        val intentDesc = Intent(this, EditDescripcion::class.java).apply {
//            putExtra("id", momentoId)
//            putExtra("descripcion", descripcionActual)
//        }
//
//        startActivity(intentDesc)
//    }
//
//
//
//    private fun cargarMomento(id: String) {
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            binding.swipeRefreshLayout.isRefreshing = false
//
//        }, 2000)
//
//        firestore.collection("momentos").document(id)
//            .get()
//            .addOnSuccessListener { doc ->
//                if (!doc.exists()) {
//                    Toast.makeText(this, "Momento no encontrado", Toast.LENGTH_SHORT).show()
//                    finish()
//                    return@addOnSuccessListener
//                }
//
//
//
//                val descripcion = doc.getString("descripcion") ?: ""
//                val fotoUrl = doc.getString("fotoUrl") ?: ""
//                val lat = doc.getDouble("latitud") ?: 0.0
//                val lon = doc.getDouble("longitud") ?: 0.0
//                val fecha = doc.getString("fecha")?: ""
//
//                binding.textViewDescripcion.text = descripcion
//                binding.textViewFecha.text = "Fecha: $fecha"
//                binding.textViewCoordenadas.text = "Coordenadas: $lat, $lon"
//
//
//
//                Glide.with(this).load(fotoUrl).into(binding.imageViewFoto)
//                binding.swipeRefreshLayout.isRefreshing = false
//
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
//                binding.swipeRefreshLayout.isRefreshing = false
//
//            }
//    }
//
//
//
//    override fun onStart() {
//        super.onStart()
//        mapView.onStart()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        mapView.onResume()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        mapView.onPause()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        mapView.onStop()
//    }
//
//    override fun onLowMemory() {
//        super.onLowMemory()
//        mapView.onLowMemory()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mapView.onDestroy()
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        mapView.onSaveInstanceState(outState)
//    }
//}

package com.example.momentum2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

// ─── Colors ───────────────────────────────────────────────────────────────────
private val BgDeep      = Color(0xFF0D1117)
private val BgCard      = Color(0xFF161B22)
private val Accent      = Color(0xFF58A6FF)
private val AccentAlt   = Color(0xFF3FB950)
private val TextPrimary = Color(0xFFE6EDF3)
private val TextMuted   = Color(0xFF7D8590)
private val FieldBorder = Color(0xFF30363D)

private val accentGradient = Brush.linearGradient(
    colors = listOf(Accent, AccentAlt),
    start  = Offset(0f, 0f),
    end    = Offset(400f, 100f)
)

// ─── Data class ───────────────────────────────────────────────────────────────
data class MomentoData(
    val descripcion: String = "",
    val fotoUrl: String     = "",
    val latitud: Double     = 0.0,
    val longitud: Double    = 0.0,
    val fecha: String       = ""
)

// ─── Activity ─────────────────────────────────────────────────────────────────
class MomentoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(this)

        val momentoId = intent.getStringExtra("id")
        val initialLat = intent.getDoubleExtra("latitud", 0.0)
        val initialLon = intent.getDoubleExtra("longitud", 0.0)
        val initialDesc = intent.getStringExtra("descripcion") ?: ""
        val apiKey = BuildConfig.MAPTILER_API_KEY

        if (momentoId == null) {
            finish()
            return
        }

        setContent {
            MaterialTheme {
                MomentoScreen(
                    momentoId    = momentoId,
                    initialLat   = initialLat,
                    initialLon   = initialLon,
                    initialDesc  = initialDesc,
                    apiKey       = apiKey,
                    onNavigateUp = { finish() },
                    onEditDesc   = { id, desc ->
                        startActivity(
                            Intent(this, EditDescripcion::class.java).apply {
                                putExtra("id", id)
                                putExtra("descripcion", desc)
                            }
                        )
                    }
                )
            }
        }
    }
}

// ─── Composable principal ─────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MomentoScreen(
    momentoId:   String,
    initialLat:  Double,
    initialLon:  Double,
    initialDesc: String,
    apiKey:      String,
    onNavigateUp: () -> Unit,
    onEditDesc:   (id: String, desc: String) -> Unit
) {
    val firestore = remember { FirebaseFirestore.getInstance() }

    var momento    by remember { mutableStateOf<MomentoData?>(null) }
    var isLoading  by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMsg   by remember { mutableStateOf<String?>(null) }

    // Coordenadas para el mapa: usa las del Intent hasta que cargue Firestore
    val mapLat = momento?.latitud ?: initialLat
    val mapLon = momento?.longitud ?: initialLon

    fun cargarMomento() {
        isRefreshing = true
        firestore.collection("momentos").document(momentoId)
            .get()
            .addOnSuccessListener { doc ->
                isLoading    = false
                isRefreshing = false
                if (!doc.exists()) {
                    errorMsg = "Momento no encontrado"
                    return@addOnSuccessListener
                }
                momento = MomentoData(
                    descripcion = doc.getString("descripcion") ?: "",
                    fotoUrl     = doc.getString("fotoUrl") ?: "",
                    latitud     = doc.getDouble("latitud") ?: 0.0,
                    longitud    = doc.getDouble("longitud") ?: 0.0,
                    fecha       = doc.getString("fecha") ?: ""
                )
            }
            .addOnFailureListener {
                isLoading    = false
                isRefreshing = false
                errorMsg = "Error al cargar datos"
            }
    }

    LaunchedEffect(momentoId) { cargarMomento() }

    // Orb decorativo
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
        // Orb de fondo
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (orbY - 40).dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Accent.copy(alpha = 0.15f), Color.Transparent)
                        )
                    )
                }
                .blur(90.dp)
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // ── TopBar ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextPrimary
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text       = "Momento",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary,
                    modifier   = Modifier.weight(1f)
                )
                // Botón refresh (equivalente al SwipeRefreshLayout)
                IconButton(
                    onClick  = { cargarMomento() },
                    enabled  = !isRefreshing
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = Accent,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = TextMuted)
                    }
                }
            }

            // ── Contenido scrollable ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Imagen (equivalente al imageViewFoto con Glide)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape  = RoundedCornerShape(20.dp),
                    color  = BgCard,
                    border = BorderStroke(1.dp, FieldBorder)
                ) {
                    if (isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Accent, modifier = Modifier.size(32.dp))
                        }
                    } else {
                        AsyncImage(
                            model             = momento?.fotoUrl,
                            contentDescription = "Foto del momento",
                            contentScale      = ContentScale.Crop,
                            modifier          = Modifier.fillMaxSize()
                        )
                    }
                }

                // ── Card info ────────────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(20.dp),
                    color    = BgCard,
                    border   = BorderStroke(1.dp, FieldBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        // Descripción (equivalente a textViewDescripcion + click para editar)
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Notes,
                                        contentDescription = null,
                                        tint     = Accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "Descripción",
                                        fontSize   = 11.sp,
                                        color      = TextMuted,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.4.sp
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text     = momento?.descripcion ?: initialDesc,
                                    fontSize = 16.sp,
                                    color    = TextPrimary,
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            // Botón editar (equivalente al setOnClickListener de editDesc)
                            IconButton(
                                onClick = {
                                    onEditDesc(momentoId, momento?.descripcion ?: initialDesc)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar descripción",
                                    tint     = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color    = FieldBorder
                        )

                        // Fecha (equivalente a textViewFecha)
                        InfoRow(
                            icon  = Icons.Default.CalendarToday,
                            label = "Fecha",
                            value = momento?.fecha ?: "—"
                        )

                        Spacer(Modifier.height(12.dp))

                        // Coordenadas (equivalente a textViewCoordenadas)
                        InfoRow(
                            icon  = Icons.Default.LocationOn,
                            label = "Coordenadas",
                            value = if (momento != null)
                                "${momento!!.latitud}, ${momento!!.longitud}"
                            else "$initialLat, $initialLon"
                        )
                    }
                }

                // ── Mapa ─────────────────────────────────────────────────────
                // MapLibre no tiene soporte Compose nativo → usamos AndroidView
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    shape    = RoundedCornerShape(20.dp),
                    color    = BgCard,
                    border   = BorderStroke(1.dp, FieldBorder)
                ) {
                    MapLibreView(
                        lat    = mapLat,
                        lon    = mapLon,
                        label  = momento?.descripcion ?: initialDesc,
                        apiKey = apiKey
                    )
                }

                // Error
                AnimatedVisibility(visible = errorMsg != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFF85149).copy(alpha = 0.12f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFF85149), modifier = Modifier.size(16.dp))
                        Text(errorMsg ?: "", color = Color(0xFFF85149), fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AsyncImage(
    model: String?,
    contentDescription: String,
    contentScale: ContentScale,
    modifier: Modifier
) {
    TODO("Not yet implemented")
}

// ─── Fila de info reutilizable ────────────────────────────────────────────────
@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .background(Accent.copy(alpha = 0.12f), CircleShape)
        ) {
            Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(label, fontSize = 11.sp, color = TextMuted, letterSpacing = 0.3.sp)
            Text(value, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        }
    }
}

// ─── MapLibre via AndroidView ─────────────────────────────────────────────────
// MapLibre no tiene Composable nativo; se usa AndroidView para integrarlo.
// El ciclo de vida se maneja con DisposableEffect.
@Composable
private fun MapLibreView(
    lat:    Double,
    lon:    Double,
    label:  String,
    apiKey: String
) {
    val context = LocalContext.current

    // Guardamos referencia al MapView para manejar el ciclo de vida
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mapViewRef.value?.onDestroy()
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp)),
        factory = { ctx ->
            MapView(ctx).also { mv ->
                mapViewRef.value = mv
                mv.onStart()
                mv.onResume()
                mv.getMapAsync { map ->
                    val styleUrl = "https://api.maptiler.com/maps/streets-v2/style.json?key=$apiKey"
                    map.setStyle(styleUrl)
                    val pos = LatLng(lat, lon)
                    map.addMarker(MarkerOptions().position(pos).title(label))
                    map.cameraPosition = CameraPosition.Builder()
                        .target(pos)
                        .zoom(15.0)
                        .build()
                }
            }
        },
        update = { mv ->
            // Cuando cambian las coordenadas (tras cargar Firestore), actualizamos el mapa
            mv.getMapAsync { map ->
                val pos = LatLng(lat, lon)
                map.clear()
                map.addMarker(MarkerOptions().position(pos).title(label))
                map.cameraPosition = CameraPosition.Builder()
                    .target(pos)
                    .zoom(15.0)
                    .build()
            }
        }
    )
}
