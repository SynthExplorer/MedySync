package com.example.medysync

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medysync.databinding.ActivityActualizarPerfilBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ActualizarPerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActualizarPerfilBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActualizarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnGuardarCambios.setOnClickListener {
            actualizarDatosUsuario()
        }
    }

    private fun actualizarDatosUsuario() {
        val nuevoNombre = binding.etNuevoNombre.text.toString().trim()
        val nuevoApellido = binding.etNuevoApellido.text.toString().trim()   // <-- nuevo campo
        val nuevoCorreo = binding.etNuevoCorreo.text.toString().trim()
        val nuevaContrasena = binding.etNuevaContrasena.text.toString().trim()
        val contrasenaActual = binding.etContrasenaActual.text.toString().trim()

        val user = auth.currentUser

        if (user != null && contrasenaActual.isNotEmpty()) {
            val providers = user.providerData.map { it.providerId }
            if (!providers.contains("password")) {
                Toast.makeText(this, "No se puede actualizar el correo porque el usuario no inició sesión con Email/Password.", Toast.LENGTH_LONG).show()
                return
            }

            val credential = EmailAuthProvider.getCredential(user.email ?: "", contrasenaActual)
            user.reauthenticate(credential).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    actualizarCorreoYContrasena(user, nuevoCorreo, nuevaContrasena,
                        onSuccess = {
                            val userId = user.uid
                            val userMap = mutableMapOf<String, Any>()
                            if (nuevoNombre.isNotEmpty()) userMap["nombre"] = nuevoNombre
                            if (nuevoApellido.isNotEmpty()) userMap["apellido"] = nuevoApellido       // <-- actualizar apellido
                            if (nuevoCorreo.isNotEmpty() && nuevoCorreo != user.email) userMap["correo"] = nuevoCorreo

                            if (userMap.isNotEmpty()) {
                                db.collection("usuarios").document(userId).update(userMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, SeetingsActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Error al actualizar datos en Firestore", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, SeetingsActivity::class.java))
                                finish()
                            }
                        },
                        onFailure = { errorMsg ->
                            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(this, "Reautenticación fallida. Verifica tu contraseña actual.", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this, "Debes ingresar tu contraseña actual para actualizar tus datos", Toast.LENGTH_LONG).show()
        }
    }

    private fun actualizarCorreoYContrasena(
        user: FirebaseUser,
        nuevoCorreo: String,
        nuevaContrasena: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (nuevoCorreo.isNotEmpty() && nuevoCorreo != user.email) {
            user.updateEmail(nuevoCorreo).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (nuevaContrasena.isNotEmpty()) {
                        user.updatePassword(nuevaContrasena).addOnCompleteListener { passTask ->
                            if (passTask.isSuccessful) {
                                onSuccess()
                            } else {
                                onFailure("Error al actualizar contraseña: ${passTask.exception?.message}")
                            }
                        }
                    } else {
                        onSuccess()
                    }
                } else {
                    onFailure("Error al actualizar correo: ${task.exception?.message}")
                }
            }
        } else {
            if (nuevaContrasena.isNotEmpty()) {
                user.updatePassword(nuevaContrasena).addOnCompleteListener { passTask ->
                    if (passTask.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("Error al actualizar contraseña: ${passTask.exception?.message}")
                    }
                }
            } else {
                onSuccess()
            }
        }
    }
}
