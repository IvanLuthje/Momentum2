// BuscarFragment.kt
package com.example.momentum2.ui.momentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momentum2.databinding.FragmentBuscarBinding
import com.example.momentum2.Moment
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import com.example.momentum2.ui.buscar.BuscarAdapter

class BuscarFragment : Fragment() {

    private var _binding: FragmentBuscarBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var adapter: BuscarAdapter
    private val listaMomentos = mutableListOf<Moment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuscarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BuscarAdapter(listaMomentos)

        binding.recyclerMomentos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMomentos.adapter = adapter

        binding.searchViewMomentos.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    adapter.filtro(query ?: "")
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filtro(newText ?: "")
                    return true
                }
            }
        )

        binding.swipeRefreshLayout.setOnRefreshListener {
            buscarMomentos()
        }

        buscarMomentos()
    }

    private fun buscarMomentos() {
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

                adapter.updateList(listaMomentos)

                EmptyMoments()
                binding.swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al cargar momentos", Toast.LENGTH_SHORT)
                    .show()
                binding.swipeRefreshLayout.isRefreshing = false
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