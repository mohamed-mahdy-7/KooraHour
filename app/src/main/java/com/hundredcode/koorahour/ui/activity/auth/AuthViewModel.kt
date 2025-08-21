package com.hundredcode.koorahour.ui.activity.auth

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hundredcode.koorahour.ui.model.User
import java.util.concurrent.TimeUnit

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val userRef = FirebaseDatabase.getInstance().getReference("users")
    private val sharedPreferences =
        application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _firebaseUser = MutableLiveData<FirebaseUser?>()
    val firebaseUser: LiveData<FirebaseUser?> = _firebaseUser

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    private val _verificationId = MutableLiveData<String>()
    val verificationId: LiveData<String> = _verificationId

    private val _resendToken = MutableLiveData<PhoneAuthProvider.ForceResendingToken>()
    val resendToken: LiveData<PhoneAuthProvider.ForceResendingToken> = _resendToken

    private val _authResult = MutableLiveData<Boolean?>()
    val authResult: LiveData<Boolean?> = _authResult

//    private lateinit var googleSignInClient: GoogleSignInClient

    init {
        _firebaseUser.value = auth.currentUser
        _isLoading.value = false
        loadUserDataFromPreferences()
//        configureGoogleSignIn(application)
    }

//    // Google Sign-In methods
//    private fun configureGoogleSignIn(context: Context) {
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(context.getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build()
//        googleSignInClient = GoogleSignIn.getClient(context, gso)
//    }

//    fun getGoogleSignInIntent(): Intent {
//        return googleSignInClient.signInIntent
//    }

//    fun handleGoogleSignInResult(data: Intent) {
//        try {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            val account = task.getResult(ApiException::class.java)
//            userData.value?.let { firebaseAuthWithGoogle(account, role = it.role) }
//        } catch (e: ApiException) {
//            _error.postValue(e.message)
//        }
//    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?, role: String) {
        if (account == null) return
        _isLoading.value = true
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            _isLoading.value = false
            if (task.isSuccessful) {
                auth.currentUser?.uid?.let { uid ->
                    checkIfUserExists(uid, account.displayName, account.email, role)
                }
            } else {
                _error.postValue(task.exception?.message)
            }
        }
    }

    // Email/Password authentication methods
    fun login(email: String, password: String) {
        _isLoading.value = true
        _error.value = null
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            _isLoading.value = false
            if (task.isSuccessful) {
                handleSuccessfulAuth(auth.currentUser)
            } else {
                _error.postValue(
                    task.exception?.message ?: "Login failed. Please check your email and password."
                )
            }
        }
    }

    fun register(name: String, email: String, phoneNumber: String, password: String, role: String) {
        _isLoading.value = true
        _error.value = null
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val newUser = User(
                            uid = uid,
                            name = name,
                            email = email,
                            phoneNumber = phoneNumber,
                            role = role
                        )
                        saveData(newUser)
                        _firebaseUser.postValue(auth.currentUser)
                    }
                } else {
                    _error.postValue(task.exception?.message ?: "Something went wrong...")
                }
            }
    }

    // Phone authentication methods
    fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        _isLoading.value = true
        _error.value = null
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun resendCode(
        phoneNumber: String,
        activity: Activity,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        _isLoading.value = true
        _error.value = null
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyCode(code: String, storedVerificationId: String) {
        _isLoading.value = true
        _error.value = null
        signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(storedVerificationId, code))
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            _isLoading.value = false
            _error.value = e.message
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            _isLoading.value = false
            _verificationId.value = verificationId
            _resendToken.value = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            _isLoading.value = false
            if (task.isSuccessful) {
                _authResult.value = true
                handleSuccessfulAuth(auth.currentUser)
            } else {
                _authResult.value = false
                _error.value = task.exception?.message
            }
        }
    }

    // --- Data Persistence and User Management ---

    // Check if user exists after successful sign-in
    private fun checkIfUserExists(uid: String, name: String?, email: String?, role: String) {
        userRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User already exists, fetch data
                    val user = snapshot.getValue(User::class.java)
                    _userData.postValue(user)
                    saveUserDataToPreferences(user!!)
                } else {
                    // New user, save data
                    val newUser = User(
                        uid = uid,
                        name = name ?: "",
                        email = email ?: "",
                        phoneNumber = "",
                        role = role
                    )
                    saveData(newUser)
                }
                _firebaseUser.postValue(auth.currentUser)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                _error.postValue(databaseError.message)
                _firebaseUser.postValue(auth.currentUser)
            }
        })
    }

    private fun handleSuccessfulAuth(firebaseUser: FirebaseUser?) {
        firebaseUser?.uid?.let { uid ->
            getData(uid)
            _firebaseUser.postValue(firebaseUser)
        }
    }

    fun getData(uid: String) {
        userRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(User::class.java)
                userData?.let {
                    _userData.postValue(it)
                    saveUserDataToPreferences(it)
                } ?: run {
                    _error.postValue("User data not found")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                _error.postValue(databaseError.message)
            }
        })
    }

    private fun saveData(modelUser: User) {
        userRef.child(modelUser.uid).setValue(modelUser).addOnSuccessListener {
            _userData.postValue(modelUser)
            saveUserDataToPreferences(modelUser)
        }.addOnFailureListener { e ->
            _error.postValue(e.message)
        }
    }

    // SharedPreferences methods
    private fun saveUserDataToPreferences(user: User) {
        with(sharedPreferences.edit()) {
            putString("uid", user.uid)
            putString("email", user.email)
            putString("name", user.name)
            putString("phoneNumber", user.phoneNumber)
            putString("role", user.role)
            apply()
        }
    }

    private fun loadUserDataFromPreferences() {
        val uid = sharedPreferences.getString("uid", null)
        val email = sharedPreferences.getString("email", null)
        val name = sharedPreferences.getString("name", null)
        val phoneNumber = sharedPreferences.getString("phoneNumber", null)
        val role = sharedPreferences.getString("role", null)
        if (uid != null && email != null && name != null && phoneNumber != null && role != null) {
            val user = User(uid, email, name, phoneNumber, role)
            _userData.postValue(user)
        }
    }

    private fun clearUserDataFromPreferences() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }

    fun logout() {
        auth.signOut()
//        googleSignInClient.signOut()
        _firebaseUser.postValue(null)
        clearUserDataFromPreferences()
    }

    fun resetPassword(email: String) {
        _isLoading.value = true
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            _isLoading.value = false
            if (task.isSuccessful) {
                _error.postValue("Password reset email sent.")
            } else {
                _error.postValue(task.exception?.message ?: "Failed to send reset email.")
            }
        }
    }
}