package com.ifpr.thiago.messenger.messages

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.thiago.messenger.R
import com.ifpr.thiago.messenger.models.Message
import com.ifpr.thiago.messenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_message_item.view.*

class LatestMessageItem(val message: Message): Item<ViewHolder>() {

    var userPartner: User? = null

    override fun getLayout(): Int {
        return R.layout.latest_message_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val chatPartner: String
        if(message.fromId == FirebaseAuth.getInstance().uid) {
            chatPartner = message.toId
        } else { chatPartner = message.fromId }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartner")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                userPartner = p0.getValue(User::class.java)
                viewHolder.itemView.username_latest_message.text = userPartner?.username

                val target = viewHolder.itemView.imageView_latest_message
                Picasso.get().load(userPartner?.profileImageUri).into(target)
            }

            override fun onCancelled(p0: DatabaseError) {}
        })

        viewHolder.itemView.message_latest_message.text = message.text
    }
}