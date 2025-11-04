package com.example.momentum2.ui.momentos

import android.app.DownloadManager
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class GaleriaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: ImagenAdapter
//    private val listaImagenes = mutableListOf<Imagen>()
//    private val firestore = FirebaseFirestore.getInstance()
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_galeria, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        recyclerView = view.findViewById(R.id.recyclerGaleria)
//        adapter = ImagenAdapter(listaImagenes)
//
//        // Grid con 3 columnas en modo vertical
//        val columnas = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
//        recyclerView.layoutManager = GridLayoutManager(requireContext(), columnas)
//        recyclerView.adapter = adapter
//
//        cargarImagenes()
//    }
//
//    private fun cargarImagenes() {
//        firestore.collection("imagenes")
//            .orderBy("timestamp", DownloadManager.Query.Direction.DESCENDING)
//            .get()
//            .addOnSuccessListener { result ->
//                listaImagenes.clear()
//                for (doc in result) {
//                    val imagen = doc.toObject(Imagen::class.java)
//                    listaImagenes.add(imagen)
//                }
//                adapter.notifyDataSetChanged()
//            }
//            .addOnFailureListener {
//                Toast.makeText(requireContext(), "Error al cargar imágenes", Toast.LENGTH_SHORT).show()
//            }
//    }
}

