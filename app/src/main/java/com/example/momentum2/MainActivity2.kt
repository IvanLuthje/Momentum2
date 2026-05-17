//package com.example.momentum2
//
//import android.content.ActivityNotFoundException
//import android.content.Intent
//import android.graphics.Bitmap
//import android.os.Bundle
//import android.provider.MediaStore
//import android.view.Menu
//import android.view.MenuItem
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.viewpager.widget.ViewPager
//import com.example.momentum2.databinding.ActivityMain2Binding
//import com.example.momentum2.ui.login.LoginActivity2
//import com.example.momentum2.ui.main.SectionsPagerAdapter
//import com.example.momentum2.ui.momentos.MomentosFragment
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//import com.google.android.material.navigation.NavigationView
//import com.google.android.material.tabs.TabLayout
//import com.google.firebase.Firebase
//import com.google.firebase.auth.auth
//import java.io.ByteArrayOutputStream
//
//class MainActivity2 : AppCompatActivity() {
//
//
//    private lateinit var binding: ActivityMain2Binding
//
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMain2Binding.inflate(layoutInflater)
//        setContentView(binding.root)
//        setSupportActionBar(binding.toolbar)
//
//        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
//        val viewPager: ViewPager = binding.viewPager
//        viewPager.adapter = sectionsPagerAdapter
//        val tabs: TabLayout = binding.tabs
//        tabs.setupWithViewPager(viewPager)
//        if (savedInstanceState == null) {
//            val fragment = MomentosFragment()
//
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, fragment)
//                .commit()
//        }
//        val fab: FloatingActionButton = binding.fab
//
//        fab.setOnClickListener { view ->
//            capturarIntent()
//        }
//
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val result = super.onCreateOptionsMenu(menu)
//
//        val navView: NavigationView? = findViewById(R.id.nav_view)
//        if (navView == null) {
//            menuInflater.inflate(R.menu.overflow, menu)
//        }
//        return result
//    }
//
//
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.nav_settings -> {
//                startActivity(Intent(this, SettingsActivity::class.java))
//
//            }
//
//            R.id.logout -> {
//                cerrarSesion()
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
//
//
//
//    private val camaraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == RESULT_OK && result.data != null) {
//            val imageBitmap = result.data!!.extras!!.get("data") as Bitmap
//
//            val stream = ByteArrayOutputStream()
//            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
//            val bytes = stream.toByteArray()
//
//            val intent = Intent(this, GuardarMomentoActivity::class.java)
//            intent.putExtra("foto", bytes)
//            startActivity(intent)
//        }
//    }
//
//
//    private fun capturarIntent() {
//        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        try {
//            camaraLauncher.launch(takePictureIntent)
//        }
//        catch (e: ActivityNotFoundException) {
//            Toast.makeText(applicationContext, R.string.camera_error , Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun cerrarSesion(){
//        Firebase.auth.signOut()
//        val intent_login = Intent(this,  LoginActivity2::class.java)
//        startActivity(intent_login)
//        finish()
//
//    }
//
//
//
//}

package com.example.momentum2

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity // Cambiado para soportar FragmentManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView // Necesario para la interoperabilidad
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.example.momentum2.ui.login.LoginActivity2
import com.example.momentum2.ui.momentos.MomentosFragment // Tu fragmento actual
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.io.ByteArrayOutputStream

class MainActivity2 : AppCompatActivity() { // Volvemos a AppCompatActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(
                fragmentManager = supportFragmentManager, // Pasamos el FragmentManager
                onLogout = { cerrarSesion() }
            )
        }
    }

    private fun cerrarSesion() {
        Firebase.auth.signOut()
        val intentLogin = Intent(this, LoginActivity2::class.java)
        startActivity(intentLogin)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(fragmentManager: FragmentManager, onLogout: () -> Unit) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val imageBitmap = result.data!!.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                val stream = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                val bytes = stream.toByteArray()

                val intent = Intent(context, GuardarMomentoActivity::class.java).apply {
                    putExtra("foto", bytes)
                }
                context.startActivity(intent)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = context.getString(R.string.app_name)) },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menú")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Configuración") },
                                onClick = {
                                    menuExpanded = false
                                    context.startActivity(Intent(context, SettingsActivity::class.java))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cerrar Sesión") },
                                onClick = {
                                    menuExpanded = false
                                    onLogout()
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    try {
                        camaraLauncher.launch(takePictureIntent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, R.string.camera_error, Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Capturar foto")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Llamamos al contenedor que cargará el fragmento tradicional
            FragmentContainerWrapper(fragmentManager = fragmentManager, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun FragmentContainerWrapper(fragmentManager: FragmentManager, modifier: Modifier = Modifier) {
    // Generamos un ID único en memoria para el contenedor del fragmento para evitar colisiones
    val containerId = remember { View.generateViewId() }

    // AndroidView nos permite renderizar una vista tradicional de Android dentro de Compose
    AndroidView(
        factory = { context ->
            FragmentContainerView(context).apply {
                id = containerId
            }
        },
        modifier = modifier,
        update = { view ->
            // Evitamos recrear o duplicar el fragmento cada vez que Compose se redibuje (Recomposición)
            if (fragmentManager.findFragmentById(view.id) == null) {
                val fragment = MomentosFragment()
                fragmentManager.beginTransaction()
                    .replace(view.id, fragment)
                    .commit()
            }
        }
    )
}