package com.example.medysync

import UserPreferences
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SeetingsActivity : AppCompatActivity() {

    private lateinit var userPreferences: UserPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_seetings)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, FabFragment())
            .commit()

        userPreferences = UserPreferences(this)

        val btnHistorial = findViewById<MaterialButton>(R.id.btnHistorial)

        btnHistorial.setOnClickListener {
            val intent = Intent(this, HistorialTomasActivity::class.java)
            startActivity(intent)
        }

        val btnSubirArchivo = findViewById<MaterialButton>(R.id.btnSubirArchivo)
        btnSubirArchivo.setOnClickListener {
            val intent = Intent(this, SubirArchivoActivity::class.java)
            startActivity(intent)
        }

        val btnCrearCita = findViewById<MaterialButton>(R.id.btnCrearCita)
        btnCrearCita.setOnClickListener {
            val intent = Intent(this, CrearCitaActivity::class.java)
            startActivity(intent)
        }

        val btnHistorialCitas = findViewById<MaterialButton>(R.id.btnHistorialCitas)
        btnHistorialCitas.setOnClickListener {
            startActivity(Intent(this, HistorialCitasActivity::class.java))
        }

        val btnActualizarPerfil = findViewById<Button>(R.id.btnActualizarPerfil)
        btnActualizarPerfil.setOnClickListener {
            val intent = Intent(this, ActualizarPerfilActivity::class.java)
            startActivity(intent)
        }

        val btnInforme = findViewById<Button>(R.id.btnInformeActivity)
        btnInforme.setOnClickListener {
            val intent = Intent(this, InformeActivity::class.java)
            startActivity(intent)
        }


        val btnCerrarSesion = findViewById<MaterialButton>(R.id.btnCerrarSesion)

        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }

    }

    private fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()

        lifecycleScope.launch {

            userPreferences.clearUserData()


            val intent = Intent(this@SeetingsActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
