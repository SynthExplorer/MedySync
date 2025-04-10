package com.example.medysync

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val etNombre = findViewById<EditText>(R.id.etRegisterNombre)
        val etApellido = findViewById<EditText>(R.id.etRegisterApellido)
        val etId = findViewById<EditText>(R.id.etRegisterId)

        val btnRegister = findViewById<Button>(R.id.btnRegisterUser)
        val btnGoLogin = findViewById<Button>(R.id.btnGoLogin)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val nombre = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val id = etId.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
                || nombre.isEmpty() || apellido.isEmpty() || id.isEmpty()
            ) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(email, password, nombre, apellido, id)
        }

        btnGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser(email: String, password: String, nombre: String, apellido: String, id: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val db = FirebaseFirestore.getInstance()

                    val userMap = hashMapOf(
                        "nombre" to nombre,
                        "apellido" to apellido,
                        "id" to id
                    )

                    userId?.let { uid ->
                        db.collection("usuarios").document(uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registro exitoso 🎉", Toast.LENGTH_SHORT).show()

                                
                                val intent = Intent(this, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                startActivity(intent)

                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al guardar los datos: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
