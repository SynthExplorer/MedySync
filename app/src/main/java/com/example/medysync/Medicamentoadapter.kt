package com.example.medysync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MedicamentoAdapter(private val listaMedicamentos: List<Medicamento>) :
    RecyclerView.Adapter<MedicamentoAdapter.MedicamentoViewHolder>() {

    class MedicamentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.tvNombre)
        val dosis: TextView = itemView.findViewById(R.id.tvDosis)
        val frecuencia: TextView = itemView.findViewById(R.id.tvFrecuencia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicamentoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medicamento, parent, false)
        return MedicamentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicamentoViewHolder, position: Int) {
        val medicamento = listaMedicamentos[position]
        holder.nombre.text = medicamento.nombre
        holder.dosis.text = medicamento.dosis
        holder.frecuencia.text = medicamento.frecuencia
    }

    override fun getItemCount(): Int = listaMedicamentos.size
}
