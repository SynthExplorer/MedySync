package com.example.medysync

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CitaAdapter(private val listaCitas: List<Cita>) :
    RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    inner class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTitulo)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        val tvFechaHora: TextView = itemView.findViewById(R.id.tvFechaHora)
        val tvFrecuencia: TextView = itemView.findViewById(R.id.tvFrecuencia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = listaCitas[position]
        holder.tvTitulo.text = cita.titulo
        holder.tvDescripcion.text = cita.descripcion
        holder.tvFechaHora.text = "${cita.fecha} | ${cita.hora}"
        holder.tvFrecuencia.text = "Frecuencia: ${cita.frecuencia}"
    }

    override fun getItemCount(): Int = listaCitas.size
}
