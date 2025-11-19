package com.example.momentum2.ui.momentos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momentum2.databinding.FragmentMomentosBinding
import com.example.momentum2.Moment
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast

class MomentosFragment : Fragment() {

    private var _binding: FragmentMomentosBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var adapter: MomentosAdapter
    private val listaMomentos = mutableListOf<Moment>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMomentosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("MomentosFragment", "Fragment creado")

        adapter = MomentosAdapter(listaMomentos) { momento, position ->
            mostrarDialogoEliminar(momento, position)
        }

        binding.recyclerMomentos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMomentos.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            cargarMomentos()
        }

        cargarMomentos()
    }

    private fun cargarMomentos() {
        binding.swipeRefreshLayout.isRefreshing = true

        firestore.collection("momentos")
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                listaMomentos.clear()
                for (document in snapshot.documents) {
                    val momento = document.toObject(Moment::class.java)?.apply {
                        id = document.id
                    }
                    if (momento != null) listaMomentos.add(momento)
                }
                adapter.notifyDataSetChanged()
                EmptyMoments()
                binding.swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al cargar momentos", Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false
            }
    }

    private fun mostrarDialogoEliminar(momento: Moment, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar momento")
            .setMessage("¿Estás seguro de que deseas eliminar este momento?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarMomento(momento, position)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarMomento(momento: Moment, position: Int) {

        firestore.collection("momentos")
            .document(momento.id)
            .delete()
            .addOnSuccessListener {
                listaMomentos.removeAt(position)
                adapter.notifyItemRemoved(position)
                Toast.makeText(requireContext(), "Momento eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun EmptyMoments() {
        if (listaMomentos.isEmpty()) {
            binding.emptyList.visibility = View.VISIBLE
            binding.recyclerMomentos.visibility = View.GONE
        } else {
            binding.emptyList.visibility = View.GONE
            binding.recyclerMomentos.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}