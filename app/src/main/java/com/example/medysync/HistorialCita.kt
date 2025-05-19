package com.example.medysync

data class HistorialCita(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val hora: String = "",
    val frecuencia: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
