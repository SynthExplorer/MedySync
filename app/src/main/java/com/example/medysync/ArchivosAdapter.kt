package com.example.medysync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ArchivosAdapter(
    private val lista: List<Archivo>,
    private val onDescargarClick: (Archivo) -> Unit,
    private val onEliminarClick: (Archivo) -> Unit
) : RecyclerView.Adapter<ArchivosAdapter.ArchivoViewHolder>() {

    inner class ArchivoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreArchivo: TextView = itemView.findViewById(R.id.tvNombreArchivo)
        val btnDescargar: ImageButton = itemView.findViewById(R.id.btnDescargar)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminar)
        val imgPreview: ImageView = itemView.findViewById(R.id.imgPreview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchivoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_archivo, parent, false)
        return ArchivoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArchivoViewHolder, position: Int) {
        val archivo = lista[position]
        holder.tvNombreArchivo.text = archivo.nombre

        // Ver si es imagen
        if (archivo.nombre.endsWith(".jpg", true) ||
            archivo.nombre.endsWith(".jpeg", true) ||
            archivo.nombre.endsWith(".png", true) ||
            archivo.nombre.endsWith(".gif", true)
        ) {
            // Carga miniatura con Glide
            Glide.with(holder.itemView.context)
                .load(archivo.url)
                .placeholder(R.drawable.ic_image)
                .into(holder.imgPreview)
        } else {
            // Ícono genérico si no es imagen
            holder.imgPreview.setImageResource(R.drawable.ic_file)
        }

        holder.btnDescargar.setOnClickListener { onDescargarClick(archivo) }
        holder.btnEliminar.setOnClickListener { onEliminarClick(archivo) }
    }

    override fun getItemCount(): Int = lista.size
}
