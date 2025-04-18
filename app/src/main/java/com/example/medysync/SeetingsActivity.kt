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
