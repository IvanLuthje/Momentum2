//package com.example.momentum2.ui.momentos
//
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.appcompat.app.AlertDialog
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.momentum2.databinding.FragmentMomentosBinding
//import com.example.momentum2.Moment
//import com.google.firebase.firestore.FirebaseFirestore
//import android.widget.Toast
//
//class MomentosFragment : Fragment() {
//
//    private var _binding: FragmentMomentosBinding? = null
//    private val binding get() = _binding!!
//
//    private val firestore = FirebaseFirestore.getInstance()
//    private lateinit var adapter: MomentosAdapter
//    private val listaMomentos = mutableListOf<Moment>()
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentMomentosBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        Log.d("MomentosFragment", "Fragment creado")
//
//        adapter = MomentosAdapter(listaMomentos) { momento, position ->
//            AlertEliminar(momento, position)
//        }
//
//        binding.recyclerMomentos.layoutManager = LinearLayoutManager(requireContext())
//        binding.recyclerMomentos.adapter = adapter
//
//        binding.swipeRefreshLayout.setOnRefreshListener {
//            cargarMomentos()
//        }
//
//        cargarMomentos()
//    }
//
//    private fun cargarMomentos() {
//        binding.swipeRefreshLayout.isRefreshing = true
//
//        firestore.collection("momentos")
//            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
//            .get()
//            .addOnSuccessListener { snapshot ->
//                listaMomentos.clear()
//                for (document in snapshot.documents) {
//                    val momento = document.toObject(Moment::class.java)?.apply {
//                        id = document.id
//                    }
//                    if (momento != null) listaMomentos.add(momento)
//                }
//                adapter.notifyDataSetChanged()
//                EmptyMoments()
//                binding.swipeRefreshLayout.isRefreshing = false
//            }
//            .addOnFailureListener { e ->
//                e.printStackTrace()
//                Toast.makeText(requireContext(), "Error al cargar momentos", Toast.LENGTH_SHORT).show()
//                binding.swipeRefreshLayout.isRefreshing = false
//            }
//    }
//
//    private fun AlertEliminar(momento: Moment, position: Int) {
//        AlertDialog.Builder(requireContext())
//            .setTitle("Eliminar momento")
//            .setMessage("¿Estás seguro de que deseas eliminar este momento?")
//            .setPositiveButton("Eliminar") { _, _ ->
//                eliminarMomento(momento, position)
//            }
//            .setNegativeButton("Cancelar", null)
//            .show()
//    }
//
//    private fun eliminarMomento(momento: Moment, position: Int) {
//
//        firestore.collection("momentos")
//            .document(momento.id)
//            .delete()
//            .addOnSuccessListener {
//                listaMomentos.removeAt(position)
//                adapter.notifyItemRemoved(position)
//                Toast.makeText(requireContext(), "Momento eliminado", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener { e ->
//                e.printStackTrace()
//                Toast.makeText(requireContext(), "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    private fun EmptyMoments() {
//        if (listaMomentos.isEmpty()) {
//            binding.emptyList.visibility = View.VISIBLE
//            binding.recyclerMomentos.visibility = View.GONE
//        } else {
//            binding.emptyList.visibility = View.GONE
//            binding.recyclerMomentos.visibility = View.VISIBLE
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}

package com.example.momentum2.ui.momentos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import coil.compose.AsyncImage
import com.example.momentum2.Moment
import com.example.momentum2.MomentoActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.momentum2.R

// ─── Colors ───────────────────────────────────────────────────────────────────
private val BgDeep       = Color(0xFF0D1117)
private val BgCard       = Color(0xFF161B22)
private val Accent       = Color(0xFF58A6FF)
private val AccentAlt    = Color(0xFF3FB950)
private val TextPrimary  = Color(0xFFE6EDF3)
private val TextMuted    = Color(0xFF7D8590)
private val FieldBorder  = Color(0xFF30363D)
private val ErrorColor   = Color(0xFFF85149)

