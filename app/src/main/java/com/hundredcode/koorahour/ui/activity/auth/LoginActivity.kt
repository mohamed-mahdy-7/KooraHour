package com.hundredcode.koorahour.ui.activity.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hundredcode.koorahour.R
import com.hundredcode.koorahour.databinding.ActivityLoginBinding
import com.hundredcode.koorahour.ui.activity.OwnerActivity
import com.hundredcode.koorahour.ui.activity.PlayerActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        binding.forgotPassword.setOnClickListener {
//            startActivity(Intent(this, PasswordActivity::class.java))
//        }

        // Keep this observer, as it handles the error messages correctly.
        authViewModel.error.observe(this) { errorMsg ->
            if (!errorMsg.isNullOrEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }

        authViewModel.userData.observe(this) { userData ->
            userData?.let { user ->
                when (user.role) {
                    "owner" -> {
                        startActivity(Intent(this, OwnerActivity::class.java))
                        finish()
                    }

                    "player" -> {
                        startActivity(Intent(this, PlayerActivity::class.java))
                        finish()
                    }

                    else -> {
                        Toast.makeText(this, "Unrecognized role", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }


        authViewModel.isLoading.observe(this) { isLoading ->
            binding.loadingLayout.loadingRootConstraintLayout.visibility =
                if (isLoading) View.VISIBLE else View.GONE
            binding.logInButton.isEnabled = !isLoading
        }

        binding.singUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.logInButton.setOnClickListener { signIn() }

//        binding.googleLoginButton.setOnClickListener {
//            googleSignInLauncher.launch(authViewModel.getGoogleSignInIntent())
//        }

    }

    private fun signIn() {
        with(binding) {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty()) {
                emailTextInputLayout.error = "Please enter your email"
                return
            }
            if (password.isEmpty()) {
                passwordTextInputLayout.error = "Please enter your password"
                return
            }

            loadingLayout.loadingRootConstraintLayout.visibility = View.VISIBLE
            authViewModel.login(email, password)
        }
    }
}