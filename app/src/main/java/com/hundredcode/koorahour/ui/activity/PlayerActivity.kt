package com.hundredcode.koorahour.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.hundredcode.koorahour.R
import com.hundredcode.koorahour.databinding.ActivityPlayerBinding
import com.hundredcode.koorahour.ui.activity.auth.AuthViewModel
import com.hundredcode.koorahour.ui.activity.auth.LoginActivity

class PlayerActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityPlayerBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        observeViewModel()

        navController = supportFragmentManager
            .findFragmentById(R.id.main_fragment)!!
            .findNavController()

        setupBottomNavigationBar()
    }

    private fun setupBottomNavigationBar() {
        binding.bottomBar.onItemSelected = { index ->
            when (index) {
                0 -> navController.navigate(R.id.player_home_fragment)
                1 -> navController.navigate(R.id.player_booking_fragment)
                2 -> navController.navigate(R.id.player_profile_fragment)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun observeViewModel() {
        authViewModel.firebaseUser.observe(this) { firebaseUser ->
            if (firebaseUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                authViewModel.getData(firebaseUser.uid)
            }
        }
        authViewModel.userData.observe(this) { user ->
            user?.let {
                // showUserData(it)
            }
        }
        authViewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                Log.e("ActivityProfile", "Error: $it")
            }
        }
        onBackPressedDispatcher.addCallback(this) {
            when (navController.currentDestination?.id) {
                R.id.player_home_fragment -> {
                    AlertDialog.Builder(this@PlayerActivity)
                        .setTitle("تأكيد الخروج")
                        .setMessage("هل تريد الخروج من التطبيق؟")
                        .setPositiveButton("نعم") { _, _ ->
                            finish()
                        }
                        .setNegativeButton("لا", null)
                        .show()
                }

                else -> {
                    // Back to first tab navigateUp()
                    navController.navigate(R.id.player_home_fragment)
                    binding.bottomBar.itemActiveIndex = 0
                }
            }
        }
    }

}