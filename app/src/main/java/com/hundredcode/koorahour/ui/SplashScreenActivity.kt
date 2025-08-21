package com.hundredcode.koorahour.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hundredcode.koorahour.R
import com.hundredcode.koorahour.ui.activity.OwnerActivity
import com.hundredcode.koorahour.ui.activity.PlayerActivity
import com.hundredcode.koorahour.ui.activity.auth.AuthViewModel
import com.hundredcode.koorahour.ui.activity.auth.LoginActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userRole = sharedPref.getString("user_role", null)

        Handler(Looper.getMainLooper()).postDelayed({
            if (userRole != null) {
                // Role exists locally, immediate routing
                val destinationActivity = when (userRole) {
                    "owner" -> OwnerActivity::class.java
                    "player" -> PlayerActivity::class.java
                    else -> LoginActivity::class.java
                }
                navigateToActivity(destinationActivity)
            } else {
                // No saved role, check current login status
                val authViewModel: AuthViewModel by viewModels()
                authViewModel.firebaseUser.observe(this) { user ->
                    if (user != null) {
                        // المستخدم مسجل الدخول، لكن الدور غير محفوظ، نطلب البيانات
                        authViewModel.getData(user.uid)
                        authViewModel.userData.observe(this) { userData ->
                            if (userData != null && !isFinishing) {
                                val destinationActivity = when (userData.role) {
                                    "owner" -> OwnerActivity::class.java
                                    "player" -> PlayerActivity::class.java
                                    else -> LoginActivity::class.java
                                }
                                navigateToActivity(destinationActivity)
                            }
                        }
                    } else {
                        // User is not logged in, redirect to login screen
                        navigateToActivity(LoginActivity::class.java)
                    }
                }
            }
        }, 1500)

    }

    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}