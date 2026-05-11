// BuscarAdapter.kt
package com.example.momentum2.ui.buscar

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momentum2.Moment
import com.example.momentum2.MomentoActivity
import com.example.momentum2.databinding.ItemBuscarBinding

class BuscarAdapter(
    private val lista: MutableList<Moment>
) : RecyclerView.Adapter<BuscarAdapter.BuscarViewHolder>() {

    private var listaFiltrada: MutableList<Moment> = lista.toMutableList()

    class BuscarViewHolder(val binding: ItemBuscarBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuscarViewHolder {
        val binding = ItemBuscarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BuscarViewHolder(binding)
    }

    fun updateList(nuevaLista: List<Moment>) {
        lista.clear()
        lista.addAll(nuevaLista)
        listaFiltrada = lista.toMutableList()
        notifyDataSetChanged()
    }

    fun filtro(texto: String) {
        val query = texto.lowercase().trim()

        listaFiltrada = if (query.isEmpty()) {
            lista.toMutableList()
        } else {
            lista.filter { momento ->
                (momento.descripcion ?: "").lowercase().contains(query)
            }.toMutableList()
        }

        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: BuscarViewHolder, position: Int) {
        val momento = listaFiltrada[position]

        try {
            holder.binding.textDescripcion.text = momento.descripcion ?: "Sin descripción"

            Glide.with(holder.itemView.context)
                .load(momento.fotoUrl ?: "")
                .override(1920, 1080)
                .into(holder.binding.imageMomento)

            holder.binding.imageMomento.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, MomentoActivity::class.java).apply {
                    putExtra("id", momento.id)
                    putExtra("descripcion", momento.descripcion)
                    putExtra("foto_url", momento.fotoUrl)
                    putExtra("fecha", momento.fecha)
                    putExtra("longitud", momento.longitud)
                    putExtra("latitud", momento.latitud)
                }
                context.startActivity(intent)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                holder.itemView.context,
                "Error en posición $position: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount() = listaFiltrada.size
}