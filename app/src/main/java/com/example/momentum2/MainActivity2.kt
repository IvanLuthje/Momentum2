package com.example.momentum2

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.momentum2.databinding.ActivityMain2Binding
import com.example.momentum2.ui.login.LoginActivity2
import com.example.momentum2.ui.main.SectionsPagerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.io.ByteArrayOutputStream

class MainActivity2 : AppCompatActivity() {


    private lateinit var binding: ActivityMain2Binding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener { view ->
            capturarIntent()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)

        val navView: NavigationView? = findViewById(R.id.nav_view)
        if (navView == null) {
            menuInflater.inflate(R.menu.overflow, menu)
        }
        return result
    }

    

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))

            }

            R.id.logout -> {
                cerrarSesion()
            }
        }
        return super.onOptionsItemSelected(item)
    }




    private val camaraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val imageBitmap = result.data!!.extras!!.get("data") as Bitmap

            val stream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            val bytes = stream.toByteArray()

            val intent = Intent(this, GuardarMomentoActivity::class.java)
            intent.putExtra("foto", bytes)
            startActivity(intent)
        }
    }


    private fun capturarIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            camaraLauncher.launch(takePictureIntent)
        }
        catch (e: ActivityNotFoundException) {
            Toast.makeText(applicationContext, R.string.camera_error , Toast.LENGTH_SHORT).show()
        }
    }

    private fun cerrarSesion(){
        Firebase.auth.signOut()
        val intent_login = Intent(this,  LoginActivity2::class.java)
        startActivity(intent_login)
        finish()

    }



}