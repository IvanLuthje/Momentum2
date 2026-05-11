//package com.example.momentum2.ui.login.ui.theme
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.example.momentum2.MainActivity2
//import com.example.momentum2.databinding.ActivityRegisterBinding
//import com.google.firebase.auth.FirebaseAuth
//
//class RegisterActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityRegisterBinding
//    private lateinit var auth: FirebaseAuth
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityRegisterBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        auth = FirebaseAuth.getInstance()
//
//        binding.botonRegistrar.setOnClickListener {
//            val username = binding.username.text.toString().trim()
//            val password = binding.password.text.toString().trim()
//            val confirmPassword = binding.confirmarPassword.text.toString().trim()
//
//
//
//            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
//                Toast.makeText(this, "Por favor completar los campos", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (password != confirmPassword) {
//                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            auth.createUserWithEmailAndPassword(username, password)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        Toast.makeText(this, "Registración exitosa", Toast.LENGTH_SHORT).show()
//                        startActivity(Intent(this, MainActivity2::class.java))
//                        finish()
//                    } else {
//                        Toast.makeText(this, "Hay un error de registración o la cuenta de email ya existe", Toast.LENGTH_LONG).show()
//                    }
//                }
//        }
//    }
//}
package com.example.momentum2.ui.login.ui.theme

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.momentum2.MainActivity2
import com.google.firebase.auth.FirebaseAuth

// ─── Colors (mismo sistema que LoginActivity) ─────────────────────────────────
private val BgDeep      = Color(0xFF0D1117)
private val BgCard      = Color(0xFF161B22)
private val Accent      = Color(0xFF58A6FF)
private val AccentAlt   = Color(0xFF3FB950)
private val TextPrimary = Color(0xFFE6EDF3)
private val TextMuted   = Color(0xFF7D8590)
private val FieldBg     = Color(0xFF0D1117)
private val FieldBorder = Color(0xFF30363D)
private val ErrorColor  = Color(0xFFF85149)
private val SuccessColor = Color(0xFF3FB950)

private val accentGradient = Brush.linearGradient(
    colors = listOf(Accent, AccentAlt),
    start  = Offset(0f, 0f),
    end    = Offset(500f, 100f)
)

// ─── Activity ─────────────────────────────────────────────────────────────────
class RegisterActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            MaterialTheme {
                RegisterScreen(
                    auth = auth,
                    onRegisterSuccess = {
                        startActivity(Intent(this, MainActivity2::class.java))
                        finish()
                    },
                    onBackClick = { finish() }
                )
            }
        }
    }
}

