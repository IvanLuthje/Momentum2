package com.example.momentum2

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.momentum2.databinding.ActivityEditDescripcionBinding


class EditDescripcion : AppCompatActivity() {


    private lateinit var binding: ActivityEditDescripcionBinding
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditDescripcionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val momentoId = intent.getStringExtra("id") ?: ""
        val descripcion = intent.getStringExtra("descripcion") ?: ""

        binding.editTextDescripcion.setText(descripcion)

        binding.buttonGuardar.setOnClickListener {
            actualizarMomento(momentoId)
        }
    }


    private fun actualizarMomento(momentoId: String) {

        val nuevaDescripcion = binding.editTextDescripcion.text.toString().trim()

        if (nuevaDescripcion.isEmpty()) {
            Toast.makeText(this, "Por favor agregar una descripción", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("momentos")
            .document(momentoId)
            .update("descripcion", nuevaDescripcion)
            .addOnSuccessListener {
                Toast.makeText(this, "Momento actualizado correctamente", Toast.LENGTH_SHORT).show()

                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }


}


