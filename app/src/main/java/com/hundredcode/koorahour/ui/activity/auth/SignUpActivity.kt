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
import com.hundredcode.koorahour.databinding.ActivitySignUpBinding
import com.hundredcode.koorahour.ui.activity.OwnerActivity
import com.hundredcode.koorahour.ui.activity.PlayerActivity

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private val authViewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // --- Observe LiveData from ViewModel ---

        // Correctly monitor user status to determine routing
        authViewModel.userData.observe(this) { userData ->

            userData?.let {
                val destinationActivity = when (it.role) {
                    "owner" -> OwnerActivity::class.java
                    "player" -> PlayerActivity::class.java
                    else -> LoginActivity::class.java
                }
                val intent = Intent(this, destinationActivity)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        // Monitor error messages
        authViewModel.error.observe(this) { errorMsg ->
            if (!errorMsg.isNullOrEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }

        // Monitor the upload status
        authViewModel.isLoading.observe(this) { isLoading ->
            binding.loadingLayout.loadingRootConstraintLayout.visibility =
                if (isLoading) View.VISIBLE else View.GONE
            binding.buttonSignup.isEnabled = !isLoading && binding.policyCheckBox.isChecked
        }

        // --- Set up View listeners ---
        binding.buttonSignup.setOnClickListener { signUp() }

        binding.signInLayout.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.policyCheckBox.setOnCheckedChangeListener { _, isChecked ->
            binding.buttonSignup.isEnabled = isChecked && !(authViewModel.isLoading.value ?: false)
        }
    }

    private fun signUp() {
        with(binding) {
            val name = editTextName.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val phoneNumber = editTextPhoneNumber.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (name.isEmpty()) {
                nameTextInputLayout.error = "Please enter your name"
                return
            } else {
                nameTextInputLayout.error = null
            }
            if (email.isEmpty()) {
                emailTextInputLayout.error = "Please enter your email"
                return
            } else {
                emailTextInputLayout.error = null
            }
            if (phoneNumber.isEmpty()) {
                phoneNumberTextInputLayout.error = "Please enter your phone number"
                return
            } else {
                phoneNumberTextInputLayout.error = null
            }
            if (password.isEmpty()) {
                passwordTextInputLayout.error = "Please enter your password"
                return
            } else {
                passwordTextInputLayout.error = null
            }


            if (!policyCheckBox.isChecked) {
                Toast.makeText(
                    this@SignUpActivity,
                    "Please accept the terms and conditions.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }


            val role = when (roleRadioGroup.checkedRadioButtonId) {
                R.id.ownerRadioButton -> "owner"
                R.id.playerRadioButton -> "player"
                else -> "player"
            }

            authViewModel.register(name, email, phoneNumber, password, role)
        }
    }

}