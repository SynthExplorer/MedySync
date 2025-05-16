package com.example.medysync

import android.animation.*
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        FirebaseApp.initializeApp(this)


        val logoImageView = findViewById<ImageView>(R.id.logoImageView)
        val appNameTextView = findViewById<TextView>(R.id.appNameTextView)
        val sloganTextView = findViewById<TextView>(R.id.sloganTextView)

        // Estado inicial
        logoImageView.alpha = 0f
        logoImageView.scaleX = 0.5f
        logoImageView.scaleY = 0.5f
        appNameTextView.alpha = 0f
        appNameTextView.translationY = 50f
        sloganTextView.alpha = 0f
        sloganTextView.translationY = 50f

        // Animaciones para el logo
        val logoFadeIn = ObjectAnimator.ofFloat(logoImageView, View.ALPHA, 0f, 1f)
        val logoScaleX = ObjectAnimator.ofFloat(logoImageView, View.SCALE_X, 0.5f, 1.1f, 1f)
        val logoScaleY = ObjectAnimator.ofFloat(logoImageView, View.SCALE_Y, 0.5f, 1.1f, 1f)

        val interpolator = AccelerateDecelerateInterpolator()
        logoFadeIn.interpolator = interpolator
        logoScaleX.interpolator = interpolator
        logoScaleY.interpolator = interpolator

        logoFadeIn.duration = 1000
        logoScaleX.duration = 1000
        logoScaleY.duration = 1000

        val logoAnimatorSet = AnimatorSet()
        logoAnimatorSet.playTogether(logoFadeIn, logoScaleX, logoScaleY)

        // Animaciones para nombre
        val appNameFadeIn = ObjectAnimator.ofFloat(appNameTextView, View.ALPHA, 0f, 1f)
        val appNameTranslateY = ObjectAnimator.ofFloat(appNameTextView, View.TRANSLATION_Y, 50f, 0f)

        appNameFadeIn.duration = 700
        appNameTranslateY.duration = 700
        appNameFadeIn.startDelay = 500
        appNameTranslateY.startDelay = 500
        appNameTranslateY.interpolator = OvershootInterpolator(1.5f)

        val appNameAnimatorSet = AnimatorSet()
        appNameAnimatorSet.playTogether(appNameFadeIn, appNameTranslateY)

        // Animaciones para el slogan
        val sloganFadeIn = ObjectAnimator.ofFloat(sloganTextView, View.ALPHA, 0f, 1f)
        val sloganTranslateY = ObjectAnimator.ofFloat(sloganTextView, View.TRANSLATION_Y, 50f, 0f)

        sloganFadeIn.duration = 700
        sloganTranslateY.duration = 700
        sloganFadeIn.startDelay = 250
        sloganTranslateY.startDelay = 250
        sloganTranslateY.interpolator = OvershootInterpolator(1.5f)

        val sloganAnimatorSet = AnimatorSet()
        sloganAnimatorSet.playTogether(sloganFadeIn, sloganTranslateY)

        // Secuencia principal
        val textAnimatorSet = AnimatorSet()
        textAnimatorSet.playSequentially(appNameAnimatorSet, sloganAnimatorSet)

        val mainAnimatorSet = AnimatorSet()
        mainAnimatorSet.playSequentially(logoAnimatorSet, textAnimatorSet)
        mainAnimatorSet.start()

        // Pulso final al logo
        Handler(Looper.getMainLooper()).postDelayed({
            val pulseX = ObjectAnimator.ofFloat(logoImageView, View.SCALE_X, 1f, 1.05f, 1f)
            val pulseY = ObjectAnimator.ofFloat(logoImageView, View.SCALE_Y, 1f, 1.05f, 1f)
            pulseX.duration = 800
            pulseY.duration = 800
            AnimatorSet().apply {
                playTogether(pulseX, pulseY)
                start()
            }
        }, 2400)

        // Animación de salida y navegación
        Handler(Looper.getMainLooper()).postDelayed({
            val exitAnimatorSet = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(logoImageView, View.ALPHA, 1f, 0f),
                    ObjectAnimator.ofFloat(logoImageView, View.SCALE_X, 1f, 1.2f),
                    ObjectAnimator.ofFloat(logoImageView, View.SCALE_Y, 1f, 1.2f),
                    ObjectAnimator.ofFloat(appNameTextView, View.ALPHA, 1f, 0f),
                    ObjectAnimator.ofFloat(appNameTextView, View.TRANSLATION_Y, 0f, -30f),
                    ObjectAnimator.ofFloat(sloganTextView, View.ALPHA, 1f, 0f),
                    ObjectAnimator.ofFloat(sloganTextView, View.TRANSLATION_Y, 0f, -50f)
                )
                duration = 500
            }

            exitAnimatorSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {

                    val currentUser = FirebaseAuth.getInstance().currentUser

                    val intent = if (currentUser != null) {
                        Intent(this@SplashActivity, MainActivity::class.java)
                    } else {
                        Intent(this@SplashActivity, LoginActivity::class.java)
                    }

                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            })

            exitAnimatorSet.start()
        }, 4500)
    }
}
