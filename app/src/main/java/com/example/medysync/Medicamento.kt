package com.example.medysync

data class Medicamento(
    val nombre: String = "",
    val dosis: String = "",
    val frecuencia: String = "",
    val frecuenciaHoras: Int = 1,
    val fechaFin: Long = 0L,
    val id: String = "",
    val fechaCreacion: Long = 0L
)
