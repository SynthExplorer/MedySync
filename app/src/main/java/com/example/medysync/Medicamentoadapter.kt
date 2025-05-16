package com.example.medysync

import android.content.Intent
import android.os.CountDownTimer
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

    class MedicamentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.tvNombre)
        val dosis: TextView = itemView.findViewById(R.id.tvDosis)
        val tvTiempoRestante: TextView = itemView.findViewById(R.id.tvTiempoRestante)
        var countDownTimer: CountDownTimer? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicamentoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicamento, parent, false)
        return MedicamentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicamentoViewHolder, position: Int) {
        val medicamento = listaMedicamentos[position]
        holder.nombre.text = medicamento.nombre
        holder.dosis.text = medicamento.dosis

        // Cancelar cualquier temporizador anterior
        holder.countDownTimer?.cancel()

        val tiempoRestante = medicamento.fechaFin - System.currentTimeMillis()

        if (tiempoRestante > 0) {
            // Verificar si ya existe un temporizador antes de crear uno nuevo
            holder.countDownTimer = object : CountDownTimer(tiempoRestante, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val segundos = millisUntilFinished / 1000 % 60
                    val minutos = millisUntilFinished / (1000 * 60) % 60
                    val horas = millisUntilFinished / (1000 * 60 * 60) % 24
                    val dias = millisUntilFinished / (1000 * 60 * 60 * 24)
                    holder.tvTiempoRestante.text = "$dias d $horas h $minutos m $segundos s"
                }

                override fun onFinish() {
                    holder.tvTiempoRestante.text = "Tratamiento finalizado"
                }
            }
            holder.countDownTimer?.start()
        } else {
            holder.tvTiempoRestante.text = "Tratamiento finalizado"
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, MedicamentoDetalleActivity::class.java).apply {
                putExtra("id", medicamento.id)
                putExtra("nombre", medicamento.nombre)
                putExtra("dosis", medicamento.dosis)
                putExtra("fechaFin", medicamento.fechaFin)
                putExtra("frecuenciaHoras", medicamento.frecuenciaHoras)

            }
            context.startActivity(intent)
        }
    }

    override fun onViewRecycled(holder: MedicamentoViewHolder) {
        super.onViewRecycled(holder)
        //
        holder.countDownTimer?.cancel()
    }

    override fun getItemCount(): Int = listaMedicamentos.size
}
