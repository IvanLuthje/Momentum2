package com.example.momentum2.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.momentum2.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.momentum2.databinding.ActivityLoginBinding
import com.example.momentum2.R
//import com.example.momentum2.ui.login.ui.theme.AddMomentActivity

import com.example.momentum2.ui.login.ui.theme.RegisterActivity



class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var email: EditText
    private lateinit var passwd: EditText
    private lateinit var login: Button

    private lateinit var loginBinding: ActivityLoginBinding

    private lateinit var register: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        auth = FirebaseAuth.getInstance()

        email = findViewById(R.id.username)
        passwd = findViewById(R.id.password)
        login = findViewById(R.id.login)
        register = findViewById(R.id.botonRegistrar)

        val intent_login = Intent(this,  MainActivity::class.java)


        register.setOnClickListener {
            val intent_register= Intent(this, RegisterActivity::class.java)
            startActivity(intent_register)
        }



        fun loginUser(email: String, password: String) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        startActivity(intent_login)
                        finish()
                    } else {
                        Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        



        login.setOnClickListener {
            val email = email.text.toString()
            val password = passwd.text.toString()
            loginUser(email, password)
        }

    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}