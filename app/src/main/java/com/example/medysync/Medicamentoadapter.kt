package com.example.medysync

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MedicamentoAdapter(private val listaMedicamentos: List<Medicamento>) :
    RecyclerView.Adapter<MedicamentoAdapter.MedicamentoViewHolder>() {

    private val handler = Handler(Looper.getMainLooper())

    class MedicamentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.tvNombre)
        val dosis: TextView = itemView.findViewById(R.id.tvDosis)
        val tvTiempoRestante: TextView = itemView.findViewById(R.id.tvTiempoRestante)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicamentoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medicamento, parent, false)
        return MedicamentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicamentoViewHolder, position: Int) {
        val medicamento = listaMedicamentos[position]
        holder.nombre.text = medicamento.nombre
        holder.dosis.text = medicamento.dosis

        val runnable = object : Runnable {
            override fun run() {
                actualizarTiempoRestante(medicamento, holder)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)

        holder.itemView.setOnClickListener {
            try {
                // Log para verificar que los datos se pasan correctamente
                Log.d("MedicamentoAdapter", "Navegando a MedicamentoDetalleActivity con id: ${medicamento.id}, nombre: ${medicamento.nombre}, dosis: ${medicamento.dosis}")

                val context = holder.itemView.context
                val intent = Intent(context, MedicamentoDetalleActivity::class.java).apply {
                    putExtra("id", medicamento.id)
                    putExtra("nombre", medicamento.nombre)
                    putExtra("dosis", medicamento.dosis)
                    putExtra("fechaFin", medicamento.fechaFin)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("MedicamentoAdapter", "Error al abrir detalle", e)
            }
        }
    }

    private fun actualizarTiempoRestante(medicamento: Medicamento, holder: MedicamentoViewHolder) {
        val tiempoRestante = medicamento.fechaFin - System.currentTimeMillis()
        val segundos = (tiempoRestante / 1000) % 60
        val minutos = (tiempoRestante / (1000 * 60)) % 60
        val horas = (tiempoRestante / (1000 * 60 * 60)) % 24
        val dias = (tiempoRestante / (1000 * 60 * 60 * 24))
        val meses = dias / 30

        val textoTiempoRestante = if (tiempoRestante > 0) {
            "${meses}m ${dias % 30}d ${horas}h ${minutos}m ${segundos}s restantes"
        } else {
            "🛑 Tratamiento finalizado"
        }

        holder.tvTiempoRestante.text = textoTiempoRestante
    }

    override fun getItemCount(): Int = listaMedicamentos.size
}
