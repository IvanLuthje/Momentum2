//package com.example.momentum2
//
//import android.os.Bundle
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.firebase.firestore.FirebaseFirestore
//import com.example.momentum2.databinding.ActivityEditDescripcionBinding
//
//
//class EditDescripcion : AppCompatActivity() {
//
//
//    private lateinit var binding: ActivityEditDescripcionBinding
//    private val firestore = FirebaseFirestore.getInstance()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityEditDescripcionBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val momentoId = intent.getStringExtra("id") ?: ""
//        val descripcion = intent.getStringExtra("descripcion") ?: ""
//
//        binding.editTextDescripcion.setText(descripcion)
//
//        binding.buttonGuardar.setOnClickListener {
//            actualizarMomento(momentoId)
//        }
//    }
//
//
//    private fun actualizarMomento(momentoId: String) {
//
//        val nuevaDescripcion = binding.editTextDescripcion.text.toString().trim()
//
//        if (nuevaDescripcion.isEmpty()) {
//            Toast.makeText(this, "Por favor agregar una descripción", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        firestore.collection("momentos")
//            .document(momentoId)
//            .update("descripcion", nuevaDescripcion)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Momento actualizado correctamente", Toast.LENGTH_SHORT).show()
//
//                finish()
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//
//}
//
//

package com.example.momentum2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

// ─── Colors (mismo sistema que el resto de la app) ────────────────────────────
private val BgDeep       = Color(0xFF0D1117)
private val BgCard       = Color(0xFF161B22)
private val Accent       = Color(0xFF58A6FF)
private val AccentAlt    = Color(0xFF3FB950)
private val TextPrimary  = Color(0xFFE6EDF3)
private val TextMuted    = Color(0xFF7D8590)
private val FieldBg      = Color(0xFF0D1117)
private val FieldBorder  = Color(0xFF30363D)
private val ErrorColor   = Color(0xFFF85149)
private val SuccessColor = Color(0xFF3FB950)

private val accentGradient = Brush.linearGradient(
    colors = listOf(Accent, AccentAlt),
    start  = Offset(0f, 0f),
    end    = Offset(400f, 100f)
)

// ─── Activity ─────────────────────────────────────────────────────────────────
class EditDescripcion : ComponentActivity() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val momentoId   = intent.getStringExtra("id") ?: ""
        val descripcion = intent.getStringExtra("descripcion") ?: ""

        setContent {
            MaterialTheme {
                EditDescripcionScreen(
                    firestore           = firestore,
                    momentoId           = momentoId,
                    descripcionInicial  = descripcion,
                    onNavigateUp        = { finish() },
                    onGuardadoExitoso   = { finish() }
                )
            }
        }
    }
}

// ─── Composable principal ─────────────────────────────────────────────────────
@Composable
fun EditDescripcionScreen(
    firestore: FirebaseFirestore,
    momentoId: String,
    descripcionInicial: String,
    onNavigateUp: () -> Unit,
    onGuardadoExitoso: () -> Unit
) {
    var texto      by remember { mutableStateOf(descripcionInicial) }
    var isLoading  by remember { mutableStateOf(false) }
    var errorMsg   by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    val hayCambios = texto.trim() != descripcionInicial.trim()
    val maxChars   = 500

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

    // Equivalente a actualizarMomento()
    fun actualizarMomento() {
        val nuevaDescripcion = texto.trim()
        if (nuevaDescripcion.isEmpty()) {
            errorMsg = "Por favor agregar una descripción"
            return
        }
        isLoading = true
        errorMsg  = null

        firestore.collection("momentos")
            .document(momentoId)
            .update("descripcion", nuevaDescripcion)
            .addOnSuccessListener {
                isLoading  = false
                successMsg = "Momento actualizado correctamente"
                // Pequeña pausa para mostrar el feedback antes de cerrar
                Handler(Looper.getMainLooper()).postDelayed({ onGuardadoExitoso() }, 800)
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMsg  = "Error al actualizar: ${e.message}"
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        // Orb decorativo
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopStart)
                .offset(x = (-70).dp, y = (orbY - 50).dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Accent.copy(alpha = 0.14f), Color.Transparent)
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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text       = "Editar descripción",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary,
                    modifier   = Modifier.weight(1f)
                )
            }

            // ── Contenido ─────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // Card con el campo de texto
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(20.dp),
                    color    = BgCard,
                    border   = BorderStroke(1.dp, FieldBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        // Label
                        Row(
                            verticalAlignment      = Alignment.CenterVertically,
                            horizontalArrangement  = Arrangement.spacedBy(6.dp),
                            modifier               = Modifier.padding(bottom = 10.dp)
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

                        // Campo multilínea (equivalente a editTextDescripcion)
                        OutlinedTextField(
                            value         = texto,
                            onValueChange = {
                                if (it.length <= maxChars) {
                                    texto    = it
                                    errorMsg = null
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 160.dp),
                            placeholder = {
                                Text("Escribí una descripción...", color = TextMuted.copy(alpha = 0.5f))
                            },
                            shape   = RoundedCornerShape(12.dp),
                            colors  = OutlinedTextFieldDefaults.colors(
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
                                imeAction      = ImeAction.Default  // multilínea
                            ),
                            maxLines = 10,
                            enabled  = !isLoading
                        )

                        // Contador de caracteres
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text     = "${texto.length} / $maxChars",
                                fontSize = 11.sp,
                                color    = if (texto.length > maxChars * 0.9) ErrorColor else TextMuted
                            )
                        }
                    }
                }

                // Mensaje de error (equivalente a Toast de error)
                AnimatedVisibility(
                    visible = errorMsg != null,
                    enter   = fadeIn() + expandVertically(),
                    exit    = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ErrorColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = ErrorColor, modifier = Modifier.size(16.dp))
                        Text(errorMsg ?: "", color = ErrorColor, fontSize = 13.sp)
                    }
                }

                // Mensaje de éxito (equivalente a Toast de éxito)
                AnimatedVisibility(
                    visible = successMsg != null,
                    enter   = fadeIn() + expandVertically(),
                    exit    = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SuccessColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = SuccessColor, modifier = Modifier.size(16.dp))
                        Text(successMsg ?: "", color = SuccessColor, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.weight(1f))

                // ── Botones ───────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancelar
                    OutlinedButton(
                        onClick  = onNavigateUp,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape   = RoundedCornerShape(14.dp),
                        border  = BorderStroke(1.dp, FieldBorder),
                        colors  = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                        enabled = !isLoading
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.Medium, color = TextMuted)
                    }

                    // Guardar (equivalente a buttonGuardar con su setOnClickListener)
                    Button(
                        onClick        = { actualizarMomento() },
                        modifier       = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape          = RoundedCornerShape(14.dp),
                        colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        enabled        = !isLoading && hayCambios
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = if (!hayCambios || isLoading)
                                        Brush.linearGradient(listOf(Accent.copy(0.4f), AccentAlt.copy(0.4f)))
                                    else accentGradient,
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = isLoading,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "saveBtn"
                            ) { loading ->
                                if (loading) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(22.dp),
                                        color       = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment     = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Save,
                                            contentDescription = null,
                                            tint     = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text       = "Guardar",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize   = 16.sp,
                                            color      = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
