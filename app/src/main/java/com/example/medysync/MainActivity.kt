package com.example.medysync

import UserPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.content.Intent
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var tvNombre: TextView
    private lateinit var tvApellido: TextView
    private lateinit var tvCedula: TextView
    private lateinit var btnEditarPerfil: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, FabFragment())
            .commit()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Vincular vistas
        tvNombre = findViewById(R.id.tvNombre)
        tvApellido = findViewById(R.id.tvApellido)
        tvCedula = findViewById(R.id.tvCedula)
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil)

        btnEditarPerfil.setOnClickListener {
            val intent = Intent(this, ActualizarPerfilActivity::class.java)
            startActivity(intent)
        }
        val ivAvatar = findViewById<ImageView>(R.id.ivAvatar)
        val bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce)
        ivAvatar.startAnimation(bounceAnim)

    }

    override fun onResume() {
        super.onResume()
        actualizarDatosUsuario()
    }

    private fun actualizarDatosUsuario() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nombre = document.getString("nombre") ?: "Usuario"
                        val apellido = document.getString("apellido") ?: ""
                        val cedula = document.getString("id") ?: ""

                        tvNombre.text = "Hola, $nombre"
                        tvApellido.text = apellido
                        tvCedula.text = cedula

                    } else {
                        Toast.makeText(this, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al obtener los datos del usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }
}