// ─── Composable principal ─────────────────────────────────────────────────────
@Composable
fun RegisterScreen(
    auth: FirebaseAuth,
    onRegisterSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    var username        by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible        by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var isLoading    by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Orb animado (igual que Login para consistencia visual)
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val orbY by infiniteTransition.animateFloat(
        initialValue = -25f, targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation  = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbY"
    )

    // Validación e indicador de fuerza de contraseña
    val passwordStrength = when {
        password.length >= 8 && password.any { it.isDigit() } && password.any { it.isUpperCase() } -> 3
        password.length >= 6 -> 2
        password.isNotEmpty() -> 1
        else -> 0
    }
    val passwordsMatch = password.isNotEmpty() && password == confirmPassword

    fun doRegister() {
        // Misma lógica de validación que el original
        if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            errorMessage = "Por favor completar los campos"
            return
        }
        if (password != confirmPassword) {
            errorMessage = "Las contraseñas no coinciden"
            return
        }
        focusManager.clearFocus()
        isLoading    = true
        errorMessage = null

        auth.createUserWithEmailAndPassword(username.trim(), password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    onRegisterSuccess()
                } else {
                    errorMessage = task.exception?.localizedMessage
                        ?: "Hay un error de registración o la cuenta de email ya existe"
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        // ── Orbs decorativos ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (orbY - 50).dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(AccentAlt.copy(alpha = 0.16f), Color.Transparent)
                        )
                    )
                }
                .blur(100.dp)
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = (-orbY + 20).dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Accent.copy(alpha = 0.14f), Color.Transparent)
                        )
                    )
                }
                .blur(90.dp)
        )

        // ── Contenido ─────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(56.dp))

            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector        = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint               = TextMuted
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Ícono
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .background(brush = accentGradient, shape = RoundedCornerShape(18.dp))
            ) {
                Icon(
                    imageVector        = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text          = "Crear cuenta",
                fontSize      = 32.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = TextPrimary,
                letterSpacing = (-0.8).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text      = "Completá tus datos para registrarte",
                fontSize  = 15.sp,
                color     = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // ── Card ──────────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                color    = BgCard,
                border   = BorderStroke(1.dp, FieldBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Email (id: username en el XML original)
                    RegisterField(
                        value         = username,
                        onValueChange = { username = it; errorMessage = null },
                        label         = "Email",
                        placeholder   = "tu@email.com",
                        leadingIcon   = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction    = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    // Contraseña (id: password en el XML original)
                    RegisterField(
                        value         = password,
                        onValueChange = { password = it; errorMessage = null },
                        label         = "Contraseña",
                        placeholder   = "••••••••",
                        leadingIcon   = Icons.Default.Lock,
                        trailingIcon  = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    // Indicador de fuerza de contraseña
                    AnimatedVisibility(visible = password.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                repeat(3) { index ->
                                    val active = index < passwordStrength
                                    val color  = when (passwordStrength) {
                                        1    -> ErrorColor
                                        2    -> Color(0xFFE3B341)
                                        else -> SuccessColor
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(4.dp)
                                            .background(
                                                color = if (active) color else FieldBorder,
                                                shape = RoundedCornerShape(2.dp)
                                            )
                                    )
                                }
                            }
                            Text(
                                text = when (passwordStrength) {
                                    1    -> "Contraseña débil"
                                    2    -> "Contraseña aceptable"
                                    3    -> "Contraseña fuerte"
                                    else -> ""
                                },
                                color    = when (passwordStrength) {
                                    1    -> ErrorColor
                                    2    -> Color(0xFFE3B341)
                                    else -> SuccessColor
                                },
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Confirmar contraseña (id: confirmarPassword en el XML original)
                    RegisterField(
                        value         = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = null },
                        label         = "Confirmar contraseña",
                        placeholder   = "••••••••",
                        leadingIcon   = Icons.Default.LockOpen,
                        trailingIcon  = {
                            if (confirmPassword.isNotEmpty()) {
                                Icon(
                                    imageVector = if (passwordsMatch) Icons.Default.CheckCircle
                                    else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (passwordsMatch) SuccessColor else ErrorColor,
                                    modifier = Modifier.size(20.dp).padding(end = 4.dp)
                                )
                            } else {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = TextMuted
                                    )
                                }
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { doRegister() })
                    )

                    // Mensaje de error (equivalente al Toast del original)
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter   = fadeIn() + expandVertically(),
                        exit    = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = ErrorColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint     = ErrorColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text     = errorMessage ?: "",
                                color    = ErrorColor,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Botón Registrar (id: botonRegistrar en el XML original)
                    Button(
                        onClick        = { doRegister() },
                        modifier       = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape          = RoundedCornerShape(14.dp),
                        colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        enabled        = !isLoading
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = if (isLoading) Brush.linearGradient(
                                        listOf(Accent.copy(0.5f), AccentAlt.copy(0.5f))
                                    ) else accentGradient,
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = isLoading,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "registerBtn"
                            ) { loading ->
                                if (loading) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(22.dp),
                                        color       = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text          = "Crear cuenta",
                                        fontWeight    = FontWeight.SemiBold,
                                        fontSize      = 16.sp,
                                        color         = Color.White,
                                        letterSpacing = 0.2.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                Text("¿Ya tenés cuenta? ", color = TextMuted, fontSize = 14.sp)
                Text(
                    text       = "Iniciá sesión",
                    color      = Accent,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.clickable(onClick = onBackClick)
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

// ─── Campo de texto reutilizable ──────────────────────────────────────────────
@Composable
private fun RegisterField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    Column {
        Text(
            text          = label,
            color         = TextMuted,
            fontSize      = 12.sp,
            fontWeight    = FontWeight.Medium,
            letterSpacing = 0.3.sp,
            modifier      = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value                = value,
            onValueChange        = onValueChange,
            modifier             = Modifier.fillMaxWidth(),
            placeholder          = { Text(placeholder, color = TextMuted.copy(alpha = 0.5f)) },
            leadingIcon          = {
                Icon(
                    imageVector        = leadingIcon,
                    contentDescription = null,
                    tint               = if (value.isNotEmpty()) Accent else TextMuted,
                    modifier           = Modifier.size(20.dp)
                )
            },
            trailingIcon         = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions      = keyboardOptions,
            keyboardActions      = keyboardActions,
            singleLine           = true,
            shape                = RoundedCornerShape(12.dp),
            colors               = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = FieldBg,
                unfocusedContainerColor = FieldBg,
                focusedBorderColor      = Accent,
                unfocusedBorderColor    = FieldBorder,
                focusedTextColor        = TextPrimary,
                unfocusedTextColor      = TextPrimary,
                cursorColor             = Accent
            )
        )
    }
}
