package com.example.momentum2.ui.momentos

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momentum2.MainActivity
import com.example.momentum2.databinding.ItemMomentosBinding
import com.example.momentum2.Moment


class MomentosAdapter(private val lista: List<Moment>) :
    RecyclerView.Adapter<MomentosAdapter.MomentoViewHolder>() {

    class MomentoViewHolder(val binding: ItemMomentosBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MomentoViewHolder {
        val binding = ItemMomentosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MomentoViewHolder(binding)

    }

    override fun onBindViewHolder(holder: MomentoViewHolder, position: Int) {
        val momento = lista[position]
        try {
            holder.binding.textDescripcion.text = momento.descripcion ?: "Sin descripción"
            Glide.with(holder.itemView.context)
                .load(momento.fotoUrl ?: "")
                .override(1024,768)
                .into(holder.binding.imageMomento)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(holder.itemView.context, "Error en posición $position: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun getItemCount() = lista.size
}

