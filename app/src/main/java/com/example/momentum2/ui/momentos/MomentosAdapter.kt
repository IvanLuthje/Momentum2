package com.example.momentum2.ui.momentos

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momentum2.databinding.ItemMomentosBinding
import com.example.momentum2.Moment
import com.example.momentum2.MomentoActivity

class MomentosAdapter(
    private val lista: MutableList<Moment>,
    private val onDeleteClick: (Moment, Int) -> Unit
) : RecyclerView.Adapter<MomentosAdapter.MomentoViewHolder>() {

    class MomentoViewHolder(val binding: ItemMomentosBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MomentoViewHolder {
        val binding = ItemMomentosBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MomentoViewHolder(binding)
    }


    private var listaFiltrada: MutableList<Moment> = lista.toMutableList()

    fun filtro(texto: String) {
        val query = texto.lowercase()

        listaFiltrada = lista.filter { momento ->
            momento.descripcion.lowercase().contains(query) ||
                    momento.descripcion.any { it.lowercase().contains(query) }
        }.toMutableList()

        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MomentoViewHolder, position: Int) {
        val momento = lista[position]

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

            holder.binding.buttonEliminar.setOnClickListener {
                onDeleteClick(momento, position)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                holder.itemView.context,
                " $position: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount() = lista.size

}