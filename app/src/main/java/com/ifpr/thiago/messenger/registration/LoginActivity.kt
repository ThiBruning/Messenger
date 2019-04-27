package com.ifpr.thiago.messenger.registration

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.ifpr.thiago.messenger.R
import com.ifpr.thiago.messenger.messages.LatestMessagesActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        button_login.setOnClickListener {
            performLogin()
        }

        text_registration_link.setOnClickListener {
            finish()
        }
    }

    private fun performLogin() {
        val email = email_edit_login.text.toString()
        val password = password_edit_login.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please type your email/password!", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                //  Se este usuario não foi logado com sucesso
                if (!it.isSuccessful) return@addOnCompleteListener

                // else if
                Toast.makeText(this, "User logged Successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }

            .addOnFailureListener{
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                return@addOnFailureListener
            }
    }
}