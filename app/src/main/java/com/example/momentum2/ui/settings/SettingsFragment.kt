//package com.example.momentum2.ui.settings
//
//import android.content.Context
//import android.content.res.Configuration
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatDelegate
//import androidx.preference.ListPreference
//import androidx.preference.PreferenceFragmentCompat
//import androidx.preference.SwitchPreferenceCompat
//import com.example.momentum2.R
//import java.util.Locale
//
//class SettingsFragment : PreferenceFragmentCompat() {
//
//    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
//        setPreferencesFromResource(R.xml.preferences, rootKey)
//
//        val themeSwitch = findPreference<SwitchPreferenceCompat>("theme_dark")
//        val languageList = findPreference<ListPreference>("language")
//
//        themeSwitch?.setOnPreferenceChangeListener { _, newValue ->
//            val enabled = newValue as Boolean
//            AppCompatDelegate.setDefaultNightMode(
//                if (enabled) AppCompatDelegate.MODE_NIGHT_YES
//                else AppCompatDelegate.MODE_NIGHT_NO
//            )
//            true
//        }
//
//        languageList?.setOnPreferenceChangeListener { _, newLang ->
//            cambiarIdioma(newLang.toString())
//            activity?.recreate()
//            true
//        }
//    }
//
//    private fun cambiarIdioma(lang: String) {
//        val locale = Locale(lang)
//        Locale.setDefault(locale)
//
//        val config = Configuration()
//        config.setLocale(locale)
//
//        requireActivity().baseContext.resources.updateConfiguration(
//            config,
//            requireActivity().baseContext.resources.displayMetrics
//        )
//
//        val prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
//        prefs.edit().putString("language", lang).apply()
//    }
//}

package com.example.momentum2.ui.settings

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import java.util.Locale
import com.example.momentum2.R

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

// ─── Idiomas disponibles (equivalente a R.xml.preferences entries) ────────────
private val idiomas = listOf(
    "es" to "Español",
    "en" to "English",
    "pt" to "Português",
    "fr" to "Français"
)

// ─── Fragment ─────────────────────────────────────────────────────────────────
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    SettingsScreen(
                        onRecreate = { activity?.recreate() }
                    )
                }
            }
        }
    }
}

// ─── Composable principal ─────────────────────────────────────────────────────
@Composable
fun SettingsScreen(onRecreate: () -> Unit) {
    val context = LocalContext.current
    val title_app = stringResource(R.string.title_app)
    val set_language = stringResource(R.string.set_language)
    val set_theme = stringResource(R.string.set_theme)
    val prefs   = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }

    // Estado del tema (equivalente a SwitchPreferenceCompat "theme_dark")
    var isDarkMode by remember {
        mutableStateOf(
            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        )
    }

    // Estado del idioma (equivalente a ListPreference "language")
    var idiomaActual by remember {
        mutableStateOf(prefs.getString("language", "es") ?: "es")
    }
    var showLanguageDialog by remember { mutableStateOf(false) }



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

    // Diálogo de selección de idioma (equivalente al ListPreference dialog)
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor   = Color(0xFF1C2128),
            shape            = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "Seleccionar idioma",
                    color      = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    idiomas.forEach { (code, nombre) ->
                        val seleccionado = code == idiomaActual
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (seleccionado) Accent.copy(alpha = 0.12f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    idiomaActual = code
                                    showLanguageDialog = false
                                    cambiarIdioma(context, code)
                                    onRecreate()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = seleccionado,
                                onClick  = null,
                                colors   = RadioButtonDefaults.colors(
                                    selectedColor   = Accent,
                                    unselectedColor = TextMuted
                                )
                            )
                            Text(
                                nombre,
                                color      = if (seleccionado) TextPrimary else TextMuted,
                                fontSize   = 15.sp,
                                fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        // Orb decorativo
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.TopStart)
                .offset(x = (-60).dp, y = (orbY - 40).dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Accent.copy(alpha = 0.12f), Color.Transparent)
                        )
                    )
                }
                .blur(90.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Encabezado de sección: Apariencia ─────────────────────────────
            SectionHeader(icon = Icons.Default.Palette, title = title_app)

            // ── Tema oscuro (SwitchPreferenceCompat "theme_dark") ─────────────
            SettingRow(
                icon        = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                iconTint    = Accent,
                title       = set_theme,
                subtitle    = if (isDarkMode) "Modo noche activado" else "Modo claro activado",
                trailing    = {
                    Switch(
                        checked         = isDarkMode,
                        onCheckedChange = { enabled ->
                            isDarkMode = enabled
                            // Equivalente al setOnPreferenceChangeListener de themeSwitch
                            AppCompatDelegate.setDefaultNightMode(
                                if (enabled) AppCompatDelegate.MODE_NIGHT_YES
                                else AppCompatDelegate.MODE_NIGHT_NO
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor       = Color.White,
                            checkedTrackColor       = Accent,
                            uncheckedThumbColor     = TextMuted,
                            uncheckedTrackColor     = FieldBorder,
                            uncheckedBorderColor    = FieldBorder
                        )
                    )
                }
            )

            Spacer(Modifier.height(4.dp))

            // ── Encabezado de sección: Idioma ─────────────────────────────────
            SectionHeader(icon = Icons.Default.Language, title = set_language)

            // ── Selector de idioma (ListPreference "language") ────────────────
            SettingRow(
                icon     = Icons.Default.Translate,
                iconTint = AccentAlt,
                title    = set_language,
                subtitle = idiomas.firstOrNull { it.first == idiomaActual }?.second ?: idiomaActual,
                trailing = {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint     = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = { showLanguageDialog = true }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Encabezado de sección ────────────────────────────────────────────────────
@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier              = Modifier.padding(start = 4.dp, bottom = 2.dp)
    ) {
        Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
        Text(
            title.uppercase(),
            fontSize      = 11.sp,
            fontWeight    = FontWeight.SemiBold,
            color         = TextMuted,
            letterSpacing = 1.sp
        )
    }
}

// ─── Fila de ajuste reutilizable ──────────────────────────────────────────────
@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        shape  = RoundedCornerShape(16.dp),
        color  = BgCard,
        border = BorderStroke(1.dp, FieldBorder)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Ícono con fondo
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(40.dp)
                    .background(iconTint.copy(alpha = 0.12f), CircleShape)
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }

            // Texto
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, color = TextMuted, fontSize = 12.sp)
            }

            // Control derecho (Switch, ChevronRight, etc.)
            trailing()
        }
    }
}

// ─── cambiarIdioma() — idéntica al original ───────────────────────────────────
private fun cambiarIdioma(context: Context, lang: String) {
    val locale = Locale(lang)
    Locale.setDefault(locale)

    val config = Configuration()
    config.setLocale(locale)

    context.resources.updateConfiguration(config, context.resources.displayMetrics)

    context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        .edit()
        .putString("language", lang)
        .apply()
}

// Alias para clip sin import extra
private fun Modifier.clip(shape: androidx.compose.ui.graphics.Shape) =
    this.then(Modifier.graphicsLayer { this.clip = true; this.shape = shape })
