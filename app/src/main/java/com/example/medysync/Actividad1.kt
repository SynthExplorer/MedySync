package com.example.medysync

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Actividad1 : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicamentoAdapter
    private var listaMedicamentos = mutableListOf<Medicamento>()
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_actividad1)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, FabFragment())
            .commit()

        // Inicializar Firestore y Auth
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewMedicamentos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MedicamentoAdapter(listaMedicamentos)
        recyclerView.adapter = adapter

        obtenerMedicamentosDelUsuario()
    }

    private fun obtenerMedicamentosDelUsuario() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("usuarios")
                .document(userId)
                .collection("medicamentos")
                .get()
                .addOnSuccessListener { result ->
                    listaMedicamentos.clear() // Limpiar la lista antes de agregar nuevos
                    for (document in result) {
                        val medicamento = document.toObject(Medicamento::class.java)
                        listaMedicamentos.add(medicamento)
                    }
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "✅ Medicamentos obtenidos correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "❌ Error al obtener los medicamentos: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "⚠️ Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }
}
