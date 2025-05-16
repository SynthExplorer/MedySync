package com.example.medysync

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout


class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        val blob1 = findViewById<ImageView>(R.id.blob_1)
        val blob2 = findViewById<ImageView>(R.id.blob_2)

        val moveX = ObjectAnimator.ofFloat(blob1, "translationX", 300f)
        val moveY = ObjectAnimator.ofFloat(blob1, "translationY", -700f)
        val rotate = ObjectAnimator.ofFloat(blob1, "rotation", 0f, 110f)
        val fadeIn = ObjectAnimator.ofFloat(blob1, "alpha", 0f, 0.5f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(moveX, moveY, rotate, fadeIn)
        animatorSet.duration = 2000
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()

        val moveX2 = ObjectAnimator.ofFloat(blob2, "translationX", -550f) // hacia la izquierda
        val moveY2 = ObjectAnimator.ofFloat(blob2, "translationY", 1900f)  // sigue abajo pero visible
        val rotate2 = ObjectAnimator.ofFloat(blob2, "rotation", 0f, -90f)
        val fadeIn2 = ObjectAnimator.ofFloat(blob2, "alpha", 0f, 0.5f)

        val animatorSet2 = AnimatorSet()
        animatorSet2.playTogether(moveX2, moveY2, rotate2, fadeIn2)
        animatorSet2.duration = 2000
        animatorSet2.interpolator = AccelerateDecelerateInterpolator()
        animatorSet2.start()

        val rootLayout = findViewById<ConstraintLayout>(R.id.main)
        val animation: Animation = AnimationUtils.loadAnimation(this, R.anim.fadein)
        rootLayout.startAnimation(animation)

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
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            //finish()
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

                                Log.d("RegisterActivity", "Datos guardados correctamente. Redirigiendo...")



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