private val accentGradient = Brush.linearGradient(
    colors = listOf(Accent, AccentAlt),
    start  = Offset(0f, 0f),
    end    = Offset(400f, 100f)
)

// ─── Fragment ─────────────────────────────────────────────────────────────────
class MomentosFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Limpia la composición cuando el View del Fragment se destruye,
            // equivalente al _binding = null en onDestroyView
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                MaterialTheme {
                    MomentosScreen()
                }
            }
        }
    }
}

// ─── Composable principal ─────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MomentosScreen() {
    val context   = LocalContext.current
    val firestore = remember { FirebaseFirestore.getInstance() }

    var momentos     by remember { mutableStateOf<List<Moment>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMsg     by remember { mutableStateOf<String?>(null) }
    var searchQuery  by remember { mutableStateOf("") }

    // Momento seleccionado para el diálogo de eliminación
    var momentoAEliminar by remember { mutableStateOf<Moment?>(null) }

    val pullState = rememberPullToRefreshState()

    // ── cargarMomentos() ──────────────────────────────────────────────────────
    fun cargarMomentos() {
        isRefreshing = true
        errorMsg     = null

        firestore.collection("momentos")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                momentos = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Moment::class.java)?.apply { id = doc.id }
                }
                isRefreshing = false
            }
            .addOnFailureListener {
                errorMsg     = "Error al cargar momentos"
                isRefreshing = false
            }
    }

    // ── eliminarMomento() ─────────────────────────────────────────────────────
    fun eliminarMomento(momento: Moment) {
        firestore.collection("momentos")
            .document(momento.id)
            .delete()
            .addOnSuccessListener {
                momentos = momentos.filter { it.id != momento.id }
            }
            .addOnFailureListener { e ->
                errorMsg = "Error al eliminar: ${e.message}"
            }
    }

    // ── filtro() ──────────────────────────────────────────────────────────────
    val momentosFiltrados = remember(momentos, searchQuery) {
        if (searchQuery.isBlank()) momentos
        else momentos.filter {
            it.descripcion.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(Unit) { cargarMomentos() }

    // Orb animado de fondo
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val orbY by infiniteTransition.animateFloat(
        initialValue = -20f, targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation  = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbY"
    )

    // ── AlertEliminar ─────────────────────────────────────────────────────────
    momentoAEliminar?.let { momento ->
        AlertDialog(
            onDismissRequest = { momentoAEliminar = null },
            containerColor   = BgCard,
            shape            = RoundedCornerShape(20.dp),
            icon = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .background(ErrorColor.copy(alpha = 0.12f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint     = ErrorColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    "Eliminar momento",
                    color      = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp
                )
            },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar este momento?",
                    color    = TextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        eliminarMomento(momento)
                        momentoAEliminar = null
                    },
                    colors         = ButtonDefaults.buttonColors(containerColor = ErrorColor),
                    shape          = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { momentoAEliminar = null },
                    shape   = RoundedCornerShape(12.dp),
                    border  = BorderStroke(1.dp, FieldBorder),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        // Orb decorativo
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = (orbY - 40).dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Accent.copy(alpha = 0.13f), Color.Transparent)
                        )
                    )
                }
                .blur(90.dp)
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.menu_transform),
                        fontSize      = 26.sp,
                        fontWeight    = FontWeight.ExtraBold,
                        color         = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        "${momentosFiltrados.size} registro${if (momentosFiltrados.size != 1) "s" else ""}",
                        fontSize = 13.sp,
                        color    = TextMuted
                    )
                }
                IconButton(
                    onClick  = { cargarMomentos() },
                    enabled  = !isRefreshing
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = Accent,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, "Actualizar", tint = TextMuted)
                    }
                }
            }

            // ── Buscador ──────────────────────────────────────────────────────
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { searchQuery = it },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp),
                placeholder   = { Text("Buscar momentos...", color = TextMuted.copy(0.5f)) },
                leadingIcon   = {
                    Icon(Icons.Default.Search, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                },
                trailingIcon  = {
                    AnimatedVisibility(visible = searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine      = true,
                shape           = RoundedCornerShape(14.dp),
                colors          = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = BgCard,
                    unfocusedContainerColor = BgCard,
                    focusedBorderColor      = Accent,
                    unfocusedBorderColor    = FieldBorder,
                    focusedTextColor        = TextPrimary,
                    unfocusedTextColor      = TextPrimary,
                    cursorColor             = Accent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )

            // ── Error banner ──────────────────────────────────────────────────
            AnimatedVisibility(visible = errorMsg != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .background(ErrorColor.copy(0.12f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, null, tint = ErrorColor, modifier = Modifier.size(16.dp))
                    Text(errorMsg ?: "", color = ErrorColor, fontSize = 13.sp)
                }
            }

            // ── Lista / Empty state con pull-to-refresh ───────────────────────
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh    = { cargarMomentos() },
                state        = pullState,
                modifier     = Modifier.fillMaxSize()
            ) {
                if (momentosFiltrados.isEmpty() && !isRefreshing) {
                    EmptyState(hayBusqueda = searchQuery.isNotEmpty())
                } else {
                    LazyColumn(
                        modifier            = Modifier.fillMaxSize(),
                        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            items = momentosFiltrados,
                            key   = { _, m -> m.id }
                        ) { _, momento ->
                            MomentoCard(
                                momento       = momento,
                                onClickImagen = {
                                    context.startActivity(
                                        Intent(context, MomentoActivity::class.java).apply {
                                            putExtra("id",          momento.id)
                                            putExtra("descripcion", momento.descripcion)
                                            putExtra("foto_url",    momento.fotoUrl)
                                            putExtra("fecha",       momento.fecha)
                                            putExtra("longitud",    momento.longitud)
                                            putExtra("latitud",     momento.latitud)
                                        }
                                    )
                                },
                                onDeleteClick = { momentoAEliminar = momento }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

// ─── Card individual ──────────────────────────────────────────────────────────
@Composable
private fun MomentoCard(
    momento: Moment,
    onClickImagen: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(18.dp),
        color    = BgCard,
        border   = BorderStroke(1.dp, FieldBorder)
    ) {
        Column {
            // Imagen clickeable
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .clickable(onClick = onClickImagen)
            ) {
                AsyncImage(
                    model              = momento.fotoUrl,
                    contentDescription = momento.descripcion,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
                // Gradiente inferior
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                                startY = 80f
                            )
                        )
                )
                // Chip de fecha
                if (momento.fecha.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.55f)
                    ) {
                        Row(
                            modifier              = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, null, tint = TextMuted, modifier = Modifier.size(11.dp))
                            Text(momento.fecha, fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
                // Ícono ver detalle
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .size(32.dp)
                        .background(Accent.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.OpenInNew, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            // Descripción + botón eliminar
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text     = momento.descripcion.ifBlank { "Sin descripción" },
                    color    = TextPrimary,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick  = onDeleteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(ErrorColor.copy(alpha = 0.12f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint     = ErrorColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ─── Estado vacío ─────────────────────────────────────────────────────────────
@Composable
private fun EmptyState(hayBusqueda: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier            = Modifier.padding(32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(Accent.copy(0.15f), AccentAlt.copy(0.15f))),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector        = if (hayBusqueda) Icons.Default.SearchOff else Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint               = Accent,
                    modifier           = Modifier.size(36.dp)
                )
            }
            Text(
                text       = if (hayBusqueda) "Sin resultados" else "Sin momentos",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary
            )
            Text(
                text      = if (hayBusqueda) "Probá con otra búsqueda"
                else "Agregá tu primer momento\ndesde el botón +",
                fontSize  = 14.sp,
                color     = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}