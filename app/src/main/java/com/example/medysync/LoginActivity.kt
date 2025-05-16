package com.example.medysync

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        Toast.makeText(this, "Login iniciado", Toast.LENGTH_SHORT).show()

        Log.d("LoginActivity", "LoginActivity iniciada")


        val blob_1 = findViewById<ImageView>(R.id.blob_1)
        val blob_2 = findViewById<ImageView>(R.id.blob_2)

        blob_1.translationX = -900f  // empieza más a la izquierda
        blob_1.translationY = -900f  // empieza más arriba
        blob_1.alpha = 0f            // comienza invisible

        blob_1.animate()
            .translationX(-500f)
            .translationY(-500f)
            .alpha(0.5f)
            .setDuration(3000)
            .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
            .start()

        blob_2.translationX = 900f  // empieza más a la izquierda
        blob_2.translationY = 900f  // empieza más arriba
        blob_2.alpha = 0f            // comienza invisible

        blob_2.animate()
            .translationX(0f)
            .translationY(0f)
            .alpha(0.5f)
            .setDuration(3000)
            .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
            .start()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener{
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Por favor ingrese su correo electrónico y contraseña", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email,password)
            }
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        }

    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    if (userId != null){
                        db.collection("usuarios").document(userId).get().addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                val nombre = document.getString("nombre") ?: ""
                                val apellido = document.getString("apellido") ?: ""
                                val cedula = document.getString("id") ?: ""

                                val intent = Intent(this, MainActivity::class.java).apply {
                                    putExtra("nombre", nombre)
                                    putExtra("apellido", apellido)
                                    putExtra("cedula", cedula)
                                }
                                Toast.makeText(this, "Inicio de sesión exitoso 🎉", Toast.LENGTH_SHORT).show()
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "No se encontraron los datos del usuario", Toast.LENGTH_SHORT).show()
                            }
                        }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al obtener los datos del usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}