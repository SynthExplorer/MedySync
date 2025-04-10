package com.example.medysync

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.widget.Button
import android.widget.EditText
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

        btnRegister.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
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