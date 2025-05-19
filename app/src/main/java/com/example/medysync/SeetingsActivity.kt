package com.example.medysync

import UserPreferences
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.medysync.databinding.ActivitySeetingsBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SeetingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeetingsBinding
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySeetingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val buttons = listOf(
            findViewById<MaterialButton>(R.id.btnHistorial),
            findViewById<MaterialButton>(R.id.btnHistorialCitas),
            findViewById<MaterialButton>(R.id.btnSubirArchivo),
            findViewById<MaterialButton>(R.id.btnCrearCita),
            findViewById<MaterialButton>(R.id.btnInformeActivity),
            findViewById<MaterialButton>(R.id.btnActualizarPerfil),
            findViewById<MaterialButton>(R.id.btnCerrarSesion)
        )

        buttons.forEachIndexed { index, button ->
            button.visibility = View.INVISIBLE
            button.postDelayed({
                button.visibility = View.VISIBLE
                val animation = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up)
                button.startAnimation(animation)
            }, index * 250L)
        }


        userPreferences = UserPreferences(this)

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnHistorial.setOnClickListener {
            animateButton(it)
            startActivity(Intent(this, HistorialTomasActivity::class.java))
        }

        binding.btnSubirArchivo.setOnClickListener {
            animateButton(it)
            startActivity(Intent(this, SubirArchivoActivity::class.java))
        }

        binding.btnCrearCita.setOnClickListener {
            animateButton(it)
            startActivity(Intent(this, CrearCitaActivity::class.java))
        }

        binding.btnHistorialCitas.setOnClickListener {
            animateButton(it)
            startActivity(Intent(this, HistorialCitasActivity::class.java))
        }

        binding.btnInformeActivity.setOnClickListener {
            animateButton(it)
            startActivity(Intent(this, InformeActivity::class.java))
        }

        binding.btnActualizarPerfil.setOnClickListener {
            animateButton(it)
            startActivity(Intent(this, ActualizarPerfilActivity::class.java))
        }

        binding.btnCerrarSesion.setOnClickListener {
            animateButton(it)
            cerrarSesion()
        }
    }

    private fun animateButton(view: View) {
        view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).duration = 100
        }.start()
    }

    private fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()

        lifecycleScope.launch {
            userPreferences.clearUserData()

            Toast.makeText(this@SeetingsActivity, "Sesión cerrada", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@SeetingsActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
