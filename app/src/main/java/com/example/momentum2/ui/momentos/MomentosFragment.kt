package com.example.momentum2.ui.momentos

import android.app.DownloadManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momentum2.GuardarMomentoActivity
import com.example.momentum2.databinding.FragmentMomentosBinding
import com.example.momentum2.Moment

import com.google.firebase.firestore.FirebaseFirestore


class MomentosFragment : Fragment() {

    private var _binding: FragmentMomentosBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
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

        adapter = MomentosAdapter(listaMomentos)
        binding.recyclerMomentos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMomentos.adapter = adapter

        cargarMomentos()

    }

    private fun cargarMomentos() {
        db.collection("momentos")
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                listaMomentos.clear()
                for (document in snapshot.documents) {
                    val momento = document.toObject(Moment::class.java)
                    if (momento != null) listaMomentos.add(momento)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


