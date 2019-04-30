package com.ifpr.thiago.messenger.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.thiago.messenger.R
import com.ifpr.thiago.messenger.models.Message
import com.ifpr.thiago.messenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.message_from.*
import kotlinx.android.synthetic.main.message_from.view.*
import kotlinx.android.synthetic.main.message_to.view.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username
        recycler_chat.adapter = adapter

        listMessages()

        bt_send.setOnClickListener {
            sendMessage()
        }
    }

    private fun listMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("user-messages/$fromId/$toId")
        // para cada mensagem que ele listar...
        ref.addChildEventListener(object : ChildEventListener  {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java)
                if (message != null) {
                    if (message.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatToItem(message = message.text, user = LatestMessagesActivity.currentUser!!))
                    }
                    else if (message.toId == toUser?.uid || message.toId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatFromItem(message = message.text, user = toUser!!))
                    }
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {}
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
        })
    }

    private fun sendMessage() {
        val text = edit_chat.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

//        val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val refTo = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        if (fromId == null) return
        if (ref != null) {
            val message = Message(ref.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
            ref.setValue(message)
            refTo.setValue(message)

            // guarda a ultima mensagem de quem está enviando
            val refLatestMessage = FirebaseDatabase.getInstance().getReference("/latest_messages/$fromId/$toId")
            refLatestMessage.setValue(message)

            // guarda a ultima mensagem de quem está recebendo
            val refLatestToMessage = FirebaseDatabase.getInstance().getReference("/latest_messages/$toId/$fromId")
            refLatestToMessage.setValue(message)
        }

        clear()
    }

    private fun clear() { edit_chat.text.clear() }
}

class ChatFromItem(val message: String, val user: User): Item<ViewHolder>() {
    override fun getLayout(): Int {
       return R.layout.message_from
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.message_from.text = message
        val target = viewHolder.itemView.imageview_from
        Picasso.get().load(user.profileImageUri).into(target)
    }
}

class ChatToItem(val message: String, val user: User): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.message_to
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.message_to.text = message
        val target = viewHolder.itemView.imageview_to
        Picasso.get().load(user.profileImageUri).into(target)
    }
}
