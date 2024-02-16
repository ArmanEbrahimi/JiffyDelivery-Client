package com.example.jiffydeliveryclient

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.jiffydeliveryclient.model.ClientModel
import com.example.jiffydeliveryclient.ui.HomeActivity
import com.example.jiffydeliveryclient.utils.Constants
import com.example.jiffydeliveryclient.utils.UserUtils
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import java.util.Arrays
import java.util.concurrent.TimeUnit

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var providers: List<AuthUI.IdpConfig>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener : FirebaseAuth.AuthStateListener

    private lateinit var getResult : ActivityResultLauncher<Intent>
    private lateinit var progressBar: ProgressBar
    private lateinit var database: FirebaseDatabase
    private lateinit var clientInfoRef : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        init()

    }
    private fun init() {
        getResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val response = IdpResponse.fromResultIntent(result.data)
                if (result.resultCode == Activity.RESULT_OK) {
                    val user = FirebaseAuth.getInstance().currentUser
                } else {
                    Toast.makeText(this, "" + response?.error, Toast.LENGTH_SHORT).show()
                }
            }
        database = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        clientInfoRef = database.getReference(Constants.CLIENT_INFO_REF)
        providers = listOf(
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        listener = FirebaseAuth.AuthStateListener { myFirebaseAuth ->
            val user = myFirebaseAuth.currentUser
            var firstName: String? = null

            FirebaseDatabase.getInstance().reference.child(Constants.CLIENT_INFO_REF)
                .child(user!!.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        firstName = snapshot.getValue(ClientModel::class.java)?.firstName
                        Log.d("firstName", firstName!!)
                        handleWelcomeMessage(user, firstName)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle errors here
                    }
                })
        }
    }

    private fun handleWelcomeMessage(user: FirebaseUser, firstName: String?) {
        if (user != null) {
            Toast.makeText(
                this@SplashScreenActivity,
                "Welcome: $firstName",
                Toast.LENGTH_SHORT
            ).show()
            FirebaseMessaging.getInstance()
                .token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        UserUtils.updateToken(this@SplashScreenActivity, token)
                        Log.d("TOKEN", token)
                    } else {
                        Toast.makeText(
                            this@SplashScreenActivity,
                            "Failed to get FCM token ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            checkUserFromFirebase()
        } else {
            showLoginLayout()
        }
    }
    private fun checkUserFromFirebase() {
        clientInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val model = snapshot.getValue(ClientModel::class.java)
                        goToHomeActivity(model)
                    } else {
                        showRegisterLayout()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SplashScreenActivity, error.message, Toast.LENGTH_LONG)
                        .show()
                }

            })

    }

    private fun showLoginLayout() {
        val authMethodPickerLayout = AuthMethodPickerLayout.Builder(R.layout.sign_in_layout)
            .setPhoneButtonId(R.id.button_phone_sign_in)
            .setGoogleButtonId(R.id.button_google_sign_in)
            .build()
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setTheme(R.style.LoginTheme)
            .setAuthMethodPickerLayout(authMethodPickerLayout)
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .build()
        getResult.launch(signInIntent)

    }



    private fun showRegisterLayout() {
        val builder = AlertDialog.Builder(this, R.style.DialogTheme)
        val itemView = LayoutInflater.from(this).inflate(R.layout.register_layout, null, false)

        val edit_text_name =
            itemView.findViewById<View>(R.id.edit_text_first_name) as TextInputEditText
        val edit_text_last_name =
            itemView.findViewById<View>(R.id.edit_text_last_name) as TextInputEditText
        val edit_text_phone_number =
            itemView.findViewById<View>(R.id.edit_text_phone_number) as TextInputEditText

        val btnContinue = itemView.findViewById<Button>(R.id.button_register)

        if (FirebaseAuth.getInstance().currentUser!!.phoneNumber != null
            && !TextUtils.isDigitsOnly(FirebaseAuth.getInstance().currentUser!!.phoneNumber)
        ) {
            edit_text_phone_number.setText(FirebaseAuth.getInstance().currentUser!!.phoneNumber)
        }

        builder.setView(itemView)
        val dialog = builder.create()
        dialog.show()

        btnContinue.setOnClickListener {
            if (TextUtils.isDigitsOnly(edit_text_name.text.toString())) {
                Toast.makeText(
                    this@SplashScreenActivity,
                    "Please enter a First Name",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener

            } else if (TextUtils.isDigitsOnly(edit_text_last_name.text.toString())) {
                Toast.makeText(
                    this@SplashScreenActivity,
                    "Please enter a Last Name",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener

            } else if (TextUtils.isEmpty(edit_text_phone_number.text.toString())) {
                return@setOnClickListener
            } else {
                val model = ClientModel(
                    edit_text_name.text.toString(),
                    edit_text_last_name.text.toString(),
                    edit_text_phone_number.text.toString(),
                    "",
                    0.0

                )
                clientInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .setValue(model)
                    .addOnFailureListener {
                        Toast.makeText(
                            this@SplashScreenActivity,
                            "${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                        progressBar.visibility = View.GONE
                    }.addOnSuccessListener {
                        Toast.makeText(
                            this@SplashScreenActivity,
                            "Register Successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()

                        goToHomeActivity(model)
                        progressBar.visibility = View.GONE

                    }
            }
        }


    }

    private fun goToHomeActivity(model: ClientModel?) {
        Constants.currentClient = model
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onStart() {
        super.onStart()
        displaySplashScreen()
    }

    private fun displaySplashScreen() {
        Completable.timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread()).subscribe{
            firebaseAuth.addAuthStateListener(listener)
        }
    }



    override fun onStop() {
        if (firebaseAuth != null && listener != null) firebaseAuth.removeAuthStateListener(listener)
        super.onStop()    }
}