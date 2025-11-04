package com.example.momentum2.ui.momentos

import android.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView

//class ImagenAdapter(private val lista: List<Imagen>) :
//    RecyclerView.Adapter<ImagenAdapter.ImagenViewHolder>() {
//
//    inner class ImagenViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val itemImageView: ImageView = view.findViewById(R.id.itemImageView)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagenViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_imagen, parent, false)
//        return ImagenViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ImagenViewHolder, position: Int) {
//        val imagen = lista[position]
//        Glide.with(holder.itemView.context)
//            .load(imagen.imageUrl)
//            .centerCrop()
//            .into(holder.itemImageView)
//    }
//
//    override fun getItemCount(): Int = lista.size
//}
