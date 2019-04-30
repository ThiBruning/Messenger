package com.ifpr.thiago.messenger.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ifpr.thiago.messenger.R
import com.ifpr.thiago.messenger.models.Message
import com.ifpr.thiago.messenger.models.User
import com.ifpr.thiago.messenger.registration.RegisterActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*

val adapter = GroupAdapter<ViewHolder>()
val latestMessagesMap = HashMap<String, Message>()

class LatestMessagesActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        recycler_latest_messages.adapter = adapter
        recycler_latest_messages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->
            // olha para cada adapter clicado como um LatestMessageItem
            val row = item as LatestMessageItem

            val intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(NewMessageActivity.USER_KEY, row.userPartner)
            startActivity(intent)
        }

        // captura o usuario que esta logado
        getCurrentUser()
        // Verifica se o usuário está logado
        verifyUserLogged()
        // Lista as ultimas mensagens
        listLatestMessages()
    }

    private fun getCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        //quando capturar o usuario do banco
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("USUARIO", "usuario logado: ${currentUser?.username}")
            }
            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun verifyUserLogged() {
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null) {
            val intent =  Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun listLatestMessages() {

        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest_messages/$fromId")
        ref.addChildEventListener(object : ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java) ?: return
                latestMessagesMap[p0.key!!] = message
                refresh()
            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java) ?: return
                latestMessagesMap[p0.key!!] = message
                refresh()
            }

            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })

    }

    private fun refresh() {
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageItem(it))
        }
    }

    // criando a navbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //quando clicar nos botoes da navbar
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.new_message -> {
                val intent =  Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }

            R.id.sign_out -> {
                FirebaseAuth.getInstance().signOut()
                verifyUserLogged()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
