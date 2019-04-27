package com.ifpr.thiago.messenger.registration

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ifpr.thiago.messenger.R
import com.ifpr.thiago.messenger.messages.LatestMessagesActivity
import com.ifpr.thiago.messenger.models.User
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_register.setOnClickListener {
            performRegister()
        }

        text_link_login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        button_register_photo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectedPhotoUri: Uri? = null

    // captura a foto selecionada
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Toast.makeText(this, "Foto capturada", Toast.LENGTH_SHORT).show()

            // URI representa a localização da imagem capturada
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            // Transforma o fundo do botao na foto selecionada
            circle_photo.setImageBitmap(bitmap)
            button_register_photo.alpha = 0f
        }

    }

    private fun performRegister() {

        val email = email_edit_register.text.toString()
        val password = password_edit_register.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please type your email/password!", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase Authentication
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                //  Se este usuario não foi cadastrado com sucesso
                if (!it.isSuccessful) return@addOnCompleteListener
                uploadImageFirebase()
            }

            .addOnFailureListener{
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                return@addOnFailureListener
            }
    }

    private fun uploadImageFirebase() {

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    saveUser(it.toString())
                }
            }
            .addOnFailureListener{
                Toast.makeText(this, "Falha ao jogar imagem", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUser(profileImageUri: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username_edit_register.text.toString(), profileImageUri)
        ref.setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Usuario cadastrado no banco de dados!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LatestMessagesActivity::class.java)
                // serve para tornar a activity "única", asssim nao volta para outra activity quando voltar
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

            .addOnFailureListener{
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }
}


