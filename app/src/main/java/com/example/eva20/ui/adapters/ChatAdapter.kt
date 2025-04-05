package com.example.eva20.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eva20.R
import com.example.eva20.network.models.ChatMessage
import java.text.SimpleDateFormat
import java.util.Locale

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ChatViewHolder(
            inflater.inflate(R.layout.item_chat_message, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageContent: TextView = itemView.findViewById(R.id.textViewMessage)
        private val messageTime: TextView = itemView.findViewById(R.id.textViewTime)
        private val messageContainer: ConstraintLayout = itemView.findViewById(R.id.messageContainer)

        fun bind(message: ChatMessage) {
            messageContent.text = message.content
            messageTime.text = dateFormat.format(message.timestamp)

            // Align messages based on sender (user or bot)
            val params = messageContainer.layoutParams as ViewGroup.MarginLayoutParams
            if (message.isFromUser) {
                messageContainer.setBackgroundResource(R.drawable.bg_message_user)
                params.apply {
                    marginStart = 100
                    marginEnd = 10
                }
            } else {
                messageContainer.setBackgroundResource(R.drawable.bg_message_bot)
                params.apply {
                    marginStart = 10
                    marginEnd = 100
                }
            }
            messageContainer.layoutParams = params
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}