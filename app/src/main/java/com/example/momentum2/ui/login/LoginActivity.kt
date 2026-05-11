package com.example.momentum2.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.momentum2.MainActivity2
import com.example.momentum2.ui.login.ui.theme.RegisterActivity
import com.google.firebase.auth.FirebaseAuth

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

private val accentGradient = Brush.linearGradient(
    colors = listOf(Accent, AccentAlt),
    start  = Offset(0f, 0f),
    end    = Offset(500f, 100f)
)

// ─── Activity ─────────────────────────────────────────────────────────────────
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            MaterialTheme {
                LoginScreen(
                    auth           = auth,
                    onLoginSuccess = {
                        startActivity(Intent(this, MainActivity2::class.java))
                        finish()
                    },
                    onRegisterClick = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Si ya hay sesión activa, ir directo a MainActivity2
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity2::class.java))
            finish()
        }
    }
}

// ─── Composable principal ─────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    auth: FirebaseAuth,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val context      = LocalContext.current

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf<String?>(null) }

    // Orb animado de fondo
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val orbY by infiniteTransition.animateFloat(
        initialValue = -25f, targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation   = tween(5000, easing = FastOutSlowInEasing),
            repeatMode  = RepeatMode.Reverse
        ),
        label = "orbY"
    )

    fun doLogin() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Completá todos los campos"
            return
        }
        focusManager.clearFocus()
        isLoading    = true
        errorMessage = null

        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    onLoginSuccess()
                } else {
                    errorMessage = task.exception?.localizedMessage
                        ?: "Error al iniciar sesión"
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        // ── Orb decorativo ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.TopStart)
                .offset(x = (-80).dp, y = (orbY - 40).dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Accent.copy(alpha = 0.18f), Color.Transparent)
                        )
                    )
                }
                .blur(100.dp)
        )
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = (-orbY + 20).dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(AccentAlt.copy(alpha = 0.15f), Color.Transparent)
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
            Spacer(Modifier.height(64.dp))

            // Ícono / logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .background(brush = accentGradient, shape = RoundedCornerShape(18.dp))
            ) {
                Icon(
                    imageVector        = Icons.Default.Bolt,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(34.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text       = "Momentum",
                fontSize   = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = TextPrimary,
                letterSpacing = (-0.8).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text     = "Iniciá sesión para continuar",
                fontSize = 15.sp,
                color    = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(36.dp))

            // ── Card ──────────────────────────────────────────────────────────
            Surface(
                modifier       = Modifier.fillMaxWidth(),
                shape          = RoundedCornerShape(20.dp),
                color          = BgCard,
                border         = BorderStroke(1.dp, FieldBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Campo email (id: username en tu XML original)
                    LoginField(
                        value         = email,
                        onValueChange = { email = it; errorMessage = null },
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

                    LoginField(
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
                                    contentDescription = "Mostrar contraseña",
                                    tint = TextMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { doLogin() })
                    )

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
                                tint = ErrorColor,
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

                    // Botón Login (id: login en tu XML original)
                    Button(
                        onClick           = { doLogin() },
                        modifier          = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape             = RoundedCornerShape(14.dp),
                        colors            = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding    = PaddingValues(0.dp),
                        enabled           = !isLoading
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
                                label = "loginBtn"
                            ) { loading ->
                                if (loading) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(22.dp),
                                        color       = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text          = "Iniciar sesión",
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
                Text("¿No tenés cuenta? ", color = TextMuted, fontSize = 14.sp)
                Text(
                    text       = "Registrate",
                    color      = Accent,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.clickable(onClick = onRegisterClick)
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun LoginField(
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
            value           = value,
            onValueChange   = onValueChange,
            modifier        = Modifier.fillMaxWidth(),
            placeholder     = { Text(placeholder, color = TextMuted.copy(alpha = 0.5f)) },
            leadingIcon     = {
                Icon(
                    imageVector        = leadingIcon,
                    contentDescription = null,
                    tint               = if (value.isNotEmpty()) Accent else TextMuted,
                    modifier           = Modifier.size(20.dp)
                )
            },
            trailingIcon          = trailingIcon,
            visualTransformation  = visualTransformation,
            keyboardOptions       = keyboardOptions,
            keyboardActions       = keyboardActions,
            singleLine            = true,
            shape                 = RoundedCornerShape(12.dp),
            colors                = OutlinedTextFieldDefaults.colors(
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